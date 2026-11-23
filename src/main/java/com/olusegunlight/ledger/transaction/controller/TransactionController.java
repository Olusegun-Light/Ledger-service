package com.olusegunlight.ledger.transaction.controller;

import com.olusegunlight.ledger.transaction.dto.EntryResponse;
import com.olusegunlight.ledger.transaction.dto.TransferRequest;
import com.olusegunlight.ledger.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Move money between accounts")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer money between wallets (flat ₦25 fee)")
    public EntryResponse transfer(@RequestBody TransferRequest request) {
        return EntryResponse.from(
                transactionService.transfer(request.fromAccountId(), request.toAccountId(), request.amount()));
    }

    @PostMapping("/{entryId}/reversals")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reverse a posted journal entry")
    public EntryResponse reverse(@PathVariable String entryId) {
        return EntryResponse.from(transactionService.reverse(entryId));
    }

}
