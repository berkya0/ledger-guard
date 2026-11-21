package com.berkaykomur.transferservice.dto;

import com.berkaykomur.transferservice.entity.TransferStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount,
        TransferStatus status,
        String failureReason,
        Instant createdAt
) {}