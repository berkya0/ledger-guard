package com.berkaykomur.transferservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID accountId,
        BigDecimal initialBalance
) {}