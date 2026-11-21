package com.berkaykomur.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "Owner name is required.")
        @Size(min=3,max = 100, message = "Owner name must be between 3 and 100 characters.")
        String ownerName,
        @NotNull(message = "Balance is required.")
        @PositiveOrZero(message = "Balance must be zero or positive.")
        BigDecimal balance

) {
}
