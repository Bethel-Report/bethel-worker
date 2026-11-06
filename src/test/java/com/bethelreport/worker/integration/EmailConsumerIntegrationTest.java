package com.bethelreport.worker.integration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.bethelreport.worker.TestcontainersConfiguration;
import com.bethelreport.worker.messaging.EmailMessage;
import com.bethelreport.worker.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSendException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestcontainersConfiguration.class)
class EmailConsumerIntegrationTest {

	@MockitoBean
	private EmailService emailService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private EmailMessage testMessage;

	@BeforeEach
	void setUp() {
		testMessage = new EmailMessage("user@example.com", "Convite para acessar o Bethel Report", "invitation",
				Map.of("invitationLink", "https://app.bethelreport.com/invite/abc123"));
	}

	@Test
	void shouldConsumeAndDeserializeMessage() {
		rabbitTemplate.convertAndSend("email.exchange", "email.send", testMessage);

		verify(emailService, timeout(10_000)).send(any(EmailMessage.class));
	}

	@Test
	void shouldRouteFailedMessageToDlq() throws InterruptedException {
		doThrow(new MailSendException("SMTP error")).when(emailService).send(any());

		rabbitTemplate.convertAndSend("email.exchange", "email.send", testMessage);

		TimeUnit.SECONDS.sleep(5);

		Message dlqMessage = rabbitTemplate.receive("email.dlq", 5000);
		assertThat(dlqMessage).isNotNull();
	}
}
