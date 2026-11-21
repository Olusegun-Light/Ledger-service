package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.CurrencyMismatchException;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.LedgerCommand.Deposit;
import com.olusegunlight.ledger.domain.LedgerCommand.Reversal;
import com.olusegunlight.ledger.domain.LedgerCommand.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryLedgerTest {

    private final EntryFactory factory = new EntryFactory();
    private InMemoryLedger ledger;

    private static Money naira(long kobo) {
        return Money.of(kobo, Currency.NGN);
    }

    @BeforeEach
    void setUp() {
        ChartOfAccounts chart = new ChartOfAccounts();
        chart.add(new LedgerAccount(EntryFactory.SETTLEMENT_ACCOUNT, "Bank settlement", AccountType.ASSET, Currency.NGN));
        chart.add(new LedgerAccount(EntryFactory.FEE_ACCOUNT, "Fee income", AccountType.INCOME, Currency.NGN));
        chart.add(new LedgerAccount("wallet:light", "Light's wallet", AccountType.LIABILITY, Currency.NGN));
        chart.add(new LedgerAccount("wallet:ada", "Ada's wallet", AccountType.LIABILITY, Currency.NGN));
        chart.add(new LedgerAccount("wallet:dollar", "Dollar wallet", AccountType.LIABILITY, Currency.USD));
        ledger = new InMemoryLedger(chart);
    }

    @Test
    void depositGrowsBothTheWalletAndTheSettlementAccount() {
        ledger.post(factory.toEntry(new Deposit("wallet:light", naira(5_000_00L))));

        assertThat(ledger.balanceOf("wallet:light")).isEqualTo(naira(5_000_00L));
        assertThat(ledger.balanceOf(EntryFactory.SETTLEMENT_ACCOUNT)).isEqualTo(naira(5_000_00L));
    }

    @Test
    void transferMovesMoneyAndCollectsTheFee() {
        ledger.post(factory.toEntry(new Deposit("wallet:light", naira(5_000_00L))));

        ledger.post(factory.toEntry(new Transfer("wallet:light", "wallet:ada", naira(2_000_00L), naira(25_00L))));

        assertThat(ledger.balanceOf("wallet:light")).isEqualTo(naira(2_975_00L));
        assertThat(ledger.balanceOf("wallet:ada")).isEqualTo(naira(2_000_00L));
        assertThat(ledger.balanceOf(EntryFactory.FEE_ACCOUNT)).isEqualTo(naira(25_00L));
    }

    @Test
    void rejectsOverdraftAndLeavesTheLedgerUntouched() {
        ledger.post(factory.toEntry(new Deposit("wallet:light", naira(1_000_00L))));
        JournalEntry tooBig = factory.toEntry(
                new Transfer("wallet:light", "wallet:ada", naira(1_000_00L), naira(25_00L)));

        assertThatThrownBy(() -> ledger.post(tooBig))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("wallet:light");

        assertThat(ledger.entries()).hasSize(1);
        assertThat(ledger.balanceOf("wallet:light")).isEqualTo(naira(1_000_00L));
        assertThat(ledger.balanceOf("wallet:ada")).isEqualTo(naira(0L));
    }

    @Test
    void rejectsUnknownAccounts() {
        JournalEntry entry = JournalEntry.builder("je-1", "To nowhere")
                .debit(EntryFactory.SETTLEMENT_ACCOUNT, naira(100_00L))
                .credit("wallet:ghost", naira(100_00L))
                .build();

        assertThatThrownBy(() -> ledger.post(entry))
                .isInstanceOf(AccountNotFoundException.class);
        assertThat(ledger.entries()).isEmpty();
    }

    @Test
    void rejectsEntryInADifferentCurrencyFromTheAccount() {
        JournalEntry entry = JournalEntry.builder("je-2", "Naira into a dollar wallet")
                .debit(EntryFactory.SETTLEMENT_ACCOUNT, naira(100_00L))
                .credit("wallet:dollar", naira(100_00L))
                .build();

        assertThatThrownBy(() -> ledger.post(entry))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void rejectsPostingTheSameEntryTwice() {
        JournalEntry deposit = factory.toEntry(new Deposit("wallet:light", naira(5_000_00L)));
        ledger.post(deposit);

        assertThatThrownBy(() -> ledger.post(deposit))
                .isInstanceOf(DuplicateEntryException.class);
        assertThat(ledger.balanceOf("wallet:light")).isEqualTo(naira(5_000_00L));
    }

    @Test
    void reversalRestoresEveryBalance() {
        JournalEntry deposit = factory.toEntry(new Deposit("wallet:light", naira(5_000_00L)));
        ledger.post(deposit);

        ledger.post(factory.toEntry(new Reversal(deposit)));

        assertThat(ledger.balanceOf("wallet:light")).isEqualTo(naira(0L));
        assertThat(ledger.balanceOf(EntryFactory.SETTLEMENT_ACCOUNT)).isEqualTo(naira(0L));
        assertThat(ledger.entries()).hasSize(2);
    }

    @Test
    void findsPostedEntriesById() {
        JournalEntry deposit = factory.toEntry(new Deposit("wallet:light", naira(5_000_00L)));
        ledger.post(deposit);

        assertThat(ledger.findEntry(deposit.id())).contains(deposit);
        assertThat(ledger.findEntry("missing")).isEmpty();
    }

    @Test
    void entriesCannotBeModifiedFromOutside() {
        assertThatThrownBy(() -> ledger.entries().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void listsOnlyTheEntriesThatTouchAnAccount() {
        JournalEntry lightDeposit = factory.toEntry(new Deposit("wallet:light", naira(5_000_00L)));
        JournalEntry adaDeposit = factory.toEntry(new Deposit("wallet:ada", naira(1_000_00L)));
        JournalEntry transfer = factory.toEntry(
                new Transfer("wallet:light", "wallet:ada", naira(2_000_00L), naira(25_00L)));
        ledger.post(lightDeposit);
        ledger.post(adaDeposit);
        ledger.post(transfer);

        assertThat(ledger.entriesFor("wallet:light")).containsExactly(lightDeposit, transfer);
        assertThat(ledger.entriesFor("wallet:ada")).containsExactly(adaDeposit, transfer);
        assertThat(ledger.entriesFor(EntryFactory.FEE_ACCOUNT)).containsExactly(transfer);
        assertThat(ledger.entriesFor(EntryFactory.SETTLEMENT_ACCOUNT)).containsExactly(lightDeposit, adaDeposit);
    }
}