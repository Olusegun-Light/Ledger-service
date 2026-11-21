package com.olusegunlight.ledger.account.dto;

import com.olusegunlight.ledger.common.Money;

public record BalanceResponse(String accountId, String amount, String currency) {
    public static BalanceResponse of(String accountId, Money balance) {
        return new BalanceResponse(accountId, balance.toMajor().toPlainString(), balance.currency().name());
    }
}
