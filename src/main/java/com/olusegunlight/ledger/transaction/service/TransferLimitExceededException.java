package com.olusegunlight.ledger.transaction.service;

import com.olusegunlight.ledger.common.Money;

public class TransferLimitExceededException extends RuntimeException {

    private final Money limit;

    public TransferLimitExceededException(Money amount, Money limit) {
        super("Transfer of " + amount + " exceeds the limit of " + limit);
        this.limit = limit;
    }

    public Money getLimit() {
        return limit;
    }
}
