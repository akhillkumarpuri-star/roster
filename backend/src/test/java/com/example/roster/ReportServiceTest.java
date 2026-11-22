package com.example.roster;

import com.example.roster.domain.EmployeeStatus;
import com.example.roster.repo.EmployeeRepository;
import com.example.roster.service.ReportService;
import com.example.roster.web.ApiExceptions;
import com.example.roster.web.Dtos.ReportSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock EmployeeRepository repo;
    @InjectMocks ReportService service;

    /** Builds the List<Object[]> a GROUP BY query returns (List.of(array) would spread the array, so avoid it). */
    private static List<Object[]> rows(Object[][] r) { return new ArrayList<>(Arrays.asList(r)); }

    @Test
    void summary_fromAfterTo_isRejected() {
        var ex = assertThrows(ApiExceptions.BadRequest.class,
                () -> service.summary(LocalDate.of(2025, 5, 2), LocalDate.of(2025, 5, 1), null));
        assertEquals("from", ex.getField());
        verifyNoInteractions(repo);
    }

    @Test
    void summary_withoutDates_defaultsToCurrentMonthAndAllStatuses() {
        service.summary(null, null, null);

        ArgumentCaptor<LocalDate> from = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> to = ArgumentCaptor.forClass(LocalDate.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<EmployeeStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(repo).countByStatus(from.capture(), to.capture(), statuses.capture());

        assertEquals(LocalDate.now().withDayOfMonth(1), from.getValue());
        assertEquals(LocalDate.now(), to.getValue());
        assertEquals(EnumSet.allOf(EmployeeStatus.class), statuses.getValue());
    }

    @Test
    void summary_withStatus_filtersOnlyThatStatus() {
        service.summary(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), EmployeeStatus.ON_LEAVE);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<EmployeeStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(repo).countByDepartment(any(), any(), statuses.capture());
        assertEquals(EnumSet.of(EmployeeStatus.ON_LEAVE), statuses.getValue());
    }

    @Test
    void summary_fillsMissingStatusesWithZeroAndSumsTotal() {
        when(repo.countByStatus(any(), any(), any()))
                .thenReturn(rows(new Object[][]{{EmployeeStatus.ACTIVE, 5L}, {EmployeeStatus.INACTIVE, 2L}}));
        when(repo.countByDepartment(any(), any(), any()))
                .thenReturn(rows(new Object[][]{{"Engineering", 4L}, {"Sales", 3L}}));

        ReportSummary s = service.summary(LocalDate.of(2020, 1, 1), LocalDate.of(2024, 1, 1), null);

        assertEquals(7, s.total());
        assertEquals(5L, s.byStatus().get("ACTIVE"));
        assertEquals(0L, s.byStatus().get("ON_LEAVE"));   // missing status still present
        assertEquals(2L, s.byStatus().get("INACTIVE"));
        assertEquals("Engineering", s.byDepartment().get(0).department());
        assertEquals(3L, s.byDepartment().get(1).count());
    }

    @Test
    void list_ignoresUnknownSort_capsPageSize_andAddsIdTieBreaker() {
        when(repo.report(any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        service.list(LocalDate.of(2020, 1, 1), LocalDate.of(2024, 1, 1), null, -3, 500, "passwordHash; drop table", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).report(any(), any(), any(), captor.capture());
        assertEquals(50, captor.getValue().getPageSize());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(Sort.by(Sort.Direction.ASC, "hireDate").and(Sort.by(Sort.Direction.ASC, "id")),
                captor.getValue().getSort());
    }

    @Test
    void export_returnsAllRowsWithoutTruncationUnderTheCap() {
        when(repo.report(any(), any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        ReportService.Export e = service.export(LocalDate.of(2020, 1, 1), LocalDate.of(2024, 1, 1), null);

        assertFalse(e.truncated());
        assertEquals("All statuses", e.statusLabel());
    }

    @Test
    void export_flagsTruncationWhenMoreRowsMatchThanTheCap() {
        Pageable p = org.springframework.data.domain.PageRequest.of(0, ReportService.EXPORT_LIMIT);
        when(repo.report(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), p, ReportService.EXPORT_LIMIT + 1L));

        ReportService.Export e = service.export(LocalDate.of(2020, 1, 1), LocalDate.of(2024, 1, 1), EmployeeStatus.ACTIVE);

        assertTrue(e.truncated());
        assertEquals(ReportService.EXPORT_LIMIT + 1L, e.totalMatching());
        assertEquals("ACTIVE", e.statusLabel());
    }
}
