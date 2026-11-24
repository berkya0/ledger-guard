package com.berkaykomur.accountservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.transfer}")
    private String transferExchange;

    @Value("${rabbitmq.queue.transfer-initiated}")
    private String transferInitiatedQueue;

    @Value("${rabbitmq.queue.transfer-dlq}")
    private String transferDlq;

    @Value("${rabbitmq.routing-key.transfer-initiated}")
    private String transferInitiatedRoutingKey;

    @Value("${rabbitmq.routing-key.transfer-dlq}")
    private String transferDlqRoutingKey;

    @Value("${rabbitmq.queue.account-created}")
    private String accountCreatedQueue;

    @Value("${rabbitmq.routing-key.account-created}")
    private String accountCreatedRoutingKey;


    @Bean
    public DirectExchange transferExchange() {
        return new DirectExchange(transferExchange);
        //mesajın üzerindeki etiket ile exchange eşleşirse mesajı ilgili kuyruklara yönlendirir
    }

    @Bean
    public Queue transferInitiatedQueue() {
        return QueueBuilder.durable(transferInitiatedQueue) //durable: kuyruk sunucu yeniden başlatıldığında kaybolmaz. Kalıcı yapar
                .withArgument("x-dead-letter-exchange", transferExchange) //hangi santrale
                .withArgument("x-dead-letter-routing-key", transferDlqRoutingKey) //hangi routing key ile yönlendirilecek
                .build(); //mesaj işlenemezse (hata olursa vb) bu kuyruktan çıkar ve dead letter queue'ya yönlendirilir
    }

    @Bean
    public Queue transferDlq() { //ölü mektup kuyruğu, işlenemeyen mesajlar buraya düşer
        return QueueBuilder.durable(transferDlq).build();
    }

    @Bean
    public Binding transferInitiatedBinding() { //binding: kuyruk ile exchange'i birbirine bağlar (ilk giden mesajlar)
        return BindingBuilder.bind(transferInitiatedQueue())
                .to(transferExchange())
                .with(transferInitiatedRoutingKey);
    }

    @Bean
    public Binding transferDlqBinding() { //binding: kuyruk ile exchange'i birbirine bağlar (ölü mesajlar)
        return BindingBuilder.bind(transferDlq())
                .to(transferExchange())
                .with(transferDlqRoutingKey);
    }

    @Bean
    public Queue accountCreatedQueue() {
        return QueueBuilder.durable(accountCreatedQueue).build();
    }

    @Bean
    public Binding accountCreatedBinding() {
        return BindingBuilder.bind(accountCreatedQueue())
                .to(transferExchange())
                .with(accountCreatedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}