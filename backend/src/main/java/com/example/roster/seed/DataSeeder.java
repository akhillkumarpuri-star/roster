package com.example.roster.seed;

import com.example.roster.domain.AppUser;
import com.example.roster.domain.Employee;
import com.example.roster.domain.EmployeeStatus;
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
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, EmployeeRepository employees, PasswordEncoder encoder) {
        this.users = users;
        this.employees = employees;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() == 0) {
            users.save(new AppUser("admin", encoder.encode("Admin@123"), "Ananya Admin", "ADMIN"));
            users.save(new AppUser("viewer", encoder.encode("Viewer@123"), "Vikram Viewer", "USER"));
        }
        if (employees.count() == 0) {
            add("Priya", "Sharma", "Engineering", "Senior Engineer", "2021-03-15", EmployeeStatus.ACTIVE);
            add("Rahul", "Mehta", "Engineering", "Tech Lead", "2019-07-01", EmployeeStatus.ACTIVE);
            add("Neha", "Patel", "Design", "Product Designer", "2022-01-10", EmployeeStatus.ACTIVE);
            add("Arjun", "Nair", "Product", "Product Manager", "2020-11-23", EmployeeStatus.ON_LEAVE);
            add("Sana", "Khan", "Finance", "Financial Analyst", "2023-02-06", EmployeeStatus.ACTIVE);
            add("Dev", "Joshi", "Operations", "Operations Lead", "2018-05-14", EmployeeStatus.ACTIVE);
            add("Meera", "Iyer", "HR", "HR Business Partner", "2021-09-01", EmployeeStatus.ACTIVE);
            add("Karan", "Singh", "Sales", "Account Executive", "2024-04-22", EmployeeStatus.ACTIVE);
            add("Isha", "Reddy", "Engineering", "QA Engineer", "2022-08-29", EmployeeStatus.INACTIVE);
            add("Rohan", "Desai", "Sales", "Sales Manager", "2017-12-04", EmployeeStatus.ACTIVE);
            add("Tara", "Banerjee", "Design", "UX Researcher", "2023-06-19", EmployeeStatus.ON_LEAVE);
            add("Aman", "Gupta", "Finance", "Controller", "2016-10-17", EmployeeStatus.ACTIVE);
            add("Lakshmi", "Rao", "Engineering", "DevOps Engineer", "2022-05-02", EmployeeStatus.ACTIVE);
        }
    }

    private void add(String first, String last, String dept, String title, String hired, EmployeeStatus status) {
        Employee e = new Employee();
        e.update(first, last, (first + "." + last + "@roster.example").toLowerCase(), "+91 98765 4" + (10000 + employees.count()),
                dept, title, LocalDate.parse(hired), status);
        employees.save(e);
    }
}
