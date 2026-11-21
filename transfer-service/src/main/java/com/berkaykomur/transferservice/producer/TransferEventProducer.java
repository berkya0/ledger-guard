package com.berkaykomur.transferservice.producer;

import com.berkaykomur.transferservice.dto.TransferInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.transfer}")
    private String transferExchange;

    @Value("${rabbitmq.routing-key.transfer-initiated}")
    private String transferInitiatedRoutingKey;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishTransferInitiated(TransferInitiatedEvent event) {
        log.info("Publishing transfer initiated event to exchange={}, routingKey={}, transferId={}",
                transferExchange, transferInitiatedRoutingKey, event.transferId());

        rabbitTemplate.convertAndSend(transferExchange, transferInitiatedRoutingKey, event);
    }
}