package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

public record JournalEntry(String id, String description, List<Posting> postings) {

    public JournalEntry {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(postings, "postings must not be null");
        postings = List.copyOf(postings);

        if (postings.size() < 2) {
            throw new UnbalancedEntryException("A journal entry needs at least two postings");
        }

        Set<Currency> currencies = postings.stream()
                .map(posting -> posting.amount().currency())
                .collect(Collectors.toSet());
        if (currencies.size() != 1) {
            throw new UnbalancedEntryException("A journal entry must use one currency, got " + currencies);
        }

        Money debits = sum(postings, Direction.DEBIT);
        Money credits = sum(postings, Direction.CREDIT);
        if (!debits.equals(credits)) {
            throw new UnbalancedEntryException("Debits " + debits + " do not equal credits " + credits);
        }
    }

    public Currency currency() {
        return postings.getFirst().amount().currency();
    }

    public Money totalDebits(){
        return sum(postings, Direction.DEBIT);
    }

    public Money totalCredits(){
        return sum(postings, Direction.CREDIT);
    }

    public Map<String, Money> netChangeByAccount() {
        return postings.stream()
                .collect(Collectors.toMap(Posting::accountId, Posting::signedAmount, Money::plus));
    }

    private static Money sum(List<Posting> postings, Direction direction) {
        Currency currency = postings.getFirst().amount().currency();
        return postings.stream().filter(posting -> posting.direction() == direction).map(Posting::amount).reduce(Money.zero(currency),Money::plus);
    }

    public List<String> accountIds() {
        return postings.stream().map(Posting::accountId).distinct().toList();
    }

    public static Builder builder(String id, String description) {
        return new Builder(id, description);
    }

    public static final class Builder {

        private final String id;
        private final String description;
        private final List<Posting> postings = new ArrayList<>();

        private Builder(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public Builder debit(String accountId, Money amount) {
            postings.add(Posting.debit(accountId, amount));
            return this;
        }

        public Builder credit(String accountId, Money amount) {
            postings.add(Posting.credit(accountId, amount));
            return this;
        }

        public JournalEntry build() {
            return new JournalEntry(id, description, postings);
        }
    }
}
