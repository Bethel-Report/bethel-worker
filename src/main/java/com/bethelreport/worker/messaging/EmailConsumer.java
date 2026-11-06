package com.bethelreport.worker.messaging;

import java.io.IOException;

import com.bethelreport.worker.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailConsumer {

	private final EmailService emailService;

	public EmailConsumer(EmailService emailService) {
		this.emailService = emailService;
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	@RabbitListener(queues = "email.queue")
	public void consume(EmailMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
			throws IOException {
		log.info("Mensagem recebida para: {}", message.recipientEmail());
		try {
			emailService.send(message);
			channel.basicAck(deliveryTag, false);
			log.info("E-mail enviado com sucesso para: {}", message.recipientEmail());
		} catch (Exception ex) {
			log.error("Falha ao enviar e-mail para: {}", message.recipientEmail(), ex);
			channel.basicNack(deliveryTag, false, false);
		}
	}
}
