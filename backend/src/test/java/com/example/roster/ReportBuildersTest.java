package com.example.roster;

import com.example.roster.domain.EmployeeStatus;
import com.example.roster.service.ExcelReportBuilder;
import com.example.roster.service.PdfReportBuilder;
import com.example.roster.service.ReportService;
import com.example.roster.web.Dtos.EmployeeResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportBuildersTest {

    private static ReportService.Export export(List<EmployeeResponse> rows, boolean truncated) {
        return new ReportService.Export(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "All statuses",
                rows, truncated ? 10_001 : rows.size(), truncated);
    }

    private static final List<EmployeeResponse> TWO = List.of(
            new EmployeeResponse(1L, "Ada", "Lovelace", "ada@x.com", "+1 555 0100", "Engineering", "Engineer",
                    LocalDate.of(2024, 3, 15), EmployeeStatus.ACTIVE),
            new EmployeeResponse(2L, "Alan", "Turing", "alan@x.com", "+1 555 0101", "Research", "Scientist",
                    LocalDate.of(2024, 2, 1), EmployeeStatus.ON_LEAVE));

    @Test
    void excel_hasHeaderRowDataRowsAndRealDateCells() throws Exception {
        byte[] bytes = new ExcelReportBuilder().build(export(TWO, false));

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            // rows 0-2 title and filters, row 3 blank, row 4 header, rows 5.. data
            assertEquals("Employee report", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("First name", sheet.getRow(4).getCell(0).getStringCellValue());
            assertEquals("Status", sheet.getRow(4).getCell(7).getStringCellValue());
            assertEquals(6, sheet.getLastRowNum());                       // two data rows: 5 and 6
            assertEquals("Ada", sheet.getRow(5).getCell(0).getStringCellValue());
            assertEquals("ON_LEAVE", sheet.getRow(6).getCell(7).getStringCellValue());

            Cell hired = sheet.getRow(5).getCell(6);
            assertTrue(DateUtil.isCellDateFormatted(hired), "hire date must be a real Excel date");
            assertEquals(LocalDate.of(2024, 3, 15), hired.getLocalDateTimeCellValue().toLocalDate());
        }
    }

    @Test
    void excel_mentionsTruncationWhenCapped() throws Exception {
        byte[] bytes = new ExcelReportBuilder().build(export(TWO, true));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            String line = wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue();
            assertTrue(line.contains("of 10001"), line);
        }
    }

    @Test
    void excel_withNoRows_stillHasAHeader() throws Exception {
        byte[] bytes = new ExcelReportBuilder().build(export(List.of(), false));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals("First name", wb.getSheetAt(0).getRow(4).getCell(0).getStringCellValue());
        }
    }

    @Test
    void pdf_isAValidPdfFile() {
        byte[] bytes = new PdfReportBuilder().build(export(TWO, false));
        assertTrue(bytes.length > 500);
        assertEquals("%PDF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void pdf_withNoRows_isStillAValidPdf() {
        byte[] bytes = new PdfReportBuilder().build(export(List.of(), false));
        assertEquals("%PDF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
    }
}
