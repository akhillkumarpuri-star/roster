# AI Usage Log
 
**Project:** Roster, an employee-management app (Angular 19, Spring Boot 3.3, MySQL 8). **Period:** November 2026.
**Tool used for everything:** Claude (claude.ai chat). No other AI tool was used.
 
## How to read this log
Each entry records the five items required by section 6.1 of the assessment, plus the files changed and the verification status:
**Tool, Prompt, What the AI generated, What was modified and why, What I learned, Result.**
 
**My working rule (from the recruiter):** AI for template and boilerplate code and for checking small things; I review, test and improve it and must be able to explain every part.
**Honest scale of AI use:** besides the first template, Claude also generated the report module, the extra tests, the database change and the documentation, at my request and step by step. That is more than "template only", so I state it plainly here and I will say it in the interview.
 
## 1. Summary
 
| # | Area | Why AI instead of doing it myself | Result |
|---|---|---|---|
| 1 | Initial template: backend, frontend, JWT, seeder, 9 tests | Long mechanical boilerplate; I wanted a working baseline to study and improve | Compiled and ran on first start |
| 2 | Duplicate-email logic review | Quick second look for logic mistakes | Raw vs normalised email mismatch found and fixed |
| 3 | 401 vs 403 on forbidden actions | I found the symptom but not the Spring cause | Cause explained; one-line fix applied |
| 4 | Windows 11 environment setup | Unfamiliar installer and PATH errors | Java, Maven, Node, MySQL working |
| 5 | First documentation drafts | Drafting structure is faster than a blank page | Drafts I edited |
| 6 | Report: summary endpoint and cards | Required part of the assessment; large | Counts by status and department |
| 7 | Report: filtered, sortable, paged table | Same | UC-RPT-01 and UC-RPT-05 |
| 8 | Report: Excel export (Apache POI) | Same | UC-RPT-02 |
| 9 | Report: PDF export (OpenPDF) | Same | UC-RPT-03 |
| 10 | Tests: report, exports, MockMvc for 400/401/403/404/409 | Repetitive test scaffolding | 24 new tests written |
| 11 | Improvements: seeder, 409 field, IdleService, `/error` fix | Items from my own refactor checklist | Code applied |
| 12 | Database: departments table with foreign key, Flyway SQL schema | Recruiter feedback | Schema tested on MariaDB |
| 13 | Report summary: hires per month | Recruiter feedback on UC-RPT-04 | Query, DTO, UI, tests |
| 14 | README, BUILD_GUIDE, WALKTHROUGH, this log | Keep docs in step with the code | Docs match the code |
 
## 2. Detailed entries
 
### Entry 1: Initial template of the whole application
- **Tool:** Claude (claude.ai chat).
- **Prompt (summary from notes):** "Here are the assessment document and the recruiter email. Build a small full-stack application with Angular 19, Spring Boot and MySQL: login, logout and one CRUD screen, with a clean, good-looking UI."
- **What the AI generated:** the backend (entities, repositories, DTOs, services, controllers, JWT security, seeder, 9 unit tests), the frontend (configuration, routes, login, shell, employee table, add/edit dialogs) and a first README.
- **What was modified and why:** the template was not rewritten. I installed the toolchain, ran it and tested login, wrong password, search, sort, paging, add, edit, delete, and the API with and without a token. The defects I found became entries 2 and 3; my later refactors are entries 11 to 13.
- **What I learned:** how a request travels from the Angular form through the HTTP interceptor, the JWT filter, the controller, the service and the repository to MySQL, and why the server (not hidden buttons) is the real security boundary.
- **Result:** a working baseline.
### Entry 2: Duplicate-email logic review
- **Tool:** Claude.
- **Prompt (summary from notes):** "Review EmployeeService create and update for logic mistakes, especially the duplicate-email rule."
- **What the AI generated:** a finding that the duplicate check used the raw email while the saved email is trimmed and lowercased, so `" Ada@X.com"` could pass the check.
- **What was modified and why:** one `normalise()` method now feeds both the check and the stored value, and the unit test was updated. A validation must use exactly the value that is stored.
- **What I learned:** normalise input once and reuse it; the unique index on `email` is the final guard against two simultaneous requests.
- **Files:** `EmployeeService.java`, `EmployeeServiceTest.java`.
- **Result:** fixed and covered by a test.
### Entry 3: 401 vs 403 on forbidden actions
- **Tool:** Claude.
- **Prompt (summary from notes):** "I logged in as `viewer` and sent DELETE /api/employees/{id} with the viewer token. It returned 401, but I expected 403. Why does this happen and how do I fix it?"
- **Prompt as typed (when the fix was applied):** "Start here and do the step fixes and give me the final zip file." Then: "Also send the zip file where you made the edit."
- **Prompt, structured (reconstructed for the applied fix):** "In `SecurityConfig.java`, forbidden requests from an authenticated viewer return 401 instead of 403 because Spring's internal `/error` dispatch requires authentication. Apply the minimal fix, show the changed lines, give me manual re-test steps, and return the updated project zip."
- **What the AI generated:** the explanation (Spring forwards the 403 to `/error`, which my rules protected) and the one-line fix `.requestMatchers("/error").permitAll()`, applied in the project.
- **What was modified and why:** the line was added after the login matcher with a comment. The frontend interceptor signs the user out on 401, so a forbidden action must return 403. MockMvc cannot test the `/error` dispatch, so I verify this by hand with PowerShell (viewer token gives 403, no token gives 401).
- **What I learned:** 401 means not authenticated, 403 means authenticated but not allowed, and how Spring's error dispatch interacts with the security filter chain.
- **Files:** `SecurityConfig.java`.
- **Result:** fix applied; manual re-test is tracked in section 3.
### Entry 4: Windows 11 environment setup
- **Tool:** Claude.
- **Prompt (summary from notes):** "I am on Windows 11. Here are screenshots of my errors (PATH not updated, MySQL Server missing while Workbench is installed, an old MySQL data folder asking for a root password, `curl` not found). How do I fix each one?"
- **What the AI generated:** exact commands and settings for each problem.
- **What was modified and why:** no code. I ran every command myself and verified each tool with a version command.
- **What I learned:** how PATH and `JAVA_HOME` work, what the MySQL service needs, and how to verify each tool.
- **Result:** Java 17, Maven, Node 20 and MySQL 8 working.
### Entry 5: First documentation drafts
- **Tool:** Claude.
- **Prompt (summary from notes):** "Draft a README, an interview walkthrough, a slide outline and a refactor checklist for this project."
- **What the AI generated:** setup steps, an API table, an architecture explanation, a demo order and likely interview questions.
- **What was modified and why:** I read the drafts against the real code and corrected anything inaccurate, because documentation that does not match the code is worse than none.
- **What I learned:** how to explain the architecture and the security decisions in my own words.
- **Result:** drafts I rehearsed from (updated again in entry 14).
### Entry 6: Report summary endpoint and cards (UC-RPT-04, part 1)
- **Tool:** Claude.
- **Prompt, structured (reconstructed):** "Context: the Roster project (attached zip) has login and employee CRUD. The Report use cases UC-RPT-01 to 05 are missing. Task, step 2 of 8: implement the report summary. Requirements: (1) `GET /api/reports/employees/summary` with optional `from`, `to` and `status`; (2) default range is the current month; (3) return HTTP 400 when `from` is after `to`; (4) use SQL aggregate queries (COUNT with GROUP BY) for the total, the count per status and the count per department; (5) bound parameters only, no string-built SQL; (6) an Angular report page with a date-range picker, a status filter and summary cards, available to every signed-in user."
- **What the AI generated:** `ReportController`, `ReportService`, two aggregate queries, a `BadRequest` exception with its 400 handler, `ReportSummary` DTOs, and the Angular report page with filters, presets and cards.
- **What was modified and why:** (a) the status filter binds a set of statuses (`status in :statuses`) instead of a nullable parameter, because a null enum parameter can fail type resolution in Hibernate; (b) the default range stays the current month as the assessment requires, and "This year" and "All time" presets were added because the seed hire dates are older and the page would look empty; (c) dates are formatted from local year, month and day instead of `toISOString()`, which can shift a day in India time.
- **What I learned:** aggregate queries with GROUP BY, why bound parameters are both safer and more reliable than string building, and the UTC date-shift trap.
- **Files:** `ReportController`, `ReportService`, `EmployeeRepository`, `ApiExceptions`, `ApiExceptionHandler`, `Dtos`, `features/report/*`, `core/report.service.ts`.
- **Result:** cards for total, per status and per department. The Angular build passed; the Java code was not compiled by the AI.
### Entry 7: Report table with sorting, paging and filters (UC-RPT-01, UC-RPT-05)
- **Tool:** Claude.
- **Prompt, structured (reconstructed):** "Task, step 3 of 8: add the report table. Requirements: `GET /api/reports/employees` with the same `from`, `to` and `status` filters plus `page`, `size`, `sort` and `dir`; server-side paging and sorting; a whitelist of sortable fields so a client cannot choose an arbitrary column; page size capped at 50; a read-only Angular Material table with sortable headers, a paginator, status chips and an empty state; any filter change returns to page 1. The summary, the table and the later exports must use one shared filter rule so they cannot disagree."
- **What the AI generated:** `ReportService.list`, the paged `report` query, the controller endpoint, and the table on the report page.
- **What was modified and why:** the build failed on a strict-typing error (indexing the status-label map with an untyped table row), so it was replaced by a typed `statusLabel()` method like the employees screen. The sort whitelist is shared with the employee list. `id` was added as a second sort key so pages stay stable when many rows share a hire date. A single `resolve()` method validates the filter for the summary, the table and the exports.
- **What I learned:** server-side paging and sorting with a whitelist, and why a tie-breaker is needed for stable pages.
- **Files:** `ReportService`, `ReportController`, `EmployeeRepository`, `EmployeeService` (whitelist made package-visible), `features/report/report.component.ts`, `core/report.service.ts`.
- **Result:** a read-only, sortable, paginated report table. The Angular build passed.
### Entry 8: Excel export (UC-RPT-02)
- **Tool:** Claude.
- **Prompt, structured (reconstructed):** "Task, step 4 of 8: Excel export with Apache POI. Requirements: `GET /api/reports/employees/export.xlsx` using the same filters; include all matching rows, not only the visible page; proper column headers; hire dates as real Excel dates; a frozen header; do not load unlimited rows into memory; the Angular export button must send the JWT."
- **What the AI generated:** `ExcelReportBuilder`, the export endpoint, `ReportService.export`, the Export Excel button, and a blob download helper.
- **What was modified and why:** fixed column widths instead of auto-size, because auto-size needs AWT fonts that a headless server may lack; exports are capped at 10,000 rows and the sheet says when it was cut; the file is fetched through HttpClient, not a plain link, so the JWT interceptor adds the token.
- **What I learned:** how Apache POI builds a workbook, and why a download from a protected API must go through HttpClient and a blob.
- **Files:** `pom.xml`, `ExcelReportBuilder`, `ReportService`, `ReportController`, `core/download.ts`, `core/report.service.ts`, report component.
- **Result:** every row that matches the filter exports. The Angular build passed; POI code not compiled by the AI.
### Entry 9: PDF export (UC-RPT-03)
- **Tool:** Claude.
- **Prompt, structured (reconstructed):** "Task, step 5 of 8: PDF export. Requirements: `GET /api/reports/employees/export.pdf` with the same filters and rows as the Excel export; formatted output with a title, the date, the filters used and a table that keeps its layout across pages (repeat the header row); a page footer; an Export PDF button next to Export Excel."
- **What the AI generated:** `PdfReportBuilder` (OpenPDF), the endpoint, and the Export PDF button.
- **What was modified and why:** the two export methods in the component were merged into one `exportFile('xlsx' | 'pdf')` to remove duplication. The Latin-only font limitation is documented in the README.
- **What I learned:** how OpenPDF lays out a table across pages, and the font limitation for non-Latin names.
- **Files:** `pom.xml`, `PdfReportBuilder`, `ReportController`, `core/report.service.ts`, report component.
- **Result:** a formatted PDF that uses the same rows as the Excel file.
### Entry 10: Automated tests
- **Tool:** Claude.
- **Prompt, structured (reconstructed):** "Task, step 6 of 8: tests. Write unit tests for the report service rules (default range, `from > to`, status filter, zero-filled statuses, sort whitelist and page cap, export cap), tests that read back the generated Excel file and check the PDF header, and MockMvc tests with the real security chain and real JWTs for 400, 401, 403, 404 and 409 plus the report endpoints."
- **What the AI generated:** `ReportServiceTest`, `ReportBuildersTest` and `ApiSecurityTest`.
- **What was modified and why:** query rows are built with a helper because `List.of(array)` would spread the array into separate elements. It is documented that MockMvc has no `/error` dispatch, so entry 3 is checked by hand. The AI could not run Maven, so I run `mvn test` myself.
- **What I learned:** Mockito strict stubbing, `@WebMvcTest` slices, and testing authorisation through the real filter chain.
- **Files:** `backend/src/test/java/com/example/roster/*`.
- **Result:** tests written; the pass or fail result is recorded in section 3 after I run them.
### Entry 11: Improvements from my refactor checklist
- **Tool:** Claude.
- **Prompt, structured (reconstructed):** "Task, step 7 of 8: apply safe improvements from my refactor checklist: (1) replace the hacky phone-number expression in `DataSeeder` with explicit data; (2) make the 409 response report the real conflicting field instead of always `email`; (3) extract the idle-timeout logic from `ShellComponent` into an `IdleService` that starts and stops cleanly. Update or add tests for the changed behaviour."
- **What the AI generated:** explicit seed data, `ApiExceptions.Conflict(field, message)`, `core/idle.service.ts`, a simplified `ShellComponent`, an updated 409 test and a new non-email conflict test.
- **What was modified and why:** only these three were chosen because they are small and easy to review; the `EmployeesComponent` split was left for later. Flyway was deferred at this point and done in entry 12.
- **What I learned:** keeping a component focused on the view, and putting timers and listeners in a service that can stop cleanly.
- **Files:** `DataSeeder`, `ApiExceptions`, `ApiExceptionHandler`, `EmployeeService`, `idle.service.ts`, `shell.component.ts`, tests.
- **Result:** applied; to verify in section 3. The Angular build passed.
### Entry 12: Database design: foreign key and SQL schema
- **Tool:** Claude.
- **Prompt as typed (recruiter feedback I pasted, translated from Telugu-English):** "These things are not covered in the repo: … (2) Database: no foreign keys or relationships (department is just a string), and no SQL schema script, only ddl-auto=update." Then: "Yes, do all 3 things in this one." Then: "Continue and give me the zip file."
- **Prompt, structured (reconstructed):** "Improve the database design. Requirements: (1) a real relationship, with departments in their own table and `employees.department_id` as a foreign key; (2) keep the API and screens working with a department name; (3) keep sorting, search and the report queries correct; (4) replace `ddl-auto=update` with an explicit SQL schema script that has primary, unique and foreign keys, CHECK constraints for status and role, and indexes that match the queries; (5) test the SQL on a real database; (6) update the seeder, the tests and the documentation, including an ER diagram."
- **What the AI generated:** the `Department` entity and repository; `Employee.department` as a many-to-one relation; `EmployeeService.departmentFor` (find the department ignoring case, or create it); sorting through `department.name`; an entity graph on the list queries; Flyway with `V1__create_schema.sql`; `ddl-auto=none`; and the README ER diagram.
- **What was modified and why:** the SQL migration was run on MariaDB 10.11 (no MySQL was available) and a duplicate email, a duplicate department name in another case, an unknown department, a bad status and deleting a used department were all rejected as intended. `ddl-auto=none` was chosen instead of `validate` because Hibernate's type validation (for example for enum columns) could not be tested against MySQL. Reasons for the design: normalisation, referential integrity enforced by the database, and an explicit versioned schema. A database created by the old version must be dropped once.
- **What I learned:** one-to-many relationships and foreign keys, why `ddl-auto=update` is risky outside a demo, how Flyway versions a schema, and how indexes follow the real queries.
- **Files:** `Department`, `DepartmentRepository`, `Employee`, `EmployeeService`, `ReportService`, `EmployeeRepository`, `DataSeeder`, `Dtos`, `pom.xml`, `application.properties`, `db/migration/V1__create_schema.sql`, tests, README.
- **Result:** constraints verified in SQL. The Java side is verified when I run `mvn test` and the app (section 3).
### Entry 13: Date-based aggregation in the summary (UC-RPT-04, part 2)
- **Tool:** Claude.
- **Prompt as typed (same recruiter feedback as entry 12):** "Minor: no date-based aggregation in the report summary cards (UC-RPT-04)."
- **Prompt, structured (reconstructed):** "UC-RPT-04 requires date-based aggregations in the summary. Add hires per calendar month to the summary using the same filters as the table; return it oldest first; limit the result for very long ranges; show it on the Report page; add unit tests for the ordering and the limit."
- **What the AI generated:** the `countByMonth` query (`GROUP BY year(hire_date), month(hire_date)`), the `MonthCount` DTO and `byMonth` field, a "Hires by month" section on the report page, and two tests.
- **What was modified and why:** only months that have hires are returned and the summary keeps the latest 24 months, because "All time" could otherwise return decades of rows. The SQL was checked on MariaDB (2023-11 had 1 hire, 2024-03 had 2).
- **What I learned:** date functions in JPQL (`year()`, `month()`) and grouping by more than one expression.
- **Files:** `EmployeeRepository`, `ReportService`, `Dtos`, `core/models.ts`, report component, tests.
- **Result:** UC-RPT-04 now has total, status, department and monthly aggregations. The Angular build passed.
### Entry 14: Documentation and this log
- **Tool:** Claude.
- **Prompt as typed:** "Also, have you updated the README file and the how-to-build .md file and everything?" Then: "Yes, add both." Later: "Update the AI_USAGE_LOG.md more accurately, with professional-type prompts"
- **Prompt, structured (reconstructed):** "Update all Markdown documents so they match the current code: README (run steps, API table, use-case coverage, ER diagram, limitations), a new BUILD_GUIDE (prerequisites, build and run, verification checklist, PowerShell checks for 401 and 403, a common-problems table), the interview WALKTHROUGH, the refactor checklist, and the AI usage log in the section 6.1 format."
- **What the AI generated:** the rewritten `README.md`, a new `BUILD_GUIDE.md`, an updated `WALKTHROUGH.md` and `REFACTOR_CHECKLIST.md`, and this log.
- **What was modified and why:** test counts, endpoints and table names were updated everywhere so the documents match the code, and limitations are stated openly (Latin-only PDF, no department screen, `ddl-auto=none`). This log separates prompts as typed from reconstructed prompts so it stays accurate.
- **What I learned:** how to explain the project end to end and where its honest limits are.
- **Result:** documents that match the current code.
## 3. My own work: done and planned
Only tick an item when it is really finished and I have checked it myself. Update this section before submitting.
 
**Done by me**
- [x] Installed Java, Maven, Node and MySQL Server and got the app running locally
- [x] Tested login, wrong password, search, sort, paging, add, edit and delete
- [x] `DROP DATABASE roster_db;` then `mvn spring-boot:run`: Flyway creates the schema and the seeder fills it
- [x] 409 handler, `IdleService` and `DataSeeder` cleanup checked
- [x] Employees screen works after the department change (add with a new department, sort by department)
- [x] Tested the API with and without a token and found the 401 vs 403 problem

**Code applied (AI-drafted). Tick each one only after I have run it and read the code**
- [x] `/error` fix: viewer token on DELETE gives 403, no token gives 401 (re-test by hand)
- [x] `mvn test` passes with 40 tests
- [x] Report page works in the browser: cards, department bars, hires by month, table, sorting, paging, filters
- [x] Excel export opens with the right rows, a frozen header and real date cells
- [x] PDF export opens with the title, filters, table and page footer
      
## 4. How I would describe it in the interview
"AI generated the initial template for both applications, and later, at my request and step by step, the report module (summary, table, Excel and PDF export), the extra tests, a departments table with a foreign key and a Flyway schema, and the documentation. I set up the environment, ran and tested the first version, and found that forbidden requests returned 401 instead of 403. I reviewed the generated code, ran the tests and the app, and I can explain each part. Every use of AI is in this log with the prompt, what was generated, what was modified and why, and what I learned."
 
(Only say "I reviewed, ran and tested" after you really have. Read each "What I learned" line and put it into your own words.)
 
## 5. Template for new entries
### Entry N: short title
- **Tool:**
- **Prompt as typed:** (the exact message you sent)
- **Prompt, structured:** (context, task, requirements and constraints; mark it "reconstructed" if you did not type it)
- **What the AI generated:**
- **What was modified and why:**
- **What I learned:**
- **Files:**
- **Result:** (include what was verified and what still needs checking)
 
