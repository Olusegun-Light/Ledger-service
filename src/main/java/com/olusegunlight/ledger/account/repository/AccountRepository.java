package com.olusegunlight.ledger.account.repository;

import com.olusegunlight.ledger.account.entity.AccountEntity;
import com.olusegunlight.ledger.domain.AccountStatus;
import com.olusegunlight.ledger.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;


public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    List<AccountEntity> findByTypeOrderByIdAsc(AccountType type);

    List<AccountEntity> findByStatus(AccountStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.id in :ids order by a.id")
    List<AccountEntity> lockAllById(Collection<String> ids);

    @Query("select a.status from AccountEntity a where a.id = :id")
    AccountStatus findStatusById(String id);
}
