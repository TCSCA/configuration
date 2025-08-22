package api.configuration.repository;

import api.configuration.model.EmailConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class JsonEmailConfigRepository {
    public static final String JSON_FILE = "emailConfigs.json";
    public List<EmailConfig> emailConfigs = new ArrayList<>();
    public final ObjectMapper mapper = new ObjectMapper();
    public Path filePath;

    @PostConstruct
    public void init() {
        try {
            this.filePath = Paths.get("src/main/resources/" + JSON_FILE);

            // Crear archivo si no existe
            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                Files.writeString(filePath, "[]");
                System.out.println("Archivo JSON de email config creado: " + filePath);
            }

            loadEmailConfigs();
            System.out.println("Configuraciones de email cargadas: " + emailConfigs.size());

        } catch (IOException e) {
            System.err.println("Error inicializando repositorio JSON de email: " + e.getMessage());
            this.emailConfigs = new ArrayList<>();
        }
    }

    private void loadEmailConfigs() throws IOException {
        String content = Files.readString(filePath).trim();

        if (content.isEmpty()) {
            Files.writeString(filePath, "[]");
            this.emailConfigs = new ArrayList<>();
            return;
        }

        try {
            this.emailConfigs = mapper.readValue(content, new TypeReference<List<EmailConfig>>() {});
            System.out.println("Configuraciones de email leídas correctamente");

        } catch (Exception e) {
            System.err.println("Error deserializando JSON de email: " + e.getMessage());

            try {
                // Intentar leer como objeto individual
                EmailConfig singleConfig = mapper.readValue(content, EmailConfig.class);
                this.emailConfigs = new ArrayList<>();
                this.emailConfigs.add(singleConfig);

                saveAllEmailConfigs(); // Convertir a array
                System.out.println("Convertido a array y guardado");

            } catch (Exception ex) {
                System.err.println("Error leyendo como objeto individual: " + ex.getMessage());
                Files.writeString(filePath, "[]");
                this.emailConfigs = new ArrayList<>();
            }
        }
    }

    // Guardar o actualizar configuración de email con actualización parcial
    public void saveEmailConfig(EmailConfig newConfig) throws IOException {
        // Buscar configuración existente para este email
        Optional<EmailConfig> existingConfigOpt = findByEmail(newConfig.getEmailConfig());

        EmailConfig configToSave;

        if (existingConfigOpt.isPresent()) {
            // Si existe, hacer merge de los campos
            EmailConfig existingConfig = existingConfigOpt.get();
            configToSave = mergeConfigs(existingConfig, newConfig);
        } else {
            // Si no existe, usar la nueva configuración
            configToSave = newConfig;
        }

        // Establecer timestamp actual
        configToSave.setLastUpdated(LocalDateTime.now());

        // Eliminar configuración existente
        emailConfigs.removeIf(config -> config.getEmailConfig().equals(configToSave.getEmailConfig()));
        emailConfigs.add(configToSave);

        saveAllEmailConfigs();
    }

    // Método para hacer merge de configuraciones (actualización parcial)
    private EmailConfig mergeConfigs(EmailConfig existing, EmailConfig updates) {
        EmailConfig merged = new EmailConfig();

        // Copiar todos los campos existentes
        merged.setClientId(existing.getClientId());
        merged.setClientSecret(existing.getClientSecret());
        merged.setRefreshToken(existing.getRefreshToken());
        merged.setEmailConfig(existing.getEmailConfig());
        merged.setBody(existing.getBody());
        merged.setSubject(existing.getSubject());
        merged.setLastUpdated(existing.getLastUpdated());

        // Actualizar solo los campos que vienen en el update (no nulos)
        if (updates.getClientId() != null) {
            merged.setClientId(updates.getClientId());
        }
        if (updates.getClientSecret() != null) {
            merged.setClientSecret(updates.getClientSecret());
        }
        if (updates.getRefreshToken() != null) {
            merged.setRefreshToken(updates.getRefreshToken());
        }
        if (updates.getBody() != null) {
            merged.setBody(updates.getBody());
        }
        if (updates.getSubject() != null) {
            merged.setSubject(updates.getSubject());
        }
        // emailConfig no debería cambiar en una actualización
        if (updates.getEmailConfig() != null && !updates.getEmailConfig().equals(existing.getEmailConfig())) {
            merged.setEmailConfig(updates.getEmailConfig());
        }

        return merged;
    }

    // Guardar todas las configuraciones
    private void saveAllEmailConfigs() throws IOException {
        String jsonContent = mapper.writeValueAsString(emailConfigs);
        Files.writeString(filePath, jsonContent);
    }

    // Buscar configuración por email
    public Optional<EmailConfig> findByEmail(String email) {
        return emailConfigs.stream()
                .filter(config -> config.getEmailConfig().equals(email))
                .findFirst();
    }

    // Obtener la ÚLTIMA configuración guardada (más reciente)
    public Optional<EmailConfig> findLatest() {
        return emailConfigs.stream()
                .max(Comparator.comparing(EmailConfig::getLastUpdated));
    }

    // Verificar si existe configuración para un email
    public boolean existsByEmail(String email) {
        return emailConfigs.stream()
                .anyMatch(config -> config.getEmailConfig().equals(email));
    }

    // Obtener todas las configuraciones ordenadas por fecha (más reciente primero)
    public List<EmailConfig> findAll() {
        return emailConfigs.stream()
                .sorted(Comparator.comparing(EmailConfig::getLastUpdated).reversed())
                .toList();
    }

    // Obtener todas las configuraciones en orden cronológico
    public List<EmailConfig> findAllChronological() {
        return new ArrayList<>(emailConfigs);
    }

}