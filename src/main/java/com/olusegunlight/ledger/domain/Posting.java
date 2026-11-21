package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Money;

import java.util.Objects;

public record Posting(String accountId, Direction direction, Money amount) {
    public Posting {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(direction, "direction must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.isZero() || amount.isNegative()) {
            throw new IllegalArgumentException("Posting amount must be positive, got " + amount);
        }
    }

    public static Posting debit(String accountId, Money amount) {
        return new Posting(accountId, Direction.DEBIT, amount);
    }

    public static Posting credit(String accountId, Money amount) {
        return new Posting(accountId, Direction.CREDIT, amount);
    }

    public Money signedAmount() {
        return direction == Direction.DEBIT ? amount : amount.negate();
    }
}