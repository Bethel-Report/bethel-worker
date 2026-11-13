package com.bethelreport.worker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bethelreport.worker.messaging.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  private static final String RECIPIENT = "user@example.com";
  private static final String SUBJECT = "Subject";

  @Mock private JavaMailSender mailSender;

  private EmailService emailService;
  private MimeMessage mimeMessage;

  @BeforeEach
  void setUp() {
    emailService = new EmailService(mailSender, "noreply@bethelreport.com");
    mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
  }

  @Test
  void sendsProvidedTextAndHtmlAsMultipartAlternativeWithoutRendering()
      throws IOException, MessagingException {
    String text = "Use this link: https://example.test/action?token=secret";
    String html = "<html><a href=\"https://example.test/action?token=secret\">Open</a></html>";

    emailService.send(new EmailMessage(RECIPIENT, SUBJECT, text, html));

    mimeMessage.saveChanges();
    assertThat(mimeMessage.getContentType() + flatten(mimeMessage.getContent()))
        .contains("multipart/", text, html);
  }

  @Test
  void delegatesPreparedMessageToSmtpSender() {
    emailService.send(new EmailMessage(RECIPIENT, SUBJECT, "Plain content", null));

    verify(mailSender).send(mimeMessage);
  }

  @Test
  void sendsHtmlOnlyAsHtml() throws IOException, MessagingException {
    emailService.send(new EmailMessage(RECIPIENT, SUBJECT, null, "<b>Ready rendered</b>"));

    mimeMessage.saveChanges();
    assertThat(flatten(mimeMessage.getContent())).contains("<b>Ready rendered</b>");
  }

  @Test
  void sendsPlainTextWhenHtmlIsAbsent() throws IOException, MessagingException {
    emailService.send(new EmailMessage(RECIPIENT, SUBJECT, "Plain content", null));

    mimeMessage.saveChanges();
    assertThat(flatten(mimeMessage.getContent())).contains("Plain content");
  }

  private String flatten(Object content) throws IOException, MessagingException {
    if (content instanceof jakarta.mail.Multipart multipart) {
      StringBuilder result = new StringBuilder();
      for (int index = 0; index < multipart.getCount(); index++) {
        result.append(flatten(multipart.getBodyPart(index).getContent()));
      }
      return result.toString();
    }
    return String.valueOf(content);
  }

  @Test
  void propagatesMailSendException() {
    doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

    assertThatThrownBy(() -> emailService.send(new EmailMessage(RECIPIENT, SUBJECT, "Text", null)))
        .isInstanceOf(MailSendException.class);
  }
}
