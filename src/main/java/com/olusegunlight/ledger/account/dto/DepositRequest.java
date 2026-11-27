package com.olusegunlight.ledger.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record DepositRequest(
        @NotNull
        @Digits(integer = 13, fraction = 2)
        @DecimalMin(value = "0.01")
        @Schema(example = "1500.00")
        String amount) {
}
