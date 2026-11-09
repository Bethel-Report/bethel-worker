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

  @RabbitListener(queues = "${app.messaging.email.queue}")
  public void consume(EmailMessage message) {
    emailService.send(message);
    log.info("Email message sent successfully");
  }
}
