# AI Usage Log: Roster (Employee Management)

## 1\. Overview

|Item|Detail|
|-|-|
|Tool|Claude (claude.ai). No other AI tool was used.|
|Prompts|Entries 1 and 12 are copied exactly. For the other entries I did not keep a copy of the prompts at the time, so they are reconstructed from memory and from the code that came back, and are marked "reconstructed".|
|Stack|Angular 19, Spring Boot 3.3 (Java 17), MySQL 8|
|What AI produced|The initial application template, the report module, most of the tests, the departments and Flyway change, the hires-per-month aggregation, and first drafts of the documentation|
|What I did|Set up the environment on Windows 11, ran and tested every feature, found the 401 vs 403 defect, chose which improvements to make, ran the test suite, and verified the exports and the database changes on my own MySQL|
|How I checked AI output|Read the generated code, ran `mvn test`, tested every use case in the browser, and tested the API with and without a token in PowerShell|

**How this was built:** AI-assisted, human-directed. I chose the domain and scope, broke the work into steps and directed each one, and Claude generated the code for each step. I set up and ran everything, tested every feature, found the 401 vs 403 bug myself, decided which improvements to make and which to skip, checked the result against the assessment, and verified it all on my own MySQL setup.

## 2\. Summary

|#|Area|AI role|My role|Outcome|
|-|-|-|-|-|
|1|Reading the assessment and planning|Summarised the doc, proposed a plan|Chose a different domain|Plan adapted|
|2|Application template|Generated backend, frontend, 9 tests|Installed, ran and tested it|Working baseline|
|3|Duplicate-email logic|Found a raw vs stored email mismatch|Requested the review|Fixed before first commit|
|4|401 instead of 403|Explained the cause, gave a one-line fix|Found the bug, re-tested by hand|Fixed and verified|
|5|Windows environment setup|Explained each error|Ran every fix myself|Toolchain working|
|6|Report summary and filters|Generated endpoint, queries, page|Tested in the browser|UC-RPT-04 (part), UC-RPT-05|
|7|Report table|Generated paged, sorted table|Tested sorting, paging, filters|UC-RPT-01|
|8|Excel export|Generated Apache POI builder|Opened and checked the file|UC-RPT-02|
|9|PDF export|Generated OpenPDF builder|Opened and checked the file|UC-RPT-03|
|10|Automated tests|Generated 25 tests|Ran `mvn test`|All passing|
|11|Refactors|Applied 3 changes|Chose which 3 from its suggestions|Applied and checked|
|12|Review of my repo against the assessment|Found gaps and doc problems|Requested it, decided what to fix|Entries 13 and 14|
|13|Departments table, FK, Flyway|Generated schema, code and 4 tests|Ran it on MySQL 8|Database design improved|
|14|Hires per month|Generated query, DTO, UI, tests|Checked it on the report page|UC-RPT-04 complete|
|15|Documentation|Drafted README and BUILD\_GUIDE|Checked them against the code|Docs match the code|

## 3\. Detailed entries

### Entry 1: Reading the assessment and planning

**Tool:** Claude

**Prompt:** I attached the assessment document and my notes from the recruiter call, then asked:

> check everything about the assessment here

> okay now tell everythign relating both



**What the AI generated:** a summary of all 17 use cases and the scoring weights, and a full project plan for an Invoices / Accounts Receivable domain (roles, customers and invoices tables, refresh tokens, Swagger).

**What I changed and why:** I chose Employee Management instead of Invoices. The assessment suggests it, it has a clear unique field (email) for the duplicate-check use case, and it kept the scope small enough to finish properly. I did not build the refresh token or Swagger parts of the plan.

**What I learned:** reading the scoring table first showed me that the AI usage log is worth 15%, so it had to be kept properly and not written at the end.

\---

### Entry 2: Application template

**Tool:** Claude

**Prompt :** Build a small full-stack app for this assessment (doc attached). Angular 19 standalone with Angular Material, Spring Boot 3, MySQL. Domain is Employee Management. Login with JWT and BCrypt, admin and viewer roles, employee CRUD with server-side paging, sorting, debounced search, an add/edit dialog, delete confirmation, and client and server validation. Add unit tests and a README.

**What the AI generated:** the Spring Boot backend (entities, repositories, DTO records, services, controllers, JWT filter and `JwtService`, `SecurityConfig`, exception handling, a data seeder) with 9 unit tests (`EmployeeServiceTest`, `JwtServiceTest`), and the Angular frontend (routes, auth service, interceptor, guard, login page, shell, employee table, add/edit dialog, confirm dialog).

**What I did:** installed the toolchain (entry 5), ran both applications, and tested login, a wrong password, search, sorting, paging, add, edit, delete, and the API with and without a token. This testing found the defect in entry 4.

**What I learned:** how a request moves from the Angular form through the interceptor, the JWT filter, the controller, the service and the repository to MySQL, and that the server-side role check, not the hidden buttons, is what actually protects the data.

\---

### Entry 3: Duplicate-email logic

**Tool:** Claude

**Prompt:** Review EmployeeService create and update for logic bugs, especially the duplicate email check.

**What the AI generated:** it pointed out that the duplicate check used the raw email while the saved value was trimmed and lowercased, so `" Ada@X.com"` could get past the check. It changed the service so one `normalise()` method feeds both the check and the stored value.

**What I did:** asked for the review and kept the database unique constraint `uk\_employee\_email` as the final guard. This fix went in before my first commit, so it is already in the earliest version of `EmployeeService` in the repo.

**What I learned:** a validation must check exactly the value that gets stored. The service check gives a friendly message; the unique constraint is what stops two simultaneous requests.

**Files:** `EmployeeService.java`, `EmployeeServiceTest.java`

\---

### Entry 4: Viewer got 401 instead of 403

**Tool:** Claude

**Prompt:** I logged in as viewer and called DELETE /api/employees/1 with the viewer token from PowerShell. I get 401 but I expected 403. Here is my SecurityConfig. Why is this happening and how do I fix it?

**What the AI generated:** an explanation that Spring forwards the access-denied error to `/error`, and `/error` itself required authentication, so the 403 turned into a 401. The fix was `.requestMatchers("/error").permitAll()` in `SecurityConfig`.

**What I did:** I found the problem myself while testing in PowerShell: a viewer token on `DELETE /api/employees/{id}` returned 401. This mattered because the Angular interceptor logs the user out on any 401, so a viewer clicking a forbidden action would have been signed out. After the fix I re-tested by hand: viewer token gave 403, no token gave 401. MockMvc does not perform the `/error` dispatch, so this cannot be covered by `ApiSecurityTest`.

**What I learned:** 401 means not authenticated, 403 means authenticated but not allowed, and Spring Security also runs on internal error dispatches.

**Files:** `SecurityConfig.java`

\---

### Entry 5: Windows 11 environment setup

**Tool:** Claude

**Prompt:** I'm on Windows 11. mvn is not recognized after installing, MySQL Workbench is installed but there is no server, an old MySQL data folder asks for a root password, and curl doesn't work in PowerShell. Screenshots attached. How do I fix each one?

Later I moved to a new Windows 11 laptop with an ARM processor and asked (reconstructed): I have a new laptop with nothing installed. How do I install and run this project on Windows from scratch?

**What the AI generated:** a fix for each error I hit: PATH not updated after installing, MySQL Server missing while only Workbench was installed, an old MySQL data folder asking for an unknown root password, and `curl` not behaving as expected in PowerShell. On the new laptop: PowerShell blocking `npm` (execution policy), Maven not on PATH, `JAVA\_HOME` not set, and which builds to use on Windows ARM (ARM64 Node; x64 MySQL, which has no ARM build and runs under emulation). It also pointed out that Angular 19 needs Node 20 or 22, not the newer version nodejs.org offers by default.

**What I did:** ran every command myself and confirmed each tool with `java -version`, `mvn -v`, `node -v` and a MySQL login. Current versions: Java 17.0.21, Maven 3.10.2, Node 22 LTS, MySQL 8.0.46. I added these problems to the BUILD\_GUIDE common-problems table.

**What I learned:** how PATH and `JAVA\_HOME` work on Windows, and that MySQL Server runs as a Windows service separate from Workbench.

\---

### Entry 6: Report summary and filters (UC-RPT-04 part 1, UC-RPT-05)

**Tool:** Claude

**Prompt:** Now add the report module step by step. First the summary: an endpoint with from, to and status filters, default to the current month, 400 if from is after to, COUNT and GROUP BY by status and by department, and an Angular report page with date pickers, a status filter and summary cards.

**What the AI generated:** `ReportController`, `ReportService`, COUNT and GROUP BY queries for totals by status and by department, a 400 response when `from` is after `to`, a default range of the current month, and the Angular report page with date pickers, a status filter, presets and summary cards. While drafting it also: bound statuses as a set (`status in :statuses`) to avoid a Hibernate null-parameter problem, zero-filled missing statuses so no card disappears, and built date strings from local year, month and day instead of `toISOString()`, which shifts the date back a day in India time.

**What I did:** tested the cards, the presets and the `from` after `to` error in the browser. The default current-month view is empty with the seed data, so I use the "All time" preset when demonstrating it.

**What I learned:** aggregate queries with GROUP BY, and that `toISOString()` converts to UTC, so date-only values should never go through it.

**Files:** `ReportController`, `ReportService`, `EmployeeRepository`, `ApiExceptions`, `ApiExceptionHandler`, `Dtos`, `features/report/report.component.ts`, `core/report.service.ts`

\---

### Entry 7: Report table (UC-RPT-01)

**Tool:** Claude

**Prompt :** Next the report table. Same filters, server-side paging and sorting with a sort whitelist, a page size cap, and a read-only Material table. The summary, the table and the exports should all use the same filter logic.

**What the AI generated:** the paged, sorted report query and endpoint, and a read-only Material table with sortable headers and a paginator. It reused the employee list's sort whitelist (making it package-visible), capped page size at 50, added `id` as a second sort key so pages stay stable when hire dates repeat, and put all filter validation in one `resolve()` method shared by the summary, the table and the exports.

**What I did:** tested sorting on every column, paging, and that changing a filter returns to page 1.

**What I learned:** why a tie-breaker sort key is needed for stable paging, and why sharing one filter method keeps the cards, table and exports from disagreeing.

\---

### Entry 8: Excel export (UC-RPT-02)

**Tool:** Claude

**Prompt:** Add Excel export with Apache POI. All filtered rows, not just the current page, proper headers, real date cells and a frozen header. The download button has to send the JWT.

**What the AI generated:** `ExcelReportBuilder` (Apache POI) with a title, the filters used, a bold frozen header, filter arrows, real date cells and fixed column widths (auto-size needs fonts a headless server may not have); a 10,000-row cap noted in the file; and a download that goes through `HttpClient` as a blob so the interceptor attaches the JWT. A plain link would have no token and return 401.

**What I did:** exported with different filters and opened the files in Excel to check the rows, the frozen header and that dates sort as dates.

**What I learned:** a download from a protected API has to go through `HttpClient`, not a normal link.

**Files:** `pom.xml`, `ExcelReportBuilder`, `ReportService`, `ReportController`, `core/download.ts`

\---

### Entry 9: PDF export (UC-RPT-03)

**Tool:** Claude

**Prompt:** Add PDF export with the same rows as Excel: title, date, the filters used, a table header that repeats on every page, and a page footer.

**What the AI generated:** `PdfReportBuilder` (OpenPDF) with landscape A4, a title, the generated date, the filters, a header row that repeats on every page and a page-number footer, plus one shared `exportFile('xlsx' | 'pdf')` method in the component.

**What I did:** opened the PDF and checked the title, filters, table and footer across pages.

**What I learned:** the built-in PDF font is Latin only, so non-Latin names would not render. This is listed in the README limitations.

\---

### Entry 10: Automated tests

**Tool:** Claude

**Prompt:** Write tests for the report service and the Excel and PDF builders, plus MockMvc tests with the real security chain for 400, 401, 403, 404 and 409.

**What the AI generated:** 25 tests: `ReportServiceTest` (7), `ReportBuildersTest` (5, reading back the generated Excel and PDF) and `ApiSecurityTest` (13, MockMvc with the real security chain and real JWTs for 400, 401, 403, 404 and 409).

**What I did:** ran `mvn test`. The AI could not run Maven in its environment, so my run was the first real execution.

**What I learned:** mocked service tests do not prove the security rules; tests through the real filter chain do.

**Files:** `backend/src/test/java/com/example/roster/\*`

\---

### Entry 11: Refactors

**Tool:** Claude

**Prompt:** What should I improve in this code before submitting? Then apply the seeder phone number fix, make the 409 response return the real field, and move the idle timeout into its own service.

**What the AI generated:** a list of improvements, then three of them applied: explicit seed phone numbers instead of an expression built from `employees.count()`, a 409 response that carries the real conflicting field instead of always `email`, and the idle-timeout logic moved out of `ShellComponent` into `IdleService` with activity resets throttled to once a second.

**What I did:** chose these three because they were small and easy to review, and left the larger ones (splitting `EmployeesComponent`, splitting the DTO file) for later. Checked a fresh database seed, a duplicate-email 409 in the dialog, and the idle warning and sign-out.

**What I learned:** timers and global event listeners belong in a service that can start and stop cleanly, not in a component.

**Files:** `DataSeeder`, `ApiExceptions`, `ApiExceptionHandler`, `EmployeeService`, `idle.service.ts`, `shell.component.ts`

\---

### Entry 12: Departments table, foreign key and Flyway schema

**Tool:** Claude

**Prompt:** Add a departments table with a foreign key from employees, keep the API sending the department name, and move the schema to a Flyway SQL migration with proper constraints and indexes.

**What the AI generated:** a `Department` entity and repository; `Employee.department` as a many-to-one relation with `fk\_employee\_department`; a service method that finds a department by name (ignoring case) or creates it, so the API and screens did not change; sorting and search through `department.name`; an entity graph so the department is loaded in the same query; and a Flyway migration `V1\_\_create\_schema.sql` with primary keys, unique keys, the foreign key, CHECK constraints on status and role, and indexes matched to the queries. `ddl-auto` was set to `none`. It also added 4 tests: 3 in `EmployeeServiceTest` (reusing an existing department, creating a new one, sorting by department) and 1 in `ReportServiceTest` (sorting by department). The AI tested the SQL on MariaDB in its own environment.

**What I did:** dropped my old `roster\_db`, started the backend on my MySQL 8 so Flyway created the schema and the seeder filled it, then added an employee with a new department and sorted by department.

**What I learned:** a one-to-many relationship with a foreign key lets the database itself reject bad data, and Flyway keeps the schema as versioned SQL instead of letting Hibernate guess.

**Files:** `Department`, `DepartmentRepository`, `Employee`, `EmployeeService`, `EmployeeRepository`, `ReportService`, `DataSeeder`, `Dtos`, `pom.xml`, `application.properties`, `db/migration/V1\_\_create\_schema.sql`, tests, README

\---

### Entry 13: Hires per month (UC-RPT-04 part 2)

**Tool:** Claude

**Prompt:** UC-RPT-04 needs date-based aggregations. Add hires per month to the report summary using the same filters and show it on the report page.

**What the AI generated:** a `countByMonth` query (`GROUP BY year(hireDate), month(hireDate)`) using the same filters as the table, a `MonthCount` DTO, a "Hires by month" section on the report page limited to the latest 24 months, and two unit tests for ordering and the limit.

**What I did:** checked the monthly bars with the "All time" and "This year" presets.

**What I learned:** JPQL date functions and grouping by more than one expression.

\---

### Entry 14: Documentation

**Tool:** Claude

**Prompt:** Update the README and write a build guide so everything matches the current code.

**What the AI generated:** the README (setup, API table, ER diagram, use-case coverage, limitations) and BUILD\_GUIDE (prerequisites, build and run, common problems).

**What I did:** checked the test counts, endpoints, table names and setup steps against the code and against a run on my machine.

**What I learned:** documentation that does not match the code is worse than none.

## 4\. Known gaps

|Item|Requirement|Plan|
|-|-|-|
|Token refresh|UC-LOGIN-03 (optional)|Refresh endpoint called before expiry|
|Active users signed out 30 minutes after login|UC-LOGIN-04|Refresh or reissue the token on activity|
|Backend token invalidation|UC-LOGOUT-01 (optional)|Token deny-list until expiry|
|Large `EmployeesComponent`, all DTOs in one file|Code quality|Split the table into its own component, split DTOs by feature|
|Leading-wildcard search cannot use an index|Performance|FULLTEXT index or prefix search|

## 5\. Reflection

* **Where AI helped most:** generating a large, consistent codebase quickly, and explaining framework behaviour I had not seen before (entry 4).
* **Where AI fell short:** it could not run Maven or MySQL, so nothing was proven until I ran it. It did not catch the 401 vs 403 problem; my testing did. It found the requirement gaps only when I asked it to review the repo against the assessment.
* **What I would do differently:** check the assessment line by line before building, design token lifetime and idle timeout together, and keep this log as I go instead of reconstructing it.

