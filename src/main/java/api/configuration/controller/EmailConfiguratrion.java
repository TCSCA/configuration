package api.configuration.controller;

import api.configuration.request.EmailCredentials;
import api.configuration.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/email")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class EmailConfiguratrion {


    private final EmailService emailService;

    public EmailConfiguratrion(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/saveConfigurationEmail")
    public ResponseEntity<String> sendEmail(@RequestBody EmailCredentials request)
            throws Exception {


        emailService.sendEmail(
                request
        );

        return ResponseEntity.ok("Correo enviado exitosamente.");
    }
}
