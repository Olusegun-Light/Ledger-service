package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Currency;

import java.util.Objects;

public record LedgerAccount(String id, String name, AccountType type, Currency currency) {

    public LedgerAccount{
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Account id must not be blank");
        }
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
