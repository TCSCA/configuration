package api.configuration.controller;



import api.configuration.request.EmailCredentials;
import api.configuration.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/email")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }


    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(@RequestBody EmailCredentials request)
            throws Exception {


        emailService.sendEmail(
                request
        );

        return ResponseEntity.ok("Correo enviado exitosamente.");
    }
}