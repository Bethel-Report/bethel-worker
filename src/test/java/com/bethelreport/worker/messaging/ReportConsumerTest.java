package com.bethelreport.worker.messaging;

import java.util.UUID;

import com.bethelreport.worker.service.ReportGenerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportConsumerTest {

	@Mock
	private ReportGenerationService reportGenerationService;

	@InjectMocks
	private ReportConsumer reportConsumer;

	@Test
	void shouldProcessReportJobSuccessfully() {
		UUID jobId = UUID.randomUUID();
		ReportJobMessage message = new ReportJobMessage(jobId);

		reportConsumer.consume(message);

		verify(reportGenerationService).process(jobId);
	}

	@Test
	void shouldPropagateExceptionOnFailure() {
		UUID jobId = UUID.randomUUID();
		ReportJobMessage message = new ReportJobMessage(jobId);
		doThrow(new IllegalArgumentException("Job não encontrado")).when(reportGenerationService).process(any());

		assertThatThrownBy(() -> reportConsumer.consume(message)).isInstanceOf(IllegalArgumentException.class);
	}
}
