package com.olusegunlight.ledger.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OpenAccountRequest(
        @NotNull
        @Pattern(regexp = "[a-z0-9-]{3,32}", message = "must be 3-32 characters: lowercase letters, digits or hyphens")
        @Schema(example = "tunde")
        String handle,

        @NotBlank
        @Size(max = 100)
        @Schema(example = "Tunde Bakare")
        String name ) {

}
