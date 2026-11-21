package com.berkaykomur.accountservice.producer;

import com.berkaykomur.accountservice.dto.TransferResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.transfer}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.transfer-result}")
    private String transferResultRoutingKey;

    public void publishTransferResult(TransferResultEvent event) {
        log.info("Publishing transfer result: transferId={}, success={}, reason={}",
                event.transferId(), event.success(), event.failureReason());
        rabbitTemplate.convertAndSend(exchangeName, transferResultRoutingKey, event);
    }
}