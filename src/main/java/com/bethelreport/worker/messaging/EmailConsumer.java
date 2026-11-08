package com.bethelreport.worker.messaging;

import com.bethelreport.worker.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailConsumer {

	private final EmailService emailService;

	public EmailConsumer(EmailService emailService) {
		this.emailService = emailService;
	}

	@RabbitListener(queues = "email.queue")
	public void consume(EmailMessage message) {
		log.info("Mensagem recebida para: {}", message.recipientEmail());
		emailService.send(message);
		log.info("E-mail enviado com sucesso para: {}", message.recipientEmail());
	}
}
