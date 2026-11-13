package com.bethelreport.worker;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  RabbitMQContainer rabbitContainer() {
    return new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.1-management-alpine"));
  }

  @Bean
  DirectExchange dlxExchange() {
    return new DirectExchange("dlx.exchange");
  }

  @Bean
  Queue dlq() {
    return QueueBuilder.durable("email.dlq").build();
  }

  @Bean
  Binding dlqBinding(Queue dlq, DirectExchange dlxExchange) {
    return BindingBuilder.bind(dlq).to(dlxExchange).with("email.dlq");
  }

  @Bean
  DirectExchange emailExchange() {
    return new DirectExchange("email.exchange");
  }

  @Bean
  Queue emailQueue() {
    return QueueBuilder.durable("email.queue")
        .deadLetterExchange("dlx.exchange")
        .deadLetterRoutingKey("email.dlq")
        .build();
  }

  @Bean
  Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(emailQueue).to(emailExchange).with("email.send");
  }
}
