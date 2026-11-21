package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.CurrencyMismatchException;
import com.olusegunlight.ledger.common.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InMemoryLedger {
    private final ChartOfAccounts chart;
    private final List<JournalEntry> entries = new ArrayList<>();
    private final Set<String> postedIds = new HashSet<>();

    public InMemoryLedger(ChartOfAccounts chart) {
        this.chart = chart;
    }

    public void post(JournalEntry entry) {
        if (postedIds.contains(entry.id())) {
            throw new DuplicateEntryException((entry.id()));
        }

        for (String accountId : entry.accountIds()) {
            LedgerAccount account = chart.get(accountId);
            if (account.currency() != entry.currency()) {
                throw new CurrencyMismatchException(account.currency(), entry.currency());
            }
        }

        List<JournalEntry> afterPosting = new ArrayList<>(entries);
        afterPosting.add(entry);
        for (String accountId : entry.accountIds()) {
            LedgerAccount account = chart.get(accountId);
            if (account.type() == AccountType.LIABILITY && balanceOf(account, afterPosting).isNegative()) {
                throw new InsufficientFundsException(accountId);
            }
        }

        entries.add(entry);
        postedIds.add(entry.id());
    }

    public Money balanceOf(String accountId) {
        return balanceOf(chart.get(accountId), entries);
    }

    public Optional<JournalEntry> findEntry(String entryId) {
        return entries.stream().filter(entry -> entry.id().equals(entryId)).findFirst();
    }

    public List<JournalEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    private static Money balanceOf(LedgerAccount account, List<JournalEntry> journal) {
        List<Posting> postings = journal.stream().flatMap(entry -> entry.postings().stream()).filter(posting -> posting.accountId().equals(account.id())).toList();

        Money debits = total(postings, Direction.DEBIT, account);
        Money credits = total(postings, Direction.CREDIT, account);
        return account.type().balanceFrom(debits, credits);
    }

    private static Money total(List<Posting> postings, Direction direction, LedgerAccount account) {
        return postings.stream().filter(posting -> posting.direction() == direction).map(Posting::amount).reduce(Money.zero(account.currency()), Money::plus);
    }

    public List<JournalEntry> entriesFor(String accountId) {
        return entries.stream().filter(entry -> entry.accountIds().contains(accountId)).toList();
    }
}
