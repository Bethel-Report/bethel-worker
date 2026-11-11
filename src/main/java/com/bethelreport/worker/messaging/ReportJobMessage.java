package com.bethelreport.worker.messaging;

import java.util.UUID;

public record ReportJobMessage(UUID jobId) {
}
