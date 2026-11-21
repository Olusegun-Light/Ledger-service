package com.olusegunlight.ledger.account.dto;

import com.olusegunlight.ledger.domain.LedgerAccount;

public record AccountResponse(String id, String name, String type, String currency) {
    public static AccountResponse from(LedgerAccount account) {
        return new AccountResponse(account.id(), account.name(), account.type().name(), account.currency().name());
    }
}
