package com.olusegunlight.ledger.domain;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String accountId, AccountStatus status) {
        super("Account " + accountId + " is " + status + " and cannot be debited");
    }
}
