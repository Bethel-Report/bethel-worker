package com.bethelreport.worker.service;

import java.util.Map;

import com.bethelreport.worker.messaging.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	private static final String TEMPLATE_NAME = "invitation";
	private static final String INVITATION_LINK_KEY = "invitationLink";

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private TemplateEngine templateEngine;

	private EmailService emailService;

	@BeforeEach
	void setUp() {
		emailService = new EmailService(mailSender, templateEngine, "noreply@bethelreport.com");
	}

	@Test
	void shouldProcessTemplateAndSendEmail() {
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class))).thenReturn("<html>test</html>");

		EmailMessage message = new EmailMessage("user@example.com", "Convite", TEMPLATE_NAME,
				Map.of(INVITATION_LINK_KEY, "https://app.bethelreport.com/invite/abc123"));

		emailService.send(message);

		verify(mailSender).send(mimeMessage);
	}

	@Test
	void shouldPropagateMailSendException() {
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class))).thenReturn("<html>test</html>");
		doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

		EmailMessage message = new EmailMessage("user@example.com", "Convite", TEMPLATE_NAME,
				Map.of(INVITATION_LINK_KEY, "https://link.com"));

		assertThatThrownBy(() -> emailService.send(message)).isInstanceOf(MailSendException.class);
	}

	@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
	@Test
	void shouldInjectVariablesIntoTemplateContext() {
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class))).thenReturn("<html></html>");

		Map<String, Object> variables = Map.of(INVITATION_LINK_KEY, "https://app.bethelreport.com/invite/xyz");
		EmailMessage message = new EmailMessage("user@example.com", "Convite", TEMPLATE_NAME, variables);

		emailService.send(message);

		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq(TEMPLATE_NAME), contextCaptor.capture());
		assertThat(contextCaptor.getValue().getVariable(INVITATION_LINK_KEY))
				.isEqualTo("https://app.bethelreport.com/invite/xyz");
	}
}
