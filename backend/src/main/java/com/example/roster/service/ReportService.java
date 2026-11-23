package com.example.roster.service;

import com.example.roster.domain.Employee;
import com.example.roster.domain.EmployeeStatus;
import com.example.roster.repo.EmployeeRepository;
import com.example.roster.web.ApiExceptions;
import com.example.roster.web.Dtos.DepartmentCount;
import com.example.roster.web.Dtos.MonthCount;
import com.example.roster.web.Dtos.EmployeeResponse;
import com.example.roster.web.Dtos.PageResponse;
import com.example.roster.web.Dtos.ReportSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ReportService {
    private final EmployeeRepository repo;

    /** The summary shows at most this many of the most recent months. */
    public static final int MAX_MONTHS = 24;

    public ReportService(EmployeeRepository repo) { this.repo = repo; }

    /** The validated filter shared by the summary, the table and (later) the exports. */
    record Filter(LocalDate from, LocalDate to, Set<EmployeeStatus> statuses) {}

    /** Default range is the current month (UC-RPT-05). A null status means "all statuses". */
    Filter resolve(LocalDate from, LocalDate to, EmployeeStatus status) {
        LocalDate today = LocalDate.now();
        LocalDate start = from != null ? from : today.withDayOfMonth(1);
        LocalDate end = to != null ? to : today;
        if (start.isAfter(end)) {
            throw new ApiExceptions.BadRequest("from", "The 'from' date must be on or before the 'to' date.");
        }
        Set<EmployeeStatus> statuses = status != null ? EnumSet.of(status) : EnumSet.allOf(EmployeeStatus.class);
        return new Filter(start, end, statuses);
    }

    public ReportSummary summary(LocalDate from, LocalDate to, EmployeeStatus status) {
        Filter f = resolve(from, to, status);

        // Every status appears in the map (0 when none) so the cards never have gaps.
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (EmployeeStatus s : EmployeeStatus.values()) byStatus.put(s.name(), 0L);
        long total = 0;
        for (Object[] row : repo.countByStatus(f.from(), f.to(), f.statuses())) {
            long count = (Long) row[1];
            byStatus.put(((EmployeeStatus) row[0]).name(), count);
            total += count;
        }

        List<DepartmentCount> byDepartment = repo.countByDepartment(f.from(), f.to(), f.statuses()).stream()
                .map(r -> new DepartmentCount((String) r[0], (Long) r[1]))
                .toList();

        // Hires per month, oldest first. Only months that have hires come back; keep the latest MAX_MONTHS.
        List<MonthCount> allMonths = repo.countByMonth(f.from(), f.to(), f.statuses()).stream()
                .map(r -> new MonthCount(String.format("%04d-%02d", ((Number) r[0]).intValue(), ((Number) r[1]).intValue()),
                        ((Number) r[2]).longValue()))
                .toList();
        List<MonthCount> byMonth = allMonths.size() > MAX_MONTHS
                ? allMonths.subList(allMonths.size() - MAX_MONTHS, allMonths.size())
                : allMonths;

        return new ReportSummary(f.from(), f.to(), total, byStatus, byDepartment, byMonth);
    }

    /** UC-RPT-01: paginated, sortable, read-only table. Sort field is whitelisted, page size capped at 50. */
    public PageResponse<EmployeeResponse> list(LocalDate from, LocalDate to, EmployeeStatus status,
                                               int page, int size, String sort, String dir) {
        Filter f = resolve(from, to, status);
        String sortField = EmployeeService.SORTABLE.contains(sort) ? sort : "hireDate";
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        // Second key keeps the order stable when many rows share the same value (e.g. hire date).
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(direction, EmployeeService.sortProperty(sortField)).and(Sort.by(Sort.Direction.ASC, "id")));
        Page<Employee> result = repo.report(f.from(), f.to(), f.statuses(), pageable);
        return new PageResponse<>(result.getContent().stream().map(EmployeeResponse::from).toList(),
                result.getTotalElements(), result.getNumber(), result.getSize());
    }

    /** Hard cap so one export cannot load an unbounded number of rows into memory. */
    public static final int EXPORT_LIMIT = 10_000;

    /** Everything an exporter needs: the resolved filter, the rows, and whether the cap cut the result. */
    public record Export(LocalDate from, LocalDate to, String statusLabel,
                         List<EmployeeResponse> rows, long totalMatching, boolean truncated) {}

    /** UC-RPT-02/03: all rows for the filter (not just one page), newest hires first. */
    public Export export(LocalDate from, LocalDate to, EmployeeStatus status) {
        Filter f = resolve(from, to, status);
        var pageable = PageRequest.of(0, EXPORT_LIMIT,
                Sort.by(Sort.Direction.DESC, "hireDate").and(Sort.by(Sort.Direction.ASC, "id")));
        Page<Employee> result = repo.report(f.from(), f.to(), f.statuses(), pageable);
        String label = status != null ? status.name() : "All statuses";
        return new Export(f.from(), f.to(), label,
                result.getContent().stream().map(EmployeeResponse::from).toList(),
                result.getTotalElements(), result.getTotalElements() > EXPORT_LIMIT);
    }
}
