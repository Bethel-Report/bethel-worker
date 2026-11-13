package com.bethelreport.worker.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bethelreport.worker.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

  @Mock private EmailService emailService;

  @InjectMocks private EmailConsumer emailConsumer;

  @Test
  void acceptsKeycloakRecipientAlias() {
    EmailMessage message =
        readMessage(
            "{\"recipient\":\"user@example.com\",\"subject\":\"Subject\","
                + "\"textBody\":\"Plain\",\"htmlBody\":\"<b>Rendered</b>\"}");

    assertThat(message.recipientEmail()).isEqualTo("user@example.com");
  }

  @Test
  void forwardsKeycloakMessageBodiesToEmailService() {
    EmailMessage message =
        readMessage(
            "{\"recipient\":\"user@example.com\",\"subject\":\"Subject\","
                + "\"textBody\":\"Plain\",\"htmlBody\":\"<b>Rendered</b>\"}");

    emailConsumer.consume(message);

    verify(emailService).send(message);
  }

  @Test
  void acceptsBackendRecipientEmailContract() {
    EmailMessage message =
        readMessage(
            "{\"recipientEmail\":\"user@example.com\",\"subject\":\"Subject\","
                + "\"textBody\":\"Plain\",\"htmlBody\":null}");

    assertThat(message.recipientEmail()).isEqualTo("user@example.com");
  }

  @Test
  void doesNotCallEmailServiceWhenMessageValidationFailsDuringDeserialization() {
    assertThatThrownBy(
            () ->
                readMessage(
                    "{\"recipient\":\"user@example.com\",\"subject\":\"Subject\",\"textBody\":\" \",\"htmlBody\":null}"))
        .hasRootCauseInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(emailService);
  }

  @Test
  void rejectsMissingRecipientDuringDeserialization() {
    assertThatThrownBy(() -> readMessage("{\"subject\":\"Subject\",\"textBody\":\"Plain\"}"))
        .hasRootCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsMissingBodiesDuringDeserialization() {
    assertThatThrownBy(
            () -> readMessage("{\"recipientEmail\":\"user@example.com\",\"subject\":\"Subject\"}"))
        .hasRootCauseInstanceOf(IllegalArgumentException.class);
  }

  private EmailMessage readMessage(String json) {
    try {
      return new ObjectMapper().readValue(json, EmailMessage.class);
    } catch (JacksonException exception) {
      throw new AssertionError("Test message JSON should be valid", exception);
    }
  }

  @Test
  void propagatesExceptionSoListenerCanRetryAndRejectAfterExhaustion() {
    EmailMessage message = new EmailMessage("user@example.com", "Subject", "Plain", null);
    doThrow(new MailSendException("SMTP error")).when(emailService).send(any());

    assertThatThrownBy(() -> emailConsumer.consume(message)).isInstanceOf(MailSendException.class);
  }
}
