package com.olusegunlight.ledger.domain;

public class EntryNotFoundException extends RuntimeException {

    public EntryNotFoundException(String entryId) {
        super("Journal entry not found: " + entryId);
    }
}