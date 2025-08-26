package api.configuration.controller;

import api.configuration.dto.StatusResponse;
import api.configuration.dto.TransactionRequest;
import api.configuration.model.Transaction;
import api.configuration.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/profile/{idProfile}")
    public ResponseEntity<List<Transaction>> getTransactionsByProfile(
            @PathVariable Long idProfile) {
        return ResponseEntity.ok(transactionService.getTransactionsByProfile(idProfile));
    }

    @PostMapping("/enable")
    public ResponseEntity<StatusResponse> enableTransaction(
            @RequestBody TransactionRequest request) {
        StatusResponse response = transactionService.enableTransaction(request);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/disable")
    public ResponseEntity<StatusResponse> disableTransaction(
            @RequestBody TransactionRequest request) {
        StatusResponse response = transactionService.disableTransaction(request);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle")
    public ResponseEntity<StatusResponse> toggleTransaction(
            @RequestBody TransactionRequest request) {
        StatusResponse response = transactionService.toggleTransaction(request);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}
