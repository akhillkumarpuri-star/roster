package com.example.roster;

import com.example.roster.domain.Department;
import com.example.roster.domain.Employee;
import com.example.roster.domain.EmployeeStatus;
import com.example.roster.repo.DepartmentRepository;
import com.example.roster.repo.EmployeeRepository;
import com.example.roster.service.EmployeeService;
import com.example.roster.web.ApiExceptions;
import com.example.roster.web.Dtos.EmployeeRequest;
import com.example.roster.web.Dtos.EmployeeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepository repo;
    @Mock DepartmentRepository departments;
    @InjectMocks EmployeeService service;

    private EmployeeRequest request(String email) {
        return new EmployeeRequest("Ada", "Lovelace", email, "+1 555 0100", "Engineering", "Engineer",
                LocalDate.of(2024, 1, 15), EmployeeStatus.ACTIVE);
    }

    @Test
    void create_rejectsDuplicateEmail() {
        when(repo.existsByEmailIgnoreCase("ada@x.com")).thenReturn(true);
        assertThrows(ApiExceptions.Conflict.class, () -> service.create(request("ada@x.com")));
        verify(repo, never()).save(any());
    }

    @Test
    void create_normalisesEmailAndSaves() {
        when(repo.existsByEmailIgnoreCase("ada@x.com")).thenReturn(false);
        when(departments.findByNameIgnoreCase("Engineering")).thenReturn(Optional.of(new Department("Engineering")));
        when(repo.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        EmployeeResponse res = service.create(request("  Ada@X.com "));
        assertEquals("ada@x.com", res.email());
        assertEquals("Lovelace", res.lastName());
    }

    @Test
    void update_unknownId_throwsNotFound() {
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ApiExceptions.NotFound.class, () -> service.update(9L, request("a@x.com")));
    }

    @Test
    void update_rejectsEmailUsedByAnotherEmployee() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Employee()));
        when(repo.existsByEmailIgnoreCaseAndIdNot("a@x.com", 1L)).thenReturn(true);
        assertThrows(ApiExceptions.Conflict.class, () -> service.update(1L, request("a@x.com")));
    }

    @Test
    void delete_removesExistingEmployee() {
        Employee e = new Employee();
        when(repo.findById(3L)).thenReturn(Optional.of(e));
        service.delete(3L);
        verify(repo).delete(e);
    }

    @Test
    void list_ignoresUnknownSortFieldAndCapsPageSize() {
        when(repo.search(eq(""), any(Pageable.class))).thenReturn(Page.empty());
        service.list(null, 0, 500, "passwordHash; drop table", "desc");
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).search(eq(""), captor.capture());
        assertEquals(50, captor.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "lastName"), captor.getValue().getSort());
    }

    @Test
    void create_reusesAnExistingDepartment_notCreatingADuplicate() {
        when(repo.existsByEmailIgnoreCase("ada@x.com")).thenReturn(false);
        when(departments.findByNameIgnoreCase("Engineering")).thenReturn(Optional.of(new Department("Engineering")));
        when(repo.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeResponse res = service.create(request("ada@x.com"));

        assertEquals("Engineering", res.department());
        verify(departments, never()).save(any());
    }

    @Test
    void create_createsTheDepartmentWhenItDoesNotExistYet() {
        when(repo.existsByEmailIgnoreCase("ada@x.com")).thenReturn(false);
        when(departments.findByNameIgnoreCase("Engineering")).thenReturn(Optional.empty());
        when(departments.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));
        when(repo.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeResponse res = service.create(request("ada@x.com"));

        assertEquals("Engineering", res.department());
        verify(departments).save(any(Department.class));
    }

    @Test
    void list_sortsByDepartmentThroughTheRelation() {
        when(repo.search(eq(""), any(Pageable.class))).thenReturn(Page.empty());
        service.list(null, 0, 10, "department", "asc");
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).search(eq(""), captor.capture());
        assertEquals(Sort.by(Sort.Direction.ASC, "department.name"), captor.getValue().getSort());
    }
}
