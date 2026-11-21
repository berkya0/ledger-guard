package com.berkaykomur.accountservice.consumer;

import com.berkaykomur.accountservice.dto.TransferRequest;
import com.berkaykomur.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferEventConsumer {

    private final AccountService accountService;

    @RabbitListener(
            queues = "${rabbitmq.queue.transfer-initiated}",
            errorHandler = "transferErrorHandler"
    )
    public void handleTransferInitiated(TransferRequest request) {
        log.info("Received transfer initiated event: transferId={}, senderAccountId={}, receiverAccountId={}, amount={}",
                request.transferId(),
                request.senderAccountId(),
                request.receiverAccountId(),
                request.amount());

        accountService.applyTransfer(request);

        log.info("Successfully processed transfer event: transferId={}", request.transferId());
    }
}