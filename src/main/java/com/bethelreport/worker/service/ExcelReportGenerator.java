package com.bethelreport.worker.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.bethelreport.worker.dto.ReportData;
import com.bethelreport.worker.dto.ReportData.ActivityInfo;
import com.bethelreport.worker.dto.ReportData.BethelInfo;
import com.bethelreport.worker.dto.ReportData.FormAnswer;
import com.bethelreport.worker.dto.ReportData.MemberInfo;
import com.bethelreport.worker.dto.ReportData.OfficerInfo;
import com.bethelreport.worker.dto.ReportData.PeriodInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.AssignmentInOperand"})
public class ExcelReportGenerator {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public byte[] generate(ReportData data) {
		log.info("Gerando relatório Excel para bethel: {}", data.bethel().name());
		try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			CellStyle headerStyle = createHeaderStyle(workbook);

			createInfoSheet(workbook, data, headerStyle);
			createFormAnswersSheet(workbook, data.answers(), headerStyle);
			createMembersSheet(workbook, data.members(), headerStyle);
			createActivitiesSheet(workbook, data.activities(), headerStyle);
			createOfficersSheet(workbook, data.officers(), headerStyle);

			workbook.write(out);
			return out.toByteArray();
		} catch (Exception ex) {
			throw new IllegalStateException("Falha ao gerar Excel", ex);
		}
	}

	private void createInfoSheet(XSSFWorkbook workbook, ReportData data, CellStyle headerStyle) {
		Sheet sheet = workbook.createSheet("Informações");
		BethelInfo bethel = data.bethel();
		PeriodInfo period = data.period();

		int rowNum = 0;
		createRow(sheet, rowNum++, headerStyle, "Bethel Nº " + bethel.number() + " - " + bethel.name());
		createRow(sheet, rowNum++, null, "Cidade: " + bethel.city());
		createRow(sheet, rowNum++, null, "Estado: " + bethel.state());
		rowNum++;
		createRow(sheet, rowNum++, headerStyle, "Período: " + period.description());
		createRow(sheet, rowNum++, null, "Abertura: " + DATE_FMT.format(period.openingDate()));
		createRow(sheet, rowNum, null, "Fechamento: " + DATE_FMT.format(period.closingDate()));

		sheet.autoSizeColumn(0);
	}

	private void createFormAnswersSheet(XSSFWorkbook workbook, List<FormAnswer> answers, CellStyle headerStyle) {
		Sheet sheet = workbook.createSheet("Dados do Relatório");
		int rowNum = 0;

		Row header = sheet.createRow(rowNum++);
		createCell(header, 0, "Seção", headerStyle);
		createCell(header, 1, "Campo", headerStyle);
		createCell(header, 2, "Valor", headerStyle);
		createCell(header, 3, "Tipo", headerStyle);
		createCell(header, 4, "Financeiro", headerStyle);

		for (FormAnswer answer : answers) {
			Row row = sheet.createRow(rowNum++);
			createCell(row, 0, answer.section() != null ? answer.section() : "", null);
			createCell(row, 1, answer.label(), null);
			createCell(row, 2, answer.value() != null ? answer.value() : "", null);
			createCell(row, 3, answer.fieldType(), null);
			createCell(row, 4, answer.financial() ? "Sim" : "Não", null);
		}

		for (int i = 0; i < 5; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void createMembersSheet(XSSFWorkbook workbook, List<MemberInfo> members, CellStyle headerStyle) {
		Sheet sheet = workbook.createSheet("Membros");
		int rowNum = 0;

		Row header = sheet.createRow(rowNum++);
		createCell(header, 0, "Nome", headerStyle);
		createCell(header, 1, "Nascimento", headerStyle);
		createCell(header, 2, "Iniciação", headerStyle);
		createCell(header, 3, "Status", headerStyle);

		for (MemberInfo member : members) {
			Row row = sheet.createRow(rowNum++);
			createCell(row, 0, member.name(), null);
			createCell(row, 1, member.birthDate() != null ? DATE_FMT.format(member.birthDate()) : "", null);
			createCell(row, 2, member.initiationDate() != null ? DATE_FMT.format(member.initiationDate()) : "", null);
			createCell(row, 3, member.status(), null);
		}

		for (int i = 0; i < 4; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void createActivitiesSheet(XSSFWorkbook workbook, List<ActivityInfo> activities, CellStyle headerStyle) {
		Sheet sheet = workbook.createSheet("Atividades");
		int rowNum = 0;

		Row header = sheet.createRow(rowNum++);
		createCell(header, 0, "Data", headerStyle);
		createCell(header, 1, "Título", headerStyle);
		createCell(header, 2, "Descrição", headerStyle);

		for (ActivityInfo activity : activities) {
			Row row = sheet.createRow(rowNum++);
			createCell(row, 0, DATE_FMT.format(activity.activityDate()), null);
			createCell(row, 1, activity.title(), null);
			createCell(row, 2, activity.description() != null ? activity.description() : "", null);
		}

		for (int i = 0; i < 3; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void createOfficersSheet(XSSFWorkbook workbook, List<OfficerInfo> officers, CellStyle headerStyle) {
		Sheet sheet = workbook.createSheet("Oficiais");
		int rowNum = 0;

		Row header = sheet.createRow(rowNum++);
		createCell(header, 0, "Nome", headerStyle);
		createCell(header, 1, "Cargo", headerStyle);
		createCell(header, 2, "Início", headerStyle);
		createCell(header, 3, "Fim", headerStyle);

		for (OfficerInfo officer : officers) {
			Row row = sheet.createRow(rowNum++);
			createCell(row, 0, officer.memberName(), null);
			createCell(row, 1, officer.position(), null);
			createCell(row, 2, officer.startDate() != null ? DATE_FMT.format(officer.startDate()) : "", null);
			createCell(row, 3, officer.endDate() != null ? DATE_FMT.format(officer.endDate()) : "", null);
		}

		for (int i = 0; i < 4; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}

	private void createRow(Sheet sheet, int rowNum, CellStyle style, String value) {
		Row row = sheet.createRow(rowNum);
		createCell(row, 0, value, style);
	}

	private void createCell(Row row, int col, String value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value);
		if (style != null) {
			cell.setCellStyle(style);
		}
	}
}
