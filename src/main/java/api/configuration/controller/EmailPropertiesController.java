package api.configuration.controller;

import api.configuration.request.EmailCredentialProperties;
import api.configuration.request.EmailCredentials;
import api.configuration.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/email")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class EmailPropertiesController {

    private final EmailService emailService;

    @Value("${client.id}")
    private String clientId;

    @Value("${client.secret}")
    private String clientSecret;

    @Value("${refresh.token}")
    private String refreshToken;

    @Value("${email.config}")
    private String emailConfig;


    public EmailPropertiesController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/sendEmailProperties")
    public ResponseEntity<String> sendEmail(@RequestBody EmailCredentialProperties request)
            throws Exception {

        EmailCredentialProperties emailCredentials = new EmailCredentialProperties();
        emailCredentials.setClientId(clientId);
        emailCredentials.setClientSecret(clientSecret);
        emailCredentials.setRefreshToken(refreshToken);
        emailCredentials.setEmailConfig(emailConfig);
        emailCredentials.setSendTo(request.getSendTo());

        emailService.sendEmail(
                emailCredentials
        );

        return ResponseEntity.ok("Correo enviado exitosamente.");
    }
}