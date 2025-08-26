package api.configuration.request;

public class EmailCredentialProperties {
    private String sendTo;
    private String subject;
    private String body;
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String emailConfig; // email del remitente

    private String title;

    private String footer;
    private String emailReception;

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

    public String getEmailReception() {
        return emailReception;
    }

    public void setEmailReception(String emailReception) {
        this.emailReception = emailReception;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String tittle) {
        this.title = tittle;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}