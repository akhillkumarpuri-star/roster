package com.example.roster.web;

import com.example.roster.domain.EmployeeStatus;
import com.example.roster.service.ExcelReportBuilder;
import com.example.roster.service.PdfReportBuilder;
import com.example.roster.service.ReportService;
import com.example.roster.web.Dtos.EmployeeResponse;
import com.example.roster.web.Dtos.PageResponse;
import com.example.roster.web.Dtos.ReportSummary;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/employees")
public class ReportController {
    private final ReportService service;
    private final ExcelReportBuilder excel;
    private final PdfReportBuilder pdf;

    public ReportController(ReportService service, ExcelReportBuilder excel, PdfReportBuilder pdf) {
        this.service = service;
        this.excel = excel;
        this.pdf = pdf;
    }

    // Read-only, so any signed-in user may call these (same rule as GET /api/employees).
    @GetMapping
    public PageResponse<EmployeeResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "hireDate") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        return service.list(from, to, status, page, size, sort, dir);
    }

    @GetMapping("/summary")
    public ReportSummary summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EmployeeStatus status) {
        return service.summary(from, to, status);
    }

    // UC-RPT-02: every row matching the filter, not just the visible page.
    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EmployeeStatus status) {
        ReportService.Export data = service.export(from, to, status);
        String filename = "employee-report-" + data.from() + "_to_" + data.to() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel.build(data));
    }

    // UC-RPT-03: same filter and rows as the Excel export, laid out as a PDF.
    @GetMapping("/export.pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EmployeeStatus status) {
        ReportService.Export data = service.export(from, to, status);
        String filename = "employee-report-" + data.from() + "_to_" + data.to() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.build(data));
    }
}
