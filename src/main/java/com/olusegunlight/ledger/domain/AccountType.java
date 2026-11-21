package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Money;

public enum AccountType {
    ASSET(Direction.DEBIT),
    LIABILITY(Direction.CREDIT),
    EQUITY(Direction.CREDIT),
    INCOME(Direction.CREDIT),
    EXPENSE(Direction.DEBIT);

    private final Direction normalBalance;

    AccountType(Direction normalBalance) {
        this.normalBalance = normalBalance;
    }

    public Direction getNormalBalance() {
        return normalBalance;
    }

    public Money balanceFrom(Money debits, Money credits) {
        return normalBalance == Direction.DEBIT ? debits.minus(credits) : credits.minus(debits);
    }
}
