package com.olusegunlight.ledger.domain;

public class AccountAlreadyExistsException extends RuntimeException {

    public AccountAlreadyExistsException(String accountId) {
        super("Account with already exists " + accountId);
    }
}
