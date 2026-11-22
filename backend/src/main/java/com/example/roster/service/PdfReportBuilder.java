package com.example.roster.service;

import com.example.roster.web.Dtos.EmployeeResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Builds the PDF for UC-RPT-03 with OpenPDF: title, date, filters, repeating table header, page footer. */
@Component
public class PdfReportBuilder {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final Color INK = new Color(0x10, 0x24, 0x3e);
    private static final Color STRIPE = new Color(0xf3, 0xf5, 0xf8);
    private static final String[] HEADERS = {"Name", "Email", "Department", "Job title", "Hired", "Status"};
    private static final float[] WIDTHS = {3f, 4.2f, 2.2f, 3f, 2f, 1.6f};

    public byte[] build(ReportService.Export data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Landscape A4 so the six columns stay readable.
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 48);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new Footer());
            doc.open();

            Font title = new Font(Font.HELVETICA, 20, Font.BOLD, INK);
            Font meta = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            doc.add(new Paragraph("Employee report", title));
            doc.add(new Paragraph("Hire date: " + DATE.format(data.from()) + " to " + DATE.format(data.to())
                    + "   |   Status: " + data.statusLabel(), meta));
            Paragraph generated = new Paragraph("Generated: " + DATE.format(LocalDate.now())
                    + "   |   Records: " + data.rows().size()
                    + (data.truncated() ? " (first " + data.rows().size() + " of " + data.totalMatching() + ")" : ""), meta);
            generated.setSpacingAfter(12);
            doc.add(generated);

            PdfPTable table = new PdfPTable(WIDTHS);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);   // header row repeats on every page
            Font head = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            for (String h : HEADERS) {
                PdfPCell cell = new PdfPCell(new Phrase(h, head));
                cell.setBackgroundColor(INK);
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font body = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
            int i = 0;
            for (EmployeeResponse e : data.rows()) {
                Color bg = (i++ % 2 == 1) ? STRIPE : Color.WHITE;
                add(table, e.firstName() + " " + e.lastName(), body, bg);
                add(table, e.email(), body, bg);
                add(table, e.department(), body, bg);
                add(table, e.jobTitle(), body, bg);
                add(table, DATE.format(e.hireDate()), body, bg);
                add(table, e.status().name(), body, bg);
            }
            if (data.rows().isEmpty()) {
                PdfPCell none = new PdfPCell(new Phrase("No employees match these filters.", body));
                none.setColspan(HEADERS.length);
                none.setPadding(12);
                none.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(none);
            }
            doc.add(table);
        } catch (DocumentException ex) {
            throw new IllegalStateException("Could not build the PDF report", ex);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private static void add(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorderColor(new Color(0xd9, 0xde, 0xe6));
        table.addCell(cell);
    }

    /** Company footer with page number on every page. */
    private static final class Footer extends PdfPageEventHelper {
        private final Font small = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase text = new Phrase("Roster - Employee report - Page " + writer.getPageNumber(), small);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, text,
                    (document.left() + document.right()) / 2, document.bottom() - 20, 0);
        }
    }
}
