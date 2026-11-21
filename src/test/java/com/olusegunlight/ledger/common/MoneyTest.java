package com.olusegunlight.ledger.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {
    @Test
    void addsAmountsInSameCurrency(){
        Money a = Money.of(150_00L, Currency.NGN);
        Money b = Money.of(25_50L,  Currency.NGN);

        assertThat(a.plus(b)).isEqualTo(Money.of(175_50L, Currency.NGN));
    }

    @Test
    void subtractsAndCanGoNegative() {
        Money result = Money.of(10_00L, Currency.NGN).minus(Money.of(25_00L, Currency.NGN));

        assertThat(result.minorUnits()).isEqualTo(-15_00L);
        assertThat(result.isNegative()).isTrue();
    }

    @Test
    void rejectsMixingCurrencies() {
        Money naira = Money.of(100L, Currency.NGN);
        Money dollars = Money.of(100L, Currency.USD);

        assertThatThrownBy(() -> naira.plus(dollars)).isInstanceOf(CurrencyMismatchException.class).hasMessageContaining("NGN").hasMessageContaining("USD");
    }

    @Test
    void failsLoudlyOnOverflow(){
        Money huge = Money.of(Long.MAX_VALUE, Currency.NGN);

        assertThatThrownBy(() -> huge.plus(Money.of(1L, Currency.NGN))).isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "12345.67, NGN, 1234567",
            "0.1,      NGN, 10",
            "500,      JPY, 500",
            "-20.5,    USD, -2050"
    })

    void convertsMajorUnitsToMinorUnits(String amount, Currency currency, long expectedMinorUnits) {
        assertThat(Money.fromMajor(amount, currency).minorUnits()).isEqualTo(expectedMinorUnits);
    }

    @ParameterizedTest
    @CsvSource({
            "10.123, NGN",
            "1.5,    JPY"
    })

    void rejectsMoreDecimalPlacesThanTheCurrencyAllows(String amount, Currency currency) {
        assertThatThrownBy(() -> Money.fromMajor(amount, currency)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void formatsWithSymbolAndThousandsSeparators() {
        assertThat(Money.of(1_234_567L, Currency.NGN)).hasToString("₦12,345.67");
        assertThat(Money.of(500L, Currency.JPY)).hasToString("¥500");
    }

    @Test
    void negateFlipsTheSign() {
        Money fifty = Money.of(50_00L, Currency.NGN);

        assertThat(fifty.negate()).isEqualTo(Money.of(-50_00L, Currency.NGN));
        assertThat(fifty.negate().negate()).isEqualTo(fifty);
    }

    @Test
    void negateFailsLoudlyOnOverflow() {
        Money smallest = Money.of(Long.MIN_VALUE, Currency.NGN);

        assertThatThrownBy(smallest::negate).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void comparesAmountsInSameCurrency() {
        Money ten = Money.of(10_000L, Currency.NGN);
        Money twenty = Money.of(20_000L, Currency.NGN);

        assertThat(twenty.isGreaterThan(ten)).isTrue();
        assertThat(ten.isGreaterThan(twenty)).isFalse();
        assertThat(ten.isGreaterThan(ten)).isFalse();
    }

    @Test
    void rejectsComparingDifferentCurrencies() {
        Money naira = Money.of(100L, Currency.NGN);
        Money dollars = Money.of(100L, Currency.USD);

        assertThatThrownBy(() -> naira.isGreaterThan(dollars)).isInstanceOf(CurrencyMismatchException.class);
    }

}
