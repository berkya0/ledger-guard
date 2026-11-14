package com.berkaykomur.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountRequest(
        @NotBlank(message = "Owner name is required.")
        @Size(max = 100, message = "Owner name must not exceed 100 characters.")
        String ownerName
) {
}
