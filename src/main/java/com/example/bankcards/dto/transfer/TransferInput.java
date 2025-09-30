package com.example.bankcards.dto.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferInput(
        @NotNull(message = "From card ID is required")
        Long fromCardId,

        @NotNull(message = "To card ID is required")
        Long toCardId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {
}
