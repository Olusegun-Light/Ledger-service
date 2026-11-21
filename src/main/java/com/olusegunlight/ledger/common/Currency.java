package com.olusegunlight.ledger.common;

public enum Currency {
    NGN(2,"₦"),

    USD(2,"$"),

    GBP(2,"£"),

    JPY(0,"¥");

    private final int fractionDigits;
    private final String symbol;

    Currency(int fractionDigits, String symbol) {
        this.fractionDigits = fractionDigits;
        this.symbol = symbol;
    }

    public int getFractionDigits() {
        return fractionDigits;
    }

    public String getSymbol() {
        return symbol;
    }
}
