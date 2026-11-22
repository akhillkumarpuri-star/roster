package com.example.roster;

import com.example.roster.config.SecurityConfig;
import com.example.roster.repo.UserRepository;
import com.example.roster.security.JwtService;
import com.example.roster.service.EmployeeService;
import com.example.roster.service.ExcelReportBuilder;
import com.example.roster.service.PdfReportBuilder;
import com.example.roster.service.ReportService;
import com.example.roster.web.ApiExceptions;
import com.example.roster.web.Dtos.ReportSummary;
import com.example.roster.web.EmployeeController;
import com.example.roster.web.ReportController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HTTP-level tests with the real security filter chain and real JWTs; only the services are mocked.
 * Covers 400, 401, 403, 404 and 409. (MockMvc has no container error dispatch, so the /error rule in
 * SecurityConfig is verified by the manual PowerShell test, not here.)
 */
@WebMvcTest(controllers = {EmployeeController.class, ReportController.class})
@Import({SecurityConfig.class, JwtService.class})
class ApiSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @Value("${app.jwt.secret}") String secret;

    @MockBean EmployeeService employees;
    @MockBean ReportService reports;
    @MockBean ExcelReportBuilder excel;
    @MockBean PdfReportBuilder pdf;
    @MockBean UserRepository users;   // needed by SecurityConfig's UserDetailsService bean

    private String admin() { return "Bearer " + jwt.generate("admin", "ADMIN"); }
    private String viewer() { return "Bearer " + jwt.generate("viewer", "USER"); }

    private static final String VALID_BODY = """
            {"firstName":"Ada","lastName":"Lovelace","email":"ada@x.com","phone":"+1 555 0100",
             "department":"Engineering","jobTitle":"Engineer","hireDate":"2024-01-15","status":"ACTIVE"}""";

    @Test
    void noToken_is401() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_is401() throws Exception {
        String expired = new JwtService(secret, -1).generate("admin", "ADMIN");
        mvc.perform(get("/api/employees").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCanRead() throws Exception {
        mvc.perform(get("/api/employees").header("Authorization", viewer())).andExpect(status().isOk());
    }

    @Test
    void viewerDelete_is403() throws Exception {
        mvc.perform(delete("/api/employees/1").header("Authorization", viewer()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDelete_is204() throws Exception {
        mvc.perform(delete("/api/employees/1").header("Authorization", admin()))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidBody_is400_withFieldErrors() throws Exception {
        mvc.perform(post("/api/employees").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void duplicateEmail_is409_withEmailError() throws Exception {
        when(employees.create(any())).thenThrow(new ApiExceptions.Conflict("email", "An employee with this email already exists."));
        mvc.perform(post("/api/employees").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void conflict_reportsTheFieldThatActuallyConflicted() throws Exception {
        when(employees.create(any())).thenThrow(new ApiExceptions.Conflict("phone", "That phone number is already used."));
        mvc.perform(post("/api/employees").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.email").doesNotExist());
    }

    @Test
    void unknownEmployee_is404() throws Exception {
        when(employees.get(99L)).thenThrow(new ApiExceptions.NotFound("Employee 99 was not found."));
        mvc.perform(get("/api/employees/99").header("Authorization", viewer()))
                .andExpect(status().isNotFound());
    }

    // ---- report endpoints ----

    @Test
    void report_withoutToken_is401() throws Exception {
        mvc.perform(get("/api/reports/employees/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCanReadReportSummary() throws Exception {
        when(reports.summary(any(), any(), any())).thenReturn(new ReportSummary(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), 3,
                Map.of("ACTIVE", 3L, "ON_LEAVE", 0L, "INACTIVE", 0L), List.of()));
        mvc.perform(get("/api/reports/employees/summary").header("Authorization", viewer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void report_fromAfterTo_is400_withFromError() throws Exception {
        when(reports.summary(any(), any(), any()))
                .thenThrow(new ApiExceptions.BadRequest("from", "The 'from' date must be on or before the 'to' date."));
        mvc.perform(get("/api/reports/employees/summary?from=2024-05-02&to=2024-05-01").header("Authorization", viewer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.from").exists());
    }

    @Test
    void report_invalidStatusValue_is400() throws Exception {
        mvc.perform(get("/api/reports/employees/summary?status=NOT_A_STATUS").header("Authorization", viewer()))
                .andExpect(status().isBadRequest());
    }
}
