package com.bethelreport.worker.integration;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.bethelreport.worker.TestcontainersConfiguration;
import com.bethelreport.worker.messaging.ReportJobMessage;
import com.bethelreport.worker.model.ReportJob;
import com.bethelreport.worker.model.ReportJobStatus;
import com.bethelreport.worker.repository.ReportJobRepository;
import com.bethelreport.worker.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestcontainersConfiguration.class)
@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
class ReportConsumerIntegrationTest {

	@MockitoBean
	private StorageService storageService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ReportJobRepository reportJobRepository;

	@Autowired
	private JdbcClient jdbcClient;

	private UUID jobId;

	@BeforeEach
	void setUp() {
		UUID bethelId = UUID.randomUUID();
		UUID periodId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		jobId = UUID.randomUUID();

		jdbcClient.sql(
				"INSERT INTO bethel (bethel_id, number, name, city, state) VALUES (:id, :num, :name, :city, :state)")
				.param("id", bethelId).param("num", 1).param("name", "Bethel Teste").param("city", "São Paulo")
				.param("state", "SP").update();

		jdbcClient.sql("""
				INSERT INTO report_period (period_id, year, description, opening_date, closing_date, status)
				VALUES (:id, :year, :desc, :open, :close, :status)""").param("id", periodId).param("year", 2024)
				.param("desc", "Período 2024").param("open", LocalDate.of(2024, 1, 1))
				.param("close", LocalDate.of(2024, 12, 31)).param("status", "ABERTO").update();

		jdbcClient.sql("INSERT INTO app_user (user_id, name, email) VALUES (:id, :name, :email)").param("id", userId)
				.param("name", "Usuário Teste").param("email", "teste@example.com").update();

		UUID formId = UUID.randomUUID();
		UUID fieldId = UUID.randomUUID();
		UUID reportId = UUID.randomUUID();

		jdbcClient.sql("INSERT INTO form (form_id, title, description) VALUES (:id, :title, :desc)").param("id", formId)
				.param("title", "Formulário Padrão").param("desc", "Formulário de teste").update();

		jdbcClient.sql("""
				INSERT INTO form_field (field_id, section, label, key, type, financial, sort_order, form_id)
				VALUES (:id, :section, :label, :key, :type, :financial, :sortOrder, :formId)""").param("id", fieldId)
				.param("section", "Dados Gerais").param("label", "Total de membros").param("key", "total_membros")
				.param("type", "NUMBER").param("financial", false).param("sortOrder", 1).param("formId", formId)
				.update();

		jdbcClient.sql("""
				INSERT INTO report (report_id, status, bethel_id, period_id, form_id)
				VALUES (:id, :status, :bethelId, :periodId, :formId)""").param("id", reportId)
				.param("status", "SUBMETIDO").param("bethelId", bethelId).param("periodId", periodId)
				.param("formId", formId).update();

		jdbcClient.sql("INSERT INTO report_answer (report_id, field_id, value) VALUES (:reportId, :fieldId, :value)")
				.param("reportId", reportId).param("fieldId", fieldId).param("value", "42").update();

		String params = "{\"bethelId\": \"" + bethelId + "\"}";
		jdbcClient.sql("""
				INSERT INTO report_job (job_id, type, format, parameters, status, period_id, user_id, requested_at)
				VALUES (:jobId, :type, :format, CAST(:params AS jsonb), :status, :periodId, :userId, :requestedAt)""")
				.param("jobId", jobId).param("type", "BETHEL_REPORT").param("format", "PDF").param("params", params)
				.param("status", "PENDENTE").param("periodId", periodId).param("userId", userId)
				.param("requestedAt", Timestamp.from(Instant.now())).update();
	}

	@Test
	void shouldConsumeAndProcessReportJob() {
		rabbitTemplate.convertAndSend("report.exchange", "report.generate", new ReportJobMessage(jobId));

		await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
			ReportJob job = reportJobRepository.findById(jobId).orElseThrow();
			assertThat(job.getStatus()).isEqualTo(ReportJobStatus.CONCLUIDO);
		});

		ReportJob job = reportJobRepository.findById(jobId).orElseThrow();
		assertThat(job.getFileKey()).isNotNull();
		assertThat(job.getCompletedAt()).isNotNull();
		verify(storageService).upload(anyString(), any(byte[].class), anyString());
	}

	@Test
	void shouldSetErrorStatusOnProcessingFailure() {
		doThrow(new IllegalStateException("MinIO error")).when(storageService).upload(anyString(), any(byte[].class),
				anyString());

		rabbitTemplate.convertAndSend("report.exchange", "report.generate", new ReportJobMessage(jobId));

		await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
			ReportJob job = reportJobRepository.findById(jobId).orElseThrow();
			assertThat(job.getStatus()).isEqualTo(ReportJobStatus.ERRO);
		});

		ReportJob job = reportJobRepository.findById(jobId).orElseThrow();
		assertThat(job.getErrorMessage()).isNotNull();
	}
}
