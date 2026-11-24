package com.berkaykomur.transferservice.consumer;

import com.berkaykomur.transferservice.dto.AccountCreatedEvent;
import com.berkaykomur.transferservice.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final LedgerService ledgerService;

    @RabbitListener(queues = "account.created.queue")
    public void consumeAccountCreated(AccountCreatedEvent event) {
        log.info("Received AccountCreatedEvent: accountId={}", event.accountId());
        ledgerService.recordInitialBalance(event);
    }
}
