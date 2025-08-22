package api.configuration.service;

import api.configuration.request.EmailCredentials;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailSSLSocketFactory;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailService {

    private final String tokenUri = "https://oauth2.googleapis.com/token";
    private String accessToken;
    private Instant expiryTime;

    /**
     * Obtiene el access_token desde Google OAuth2 con refresh_token
     */
    private String getAccessToken(EmailCredentials credentials) {
        if (accessToken != null && expiryTime != null && Instant.now().isBefore(expiryTime)) {
            return accessToken;
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + credentials.getClientId()
                + "&client_secret=" + credentials.getClientSecret()
                + "&refresh_token=" + credentials.getRefreshToken()
                + "&grant_type=refresh_token";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            accessToken = (String) responseBody.get("access_token");
            Integer expiresIn = (Integer) responseBody.get("expires_in");
            expiryTime = Instant.now().plusSeconds(expiresIn);
            return accessToken;
        }

        throw new RuntimeException("No se pudo obtener el access_token de Gmail");
    }

    /**
     * Envía un correo usando Gmail + OAuth2
     */

    public void sendEmail(EmailCredentials credentials) throws Exception {
        String token = getAccessToken(credentials);

        // Cargar plantilla HTML desde resources
        ClassPathResource resource = new ClassPathResource("templates/forgotPassword.html");
        if (!resource.exists()) {
            throw new RuntimeException("No se encontró la plantilla: templates/forgotPassword.html");
        }

        String body;
        try (InputStream inputStream = resource.getInputStream()) {
            body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }

        String subject = "Recuperar contraseña";

        if (credentials.getEmail().contains("gmail")){
        Properties props = new Properties();
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // ⚡ Confiar en todos los certificados SSL (para desarrollo)
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        props.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(credentials.getEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(credentials.getSendTo()));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8"); // enviar HTML

        SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
        transport.connect("smtp.gmail.com", credentials.getEmail(), token);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
        }
    }
}
