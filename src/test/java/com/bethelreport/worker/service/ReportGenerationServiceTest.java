package com.bethelreport.worker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.bethelreport.worker.dto.ReportData;
import com.bethelreport.worker.dto.ReportData.BethelInfo;
import com.bethelreport.worker.dto.ReportData.PeriodInfo;
import com.bethelreport.worker.model.ReportFormat;
import com.bethelreport.worker.model.ReportJob;
import com.bethelreport.worker.model.ReportJobStatus;
import com.bethelreport.worker.model.ReportType;
import com.bethelreport.worker.repository.ReportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
class ReportGenerationServiceTest {

	@Mock
	private ReportJobRepository reportJobRepository;

	@Mock
	private ReportDataService reportDataService;

	@Mock
	private PdfReportGenerator pdfReportGenerator;

	@Mock
	private ExcelReportGenerator excelReportGenerator;

	@Mock
	private StorageService storageService;

	@InjectMocks
	private ReportGenerationService reportGenerationService;

	private UUID jobId;
	private UUID periodId;
	private UUID bethelId;
	private ReportJob job;
	private ReportData reportData;

	@BeforeEach
	void setUp() {
		jobId = UUID.randomUUID();
		periodId = UUID.randomUUID();
		bethelId = UUID.randomUUID();

		job = new ReportJob();
		job.setJobId(jobId);
		job.setType(ReportType.BETHEL_REPORT);
		job.setFormat(ReportFormat.PDF);
		job.setStatus(ReportJobStatus.PENDENTE);
		job.setPeriodId(periodId);
		job.setParameters(Map.of("bethelId", bethelId.toString()));

		reportData = new ReportData(new BethelInfo(bethelId, 1, "Bethel Teste", "São Paulo", "SP"),
				new PeriodInfo(periodId, 2024, "Período 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
				List.of(), List.of(), List.of(), List.of());
	}

	@Test
	void shouldProcessPdfReportSuccessfully() {
		when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
		when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(i -> i.getArgument(0));
		when(reportDataService.consolidate(periodId, bethelId)).thenReturn(reportData);
		when(pdfReportGenerator.generate(reportData)).thenReturn(new byte[]{1, 2, 3});

		reportGenerationService.process(jobId);

		verify(storageService).upload(eq("reports/" + jobId + "/relatorio.pdf"), any(byte[].class),
				eq("application/pdf"));
		assertThat(job.getStatus()).isEqualTo(ReportJobStatus.CONCLUIDO);
		assertThat(job.getFileKey()).isEqualTo("reports/" + jobId + "/relatorio.pdf");
		assertThat(job.getCompletedAt()).isNotNull();
	}

	@Test
	void shouldProcessExcelReportSuccessfully() {
		job.setFormat(ReportFormat.XLSX);
		when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
		when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(i -> i.getArgument(0));
		when(reportDataService.consolidate(periodId, bethelId)).thenReturn(reportData);
		when(excelReportGenerator.generate(reportData)).thenReturn(new byte[]{1, 2, 3});

		reportGenerationService.process(jobId);

		verify(storageService).upload(eq("reports/" + jobId + "/relatorio.xlsx"), any(byte[].class),
				eq("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
		assertThat(job.getStatus()).isEqualTo(ReportJobStatus.CONCLUIDO);
	}

	@Test
	void shouldSetErrorStatusOnFailure() {
		when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
		when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(i -> i.getArgument(0));
		when(reportDataService.consolidate(any(), any())).thenThrow(new RuntimeException("Erro de consulta"));

		reportGenerationService.process(jobId);

		assertThat(job.getStatus()).isEqualTo(ReportJobStatus.ERRO);
		assertThat(job.getErrorMessage()).isEqualTo("Erro de consulta");
		assertThat(job.getCompletedAt()).isNotNull();
		verify(storageService, never()).upload(anyString(), any(byte[].class), anyString());
	}

	@Test
	void shouldThrowWhenJobNotFound() {
		when(reportJobRepository.findById(jobId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reportGenerationService.process(jobId)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldSkipAlreadyProcessedJob() {
		job.setStatus(ReportJobStatus.CONCLUIDO);
		when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));

		reportGenerationService.process(jobId);

		verify(reportDataService, never()).consolidate(any(), any());
		verify(storageService, never()).upload(anyString(), any(byte[].class), anyString());
	}

	@Test
	void shouldSetErrorWhenBethelIdIsMissing() {
		job.setParameters(Map.of());
		when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
		when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(i -> i.getArgument(0));

		reportGenerationService.process(jobId);

		assertThat(job.getStatus()).isEqualTo(ReportJobStatus.ERRO);
		assertThat(job.getErrorMessage()).contains("bethelId");
	}
}
