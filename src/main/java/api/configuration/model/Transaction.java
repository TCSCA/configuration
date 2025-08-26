package api.configuration.model;

import lombok.Data;

@Data
public class Transaction {
    private Long idTransaction;
    private String transaction;
    private Boolean status;
    private Long idProfile;
    private String url;
}
