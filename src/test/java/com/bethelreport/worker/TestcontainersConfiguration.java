package com.bethelreport.worker;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
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
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
	}

	// Email queue topology

	@Bean
	DirectExchange dlxExchange() {
		return new DirectExchange("dlx.exchange");
	}

	@Bean
	Queue emailDlq() {
		return QueueBuilder.durable("email.dlq").build();
	}

	@Bean
	Binding emailDlqBinding(Queue emailDlq, DirectExchange dlxExchange) {
		return BindingBuilder.bind(emailDlq).to(dlxExchange).with("email.send");
	}

	@Bean
	DirectExchange emailExchange() {
		return new DirectExchange("email.exchange");
	}

	@Bean
	Queue emailQueue() {
		return QueueBuilder.durable("email.queue").deadLetterExchange("dlx.exchange").deadLetterRoutingKey("email.send")
				.build();
	}

	@Bean
	Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
		return BindingBuilder.bind(emailQueue).to(emailExchange).with("email.send");
	}

	// Report queue topology

	@Bean
	DirectExchange reportExchange() {
		return new DirectExchange("report.exchange");
	}

	@Bean
	Queue reportQueue() {
		return QueueBuilder.durable("report.queue").deadLetterExchange("dlx.exchange")
				.deadLetterRoutingKey("report.generate").build();
	}

	@Bean
	Queue reportDlq() {
		return QueueBuilder.durable("report.dlq").build();
	}

	@Bean
	Binding reportBinding(Queue reportQueue, DirectExchange reportExchange) {
		return BindingBuilder.bind(reportQueue).to(reportExchange).with("report.generate");
	}

	@Bean
	Binding reportDlqBinding(Queue reportDlq, DirectExchange dlxExchange) {
		return BindingBuilder.bind(reportDlq).to(dlxExchange).with("report.generate");
	}
}
