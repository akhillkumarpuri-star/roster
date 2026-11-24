package com.example.roster.seed;

import com.example.roster.domain.AppUser;
import com.example.roster.domain.Department;
import com.example.roster.domain.Employee;
import com.example.roster.domain.EmployeeStatus;
import com.example.roster.repo.DepartmentRepository;
import com.example.roster.repo.EmployeeRepository;
import com.example.roster.repo.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Seeds demo users and employees on first start (only when the tables are empty). */
@Component
public class DataSeeder implements ApplicationRunner {
    private final UserRepository users;
    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, EmployeeRepository employees, DepartmentRepository departments,
                      PasswordEncoder encoder) {
        this.users = users;
        this.employees = employees;
        this.departments = departments;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() == 0) {
            users.save(new AppUser("admin", encoder.encode("Admin@123"), "Ananya Admin", "ADMIN"));
            users.save(new AppUser("viewer", encoder.encode("Viewer@123"), "Vikram Viewer", "USER"));
        }
        if (employees.count() == 0) {
            // A few hire dates are relative to today so the default "current month" report is never empty (UC-RPT-05).
            LocalDate today = LocalDate.now();
            //  first, last, department, title, hired, status, phone
            add("Priya", "Sharma", "Engineering", "Senior Engineer", "2021-03-15", EmployeeStatus.ACTIVE, "+91 98765 41001");
            add("Rahul", "Mehta", "Engineering", "Tech Lead", "2019-07-01", EmployeeStatus.ACTIVE, "+91 98765 41002");
            add("Neha", "Patel", "Design", "Product Designer", "2022-01-10", EmployeeStatus.ACTIVE, "+91 98765 41003");
            add("Arjun", "Nair", "Product", "Product Manager", "2020-11-23", EmployeeStatus.ON_LEAVE, "+91 98765 41004");
            add("Sana", "Khan", "Finance", "Financial Analyst", today.minusMonths(1).toString(), EmployeeStatus.ACTIVE, "+91 98765 41005");
            add("Dev", "Joshi", "Operations", "Operations Lead", "2018-05-14", EmployeeStatus.ACTIVE, "+91 98765 41006");
            add("Meera", "Iyer", "HR", "HR Business Partner", "2021-09-01", EmployeeStatus.ACTIVE, "+91 98765 41007");
            add("Karan", "Singh", "Sales", "Account Executive", today.withDayOfMonth(1).toString(), EmployeeStatus.ACTIVE, "+91 98765 41008");
            add("Isha", "Reddy", "Engineering", "QA Engineer", "2022-08-29", EmployeeStatus.INACTIVE, "+91 98765 41009");
            add("Rohan", "Desai", "Sales", "Sales Manager", "2017-12-04", EmployeeStatus.ACTIVE, "+91 98765 41010");
            add("Tara", "Banerjee", "Design", "UX Researcher", today.toString(), EmployeeStatus.ON_LEAVE, "+91 98765 41011");
            add("Aman", "Gupta", "Finance", "Controller", "2016-10-17", EmployeeStatus.ACTIVE, "+91 98765 41012");
            add("Lakshmi", "Rao", "Engineering", "DevOps Engineer", "2022-05-02", EmployeeStatus.ACTIVE, "+91 98765 41013");
        }
    }

    private void add(String first, String last, String dept, String title, String hired,
                     EmployeeStatus status, String phone) {
        Employee e = new Employee();
        Department department = departments.findByNameIgnoreCase(dept)
                .orElseGet(() -> departments.save(new Department(dept)));
        e.update(first, last, (first + "." + last + "@roster.example").toLowerCase(), phone,
                department, title, LocalDate.parse(hired), status);
        employees.save(e);
    }
}
