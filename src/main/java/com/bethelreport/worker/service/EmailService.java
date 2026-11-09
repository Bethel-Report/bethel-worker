package com.bethelreport.worker.service;

import com.bethelreport.worker.messaging.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;
  private final String mailFrom;

  public EmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String mailFrom) {
    this.mailSender = mailSender;
    this.mailFrom = mailFrom;
  }

  public void send(EmailMessage message) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper =
          new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
      helper.setFrom(mailFrom);
      helper.setTo(message.recipientEmail());
      helper.setSubject(message.subject());
      String text = message.textBody();
      String html = message.htmlBody();
      if (hasContent(text) && hasContent(html)) {
        helper.setText(text, html);
      } else if (hasContent(html)) {
        helper.setText(html, true);
      } else {
        helper.setText(text, false);
      }
      mailSender.send(mimeMessage);
    } catch (MessagingException ex) {
      throw new MailPreparationException("Failed to prepare email message", ex);
    }
  }

  private boolean hasContent(String value) {
    return value != null && !value.isBlank();
  }
}
