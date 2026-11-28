package com.olusegunlight.ledger.account.controller;

import com.olusegunlight.ledger.account.dto.AccountResponse;
import com.olusegunlight.ledger.account.dto.BalanceResponse;
import com.olusegunlight.ledger.account.dto.DepositRequest;
import com.olusegunlight.ledger.account.dto.OpenAccountRequest;
import com.olusegunlight.ledger.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.olusegunlight.ledger.transaction.dto.EntryResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Open wallets, deposit money and check balances")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Open a customer wallet")
    @ApiResponse(responseCode = "201", description = "Wallet opened; Location header points to it")
    @ApiResponse(responseCode = "400", description = "Invalid handle or name",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "A wallet with this handle already exists",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        AccountResponse account = AccountResponse.from(accountService.openWallet(request.handle(), request.name()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{accountId}")
                .buildAndExpand(account.id())
                .toUri();
        return ResponseEntity.created(location).body(account);
    }


    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get an account's current balance")
    public BalanceResponse balance(@PathVariable String accountId) {
        return BalanceResponse.of(accountId, accountService.balanceOf(accountId));
    }

    @PostMapping("/{accountId}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Deposit money into a wallet")
    public BalanceResponse deposit(@PathVariable String accountId, @Valid @RequestBody DepositRequest request) {
        return BalanceResponse.of(accountId, accountService.deposit(accountId, request.amount()));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get  an account's details")
    public AccountResponse open(@PathVariable String accountId) {
        return AccountResponse.from(accountService.getAccount(accountId));
    }

    @GetMapping("/{accountId}/statement")
    @Operation(summary = "List every journal entry that touched an account, oldest first")
    public List<EntryResponse> statement(@PathVariable String accountId) {
        return accountService.statement(accountId).stream().map(EntryResponse::from).toList();
    }

}
