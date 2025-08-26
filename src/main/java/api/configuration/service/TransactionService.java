package api.configuration.service;

import api.configuration.dto.StatusResponse;
import api.configuration.dto.TransactionRequest;
import api.configuration.model.Transaction;
import api.configuration.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByProfile(Long idProfile) {
        return transactionRepository.findByProfileIdAndStatusTrue(idProfile);
    }

    public StatusResponse enableTransaction(TransactionRequest request) {
        try {
            Transaction updated = transactionRepository.updateTransactionStatus(
                    request.transaction(),
                    request.idProfile(),
                    true
            );
            return new StatusResponse(
                    "Transacción habilitada exitosamente",
                    true,
                    updated.getStatus()
            );
        } catch (IOException e) {
            return new StatusResponse(
                    "Error habilitando transacción: " + e.getMessage(),
                    false,
                    null
            );
        } catch (RuntimeException e) {
            return new StatusResponse(
                    e.getMessage(),
                    false,
                    null
            );
        }
    }

    public StatusResponse disableTransaction(TransactionRequest request) {
        try {
            Transaction updated = transactionRepository.updateTransactionStatus(
                    request.transaction(),
                    request.idProfile(),
                    false
            );
            return new StatusResponse(
                    "Transacción deshabilitada exitosamente",
                    true,
                    updated.getStatus()
            );
        } catch (IOException e) {
            return new StatusResponse(
                    "Error deshabilitando transacción: " + e.getMessage(),
                    false,
                    null
            );
        } catch (RuntimeException e) {
            return new StatusResponse(
                    e.getMessage(),
                    false,
                    null
            );
        }
    }

    public StatusResponse toggleTransaction(TransactionRequest request) {
        try {
            Transaction updated = transactionRepository.toggleTransactionStatus(
                    request.transaction(),
                    request.idProfile()
            );
            String action = updated.getStatus() ? "habilitada" : "deshabilitada";
            return new StatusResponse(
                    "Transacción " + action + " exitosamente",
                    true,
                    updated.getStatus()
            );
        } catch (IOException e) {
            return new StatusResponse(
                    "Error cambiando estado de transacción: " + e.getMessage(),
                    false,
                    null
            );
        } catch (RuntimeException e) {
            return new StatusResponse(
                    e.getMessage(),
                    false,
                    null
            );
        }
    }

}
