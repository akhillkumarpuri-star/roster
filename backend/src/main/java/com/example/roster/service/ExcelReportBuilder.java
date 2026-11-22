package com.example.roster.service;

import com.example.roster.web.Dtos.EmployeeResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

/** Builds the .xlsx for UC-RPT-02 with Apache POI. */
@Component
public class ExcelReportBuilder {
    private static final String[] HEADERS =
            {"First name", "Last name", "Email", "Phone", "Department", "Job title", "Hire date", "Status"};
    // Explicit widths in characters: autoSizeColumn needs AWT fonts, which a headless server may not have.
    private static final int[] WIDTHS = {14, 14, 34, 18, 16, 24, 13, 12};

    public byte[] build(ReportService.Export data) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Employees");

            Font bold = wb.createFont();
            bold.setBold(true);
            CellStyle titleStyle = wb.createCellStyle();
            Font big = wb.createFont();
            big.setBold(true);
            big.setFontHeightInPoints((short) 14);
            titleStyle.setFont(big);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd-mmm-yyyy"));

            // Rows 0-2: title and the filters used, so the file explains itself.
            int r = 0;
            Cell title = sheet.createRow(r++).createCell(0);
            title.setCellValue("Employee report");
            title.setCellStyle(titleStyle);
            sheet.createRow(r++).createCell(0).setCellValue(
                    "Hire date: " + data.from() + " to " + data.to() + "   |   Status: " + data.statusLabel());
            sheet.createRow(r++).createCell(0).setCellValue(
                    "Generated: " + LocalDate.now() + "   |   Records: " + data.rows().size()
                            + (data.truncated() ? " (first " + data.rows().size() + " of " + data.totalMatching() + ")" : ""));
            r++; // blank spacer row

            int headerRow = r;
            Row header = sheet.createRow(r++);
            for (int c = 0; c < HEADERS.length; c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(HEADERS[c]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(c, WIDTHS[c] * 256);
            }

            List<EmployeeResponse> rows = data.rows();
            for (EmployeeResponse e : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(e.firstName());
                row.createCell(1).setCellValue(e.lastName());
                row.createCell(2).setCellValue(e.email());
                row.createCell(3).setCellValue(e.phone());
                row.createCell(4).setCellValue(e.department());
                row.createCell(5).setCellValue(e.jobTitle());
                Cell hired = row.createCell(6);
                hired.setCellValue(e.hireDate());   // a real date cell, so Excel can sort and filter it
                hired.setCellStyle(dateStyle);
                row.createCell(7).setCellValue(e.status().name());
            }

            sheet.createFreezePane(0, headerRow + 1);   // header stays visible while scrolling
            sheet.setAutoFilter(new CellRangeAddress(headerRow, Math.max(headerRow, r - 1), 0, HEADERS.length - 1));

            wb.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not build the Excel report", ex);
        }
    }
}
