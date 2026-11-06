package com.bethelreport.worker.service;

import java.nio.charset.StandardCharsets;

import com.bethelreport.worker.messaging.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final String mailFrom;

	public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
			@Value("${app.mail.from}") String mailFrom) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.mailFrom = mailFrom;
	}

	public void send(EmailMessage message) {
		Context context = new Context();
		context.setVariables(message.variables());
		String html = templateEngine.process(message.template(), context);

		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
			helper.setFrom(mailFrom);
			helper.setTo(message.recipientEmail());
			helper.setSubject(message.subject());
			helper.setText(html, true);
			mailSender.send(mimeMessage);
		} catch (MessagingException ex) {
			throw new MailPreparationException("Falha ao preparar e-mail para: " + message.recipientEmail(), ex);
		}
	}
}
