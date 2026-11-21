package com.olusegunlight.ledger.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(long minorUnits, Currency currency) {

    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
    }

    public static Money of(long minorUnits, Currency currency) {
        return new Money(minorUnits, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0, currency);
    }

    public static Money fromMajor(String amount, Currency currency) {
        try {
            BigDecimal minor = new BigDecimal(amount).setScale(currency.getFractionDigits(), RoundingMode.UNNECESSARY).movePointRight(currency.getFractionDigits());
            return new Money(minor.longValueExact(), currency);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amount " + amount + " is not valid for " + currency, e);
        }
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(minorUnits, other.minorUnits), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.subtractExact(minorUnits, other.minorUnits), currency);
    }

    public boolean isZero() {
        return minorUnits == 0;
    }

    public boolean isNegative() {
        return minorUnits < 0;
    }

    public BigDecimal toMajor() {
        return BigDecimal.valueOf(minorUnits,  currency.getFractionDigits());
    }

    private void requireSameCurrency(Money other) {
        if (currency != other.currency) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }

    public Money negate() {
        return new Money(Math.negateExact(minorUnits), currency);
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return minorUnits > other.minorUnits;
    }

    @Override
    public String toString() {
        return String.format("%s%,." + currency.getFractionDigits() + "f", currency.getSymbol(), toMajor());
    }
}
