package com.bethelreport.worker.messaging;

import java.util.Map;

public record EmailMessage(String recipientEmail, String subject, String template, Map<String, Object> variables) {
}
