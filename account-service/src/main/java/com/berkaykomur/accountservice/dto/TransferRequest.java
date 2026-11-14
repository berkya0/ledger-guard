package com.berkaykomur.accountservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID transferId, //önemli aynı transfer işlemi tekrar gönderilirse
                        //buraya bakılarak kontrol edilecek ve tekrar transfer işlemi yapılmayacak
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount
) {
}
