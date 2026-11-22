package com.berkaykomur.transferservice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AccountLedgerSummaryResponse(
        UUID accountId,
        BigDecimal expectedNetBalance,
        int entryCount,
        List<LedgerEntryResponse> entries
) {}