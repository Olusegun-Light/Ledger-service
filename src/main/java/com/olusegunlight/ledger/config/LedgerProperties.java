package com.olusegunlight.ledger.config;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

@ConfigurationProperties(prefix = "ledger")
public record LedgerProperties(Currency currency, String transferFee, String maxTransferAmount) {
    public LedgerProperties {
        Objects.requireNonNull(currency, "ledger.currency must be set");
        if(transferFee == null || transferFee.isBlank()) {
            throw new IllegalArgumentException("ledger.transfer-fee must be set");
        }

        if (maxTransferAmount == null || maxTransferAmount.isBlank()) {
            throw new IllegalArgumentException("ledger.max-transfer-amount must be set");
        }
        Money.fromMajor(transferFee, currency);
        Money.fromMajor(maxTransferAmount, currency);
    }

    public Money transferFeeAmount() {
        return Money.fromMajor(transferFee, currency);
    }

    public Money maxTransferAmountValue(){
        return Money.fromMajor(maxTransferAmount, currency);
    }

}
