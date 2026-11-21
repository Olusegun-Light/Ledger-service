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

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Open wallets, deposit money and check balances")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open a customer wallet")
    public AccountResponse open(@RequestBody OpenAccountRequest request) {
        return AccountResponse.from(accountService.openWallet(request.handle(), request.name()));
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get an account's current balance")
    public BalanceResponse balance(@PathVariable String accountId) {
        return BalanceResponse.of(accountId, accountService.balanceOf(accountId));
    }

    @PostMapping("/{accountId}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Deposit money into a wallet")
    public BalanceResponse deposit(@PathVariable String accountId, @RequestBody DepositRequest request) {
        return BalanceResponse.of(accountId, accountService.deposit(accountId, request.amount()));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get  an account's details")
    public AccountResponse open(@PathVariable String accountId){
        return AccountResponse.from(accountService.getAccount(accountId));
    }

}
