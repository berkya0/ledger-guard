package com.berkaykomur.transferservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferInitiatedEvent(
        UUID transferId,
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount
) {}
//not accountservicedeki ile alan adları aynı olmalı
// ki mesajı alırken map edebilelim