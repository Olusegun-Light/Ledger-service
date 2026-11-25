package com.olusegunlight.ledger.transaction.entity;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.domain.JournalEntry;
import com.olusegunlight.ledger.domain.Posting;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries")
public class JournalEntryEntity implements Persistable<String> {

    @Id
    private String id;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.PERSIST)
    @OrderBy("id")
    private List<PostingEntity> postings = new ArrayList<>();

    @Transient
    private boolean newEntity = true;

    protected JournalEntryEntity() {
    }

    private JournalEntryEntity(String id, String description, Currency currency) {
        this.id = id;
        this.description = description;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    public static JournalEntryEntity from(JournalEntry entry) {
        JournalEntryEntity entity = new JournalEntryEntity(entry.id(), entry.description(), entry.currency());
        for (Posting posting : entry.postings()) {
            entity.addPosting(new PostingEntity(posting.accountId(), posting.direction(), posting.amount().minorUnits()));
        }
        return entity;
    }

    private void addPosting(PostingEntity posting) {
        posting.attachTo(this);
        postings.add(posting);
    }

    public JournalEntry toDomain() {
        List<Posting> domainPostings = postings.stream().map(posting -> posting.toDomain(currency)).toList();
        return new JournalEntry(id, description, domainPostings);
    }

    @Override
    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PostingEntity> getPostings() {
        return List.copyOf(postings);
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.newEntity = false;
    }
}