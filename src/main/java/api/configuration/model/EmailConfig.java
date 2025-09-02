package api.configuration.model;

import api.configuration.request.EmailCredentials;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailConfig extends EmailCredentials {

    private Long id;
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String emailConfig;
    private String body;
    private String subject;
    private String emailReception;
    private String title;
    private String footer;
    private String logoEmail;
    private LocalDateTime lastUpdated;
}
