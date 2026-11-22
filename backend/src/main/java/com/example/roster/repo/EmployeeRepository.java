package com.example.roster.repo;

import com.example.roster.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // Parameterised JPQL: user input is bound, never concatenated (SQL-injection safe).
    @Query("""
           select e from Employee e
           where lower(e.firstName) like lower(concat('%', :q, '%'))
              or lower(e.lastName)  like lower(concat('%', :q, '%'))
              or lower(e.email)     like lower(concat('%', :q, '%'))
              or lower(e.department) like lower(concat('%', :q, '%'))
              or lower(e.jobTitle)  like lower(concat('%', :q, '%'))
           """)
    Page<Employee> search(@Param("q") String q, Pageable pageable);
}
