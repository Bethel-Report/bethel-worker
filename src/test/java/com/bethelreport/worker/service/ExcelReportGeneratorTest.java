package com.bethelreport.worker.service;

import java.io.ByteArrayInputStream;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
class ExcelReportGeneratorTest {

	private final ExcelReportGenerator generator = new ExcelReportGenerator();

	@Test
	void shouldGenerateExcelWithAllSheets() throws Exception {
		ReportData data = createTestData();

		byte[] result = generator.generate(data);

		assertThat(result).isNotNull();
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
			assertThat(workbook.getNumberOfSheets()).isEqualTo(5);
			assertThat(workbook.getSheetName(0)).isEqualTo("Informações");
			assertThat(workbook.getSheetName(1)).isEqualTo("Dados do Relatório");
			assertThat(workbook.getSheetName(2)).isEqualTo("Membros");
			assertThat(workbook.getSheetName(3)).isEqualTo("Atividades");
			assertThat(workbook.getSheetName(4)).isEqualTo("Oficiais");
		}
	}

	@Test
	void shouldGenerateExcelWithCorrectMemberData() throws Exception {
		ReportData data = createTestData();

		byte[] result = generator.generate(data);

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
			XSSFSheet membersSheet = workbook.getSheet("Membros");
			assertThat(membersSheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Maria");
			assertThat(membersSheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("ATIVA");
		}
	}

	@Test
	void shouldGenerateExcelWithEmptyLists() throws Exception {
		ReportData data = new ReportData(new BethelInfo(UUID.randomUUID(), 1, "Bethel", "Cidade", "UF"),
				new PeriodInfo(UUID.randomUUID(), 2024, "Período", LocalDate.now(), LocalDate.now()), List.of(),
				List.of(), List.of(), List.of());

		byte[] result = generator.generate(data);

		assertThat(result).isNotNull();
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
			assertThat(workbook.getNumberOfSheets()).isEqualTo(5);
		}
	}

	private ReportData createTestData() {
		return new ReportData(new BethelInfo(UUID.randomUUID(), 1, "Bethel Teste", "São Paulo", "SP"),
				new PeriodInfo(UUID.randomUUID(), 2024, "Período 2024", LocalDate.of(2024, 1, 1),
						LocalDate.of(2024, 12, 31)),
				List.of(new FormAnswer("Seção 1", "Campo 1", "Valor 1", "TEXT", false)),
				List.of(new MemberInfo(UUID.randomUUID(), "Maria", LocalDate.of(2000, 1, 1), LocalDate.of(2020, 6, 15),
						"ATIVA")),
				List.of(new ActivityInfo(UUID.randomUUID(), "Reunião", "Reunião mensal", LocalDate.of(2024, 3, 15))),
				List.of(new OfficerInfo(UUID.randomUUID(), "Ana", "Honored Queen", LocalDate.of(2024, 1, 1),
						LocalDate.of(2024, 12, 31))));
	}
}
