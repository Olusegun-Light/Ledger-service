package com.olusegunlight.ledger.common;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(Currency expected,  Currency actual) {
        super("Currency mismatch: expected " + expected + " but got " + actual);
    }
}
