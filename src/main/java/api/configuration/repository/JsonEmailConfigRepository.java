package api.configuration.repository;

import api.configuration.model.EmailConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class JsonEmailConfigRepository {
    public static final String JSON_FILE = "email-configs.json";
    public List<EmailConfig> emailConfigs = new ArrayList<>();
    public final ObjectMapper mapper;
    public Path filePath;

    public JsonEmailConfigRepository() {
        this.mapper = new ObjectMapper();
        // ✅ Configurar ObjectMapper para LocalDateTime
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

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

            // Asegurar que todos los registros tengan ID válido
            boolean needsSave = false;
            for (EmailConfig config : emailConfigs) {
                if (config.getId() == null) {
                    config.setId(generateNewId());
                    needsSave = true;
                }
            }

            // Guardar con IDs si se agregaron algunos
            if (needsSave) {
                saveAllEmailConfigs();
            }

            System.out.println("Configuraciones de email leídas correctamente");

        } catch (Exception e) {
            System.err.println("Error deserializando JSON de email: " + e.getMessage());

            try {
                // Intentar leer como objeto individual
                EmailConfig singleConfig = mapper.readValue(content, EmailConfig.class);
                this.emailConfigs = new ArrayList<>();

                // Asignar ID si es necesario
                if (singleConfig.getId() == null) {
                    singleConfig.setId(1L); // Primer ID
                }

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
        // Generar ID si no existe
        if (newConfig.getId() == null) {
            newConfig.setId(generateNewId());
        }

        // Buscar configuración existente por ID
        Optional<EmailConfig> existingConfigOpt = findById(newConfig.getId());

        EmailConfig configToSave;

        if (existingConfigOpt.isPresent()) {
            // Si existe, hacer merge de los campos
            EmailConfig existingConfig = existingConfigOpt.get();
            configToSave = mergeConfigs(existingConfig, newConfig);
        } else {
            // Si no existe, usar la nueva configuración
            configToSave = newConfig;

            // Asegurar que los campos tengan valores por defecto si son nulos
            if (configToSave.getEmailReception() == null) {
                configToSave.setEmailReception("");
            }
            if (configToSave.getFooter() == null) {
                configToSave.setFooter(""); // Valor por defecto para footer
            }
            if (configToSave.getTitle() == null) {
                configToSave.setTitle(""); // Valor por defecto para tittle
            }
        }

        // Establecer timestamp actual
        configToSave.setLastUpdated(LocalDateTime.now());

        // Eliminar configuración existente por ID
        emailConfigs.removeIf(config ->
                config.getId() != null && config.getId().equals(configToSave.getId()));

        emailConfigs.add(configToSave);

        saveAllEmailConfigs();
    }

    // Generar un nuevo ID único
    private Long generateNewId() {
        // Filtrar valores nulos y obtener el máximo ID existente
        Optional<Long> maxId = emailConfigs.stream()
                .map(EmailConfig::getId)
                .filter(Objects::nonNull)
                .max(Long::compare);

        return maxId.orElse(0L) + 1;
    }

    // Método para hacer merge de configuraciones (actualización parcial)
    private EmailConfig mergeConfigs(EmailConfig existing, EmailConfig updates) {
        EmailConfig merged = new EmailConfig();

        // Mantener el ID original
        merged.setId(existing.getId());

        // Copiar todos los campos existentes
        merged.setClientId(existing.getClientId());
        merged.setClientSecret(existing.getClientSecret());
        merged.setRefreshToken(existing.getRefreshToken());
        merged.setEmailConfig(existing.getEmailConfig());
        merged.setBody(existing.getBody());
        merged.setSubject(existing.getSubject());
        merged.setEmailReception(existing.getEmailReception());
        merged.setFooter(existing.getFooter());
        merged.setTitle(existing.getTitle());
        merged.setLogoEmail(existing.getLogoEmail());
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
        // Permitir cambio de email
        if (updates.getEmailConfig() != null) {
            merged.setEmailConfig(updates.getEmailConfig());
        }

        if (updates.getEmailReception() != null) {
            merged.setEmailReception(updates.getEmailReception());
        }

        if (updates.getFooter() != null) {
            merged.setFooter(updates.getFooter());
        }

        if (updates.getTitle() != null) {
            merged.setTitle(updates.getTitle());
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

    // Buscar configuración por ID
    public Optional<EmailConfig> findById(Long id) {
        return emailConfigs.stream()
                .filter(config -> config.getId().equals(id))
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

    // Eliminar configuración por ID
    public boolean deleteById(Long id) throws IOException {
        boolean removed = emailConfigs.removeIf(config -> config.getId().equals(id));
        if (removed) {
            saveAllEmailConfigs();
        }
        return removed;
    }

    // Eliminar configuración por email
    public boolean deleteByEmail(String email) throws IOException {
        boolean removed = emailConfigs.removeIf(config -> config.getEmailConfig().equals(email));
        if (removed) {
            saveAllEmailConfigs();
        }
        return removed;
    }
}