package com.olusegunlight.ledger.transaction.repository;

import com.olusegunlight.ledger.domain.Direction;
import com.olusegunlight.ledger.transaction.entity.PostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostingRepository extends JpaRepository<PostingEntity, Long> {

    @Query("""
            select coalesce(sum(p.amountMinor), 0) from PostingEntity p
            where p.accountId = :accountId and p.direction = :direction
            """)
    long sumAmounts(String accountId, Direction direction);
}
