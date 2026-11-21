package com.olusegunlight.ledger.domain;

public class DuplicateEntryException extends RuntimeException {

    public DuplicateEntryException(String entryId) {

      super("Journal entry already posted: " + entryId);
    }
}
