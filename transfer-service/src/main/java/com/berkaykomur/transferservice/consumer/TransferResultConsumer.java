package com.berkaykomur.transferservice.consumer;

import com.berkaykomur.transferservice.dto.TransferResultEvent;
import com.berkaykomur.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferResultConsumer {

    private final TransferService transferService;

    @RabbitListener(queues = "${rabbitmq.queue.transfer-result}")
    public void consumeTransferResult(TransferResultEvent event) {
        log.info("Received transfer result event: transferId={}, success={}", event.transferId(), event.success());
        transferService.handleTransferResult(event);
    }
}