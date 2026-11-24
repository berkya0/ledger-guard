package com.berkaykomur.accountservice.producer;

import com.berkaykomur.accountservice.dto.AccountCreatedEvent;
import com.berkaykomur.accountservice.dto.TransferResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.transfer}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.transfer-result}")
    private String transferResultRoutingKey;

    @Value("${rabbitmq.routing-key.account-created}")
    private String accountCreatedRoutingKey;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferResultEvent(TransferResultEvent event) {
        publishTransferResult(event);
    }

    public void publishTransferResult(TransferResultEvent event) {
        log.info("Publishing transfer result: transferId={}, success={}, reason={}",
                event.transferId(), event.success(), event.failureReason());
        rabbitTemplate.convertAndSend(exchangeName, transferResultRoutingKey, event);
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        log.info("Publishing AccountCreatedEvent after commit: accountId={}, initialBalance={}",
                event.accountId(), event.initialBalance());
        rabbitTemplate.convertAndSend(
                exchangeName,
                accountCreatedRoutingKey,
                event
        );
    }
}