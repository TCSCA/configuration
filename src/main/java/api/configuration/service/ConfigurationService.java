package api.configuration.service;

import api.configuration.dto.LogoConfig;
import api.configuration.model.EmailConfig;
import api.configuration.repository.ConfigurationRepository;
import api.configuration.repository.JsonEmailConfigRepository;
import api.configuration.request.EmailCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfigurationService {


    @Autowired
    private ConfigurationRepository configurationRepository;
    public void saveConfig(LogoConfig logoConfig) throws Exception {


        LogoConfig logoConfigSet= new LogoConfig();
        logoConfigSet.setLogo(logoConfig.getLogo());


        configurationRepository.saveLogo(logoConfigSet.getLogo());
    }


    public LogoConfig getLatestConfig() {
        try {
            Optional<LogoConfig> latestConfig = configurationRepository.findLatest();

            if (latestConfig.isPresent()) {
                return latestConfig.get();
            } else {
                throw new RuntimeException("No hay configuraciones  disponibles");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la última configuración: " + e.getMessage(), e);
        }
    }

    public LogoConfig updateConfig(LogoConfig credentials) {
        try {
            // Buscar la ÚLTIMA configuración (la más reciente)
            Optional<LogoConfig> latestConfigOpt = configurationRepository.findLatest();

            // Obtener la última configuración para actualizarla
            LogoConfig latestConfig = latestConfigOpt.get();

            // Actualizar solo los campos que vienen en el request (no nulos)
            if (credentials.getLogo() != null) {
                latestConfig.setLogo(credentials.getLogo());
            }

            // Guardar los cambios (actualizar la última configuración)
            configurationRepository.saveLogo(latestConfig.getLogo());

            return latestConfig;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la configuración: " + e.getMessage(), e);
        }
    }
}
