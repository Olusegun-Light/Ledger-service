package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Money;

import java.util.List;
import java.util.Optional;

public interface Ledger {
    void openAccount(LedgerAccount account);

    Optional<LedgerAccount> findAccount(String accountId);

    default LedgerAccount getAccount(String accountId) {
        return findAccount(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    void post(JournalEntry entry);

    Money balanceOf(String accountId);

    Optional<JournalEntry> findEntry(String accountId);

    List<JournalEntry> entriesFor(String accountId);
}
