# AI Usage Log: Roster (Employee Management)

Nov 23, 2026 · @akhil

## 1. Overview

| Item | Detail |
|---|---|
| AI tool | Claude (claude.ai) only |
| Stack | Angular 19, Spring Boot 3.3 (Java 17), MySQL 8 |
| What Claude wrote | Most of the code: backend, Angular screens, Flyway migration, report module, Excel and PDF exports, most of the tests, first drafts of the README and BUILD_GUIDE |
| What I did | Chose the domain and scope, wrote the prompts, ran and tested everything on my own Windows machine and MySQL, rejected or redirected Claude's output where it was wrong or too big, and checked the result against the brief |
| How I reviewed output | Read the code before accepting it, ran `mvn test`, tested every use case in the browser, and called the API with and without tokens from PowerShell |

## 2. Summary

| # | Area | What Claude gave me | What I changed or caught | Result |
|---|---|---|---|---|
| 1 | Planning | A plan for an Invoices domain with refresh tokens and Swagger | Switched to Employee Management; cut refresh tokens and Swagger | Smaller, finishable scope |
| 2 | Baseline app | Full backend, frontend, 8 tests | Nothing yet; installed and tested it | Working baseline |
| 3 | Schema | Department stored as a plain string | Asked for a departments table with a foreign key | Better database design |
| 4 | Auth | JWT, filter, SecurityConfig | Accepted after a line-by-line walkthrough | Working login |
| 5 | Employee API | CRUD, paging, 409 on duplicates | Set the page size cap and sort whitelist | Working API |
| 6 | Angular auth | Login, interceptor, guard | Accepted; tested in browser | Working auth flow |
| 7 | Screens | Table, dialog, debounced search | Accepted; tested every action | Working screens |
| 8 | Bug | Duplicate check used the raw email | I found it; Claude fixed it | Fixed, plus a test |
| 9 | Bug | SecurityConfig turned 403 into 401 | I found it in PowerShell | One-line fix, retested |
| 10 | Report summary | Summary endpoint and cards | Specified the filter rules | UC-RPT-04 (part), UC-RPT-05 |
| 11 | Report table | Paged, sorted table; added an id tie-breaker unasked | Asked why the tie-breaker was there; tested paging | UC-RPT-01 |
| 12 | Excel export | POI builder: frozen header, date cells, 10,000 row cap | Chose backend POI over browser SheetJS | UC-RPT-02 |
| 13 | PDF export | OpenPDF builder: repeating header, page footer | Checked the licence: OpenPDF (LGPL/MPL) over iText 7 (AGPL) | UC-RPT-03 |
| 14 | Tests | 25 tests (service, builders, real security chain) | Ran the suite and checked the counts | 40 tests passing |
| 15 | Review | 3 refactors; missed UC-RPT-04 aggregation | Picked 3 of 5 refactors; found the gap | UC-RPT-04 complete |
| 16 | Docs | README and BUILD_GUIDE drafts | Corrected what didn't match the code and my machine | Docs match code |
| 17 | Fixes | Relative seed dates, id tie-breaker | Found both in my own review | UC-RPT-05 and UC-TXN-01 fixed |

## 3. Detailed entries

### Entry 1: Planning

**Prompt 1:** I attached the assessment document

**Prompt 2:** Here's the brief. I'm building Employee Management because email gives a natural unique field for UC-TXN-05. My plan: users, departments and employees tables, JWT valid for 30 minutes, admin and viewer roles, exports done on the backend. Check my plan against the scoring table and tell me what I've missed. Don't write code yet.

**What Claude gave me:** First, a summary of the 17 use cases and a full plan for an Invoices domain, with refresh tokens and Swagger. Then, reviewing my plan, it confirmed the tables and roles and flagged three things: the AI log is worth 15%, UC-RPT-04 needs a date-based aggregation, and UC-LOGIN-03 and UC-LOGOUT-01 have optional parts.

**What I changed and why:** I rejected the Invoices plan. Employee Management is suggested in the brief, has an obvious unique field, and was small enough to finish properly. I dropped refresh tokens and Swagger to protect time for the required use cases.

**What I learned:** Read the scoring table before planning. The log alone is worth more than testing and documentation combined.

### Entry 2: Baseline application

**Prompt:** Build a small full-stack app for this assessment (doc attached). Angular 19 standalone with Angular Material, Spring Boot 3, MySQL. Domain is Employee Management. Login with JWT and BCrypt, admin and viewer roles, employee CRUD with server-side paging, sorting, debounced search, an add/edit dialog, delete confirmation, and client and server validation. Add unit tests and a README.

**What Claude gave me:** The whole baseline: Spring Boot entities, repositories, DTO records, services, controllers, the JWT filter, SecurityConfig, exception handling and a data seeder; 8 unit tests; and the Angular routes, auth service, interceptor, guard, login page, shell, employee table and dialogs.

**What I changed and why:** Nothing at this point. I wanted a running baseline before judging it.

**How I checked:** Installed the toolchain on my Windows machine (the problems I hit are listed in BUILD_GUIDE under common problems), ran both apps, and tested login, a wrong password, search, sort, paging, add, edit, delete, and the API with and without a token. That testing turned up Entries 8 and 9.

**What I learned:** How one request travels from the Angular form through the interceptor, JWT filter, controller, service and repository to MySQL.

After the baseline worked, I reviewed it one layer at a time (Entries 4 to 7). Each entry shows the requirement that produced that layer and what I checked; for security I also asked for a line-by-line explanation.

### Entry 3: Schema

**Prompt 1:** Propose a MySQL schema for Employee Management with login users and admin/viewer roles. Explain why each table, constraint and index is there. Don't write any Java yet.

**What Claude gave me:** users and employees tables, with department as a VARCHAR column on employees, a unique key on email, NOT NULL columns, a CHECK on status, and indexes on (hire_date, status) and last_name.

**Prompt 2:** Why is department a plain string and not its own table? The rubric grades relationships, so use a departments table with a foreign key. Also explain the index on last_name. Then put the final schema in a Flyway migration (not ddl-auto) and generate the entities and repositories only.

**What I changed and why:** I rejected department as a string. Typos would create fake departments, and the rubric grades relationships. I also asked for Flyway instead of ddl-auto so the schema is versioned SQL I can read.

**Result:** `V1__create_schema.sql` with a departments table (case-insensitive unique name), `fk_employee_department`, CHECK constraints on status and role, and ddl-auto set to none. A find-or-create method keeps the API sending department names, so the screens didn't change. 4 new tests.

**How I checked:** Dropped my old roster_db, let Flyway build the schema on MySQL 8, added an employee with a new department, and sorted by department.

**What I learned:** A foreign key makes the database itself reject bad data, instead of trusting the code to.

### Entry 4: Authentication

**Source:** generated by the Entry 2 prompt.

**Requirements I checked it against:** JWT HS256 with a 30-minute expiry, BCrypt, the role as a claim, stateless, and the filter placed before UsernamePasswordAuthenticationFilter.

**Review question:** Explain what each line of SecurityConfig does.

**What Claude gave me:** JwtService, JwtAuthFilter, SecurityConfig (stateless, CSRF off, CORS for localhost:4200, a 401 entry point, method security), the BCrypt encoder, `POST /api/auth/login`, and the line-by-line explanation. In the code the roles are ADMIN and USER; the seeded read-only account is named viewer.

**What I changed and why:** Accepted as generated. I asked for the walkthrough. One line in it turned out to be incomplete; see Entry 9.

**How I checked:** Logged in as admin and as viewer, tried a wrong password, and sent requests with and without a token.

**What I learned:** What each part of the security chain does, and why the JWT filter has to run before UsernamePasswordAuthenticationFilter.

### Entry 5: Employee API

**Requirements I gave:** Paging with a maximum page size of 50, sorting limited to these fields: firstName, lastName, email, department, jobTitle, hireDate, status, 409 on a duplicate email, and one error format: `{message, errors}`.

**What Claude gave me:** EmployeeController with `@PreAuthorize` ADMIN on writes, EmployeeService with the sort whitelist and page cap, DTO records, a single exception handler, and a seeder with 2 accounts and 13 employees.

**What I changed and why:** The page cap and the sort whitelist were my requirements. The sort field comes from the client, so unchecked it's an error risk.

**How I checked:** Called every endpoint as admin and as viewer; checked paging, sorting and the 409.

### Entry 6: Login, interceptor and guard

**Source:** generated by the Entry 2 prompt. I reviewed it against UC-LOGIN-01, UC-LOGIN-02 and UC-LOGOUT-02.

**What Claude gave me:** a standalone login component with a reactive form (username at least 3 characters, password at least 6, an error message for each rule, submit disabled while the form is invalid or a request is in flight, and a "Remember my username" checkbox); AuthService (session in localStorage, token expiry read from the JWT, remembered username); authInterceptor, which attaches the token to API calls and logs out on a 401 except on the login call itself; and authGuard, which also rejects an expired token. All protected routes are lazy loaded under one guarded shell.

**What I changed and why:** Accepted as generated. I checked the validators against UC-LOGIN-02 and the guard against UC-LOGOUT-02.

**How I checked:** Validation messages, login, logout, an expired token, and typing a protected URL with no session.

### Entry 7: Employee screens

**Source:** generated by the Entry 2 prompt. I reviewed it against UC-TXN-01 to UC-TXN-06.

**What Claude gave me:** EmployeesComponent (Material table with server-side paging and sorting, search debounced at 300 ms with a clear button, clicking a row opens the edit dialog for admins, and the actions column and add button shown only to admins); EmployeeDialogComponent (email and phone validation, no future hire dates, server 400 and 409 errors mapped onto the matching field); ConfirmDialogComponent; and snackbar notifications for add, edit, delete and load errors.

**What I changed and why:** Accepted as generated. EmployeesComponent is larger than I'd like; I chose not to split it (see Entry 15).

**How I checked:** Paging, sorting, search, clearing the search, add, edit, delete, a duplicate email, and confirmed a viewer sees no write buttons and can't open the edit dialog.

**What I learned:** Hiding buttons is only cosmetic. The server-side role check is what protects the data.

### Entry 8: Bug in Claude's duplicate-email check

**Prompt:** Adding ' Ada@X.com' with a leading space got past the duplicate check, but the database stored it trimmed. Why, and what's the cleanest fix?

**What was wrong:** Claude's EmployeeService checked for duplicates using the raw input but saved a trimmed, lowercased value, so the check and the database were looking at different strings.

**What Claude gave me:** One `normalise()` method used by both the check and the save, plus a unit test.

**What I changed and why:** I kept the database unique constraint `uk_employee_email` as the final guard. The service check gives a friendly message, but only the constraint stops two simultaneous requests.

**What I learned:** Validate exactly the value you store.

### Entry 9: Bug in Claude's SecurityConfig: viewer got 401 instead of 403

**How I found it:** I called `DELETE /api/employees/1` from PowerShell with the viewer token and expected 403. I got 401. I turned on the security debug log and saw the 403 being forwarded to /error and then rejected as unauthenticated.

**Prompt:** The debug log shows my 403 gets forwarded to /error and then rejected as unauthenticated. Why does /error need authentication, and what's the minimal fix?

**What Claude gave me:** The cause (Spring Security also runs on the internal /error dispatch, and the generated config didn't permit it) and the fix: `.requestMatchers("/error").permitAll()`. It also pointed out that MockMvc doesn't perform that forward, so a MockMvc test can't catch this. A full server test (`@SpringBootTest` with a random port) would.

**Why it mattered:** The interceptor from Entry 6 logs out on any 401. A viewer clicking a forbidden action would have been signed out.

**How I checked:** Retested by hand: the viewer token now gives 403, and no token gives 401.

**What I learned:** 401 means "who are you?" and 403 means "I know you, and no." Also, tests passing didn't mean the security was right.

### Entry 10: Report summary and filters (UC-RPT-04 part 1, UC-RPT-05)

**Prompt:** Now add the report module step by step. First the summary: an endpoint with from, to and status filters, default to the current month, 400 if from is after to, COUNT and GROUP BY by status and by department, and an Angular report page with date pickers, a status filter and summary cards.

**What Claude gave me:** ReportController, ReportService with a `resolve()` filter method, the two GROUP BY queries, zero-filled status counts so no card disappears, the 400 check, and the Angular page with This month, This year and All time presets. It built date strings from the local year, month and day instead of `toISOString()`.

**Follow-up I asked:** "Why did you bind status as a set (`status in :statuses`) instead of a nullable parameter?" Answer: Hibernate handles a null parameter in that pattern unreliably; a set of all statuses means "no filter" without any null.

**What I changed and why:** Accepted as generated.

**How I checked:** Cards, presets, and the from-after-to error in the browser.

**Problem I found:** the seed data has no hires in the current month, so the default view is empty. Fixed in Entry 17.

**What I learned:** `toISOString()` converts to UTC, which shifts a date back a day in India time. Date-only values should never go through it without removing the timezone offset first, which is what the employee dialog's `todayIso()` does.

### Entry 11: Report table (UC-RPT-01)

**Prompt:** Next the report table. Same filters, server-side paging and sorting with a sort whitelist, a page size cap, and a read-only Material table. The summary, the table and the exports should all use the same filter logic.

**What Claude gave me:** the paged, sorted report query and endpoint, and a read-only Material table with sortable headers and a paginator. It reused the employee sort whitelist (making it package-visible) and the page cap of 50, put all filter validation in one `resolve()` method shared by the summary, the table and the exports, and added id as a second sort key without being asked.

**What I changed and why:** Accepted as generated. I asked why the extra id sort was there: rows with equal sort values have no guaranteed order, so the database can return them differently on each page query, and a row can appear on two pages or on none. A unique tie-breaker fixes the order.

**How I checked:** Sorted every column, paged through, and confirmed that changing a filter goes back to page 1.

**What I learned:** Stable paging needs a unique tie-breaker in the sort, and sharing one filter method keeps the summary cards, the table and the exports from disagreeing.

### Entry 12: Excel export (UC-RPT-02)

**Prompt:** Add Excel export with Apache POI. All filtered rows, not just the current page, proper headers, real date cells and a frozen header. The download button has to send the JWT.

**What Claude gave me:** ExcelReportBuilder with a title, the filters used, a bold frozen header with filter arrows, real date cells, fixed column widths (auto-sizing needs fonts a headless server may not have) and a 10,000 row cap. The download goes through HttpClient as a blob.

**What I changed and why:** Accepted as generated. I chose backend export with POI over SheetJS in the browser because the filters already run on the server, so the file always matches the report, and the browser never needs every row.

**How I checked:** Exported with different filters and opened each file in Excel: right rows, frozen header, and dates sorting as dates.

**What I learned:** A plain download link sends no token and gets 401. Protected downloads have to go through HttpClient so the interceptor adds the JWT.

### Entry 13: PDF export (UC-RPT-03)

**Prompt:** Add PDF export with the same rows as Excel: title, date, the filters used, a table header that repeats on every page, and a page footer.

**What Claude gave me:** PdfReportBuilder using OpenPDF: landscape A4, title, generated date, the filters used, a header row repeated on every page, and a page number footer. One shared `exportFile('xlsx' | 'pdf')` method in the component handles both downloads.

**What I changed and why:** Accepted as generated. I checked the library choice: iText 7 is AGPL or commercial, which would put licence obligations on the whole application, while OpenPDF is LGPL/MPL, so it was the right pick.

**How I checked:** Opened multi-page PDFs and checked the title, filters, and the header and footer on every page.

**What I learned:** The built-in PDF font is Latin only, so non-Latin names won't render. Listed in the README limitations.

### Entry 14: Automated tests

**Prompt:** Write tests for the report service and the Excel and PDF builders, plus MockMvc tests with the real security chain for 400, 401, 403, 404 and 409.

**What Claude gave me:** 25 tests: ReportServiceTest (7; 1 department and 2 monthly tests were added to the same file later, 10 in total), ReportBuildersTest (5, reading the generated Excel and PDF files back) and ApiSecurityTest (13, through the real security chain with real JWTs).

**What I did:** Ran `mvn test` on the full suite. Claude couldn't run Maven, so every test count in this log comes from my own runs.

**Test count:** 8 baseline + 1 from Entry 8 + 25 here + 4 department + 2 monthly = 40.

**What I learned:** Tests with a mocked service don't prove the security rules. Only tests through the real filter chain do. Even those have limits: MockMvc doesn't perform the /error dispatch, so the 401 vs 403 bug from Entry 9 can't be caught by ApiSecurityTest, and I tested that case by hand.

### Entry 15: Review and refactors

**Prompt:** Here's my refactor checklist: seed phone numbers built from employees.count(), the 409 always reporting email, the idle timer living in ShellComponent, EmployeesComponent too big, all DTOs in one file. Do you agree, which matter most, and apply the ones worth doing now.

**What Claude gave me:** Agreement on all five, with the idle timer as the most important because it holds global event listeners. Then the code for three of them.

**What I changed and why:** I chose to apply three small changes and skip two large ones:

- Applied: explicit seed phone numbers (the old expression depended on `employees.count()`)
- Applied: the 409 now gets its field from the service (`Conflict(field, message)`) instead of the exception handler hardcoding email. Email is still the only unique employee field, so it still reports email.
- Applied: idle logic moved into IdleService, with activity resets throttled to once a second
- Skipped: splitting EmployeesComponent and splitting the DTO file. Both touch many files, and I didn't want a large refactor late in the project.

**How I checked:** A fresh database seed, a duplicate email 409 in the dialog, and the idle warning and sign-out.

**Review against the brief:** Checking the report against UC-RPT-04, I found the date-based aggregation was missing. Claude had flagged it in Entry 1 but left it out of the report build in Entry 10. I asked Claude to add it: a hires-per-month query using the same shared filters, oldest month first, limited to the latest 24 months, a 'Hires by month' section on the report page, and 2 tests.

### Entry 16: Documentation

**Prompt:** Update the README and write a build guide so everything matches the current code.

**What Claude gave me:** the README (setup, API table, ER diagram, use case coverage, limitations) and BUILD_GUIDE (prerequisites, build and run, common problems).

**What I changed and why:** Checked both against the code and against a clean run on my machine, and corrected what didn't match: the department count and MySQL steps, Node 22 LTS instead of the latest Node, the Windows ARM setup problems I hit while installing the toolchain, and the use case status. I also removed docker-compose.yml, because it was never used or tested, and an untested setup path is worse than none.

**How I checked:** Followed BUILD_GUIDE from scratch on my machine and compared test counts, endpoints and table names with the code.

### Entry 17: Fixing three issues found in review

**Prompt:** The default report month is empty because seed hire dates are fixed in the past, the employee list has no id tie-breaker in its sort, and active users are signed out 30 minutes after login (UC-LOGIN-04). Give me the smallest safe fix for each.

**What Claude gave me:** Three seed hire dates made relative to today (one last month, two this month); id added as a second sort key in EmployeeService, matching the report; and an 8 hour token as an upper limit, with last activity stored in localStorage so the 30 minute idle rule also holds after the tab is closed.

**What I changed and why:** Accepted the fixes. Adding the tie-breaker broke 2 existing tests that checked the exact sort, so I updated them to expect id as the second key. I also updated the README and BUILD_GUIDE, which still described the old behaviour.

**How I checked:** Ran `mvn test` (40 passing), reset the database, saw 2 hires this month on the report, paged through the employee list sorted by department with no repeated rows, set last activity 31 minutes back in DevTools and reloaded and landed on the login page, then logged in again and stayed in.

## 4. Known limitations

| Gap | Requirement | Why it's open | How I'd fix it |
|---|---|---|---|
| EmployeesComponent too large; all DTOs in one file | Code quality | Skipped in Entry 15 as too large a change late on | Split the table into its own component; split DTOs by feature |
| Search with a leading wildcard can't use an index | Performance | Fine at this data size | FULLTEXT index or prefix search |

## 5. Reflection

**Where Claude helped most:** It wrote a large, consistent codebase quickly, and it explained framework behaviour I hadn't seen before, like Spring Security running on the /error dispatch and Hibernate's trouble with null collection parameters.

**Where it fell short:** Two of the bugs I found were in its own code: the duplicate-email check and the 401/403 config. It was also inconsistent: it added an id tie-breaker to the report table but not to the employee list, which can repeat rows across pages. Fixed in Entry 17. It couldn't run Maven or MySQL, so none of its code was proven until I ran it. And it flagged the UC-RPT-04 date aggregation on day one, then left it out of the first report build; the hires-per-month aggregation was only added near the end.

**What I'd do differently:** Check the brief line by line before building, not at the end. Design the token lifetime and the idle timeout together. Use smaller prompts from the start instead of one prompt for the whole baseline, and ask Claude to review each step against the requirements before moving on.
