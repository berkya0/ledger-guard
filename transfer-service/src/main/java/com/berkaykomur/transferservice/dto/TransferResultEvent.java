package com.berkaykomur.transferservice.dto;

import java.time.Instant;
import java.util.UUID;

public record TransferResultEvent(
        UUID transferId,
        boolean success,
        String failureReason,
        Instant processedAt
) {}