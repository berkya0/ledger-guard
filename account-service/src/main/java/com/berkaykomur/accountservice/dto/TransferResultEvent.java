package com.berkaykomur.accountservice.dto;

import java.time.Instant;
import java.util.UUID;

public record TransferResultEvent(
        UUID transferId,
        boolean success,
        String failureReason,
        Instant processedAt
) {
    public static TransferResultEvent completed(UUID transferId) {
        return new TransferResultEvent(transferId, true, null, Instant.now());
    }

    public static TransferResultEvent failed(UUID transferId, String reason) {
        return new TransferResultEvent(transferId, false, reason, Instant.now());
    }
}