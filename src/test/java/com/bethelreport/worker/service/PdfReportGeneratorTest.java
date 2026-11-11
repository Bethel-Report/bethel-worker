package com.bethelreport.worker.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.bethelreport.worker.dto.ReportData;
import com.bethelreport.worker.dto.ReportData.ActivityInfo;
import com.bethelreport.worker.dto.ReportData.BethelInfo;
import com.bethelreport.worker.dto.ReportData.FormAnswer;
import com.bethelreport.worker.dto.ReportData.MemberInfo;
import com.bethelreport.worker.dto.ReportData.OfficerInfo;
import com.bethelreport.worker.dto.ReportData.PeriodInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
class PdfReportGeneratorTest {

	private final PdfReportGenerator generator = new PdfReportGenerator();

	@Test
	void shouldGeneratePdfWithAllSections() {
		ReportData data = createTestData();

		byte[] result = generator.generate(data);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
		assertThat(new String(result, 0, 5, StandardCharsets.US_ASCII)).startsWith("%PDF");
	}

	@Test
	void shouldGeneratePdfWithEmptyLists() {
		ReportData data = new ReportData(new BethelInfo(UUID.randomUUID(), 1, "Bethel", "Cidade", "UF"),
				new PeriodInfo(UUID.randomUUID(), 2024, "Período", LocalDate.now(), LocalDate.now()), List.of(),
				List.of(), List.of(), List.of());

		byte[] result = generator.generate(data);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	private ReportData createTestData() {
		return new ReportData(new BethelInfo(UUID.randomUUID(), 1, "Bethel Teste", "São Paulo", "SP"),
				new PeriodInfo(UUID.randomUUID(), 2024, "Período 2024", LocalDate.of(2024, 1, 1),
						LocalDate.of(2024, 12, 31)),
				List.of(new FormAnswer("Seção 1", "Campo 1", "Valor 1", "TEXT", false),
						new FormAnswer("Seção 1", "Campo 2", "100", "NUMBER", true)),
				List.of(new MemberInfo(UUID.randomUUID(), "Maria", LocalDate.of(2000, 1, 1), LocalDate.of(2020, 6, 15),
						"ATIVA")),
				List.of(new ActivityInfo(UUID.randomUUID(), "Reunião", "Reunião mensal", LocalDate.of(2024, 3, 15))),
				List.of(new OfficerInfo(UUID.randomUUID(), "Ana", "Honored Queen", LocalDate.of(2024, 1, 1),
						LocalDate.of(2024, 12, 31))));
	}
}
