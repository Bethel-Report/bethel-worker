package com.bethelreport.worker.service;

import java.awt.Color;
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
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.LooseCoupling", "PMD.AvoidCatchingGenericException",
		"PMD.CloseResource", "PMD.AvoidUncheckedExceptionsInSignatures"})
public class PdfReportGenerator {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Color BRAND_COLOR = new Color(79, 70, 229);
	private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
	private static final Font HEADER_FONT = new Font(Font.HELVETICA, 14, Font.BOLD);
	private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 11, Font.NORMAL);
	private static final Font TABLE_HEADER_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);

	public byte[] generate(ReportData data) {
		log.info("Gerando relatório PDF para bethel: {}", data.bethel().name());
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Document document = new Document(PageSize.A4);
			PdfWriter.getInstance(document, out);
			document.open();

			addTitle(document, data);
			addPeriodSection(document, data.period());
			addFormAnswers(document, data.answers());
			addMembersTable(document, data.members());
			addActivitiesTable(document, data.activities());
			addOfficersTable(document, data.officers());

			document.close();
			return out.toByteArray();
		} catch (Exception ex) {
			throw new IllegalStateException("Falha ao gerar PDF", ex);
		}
	}

	private void addTitle(Document document, ReportData data) throws DocumentException {
		BethelInfo bethel = data.bethel();
		Paragraph title = new Paragraph("Relatório - Bethel Nº " + bethel.number() + " " + bethel.name(), TITLE_FONT);
		title.setAlignment(Element.ALIGN_CENTER);
		title.setSpacingAfter(10);
		document.add(title);

		Paragraph location = new Paragraph(bethel.city() + " - " + bethel.state(), NORMAL_FONT);
		location.setAlignment(Element.ALIGN_CENTER);
		location.setSpacingAfter(20);
		document.add(location);
	}

	private void addPeriodSection(Document document, PeriodInfo period) throws DocumentException {
		Paragraph header = new Paragraph("Período: " + period.description(), HEADER_FONT);
		header.setSpacingAfter(5);
		document.add(header);

		String dates = DATE_FMT.format(period.openingDate()) + " a " + DATE_FMT.format(period.closingDate());
		Paragraph datesParagraph = new Paragraph(dates, NORMAL_FONT);
		datesParagraph.setSpacingAfter(15);
		document.add(datesParagraph);
	}

	private void addFormAnswers(Document document, List<FormAnswer> answers) throws DocumentException {
		if (answers.isEmpty()) {
			return;
		}

		Paragraph header = new Paragraph("Dados do Relatório", HEADER_FONT);
		header.setSpacingAfter(10);
		document.add(header);

		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		table.setWidths(new float[]{40, 60});
		addTableHeader(table, "Campo", "Valor");

		String currentSection = "";
		for (FormAnswer answer : answers) {
			String sectionName = answer.section() != null ? answer.section() : "";
			if (!sectionName.equals(currentSection)) {
				currentSection = sectionName;
				PdfPCell sectionCell = new PdfPCell(new Phrase(currentSection, HEADER_FONT));
				sectionCell.setColspan(2);
				sectionCell.setBackgroundColor(new Color(240, 240, 240));
				sectionCell.setPadding(5);
				table.addCell(sectionCell);
			}
			table.addCell(new Phrase(answer.label(), NORMAL_FONT));
			table.addCell(new Phrase(answer.value() != null ? answer.value() : "", NORMAL_FONT));
		}
		document.add(table);
		document.add(Chunk.NEWLINE);
	}

	private void addMembersTable(Document document, List<MemberInfo> members) throws DocumentException {
		if (members.isEmpty()) {
			return;
		}

		Paragraph header = new Paragraph("Membros", HEADER_FONT);
		header.setSpacingAfter(10);
		document.add(header);

		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(100);
		table.setWidths(new float[]{35, 20, 25, 20});
		addTableHeader(table, "Nome", "Nascimento", "Iniciação", "Status");

		for (MemberInfo member : members) {
			table.addCell(new Phrase(member.name(), NORMAL_FONT));
			table.addCell(
					new Phrase(member.birthDate() != null ? DATE_FMT.format(member.birthDate()) : "", NORMAL_FONT));
			table.addCell(new Phrase(member.initiationDate() != null ? DATE_FMT.format(member.initiationDate()) : "",
					NORMAL_FONT));
			table.addCell(new Phrase(member.status(), NORMAL_FONT));
		}
		document.add(table);
		document.add(Chunk.NEWLINE);
	}

	private void addActivitiesTable(Document document, List<ActivityInfo> activities) throws DocumentException {
		if (activities.isEmpty()) {
			return;
		}

		Paragraph header = new Paragraph("Atividades", HEADER_FONT);
		header.setSpacingAfter(10);
		document.add(header);

		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(100);
		table.setWidths(new float[]{20, 40, 40});
		addTableHeader(table, "Data", "Título", "Descrição");

		for (ActivityInfo activity : activities) {
			table.addCell(new Phrase(DATE_FMT.format(activity.activityDate()), NORMAL_FONT));
			table.addCell(new Phrase(activity.title(), NORMAL_FONT));
			table.addCell(new Phrase(activity.description() != null ? activity.description() : "", NORMAL_FONT));
		}
		document.add(table);
		document.add(Chunk.NEWLINE);
	}

	private void addOfficersTable(Document document, List<OfficerInfo> officers) throws DocumentException {
		if (officers.isEmpty()) {
			return;
		}

		Paragraph header = new Paragraph("Oficiais", HEADER_FONT);
		header.setSpacingAfter(10);
		document.add(header);

		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(100);
		table.setWidths(new float[]{30, 30, 20, 20});
		addTableHeader(table, "Nome", "Cargo", "Início", "Fim");

		for (OfficerInfo officer : officers) {
			table.addCell(new Phrase(officer.memberName(), NORMAL_FONT));
			table.addCell(new Phrase(officer.position(), NORMAL_FONT));
			table.addCell(
					new Phrase(officer.startDate() != null ? DATE_FMT.format(officer.startDate()) : "", NORMAL_FONT));
			table.addCell(new Phrase(officer.endDate() != null ? DATE_FMT.format(officer.endDate()) : "", NORMAL_FONT));
		}
		document.add(table);
	}

	private void addTableHeader(PdfPTable table, String... headers) {
		for (String header : headers) {
			PdfPCell cell = new PdfPCell(new Phrase(header, TABLE_HEADER_FONT));
			cell.setBackgroundColor(BRAND_COLOR);
			cell.setPadding(5);
			table.addCell(cell);
		}
	}
}
