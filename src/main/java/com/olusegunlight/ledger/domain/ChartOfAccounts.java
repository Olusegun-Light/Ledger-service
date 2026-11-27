package com.olusegunlight.ledger.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChartOfAccounts {
    private final Map<String, LedgerAccount> accounts = new HashMap<>();

    public void add(LedgerAccount account) {
        LedgerAccount existing = accounts.putIfAbsent(account.id(), account);
        if(existing != null) {
            throw new AccountAlreadyExistsException("Account already exists: " + account.id());
        }
    }

    public Optional<LedgerAccount> find(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public LedgerAccount get(String id) {
        return find(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public int size() {
        return accounts.size();
    }
}
