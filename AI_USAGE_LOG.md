# AI Usage Log

**Tool used:** Claude (claude.ai chat). No other AI tool was used.

**Rule I follow (from the recruiter):** AI is used for template and boilerplate code and for checking small things. I review it, test it, improve it and can explain every part. For every use I record **where**, **why I used AI instead of doing it directly**, and **what result it achieved**.

## 1. Summary

| # | Where | Why AI instead of doing it myself | Result AI achieved | What I did / changed |
|---|---|---|---|---|
| 1 | Initial template of the whole project: Spring Boot backend (entities, repositories, DTOs, services, controllers, JWT security, seeder, 9 unit tests) and Angular frontend (config, routes, login, shell, employee table, dialogs) | Long, mechanical boilerplate (Maven and Angular config, Material templates, security wiring). Writing it by hand adds little value and I wanted a working baseline to study and improve | A complete working baseline that compiled and ran on first start | Installed the toolchain, ran it, tested every feature end to end, reviewed the code against the walkthrough, and am refactoring (see section 3) |
| 2 | Checking and fixing the code: duplicate-email check compared the raw email while the stored value is trimmed and lowercased | Quick review of the service for logic mistakes | Found the mismatch and fixed it with one shared `normalise()` method plus an updated test | Read the fix and can explain why the check and the stored value must use the same normalisation |
| 3 | Diagnosing the 401 vs 403 result in my own test (a viewer's forbidden DELETE returned 401) | I found the symptom myself with a PowerShell test, but did not know the Spring cause | Explained that the internal `/error` dispatch was not permitted, and gave the one-line fix | I ran the test and found the issue. I apply and re-test the fix myself (see section 3) |
| 4 | Environment setup on Windows 11: installing Java 17, Maven, Node and MySQL Server; fixing PATH problems, a missing MySQL server, an old MySQL data folder, and `curl` not being found | Unfamiliar installer and PATH errors; faster than searching forums | Got the whole toolchain working (Java 17, Maven 3.10.2, Node 20, MySQL 8.0.46) | I installed and configured everything on my laptop and ran each command |
| 5 | Documentation drafts: README, walkthrough notes, interview slide deck, refactor checklist, this log's structure | Documentation scaffolding and a presentation outline | Drafts covering setup, API, architecture, demo script and likely questions | I review and edit them so they match what I actually did, and rehearse from them |
| 6 | Limited static checks (Java and TypeScript syntax) in the AI's own environment | The AI could not run a full build there | Confirmed there were no syntax errors; it did not replace a real build | I did the real build and run on my machine |
| 7 | Report module, backend: summary endpoint (COUNT/GROUP BY), filtered paged table, Excel export (Apache POI), PDF export (OpenPDF), `from <= to` validation | The assessment requires it and I asked the AI to build it step by step in chat | Working code for UC-RPT-01 to 05 on the server side; the Angular build passes; the Java code was not compiled in the AI's environment | I must review it, run it, and be able to explain every class (see Entry 6) |
| 8 | Report module, frontend: report page with date range, status filter, presets, summary cards, sortable paged table, two export buttons, navigation links | Same as above | The Angular app compiles; behaviour still has to be checked in the browser | Same as above |
| 9 | 24 new tests: `ReportServiceTest`, `ReportBuildersTest`, `ApiSecurityTest` (MockMvc for 400, 401, 403, 404, 409) | Test scaffolding is repetitive; I wanted the checklist item covered | Tests written but not executed in the AI's environment | I run `mvn test` and fix anything that fails |
| 10 | Improvements: `DataSeeder` explicit phone numbers, 409 handler reports the real field, `IdleService` extracted from `ShellComponent`, `/error` permitted in `SecurityConfig` | Items from my own refactor checklist and the 401 vs 403 finding | The code changes are applied; frontend compiles | I re-test the 403 fix by hand and review each change |
| 11 | Updating README, this log, the walkthrough and the refactor checklist after the report module | Keeping the docs in step with the code | Documents that match the current code | I read them against the code before submitting |

## 2. Detailed entries

### Entry 1: Initial template of the application
- **Where I used it:** the whole project skeleton under `backend/` and `frontend/`.
- **Prompt (summary):** I shared the assessment document and the recruiter email and asked for a small full-stack app (Angular 19, Spring Boot, MySQL) with login, logout and one CRUD screen, with a good-looking UI.
- **Why AI instead of writing it directly:** the Maven and Angular setup, entity and DTO classes, JWT wiring and Material templates are long and repetitive. I wanted a working baseline in less time so I could spend my effort on understanding, testing and improving it.
- **What AI produced:** the backend and frontend source, 9 unit tests, a seeder with demo data, and the first README.
- **What result it achieved:** a baseline that builds and runs, with login, paging, sorting, search, add, edit and delete working.
- **What I did afterwards:** set up the machine, ran the app, tested the login, search, sort, paging, add, edit and delete flows, and checked the API with and without a token.
- **What I learned:** how the request travels from the Angular form to the JWT filter, the controller, the service, the repository and MySQL.

### Entry 2: Duplicate-email logic review
- **Where:** `EmployeeService` (create and update).
- **Why AI:** a fast second look at the business rules.
- **Result:** the duplicate check used the raw email, while the saved email is trimmed and lowercased. Both now use one `normalise()` method, and the test was updated.
- **What I learned:** a validation check must use exactly the same normalised value that gets stored, otherwise duplicates can slip through.

### Entry 3: 401 vs 403 on forbidden actions
- **Where:** `SecurityConfig`.
- **How it was found:** my own test. I logged in as `viewer` and sent a DELETE with the viewer token. It returned 401, but a forbidden action should return 403.
- **Why AI:** I did not know why Spring returned 401.
- **Result:** the explanation is that Spring forwards the 403 to the internal `/error` path, which my config required authentication for. The fix is `.requestMatchers("/error").permitAll()`.
- **What I learned:** the difference between 401 (not authenticated) and 403 (authenticated but not allowed), and why the frontend interceptor must not treat a 403 as an expired session.

### Entry 4: Environment setup
- **Where:** my Windows 11 laptop.
- **Why AI:** I hit several setup errors (PATH not updated, MySQL Server missing while Workbench was installed, an old MySQL data folder asking for a root password, `curl` not found).
- **Result:** each was diagnosed from my screenshots and fixed with exact commands.
- **What I learned:** how PATH and `JAVA_HOME` work, what the MySQL service needs, and how to verify each tool with a version command.

### Entry 5: Documentation and presentation drafts
- **Where:** `README.md`, `WALKTHROUGH.md`, the interview slide deck, `REFACTOR_CHECKLIST.md`.
- **Why AI:** drafting structure and wording is quicker than starting from a blank page.
- **Result:** setup steps, API table, architecture explanation, demo order and interview questions.
- **What I do:** read them against the real code, correct anything that is not accurate, and rehearse in my own words.

### Entry 6: Report module (backend and frontend), tests and improvements
- **Where I used it:** `ReportController`, `ReportService`, `ExcelReportBuilder`, `PdfReportBuilder`, the new repository queries, `features/report`, `core/report.service.ts`, `core/download.ts`, navigation links, the tests listed in row 9, and the improvements in row 10.
- **Prompt (summary):** I shared the assessment document, my project zip and the list of missing items, then asked the AI to build the missing parts one step at a time: the 403 fix, the summary cards, the report table, Excel export, PDF export, tests, improvements, docs.
- **Why AI instead of writing it directly:** the report module was the largest missing part and the deadline is short. This is a larger share of AI-written code than the recruiter's "template and boilerplate" guideline suggests, so I am stating it plainly here and I will say it in the interview.
- **What AI produced:** the code and tests above. One shared filter rule (`ReportService.resolve`) feeds the summary, the table and both exports so they always agree. Dates are formatted locally in Angular to avoid a one-day shift in IST. Exports use HttpClient blobs so the JWT interceptor runs. Exports are capped at 10,000 rows.
- **What result it achieved:** the Angular app builds. The Java code and the tests have not been compiled or run by the AI (no Maven access in its environment).
- **What I reviewed and changed:** (fill in after you review: for example, what you read, what you ran, what you changed or fixed.)
- **What I learned:** (write one or two sentences of your own, for example how one filter object keeps the table, summary and exports consistent, or why a blob download is needed with JWT.)

## 3. My own work: done and planned
Only tick an item when it is really finished and I have checked it myself. Update this section before submitting.

**Done by me**
- [x] Installed Java, Maven, Node and MySQL Server and got the app running locally
- [x] Tested login, wrong password, search, sort, paging, add, edit and delete
- [x] Tested the API with and without a token and found the 401 vs 403 problem

**Code applied (AI-drafted). Tick each one only after I have run it and read the code**
- [ ] `/error` fix in `SecurityConfig`: viewer token on DELETE gives 403, no token gives 401 (re-test by hand)
- [ ] `mvn test` passes with 34 tests (adds `ReportServiceTest`, `ReportBuildersTest`, `ApiSecurityTest`)
- [ ] Report page works in the browser: cards, table, sorting, paging, date range, status filter
- [ ] Excel export opens in Excel with the right rows, a frozen header and real date cells
- [ ] PDF export opens with the title, filters, table and page footer
- [ ] `DataSeeder` cleanup (explicit phone numbers): fresh database seeds correctly
- [ ] 409 handler reports the real conflicting field
- [ ] `IdleService` extracted: warning and sign-out still work (test with short timeouts)

**Still to do (optional, only if I do them myself)**
- [ ] Move schema creation to a Flyway migration instead of `ddl-auto=update`
- [ ] Narrow or index the search query (leading-wildcard `LIKE` cannot use an index)
- [ ] Split the large `EmployeesComponent`

## 4. How I would describe it in the interview
"AI generated the initial template for both applications, and later the report module (summary, table, Excel and PDF export), the extra tests and a few improvements, which I asked for step by step. I set up the environment, ran and tested the first version, and found that forbidden requests returned 401 instead of 403. I reviewed the generated code, ran the tests and the app, and I can explain each part. Every use of AI is in this log, with why I used it and what it achieved."

(Only say "I reviewed, ran and tested" after you really have. Fill in Entry 6 first.)

## 5. Template for new entries
### Entry N: short title
- **Where I used it:**
- **Prompt (summary):**
- **Why AI instead of doing it directly:**
- **What AI produced:**
- **What result it achieved:**
- **What I reviewed and changed:**
- **What I learned:**
