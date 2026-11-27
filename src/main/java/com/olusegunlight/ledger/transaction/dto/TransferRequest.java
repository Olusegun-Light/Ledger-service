package com.olusegunlight.ledger.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
        @NotBlank
        @Schema(example = "wallet:light")
        String fromAccountId,

        @NotBlank
        @Schema(example = "wallet:ada")
        String toAccountId,

        @NotNull
        @Digits(integer = 13, fraction = 2)
        @DecimalMin("0.01")
        @Schema(example = "100.00")
        String amount) {

    @AssertTrue(message = "must be different from fromAccountId")
    @Schema(hidden = true)
    public boolean isToAccountDifferent() {
        return fromAccountId == null || !fromAccountId.equals(toAccountId);
    }
}
