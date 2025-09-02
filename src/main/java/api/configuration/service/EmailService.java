package api.configuration.service;

import api.configuration.model.EmailConfig;
import api.configuration.repository.JsonEmailConfigRepository;
import api.configuration.request.EmailCredentialProperties;
import api.configuration.request.EmailCredentials;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailSSLSocketFactory;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.URLDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Service
public class EmailService {

    private final String tokenUri = "https://oauth2.googleapis.com/token";
    private String accessToken;
    private Instant expiryTime;

    @Autowired
    private JsonEmailConfigRepository jsonEmailConfigRepository;

    /**
     * Obtiene el access_token desde Google OAuth2 con refresh_token
     */
    private String getAccessToken(EmailCredentialProperties credentials) {
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

    public void sendEmail(EmailCredentialProperties credentials) throws Exception {
        String token = getAccessToken(credentials);

        // Cargar plantilla HTML desde resources
        ClassPathResource resource = new ClassPathResource("templates/generalTemplate.html");
        if (!resource.exists()) {
            throw new RuntimeException("No se encontró la plantilla: templates/generalTemplate.html");
        }

        // Leer el contenido de la plantilla
        String htmlTemplate = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

        // Reemplazar los placeholders con los valores reales
        String processedHtml = htmlTemplate
                .replace("${body}", credentials.getBody() != null ? credentials.getBody() : "")
                .replace("$emailReception", credentials.getEmailReception() != null ? credentials.getEmailReception() : "")
                .replace("$title", credentials.getTitle() != null ? credentials.getTitle() : "")
                .replace("$footer", credentials.getFooter() != null ? credentials.getFooter() : "");

        Properties props = new Properties();
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // ⚡ Confiar en todos los certificados SSL (solo para desarrollo)
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        props.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(credentials.getEmailConfig()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(credentials.getSendTo()));
        message.setSubject(credentials.getSubject());

        // 🔹 Armamos multipart con HTML + Imagen embebida
        MimeMultipart multipart = new MimeMultipart("related");

        // Parte HTML
        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(processedHtml, "text/html; charset=utf-8");
        multipart.addBodyPart(htmlPart);

        if (credentials.getLogoEmail() != null && !credentials.getLogoEmail().isEmpty()) {
            setImageToMail("logo", multipart, credentials.getLogoEmail());
        }
        message.setContent(multipart);

        SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
        transport.connect("smtp.gmail.com", credentials.getEmailConfig(), token);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }


    public void saveConfig(EmailCredentials credentials) throws Exception {


            EmailConfig emailConfig = new EmailConfig();
            emailConfig.setEmailConfig(credentials.getEmail());
            emailConfig.setSubject(credentials.getSubject());
            emailConfig.setBody(credentials.getBody());
            emailConfig.setClientSecret(credentials.getClientSecret());
            emailConfig.setRefreshToken(credentials.getRefreshToken());
            emailConfig.setClientId(credentials.getClientId());
            emailConfig.setEmailReception(credentials.getEmailReception());
            emailConfig.setTitle(credentials.getTitle());
            emailConfig.setFooter(credentials.getFooter());
            emailConfig.setLogoEmail(credentials.getLogoEmail());
            emailConfig.setLastUpdated(LocalDateTime.now());


            jsonEmailConfigRepository.saveEmailConfig(emailConfig);
    }


    public EmailConfig getLatestEmailConfig() {
        try {
            Optional<EmailConfig> latestConfig = jsonEmailConfigRepository.findLatest();

            if (latestConfig.isPresent()) {
                return latestConfig.get();
            } else {
                throw new RuntimeException("No hay configuraciones de email disponibles");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la última configuración: " + e.getMessage(), e);
        }
    }

    public EmailConfig updateEmailConfig(EmailCredentials credentials) {
        try {
            // Buscar la ÚLTIMA configuración (la más reciente)
            Optional<EmailConfig> latestConfigOpt = jsonEmailConfigRepository.findLatest();

            // Obtener la última configuración para actualizarla
            EmailConfig latestConfig = latestConfigOpt.get();

            // Actualizar solo los campos que vienen en el request (no nulos)
            if (credentials.getSubject() != null) {
                latestConfig.setSubject(credentials.getSubject());
            }
            if (credentials.getBody() != null) {
                latestConfig.setBody(credentials.getBody());
            }
            if (credentials.getClientSecret() != null) {
                latestConfig.setClientSecret(credentials.getClientSecret());
            }
            if (credentials.getRefreshToken() != null) {
                latestConfig.setRefreshToken(credentials.getRefreshToken());
            }
            if (credentials.getClientId() != null) {
                latestConfig.setClientId(credentials.getClientId());
            }

            if (credentials.getEmailReception() != null) {
                latestConfig.setEmailReception(credentials.getEmailReception());
            }

            if (credentials.getTitle() != null){
                latestConfig.setTitle(credentials.getTitle());
            }

            if (credentials.getFooter() != null){
                latestConfig.setFooter(credentials.getFooter());
            }
            // Actualizar email solo si se proporciona uno nuevo
            if (credentials.getEmail() != null && !credentials.getEmail().equals(latestConfig.getEmailConfig())) {
                latestConfig.setEmailConfig(credentials.getEmail());
            }

            if (credentials.getFooter() != null){
                latestConfig.setFooter(credentials.getFooter());
            }
            // Actualizar email solo si se proporciona uno nuevo
            if (credentials.getTitle() != null){
                latestConfig.setTitle(credentials.getTitle());
            }

            if (credentials.getLogoEmail() != null){
                latestConfig.setLogoEmail(credentials.getLogoEmail());
            }
            // Actualizar la fecha
            latestConfig.setLastUpdated(LocalDateTime.now());

            // Guardar los cambios (actualizar la última configuración)
            jsonEmailConfigRepository.saveEmailConfig(latestConfig);

            return latestConfig;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la configuración: " + e.getMessage(), e);
        }
    }


    private void setImageToMail(String fileName, Multipart multipart, String logo) throws MessagingException, IOException {
        MimeBodyPart imagePart = new MimeBodyPart();

        if (logo.startsWith("http://") || logo.startsWith("https://")) {

            URL imageUrl = new URL(logo);
            DataSource dataSource = new URLDataSource(imageUrl);
            imagePart.setDataHandler(new DataHandler(dataSource));
        } else {

            if (logo.startsWith("data:")) {
                logo = logo.substring(logo.indexOf(",") + 1); // limpiar prefijo
            }
            byte[] logoBytes = Base64.getDecoder().decode(logo);
            DataSource fds = new ByteArrayDataSource(logoBytes, "image/png");
            imagePart.setDataHandler(new DataHandler(fds));
        }

        imagePart.setHeader("Content-ID", "<" + fileName + ">");
        imagePart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(imagePart);
    }


    }
