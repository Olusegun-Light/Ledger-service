package com.olusegunlight.ledger.transaction.entity;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.Direction;
import com.olusegunlight.ledger.domain.Posting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "postings")
public class PostingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id")
    private JournalEntryEntity journalEntry;

    @Column(nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private Direction direction;

    @Column(nullable = false)
    private long amountMinor;

    protected PostingEntity() {}

    PostingEntity(String accountId, Direction direction, long amountMinor) {
        this.accountId = accountId;
        this.direction = direction;
        this.amountMinor = amountMinor;
    }

    void attachTo(JournalEntryEntity journalEntry) {
        this.journalEntry = journalEntry;
    }

    Posting toDomain(Currency currency) {
        return new Posting(accountId, direction, Money.of(amountMinor, currency));
    }

    public Long getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public Direction getDirection() {
        return direction;
    }

    public long getAmountMinor() {
        return amountMinor;
    }
}
