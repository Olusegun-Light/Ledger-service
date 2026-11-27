package com.olusegunlight.ledger.persistence;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.AccountType;
import com.olusegunlight.ledger.domain.DuplicateEntryException;
import com.olusegunlight.ledger.domain.EntryFactory;
import com.olusegunlight.ledger.domain.InsufficientFundsException;
import com.olusegunlight.ledger.domain.JournalEntry;
import com.olusegunlight.ledger.domain.LedgerAccount;
import com.olusegunlight.ledger.domain.LedgerCommand.Deposit;
import com.olusegunlight.ledger.domain.LedgerCommand.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresLedger.class)
class PostgresLedgerTest {

    @Autowired
    PostgresLedger ledger;

    private final EntryFactory factory = new EntryFactory();

    private static Money naira(long kobo) {
        return Money.of(kobo, Currency.NGN);
    }

    @BeforeEach
    void openWallets() {
        ledger.openAccount(new LedgerAccount("wallet:test-a", "Test A", AccountType.LIABILITY, Currency.NGN));
        ledger.openAccount(new LedgerAccount("wallet:test-b", "Test B", AccountType.LIABILITY, Currency.NGN));
    }

    @Test
    void depositIsStoredAndBalanceIsComputedFromPostings() {
        ledger.post(factory.toEntry(new Deposit("wallet:test-a", naira(10_000_00))));

        assertThat(ledger.balanceOf("wallet:test-a")).isEqualTo(naira(10_000_00));
        assertThat(ledger.entriesFor("wallet:test-a")).hasSize(1);
    }

    @Test
    void transferThatWouldOverdrawIsRejectedAndNothingIsStored() {
        ledger.post(factory.toEntry(new Deposit("wallet:test-a", naira(100_00))));

        assertThatThrownBy(() -> ledger.post(factory.toEntry(
                new Transfer("wallet:test-a", "wallet:test-b", naira(100_00), naira(25_00)))))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(ledger.balanceOf("wallet:test-a")).isEqualTo(naira(100_00));
        assertThat(ledger.entriesFor("wallet:test-b")).isEmpty();
    }

    @Test
    void postingTheSameEntryTwiceIsRejected() {
        JournalEntry deposit = factory.toEntry(new Deposit("wallet:test-a", naira(500_00)));
        ledger.post(deposit);

        assertThatThrownBy(() -> ledger.post(deposit)).isInstanceOf(DuplicateEntryException.class);
        assertThat(ledger.balanceOf("wallet:test-a")).isEqualTo(naira(500_00));
    }

    @Test
    void openingAnExistingAccountIsRejected() {
        assertThatThrownBy(() -> ledger.openAccount(
                new LedgerAccount("wallet:test-a", "Someone else", AccountType.LIABILITY, Currency.NGN)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wallet:test-a");
    }
}