package com.berkaykomur.transferservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransferRequest(
        @NotNull(message = "Sender account ID is required")
        UUID senderAccountId,

        @NotNull(message = "Receiver account ID is required")
        UUID receiverAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        BigDecimal amount
) {}