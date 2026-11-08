package com.bethelreport.worker.messaging;

import java.util.Map;

import com.bethelreport.worker.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

	@Mock
	private EmailService emailService;

	@InjectMocks
	private EmailConsumer emailConsumer;

	@Test
	void shouldSendEmailSuccessfully() {
		EmailMessage message = new EmailMessage("user@example.com", "Assunto", "invitation",
				Map.of("invitationLink", "https://link.com"));

		emailConsumer.consume(message);

		verify(emailService).send(message);
	}

	@Test
	void shouldPropagateExceptionOnFailure() {
		EmailMessage message = new EmailMessage("user@example.com", "Assunto", "invitation",
				Map.of("invitationLink", "https://link.com"));
		doThrow(new MailSendException("SMTP error")).when(emailService).send(any());

		assertThatThrownBy(() -> emailConsumer.consume(message)).isInstanceOf(MailSendException.class);
	}
}
