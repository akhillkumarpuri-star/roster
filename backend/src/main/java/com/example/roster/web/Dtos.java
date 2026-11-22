package com.example.roster.web;

import com.example.roster.domain.Employee;
import com.example.roster.domain.EmployeeStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record LoginRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be 3-50 characters") String username,
            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 100, message = "Password must be at least 6 characters") String password) {}

    public record LoginResponse(String token, String username, String fullName, String role, long expiresInSeconds) {}

    public record EmployeeRequest(
            @NotBlank(message = "First name is required") @Size(max = 80) String firstName,
            @NotBlank(message = "Last name is required") @Size(max = 80) String lastName,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") @Size(max = 150) String email,
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "^[+0-9()\\-\\s]{7,20}$", message = "Enter a valid phone number") String phone,
            @NotBlank(message = "Department is required") @Size(max = 80) String department,
            @NotBlank(message = "Job title is required") @Size(max = 100) String jobTitle,
            @NotNull(message = "Hire date is required")
            @PastOrPresent(message = "Hire date cannot be in the future") LocalDate hireDate,
            @NotNull(message = "Status is required") EmployeeStatus status) {}

    public record EmployeeResponse(Long id, String firstName, String lastName, String email, String phone,
                                   String department, String jobTitle, LocalDate hireDate, EmployeeStatus status) {
        public static EmployeeResponse from(Employee e) {
            return new EmployeeResponse(e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(),
                    e.getDepartment(), e.getJobTitle(), e.getHireDate(), e.getStatus());
        }
    }

    public record PageResponse<T>(List<T> content, long totalElements, int page, int size) {}
}
