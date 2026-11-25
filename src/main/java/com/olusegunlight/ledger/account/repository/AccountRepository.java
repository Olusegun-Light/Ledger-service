package com.olusegunlight.ledger.account.repository;

import com.olusegunlight.ledger.account.entity.AccountEntity;
import com.olusegunlight.ledger.domain.AccountStatus;
import com.olusegunlight.ledger.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    List<AccountEntity> findByTypeOrderByIdAsc(AccountType type);

    List<AccountEntity> findByStatus(AccountStatus status);
}
