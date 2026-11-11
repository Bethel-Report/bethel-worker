package com.bethelreport.worker.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bethelreport.worker.dto.ReportData;
import com.bethelreport.worker.model.ReportFormat;
import com.bethelreport.worker.model.ReportJob;
import com.bethelreport.worker.model.ReportJobStatus;
import com.bethelreport.worker.repository.ReportJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class ReportGenerationService {

	private final ReportJobRepository reportJobRepository;
	private final ReportDataService reportDataService;
	private final PdfReportGenerator pdfReportGenerator;
	private final ExcelReportGenerator excelReportGenerator;
	private final StorageService storageService;

	public ReportGenerationService(ReportJobRepository reportJobRepository, ReportDataService reportDataService,
			PdfReportGenerator pdfReportGenerator, ExcelReportGenerator excelReportGenerator,
			StorageService storageService) {
		this.reportJobRepository = reportJobRepository;
		this.reportDataService = reportDataService;
		this.pdfReportGenerator = pdfReportGenerator;
		this.excelReportGenerator = excelReportGenerator;
		this.storageService = storageService;
	}

	public void process(UUID jobId) {
		ReportJob job = reportJobRepository.findById(jobId)
				.orElseThrow(() -> new IllegalArgumentException("ReportJob não encontrado: " + jobId));

		if (job.getStatus() != ReportJobStatus.PENDENTE) {
			log.warn("Job {} já processado ou em processamento. Status: {}", jobId, job.getStatus());
			return;
		}

		log.info("Processando job {} - tipo: {}, formato: {}", jobId, job.getType(), job.getFormat());

		Map<String, Object> params = job.getParameters() != null ? job.getParameters() : Map.of();
		Object rawBethelId = params.get("bethelId");
		if (rawBethelId == null) {
			markAsError(job, "Parâmetro 'bethelId' é obrigatório para o job: " + jobId);
			return;
		}

		updateStatus(job, ReportJobStatus.PROCESSANDO);

		try {
			UUID bethelId = UUID.fromString(String.valueOf(rawBethelId));
			UUID periodId = job.getPeriodId();

			ReportData data = reportDataService.consolidate(periodId, bethelId);

			byte[] content;
			String extension;
			String contentType;

			if (job.getFormat() == ReportFormat.PDF) {
				content = pdfReportGenerator.generate(data);
				extension = "pdf";
				contentType = "application/pdf";
			} else {
				content = excelReportGenerator.generate(data);
				extension = "xlsx";
				contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			}

			String fileKey = "reports/" + jobId + "/relatorio." + extension;
			storageService.upload(fileKey, content, contentType);

			job.setFileKey(fileKey);
			job.setCompletedAt(Instant.now());
			updateStatus(job, ReportJobStatus.CONCLUIDO);

			log.info("Job {} concluído com sucesso. Arquivo: {}", jobId, fileKey);
		} catch (Exception ex) {
			log.error("Erro ao processar job {}: {}", jobId, ex.getMessage(), ex);
			job.setErrorMessage(ex.getMessage());
			job.setCompletedAt(Instant.now());
			updateStatus(job, ReportJobStatus.ERRO);
		}
	}

	private void updateStatus(ReportJob job, ReportJobStatus status) {
		job.setStatus(status);
		reportJobRepository.save(job);
	}

	private void markAsError(ReportJob job, String message) {
		log.error("Erro de validação no job {}: {}", job.getJobId(), message);
		job.setErrorMessage(message);
		job.setCompletedAt(Instant.now());
		updateStatus(job, ReportJobStatus.ERRO);
	}
}
