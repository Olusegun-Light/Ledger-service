package com.olusegunlight.ledger.common;

public final class Naira {
    private Naira() {
    }

    public static String format(long kobo) {
        return String.format("₦%,d.%02d", kobo / 100, kobo % 100);
    }

}
