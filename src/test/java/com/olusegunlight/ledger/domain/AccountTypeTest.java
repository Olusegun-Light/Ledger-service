package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTypeTest {

    private final Money debits = Money.of(700_00L, Currency.NGN);
    private final Money credits = Money.of(1_000_00L, Currency.NGN);

    @Test
    void liabilityGrowsWithCredits() {
        assertThat(AccountType.LIABILITY.balanceFrom(debits, credits))
                .isEqualTo(Money.of(300_00L, Currency.NGN));
    }

    @Test
    void assetGrowsWithDebits() {
        assertThat(AccountType.ASSET.balanceFrom(debits, credits))
                .isEqualTo(Money.of(-300_00L, Currency.NGN));
    }
}
