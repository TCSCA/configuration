package api.configuration.request;

public class EmailCredentialProperties {
    private String sendTo;
    private String subject;
    private String body;
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String emailConfig; // email del remitente

    // Getters y Setters
    public String getSendTo() { return sendTo; }
    public void setSendTo(String sendTo) { this.sendTo = sendTo; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getEmailConfig() { return emailConfig; }
    public void setEmailConfig(String emailConfig) { this.emailConfig = emailConfig; }
}