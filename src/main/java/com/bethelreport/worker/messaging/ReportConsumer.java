package com.bethelreport.worker.messaging;

import com.bethelreport.worker.service.ReportGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportConsumer {

	private final ReportGenerationService reportGenerationService;

	public ReportConsumer(ReportGenerationService reportGenerationService) {
		this.reportGenerationService = reportGenerationService;
	}

	@RabbitListener(queues = "report.queue")
	public void consume(ReportJobMessage message) {
		log.info("Mensagem de geração de relatório recebida para job: {}", message.jobId());
		reportGenerationService.process(message.jobId());
		log.info("Processamento do job {} finalizado", message.jobId());
	}
}
