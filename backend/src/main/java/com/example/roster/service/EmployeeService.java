package com.example.roster.service;

import com.example.roster.domain.Employee;
import com.example.roster.repo.EmployeeRepository;
import com.example.roster.web.ApiExceptions;
import com.example.roster.web.Dtos.EmployeeRequest;
import com.example.roster.web.Dtos.EmployeeResponse;
import com.example.roster.web.Dtos.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class EmployeeService {
    // Whitelist: the client can only sort by these properties.
    static final Set<String> SORTABLE =
            Set.of("firstName", "lastName", "email", "department", "jobTitle", "hireDate", "status");

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> list(String q, int page, int size, String sort, String dir) {
        String sortField = SORTABLE.contains(sort) ? sort : "lastName";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(direction, sortField));
        Page<Employee> result = repo.search(q == null ? "" : q.trim(), pageable);
        return new PageResponse<>(result.getContent().stream().map(EmployeeResponse::from).toList(),
                result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id) { return EmployeeResponse.from(find(id)); }

    public EmployeeResponse create(EmployeeRequest r) {
        if (repo.existsByEmailIgnoreCase(normalise(r.email()))) {
            throw new ApiExceptions.Conflict("email", "An employee with this email already exists.");
        }
        Employee e = new Employee();
        apply(e, r);
        return EmployeeResponse.from(repo.save(e));
    }

    public EmployeeResponse update(Long id, EmployeeRequest r) {
        Employee e = find(id);
        if (repo.existsByEmailIgnoreCaseAndIdNot(normalise(r.email()), id)) {
            throw new ApiExceptions.Conflict("email", "An employee with this email already exists.");
        }
        apply(e, r);
        return EmployeeResponse.from(e);
    }

    public void delete(Long id) { repo.delete(find(id)); }

    private Employee find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("Employee " + id + " was not found."));
    }

    // Same normalisation for the duplicate check and for what gets stored.
    private static String normalise(String email) { return email.trim().toLowerCase(); }

    private void apply(Employee e, EmployeeRequest r) {
        e.update(r.firstName().trim(), r.lastName().trim(), normalise(r.email()), r.phone().trim(),
                r.department().trim(), r.jobTitle().trim(), r.hireDate(), r.status());
    }
}
