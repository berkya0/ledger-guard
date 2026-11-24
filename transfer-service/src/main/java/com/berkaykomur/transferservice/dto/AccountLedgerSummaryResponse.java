package com.berkaykomur.transferservice.dto;

import org.springframework.data.web.PagedModel;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountLedgerSummaryResponse(
        UUID accountId,
        BigDecimal expectedNetBalance,
        int entryCount,
        PagedModel<LedgerEntryResponse> entries
) {}