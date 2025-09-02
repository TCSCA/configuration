package api.configuration.controller;


import api.configuration.dto.LogoConfig;
import api.configuration.model.EmailConfig;
import api.configuration.request.EmailCredentials;
import api.configuration.service.ConfigurationService;
import api.configuration.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping("/saveConfigurationLogo")
    public ResponseEntity<String> saveConfigurationEmail(@RequestBody LogoConfig request)
            throws Exception {


        configurationService.saveConfig(
                request
        );

        return ResponseEntity.ok("Información guardada exitosamente.");
    }

    @GetMapping("/getLastConfigLogo")
    public LogoConfig getLastConfig()
            throws Exception {

        return configurationService.getLatestConfig();
    }

    @PostMapping("/updateConfig")
    public ResponseEntity<String> updateConfigEmail(@RequestBody LogoConfig request)
            throws Exception {

        configurationService.updateConfig(request);

        return ResponseEntity.ok("Información actualizada exitosamente.");
    }


}
