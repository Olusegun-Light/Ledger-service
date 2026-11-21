package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Money;

public sealed interface LedgerCommand {
    record Deposit(String walletId, Money amount) implements LedgerCommand {}

    record Withdrawal(String walletId, Money amount) implements LedgerCommand {}

    record Transfer(String fromWalletId, String toWalletId, Money amount, Money fee) implements LedgerCommand {}
}
