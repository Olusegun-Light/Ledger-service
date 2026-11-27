package com.olusegunlight.ledger.transaction.repository;

import com.olusegunlight.ledger.transaction.entity.JournalEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntryEntity, String> {
    @Query("""
                        select e from JournalEntryEntity e join fetch e.postings
                        where e.id in (select p.journalEntry.id from PostingEntity p where p.accountId = :accountId)
                        order by e.createdAt, e.id
            """)
    List<JournalEntryEntity> findAllTouchingAccount(String accountId);
}
