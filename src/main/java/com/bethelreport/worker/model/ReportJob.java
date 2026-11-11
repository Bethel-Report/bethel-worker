package com.bethelreport.worker.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "report_job")
@Getter
@Setter
@NoArgsConstructor
public class ReportJob {

	@Id
	@Column(name = "job_id")
	private UUID jobId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private ReportType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "format")
	private ReportFormat format;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "parameters", columnDefinition = "jsonb")
	private Map<String, Object> parameters;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ReportJobStatus status;

	@Column(name = "file_key")
	private String fileKey;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "requested_at")
	private Instant requestedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "supreme_email")
	private String supremeEmail;

	@Column(name = "sent_to_supreme_at")
	private Instant sentToSupremeAt;

	@Column(name = "period_id")
	private UUID periodId;

	@Column(name = "user_id")
	private UUID userId;
}
