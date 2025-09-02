package api.configuration.controller;



import api.configuration.model.EmailConfig;
import api.configuration.request.EmailCredentialProperties;
import api.configuration.request.EmailCredentials;
import api.configuration.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/email")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }


    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(@RequestBody EmailCredentials emailCredentials)
            throws Exception {

        // Obtener la última configuración de email
        EmailConfig emailConfig = emailService.getLatestEmailConfig();

        // Crear el objeto de solicitud para enviar el email
        EmailCredentialProperties sendRequest = new EmailCredentialProperties();
        sendRequest.setSendTo(emailCredentials.getEmail()); // El email del destinatario
        sendRequest.setSubject(emailConfig.getSubject());
        sendRequest.setBody(emailConfig.getBody());
        sendRequest.setClientId(emailConfig.getClientId());
        sendRequest.setClientSecret(emailConfig.getClientSecret());
        sendRequest.setRefreshToken(emailConfig.getRefreshToken());
        sendRequest.setEmailConfig(emailConfig.getEmailConfig()); // El email del remitente
        sendRequest.setEmailReception(emailConfig.getEmailReception());
        sendRequest.setFooter(emailConfig.getFooter());
        sendRequest.setTitle(emailConfig.getTitle());
        sendRequest.setLogoEmail(emailConfig.getLogoEmail());

        // Enviar el email
        emailService.sendEmail(sendRequest);

        return ResponseEntity.ok("Correo enviado exitosamente.");
    }
}