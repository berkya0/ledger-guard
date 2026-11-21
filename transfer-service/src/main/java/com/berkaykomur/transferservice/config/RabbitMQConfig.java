package com.berkaykomur.transferservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.transfer}")
    private String transferExchange;

    @Value("${rabbitmq.queue.transfer-result}")
    private String transferResultQueue;

    @Value("${rabbitmq.routing-key.transfer-result}")
    private String transferResultRoutingKey;

    @Bean
    public DirectExchange transferExchange() {
        return new DirectExchange(transferExchange);
    }

    @Bean
    public Queue transferResultQueue() {
        return QueueBuilder.durable(transferResultQueue).build();
    }

    @Bean
    public Binding transferResultBinding(Queue transferResultQueue, DirectExchange transferExchange) {
        return BindingBuilder.bind(transferResultQueue)
                .to(transferExchange)
                .with(transferResultRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}