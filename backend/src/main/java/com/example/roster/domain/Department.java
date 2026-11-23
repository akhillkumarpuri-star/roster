package com.example.roster.domain;

import jakarta.persistence.*;

/** A department. Employees reference it through a foreign key (employees.department_id). */
@Entity
@Table(name = "departments",
       uniqueConstraints = @UniqueConstraint(name = "uk_department_name", columnNames = "name"))
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    protected Department() {}

    public Department(String name) { this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }
}
