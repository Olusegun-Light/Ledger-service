package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Currency;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChartOfAccountsTest {

    private final ChartOfAccounts chart = new ChartOfAccounts();
    private final LedgerAccount lightWallet =
            new LedgerAccount("wallet:light", "Light's wallet", AccountType.LIABILITY, Currency.NGN);

    @Test
    void findsAnAccountThatExists() {
        chart.add(lightWallet);

        assertThat(chart.find("wallet:light")).contains(lightWallet);
        assertThat(chart.get("wallet:light").type()).isEqualTo(AccountType.LIABILITY);
    }

    @Test
    void returnsEmptyForUnknownAccount() {
        assertThat(chart.find("wallet:nobody")).isEmpty();
    }

    @Test
    void getThrowsForUnknownAccount() {
        assertThatThrownBy(() -> chart.get("wallet:nobody"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("wallet:nobody");
    }

    @Test
    void rejectsDuplicateIds() {
        chart.add(lightWallet);

        assertThatThrownBy(() -> chart.add(lightWallet))
                .isInstanceOf(AccountAlreadyExistsException.class);
        assertThat(chart.size()).isEqualTo(1);
    }
}