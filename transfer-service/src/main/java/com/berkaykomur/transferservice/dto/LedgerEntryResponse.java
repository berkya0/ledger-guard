package com.berkaykomur.transferservice.dto;

import com.berkaykomur.transferservice.entity.EntryType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id,
        UUID accountId,
        UUID transferId,
        BigDecimal amount,
        EntryType entryType,
        Instant createdAt
) {}