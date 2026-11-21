package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.LedgerCommand.Deposit;
import com.olusegunlight.ledger.domain.LedgerCommand.Transfer;
import com.olusegunlight.ledger.domain.LedgerCommand.Withdrawal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntryFactoryTest {

    private final EntryFactory factory = new EntryFactory();

    private static Money naira(long kobo) {
        return Money.of(kobo, Currency.NGN);
    }

    @Test
    void depositDebitsSettlementAndCreditsWallet() {
        JournalEntry entry = factory.toEntry(new Deposit("wallet:light", naira(5_000_00L)));

        assertThat(entry.postings()).containsExactly(
                Posting.debit(EntryFactory.SETTLEMENT_ACCOUNT, naira(5_000_00L)),
                Posting.credit("wallet:light", naira(5_000_00L)));
    }

    @Test
    void withdrawalIsTheMirrorOfDeposit() {
        JournalEntry entry = factory.toEntry(new Withdrawal("wallet:light", naira(1_000_00L)));

        assertThat(entry.postings()).containsExactly(
                Posting.debit("wallet:light", naira(1_000_00L)),
                Posting.credit(EntryFactory.SETTLEMENT_ACCOUNT, naira(1_000_00L)));
    }

    @Test
    void transferWithFeeHasThreePostings() {
        JournalEntry entry = factory.toEntry(
                new Transfer("wallet:light", "wallet:ada", naira(10_000_00L), naira(25_00L)));

        assertThat(entry.postings()).containsExactly(
                Posting.debit("wallet:light", naira(10_025_00L)),
                Posting.credit("wallet:ada", naira(10_000_00L)),
                Posting.credit(EntryFactory.FEE_ACCOUNT, naira(25_00L)));
    }

    @Test
    void transferWithoutFeeHasTwoPostings() {
        JournalEntry entry = factory.toEntry(
                new Transfer("wallet:light", "wallet:ada", naira(10_000_00L), Money.zero(Currency.NGN)));

        assertThat(entry.postings()).hasSize(2);
        assertThat(entry.netChangeByAccount()).doesNotContainKey(EntryFactory.FEE_ACCOUNT);
    }

    @Test
    void everyEntryGetsAUniqueId() {
        Deposit deposit = new Deposit("wallet:light", naira(100L));

        assertThat(factory.toEntry(deposit).id()).isNotEqualTo(factory.toEntry(deposit).id());
    }
}