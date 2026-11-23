package com.example.roster.repo;

import com.example.roster.domain.Employee;
import com.example.roster.domain.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // Parameterised JPQL: user input is bound, never concatenated (SQL-injection safe).
    @Query("""
           select e from Employee e
           where lower(e.firstName) like lower(concat('%', :q, '%'))
              or lower(e.lastName)  like lower(concat('%', :q, '%'))
              or lower(e.email)     like lower(concat('%', :q, '%'))
              or lower(e.department.name) like lower(concat('%', :q, '%'))
              or lower(e.jobTitle)  like lower(concat('%', :q, '%'))
           """)
    @EntityGraph(attributePaths = "department")
    Page<Employee> search(@Param("q") String q, Pageable pageable);

    // ---- Report aggregates (COUNT + GROUP BY). Dates and statuses are bound parameters. ----

    @Query("""
           select e.status, count(e) from Employee e
           where e.hireDate between :from and :to and e.status in :statuses
           group by e.status
           """)
    List<Object[]> countByStatus(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                 @Param("statuses") Collection<EmployeeStatus> statuses);

    @Query("""
           select e.department.name, count(e) from Employee e
           where e.hireDate between :from and :to and e.status in :statuses
           group by e.department.name
           order by count(e) desc, e.department.name asc
           """)
    List<Object[]> countByDepartment(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                     @Param("statuses") Collection<EmployeeStatus> statuses);

    // Date-based aggregation (UC-RPT-04): hires per calendar month.
    @Query("""
           select year(e.hireDate), month(e.hireDate), count(e) from Employee e
           where e.hireDate between :from and :to and e.status in :statuses
           group by year(e.hireDate), month(e.hireDate)
           order by year(e.hireDate), month(e.hireDate)
           """)
    List<Object[]> countByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                @Param("statuses") Collection<EmployeeStatus> statuses);

    // Report table: same filter as the aggregates, paged and sorted by the Pageable.
    @Query("""
           select e from Employee e
           where e.hireDate between :from and :to and e.status in :statuses
           """)
    @EntityGraph(attributePaths = "department")
    Page<Employee> report(@Param("from") LocalDate from, @Param("to") LocalDate to,
                          @Param("statuses") Collection<EmployeeStatus> statuses, Pageable pageable);
}
