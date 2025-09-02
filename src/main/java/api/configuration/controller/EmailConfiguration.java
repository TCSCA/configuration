package api.configuration.controller;

import api.configuration.model.EmailConfig;
import api.configuration.request.EmailCredentials;
import api.configuration.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/email")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class EmailConfiguration {


    private final EmailService emailService;

    public EmailConfiguration(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/saveConfigurationEmail")
    public ResponseEntity<String> saveConfigurationEmail(@RequestBody EmailCredentials request)
            throws Exception {


        emailService.saveConfig(
                request
        );

        return ResponseEntity.ok("Información guardada exitosamente.");
    }

    @GetMapping("/getLastConfig")
    public EmailConfig getLastConfig()
            throws Exception {


        return emailService.getLatestEmailConfig();
    }

    @PostMapping("/updateConfigEmail")
    public ResponseEntity<String> updateConfigEmail(@RequestBody EmailConfig request)
            throws Exception {

        emailService.updateEmailConfig(request);

        return ResponseEntity.ok("Información actualizada exitosamente.");
    }



}
