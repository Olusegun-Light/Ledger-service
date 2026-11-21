package com.olusegunlight.ledger.domain;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountId) {
        super("Insufficient funds in " + accountId);
    }
}
