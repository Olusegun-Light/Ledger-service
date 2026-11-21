package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JournalEntryTest {
    private static Money naira(long kobo) {
        return Money.of(kobo, Currency.NGN);
    }

    @Test
    void acceptsBalancedTransferWithFee() {
        JournalEntry entry = new JournalEntry("je-1", "Transfer to Ada", List.of(
                Posting.debit("wallet:light", naira(10_025_00L)),
                Posting.credit("wallet:ada", naira(10_000_00L)),
                Posting.credit("income:fees", naira(25_00L))
        ));

        assertThat(entry.totalDebits()).isEqualTo(naira(10_025_00L));
        assertThat(entry.totalCredits()).isEqualTo(entry.totalDebits());
        assertThat(entry.currency()).isEqualTo(Currency.NGN);
    }

    @Test
    void reportsNetChangePerAccount() {
        JournalEntry entry = new JournalEntry("je-2", "Two-step debit", List.of(
                Posting.debit("wallet:light", naira(300_00L)),
                Posting.debit("wallet:light", naira(200_00L)),
                Posting.credit("wallet:ada", naira(500_00L))
        ));

        Map<String, Money> changes = entry.netChangeByAccount();

        assertThat(changes)
                .hasSize(2)
                .containsEntry("wallet:light", naira(500_00L))
                .containsEntry("wallet:ada", naira(-500_00L));
    }

    @Test
    void rejectsUnbalancedEntry() {
        List<Posting> postings = List.of(
                Posting.debit("wallet:light", naira(100_00L)),
                Posting.credit("wallet:ada", naira(99_00L))
        );

        assertThatThrownBy(() -> new JournalEntry("je-3", "Leaky", postings))
                .isInstanceOf(UnbalancedEntryException.class)
                .hasMessageContaining("do not equal");
    }

    @Test
    void rejectsSinglePosting() {
        List<Posting> postings = List.of(Posting.debit("wallet:light", naira(100_00L)));

        assertThatThrownBy(() -> new JournalEntry("je-4", "Lonely", postings))
                .isInstanceOf(UnbalancedEntryException.class);
    }

    @Test
    void rejectsMixedCurrencies(){
        List<Posting> postings = List.of(
                Posting.debit("wallet:light", naira(100_00L)),
                Posting.credit("wallet:ada", Money.of(100_00L, Currency.USD))
        );

        assertThatThrownBy(() -> new JournalEntry("je-5", "Mixed", postings))
                .isInstanceOf(UnbalancedEntryException.class)
                .hasMessageContaining("one currency");
    }

    @Test
    void cannotBeChangedAfterCreation() {
        List<Posting> postings = new ArrayList<>();
        postings.add(Posting.debit("wallet:light", naira(100_00L)));
        postings.add(Posting.credit("wallet:ada", naira(100_00L)));
        JournalEntry entry = new JournalEntry("je-6", "Immutable", postings);

        postings.add(Posting.credit("wallet:thief", naira(1_000_00L)));

        assertThat(entry.postings()).hasSize(2);
        assertThatThrownBy(() -> entry.postings().add(Posting.credit("wallet:thief", naira(1L))))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void postingRejectsZeroOrNegativeAmounts() {
        assertThatThrownBy(() -> Posting.debit("wallet:light", naira(0L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Posting.credit("wallet:light", naira(-1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listsEachAccountOnceInOrderOfFirstAppearance() {
        JournalEntry entry = new JournalEntry("je-7", "Two-step debit", List.of(
                Posting.debit("wallet:light", naira(300_00L)),
                Posting.debit("wallet:light", naira(200_00L)),
                Posting.credit("wallet:ada", naira(500_00L))
        ));

        assertThat(entry.accountIds()).containsExactly("wallet:light", "wallet:ada");
    }

}
