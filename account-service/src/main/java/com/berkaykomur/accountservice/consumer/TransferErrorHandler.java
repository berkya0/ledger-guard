package com.berkaykomur.accountservice.consumer;

import com.berkaykomur.accountservice.dto.TransferRequest;
import com.berkaykomur.accountservice.dto.TransferResultEvent;
import com.berkaykomur.accountservice.exception.BusinessException;
import com.berkaykomur.accountservice.producer.TransferEventProducer;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.stereotype.Component;

@Component("transferErrorHandler")
@RequiredArgsConstructor
@Slf4j
public class TransferErrorHandler implements RabbitListenerErrorHandler {

    private final TransferEventProducer transferEventProducer;

    @Override
    public Object handleError(Message amqpMessage, Channel channel, org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) {
        Throwable cause = exception.getCause();

        if (message != null && message.getPayload() instanceof TransferRequest request) {
            if (cause instanceof BusinessException) {
                log.warn("Business validation failed for transferId={}: {}", request.transferId(), cause.getMessage());
                transferEventProducer.publishTransferResult(
                        TransferResultEvent.failed(request.transferId(), cause.getMessage())
                );
                throw new AmqpRejectAndDontRequeueException(cause.getMessage(), cause);
            }
        }

        log.error("Unexpected system error while processing transfer message", cause);
        throw exception;
    }
}