package api.configuration.repository;

import api.configuration.dto.LogoConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class ConfigurationRepository {
    private static final String JSON_FILE = "logo.json";
    private final ObjectMapper mapper;
    private Path filePath;
    private List<LogoConfig> logos = new ArrayList<>();

    public ConfigurationRepository() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void init() {
        try {
            this.filePath = Paths.get("src/main/resources/" + JSON_FILE);

            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                Files.writeString(filePath, "[]");
                System.out.println("Archivo JSON de logos creado: " + filePath);
            }

            loadLogos();
            System.out.println("Logos cargados: " + logos.size());

        } catch (IOException e) {
            System.err.println("Error inicializando LogoRepository: " + e.getMessage());
            this.logos = new ArrayList<>();
        }
    }

    private void loadLogos() throws IOException {
        String content = Files.readString(filePath).trim();

        if (content.isEmpty()) {
            Files.writeString(filePath, "[]");
            this.logos = new ArrayList<>();
            return;
        }

        try {
            this.logos = mapper.readValue(content, new TypeReference<List<LogoConfig>>() {});
        } catch (Exception e) {
            System.err.println("Error deserializando logos.json: " + e.getMessage());
            Files.writeString(filePath, "[]");
            this.logos = new ArrayList<>();
        }
    }

    private void saveAll() throws IOException {
        String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(logos);
        Files.writeString(filePath, jsonContent);
    }

    public void saveLogo(String base64) throws IOException {
        if (logos.isEmpty()) {
            // Crear el primer logo
            LogoConfig logo = new LogoConfig();
            logo.setId(1L);
            logo.setLogo(base64);
            logo.setLastUpdated(LocalDateTime.now());

            logos.add(logo);
        } else {
            // Actualizar siempre el primero (ID=1)
            LogoConfig logo = logos.get(0);
            logo.setLogo(base64);
            logo.setLastUpdated(LocalDateTime.now());
        }

        saveAll();
    }

    private Long generateNewId() {
        Optional<Long> maxId = logos.stream()
                .map(LogoConfig::getId)
                .filter(Objects::nonNull)
                .max(Long::compare);
        return maxId.orElse(0L) + 1;
    }

    // 🔹 Actualizar un logo existente por ID
    public boolean updateLogo(Long id, String newBase64) throws IOException {
        for (LogoConfig logo : logos) {
            if (Objects.equals(logo.getId(), id)) {
                logo.setLogo(newBase64);
                logo.setLastUpdated(LocalDateTime.now());
                saveAll();
                return true; // actualizado
            }
        }
        return false; // no encontrado
    }

    // 🔹 Obtener el último logo guardado
    public Optional<LogoConfig> findLatest() {
        return logos.stream()
                .max(Comparator.comparing(LogoConfig::getLastUpdated));
    }

    // 🔹 Obtener todos
    public List<LogoConfig> findAll() {
        return logos.stream()
                .sorted(Comparator.comparing(LogoConfig::getLastUpdated).reversed())
                .toList();
    }
}
