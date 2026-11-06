package com.bethelreport.worker.messaging;

import java.io.IOException;
import java.util.Map;

import com.bethelreport.worker.service.EmailService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

	@Mock
	private EmailService emailService;

	@Mock
	private Channel channel;

	@InjectMocks
	private EmailConsumer emailConsumer;

	@Test
	void shouldAckOnSuccessfulSend() throws IOException {
		EmailMessage message = new EmailMessage("user@example.com", "Assunto", "invitation",
				Map.of("invitationLink", "https://link.com"));

		emailConsumer.consume(message, channel, 1L);

		verify(channel).basicAck(1L, false);
	}

	@Test
	void shouldNackWithoutRequeueOnFailure() throws IOException {
		EmailMessage message = new EmailMessage("user@example.com", "Assunto", "invitation",
				Map.of("invitationLink", "https://link.com"));
		doThrow(new MailSendException("SMTP error")).when(emailService).send(any());

		emailConsumer.consume(message, channel, 1L);

		verify(channel).basicNack(1L, false, false);
	}

	@Test
	void shouldNotAckOnFailure() throws IOException {
		EmailMessage message = new EmailMessage("user@example.com", "Assunto", "invitation",
				Map.of("invitationLink", "https://link.com"));
		doThrow(new MailSendException("SMTP error")).when(emailService).send(any());

		emailConsumer.consume(message, channel, 1L);

		verify(channel, never()).basicAck(1L, false);
	}
}
