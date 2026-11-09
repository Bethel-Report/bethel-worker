package com.bethelreport.worker.messaging;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EmailMessage(
    @JsonAlias("recipient") String recipientEmail,
    String subject,
    String textBody,
    String htmlBody) {
  public EmailMessage {
    if (recipientEmail == null || recipientEmail.isBlank()) {
      throw new IllegalArgumentException("Email recipient is required");
    }
    if ((textBody == null || textBody.isBlank()) && (htmlBody == null || htmlBody.isBlank())) {
      throw new IllegalArgumentException("Email body is required");
    }
  }
}
