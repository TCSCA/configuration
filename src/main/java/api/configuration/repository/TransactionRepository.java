package api.configuration.repository;

import api.configuration.model.Transaction;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {

    private static final String JSON_FILE = "config.json";
    private List<Transaction> transactions;
    private final ObjectMapper mapper = new ObjectMapper();
    private Path filePath;

    @PostConstruct
    public void init() {
        try {
            this.filePath = Paths.get("src/main/resources/" + JSON_FILE);
            loadTransactions();
        } catch (IOException e) {
            throw new RuntimeException("Error inicializando repositorio de transacciones", e);
        }
    }

    private void loadTransactions() throws IOException {
        if (Files.exists(filePath)) {
            String content = Files.readString(filePath);
            this.transactions = mapper.readValue(content, new TypeReference<>() {});
        } else {
            throw new RuntimeException("Archivo config.json no encontrado");
        }
    }

    private void saveTransactions() throws IOException {
        String jsonContent = mapper.writeValueAsString(transactions);
        Files.writeString(filePath, jsonContent);
    }

    public List<Transaction> findAll() {
        return transactions;
    }

    public List<Transaction> findByProfileIdAndStatusTrue(Long idProfile) {
        return transactions.stream()
                .filter(t -> t.getIdProfile().equals(idProfile) && t.getStatus())
                .toList();
    }

    public Optional<Transaction> findByTransactionNameAndProfile(String transactionName, Long idProfile) {
        return transactions.stream()
                .filter(t -> t.getTransaction().equalsIgnoreCase(transactionName)
                        && t.getIdProfile().equals(idProfile))
                .findFirst();
    }

    public Transaction updateTransactionStatus(String transactionName, Long idProfile, Boolean newStatus) throws IOException {
        Optional<Transaction> transactionOpt = findByTransactionNameAndProfile(transactionName, idProfile);

        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            transaction.setStatus(newStatus);
            saveTransactions();
            return transaction;
        }
        throw new RuntimeException("Transacción no encontrada");
    }

    public Transaction toggleTransactionStatus(String transactionName, Long idProfile) throws IOException {
        Optional<Transaction> transactionOpt = findByTransactionNameAndProfile(transactionName, idProfile);

        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            transaction.setStatus(!transaction.getStatus());
            saveTransactions();
            return transaction;
        }
        throw new RuntimeException("Transacción no encontrada");
    }

}
