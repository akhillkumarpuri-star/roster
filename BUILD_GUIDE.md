# Roster: build, run and verify guide

How to build the project from a clean machine, check that it works, and fix the common problems.
For the API, the database design and the use-case coverage, see `README.md`.

## 1. What you need
| Tool | Version | Check |
|---|---|---|
| Java JDK | 17 or newer | `java -version` |
| Maven | 3.9 or newer | `mvn -v` (must show Java 17+) |
| Node.js and npm | Node 22 LTS (Angular 19 does not support Node 24 or newer) | `node -v`, `npm -v` |
| MySQL Server | 8.x, running on port 3306 | `mysql --version`, and the MySQL service is started |

Default database login is `root` / `root`. If yours differs, set `DB_USER` and `DB_PASSWORD` (section 3).
The first Maven build and the first `npm install` need internet.

## 2. Project layout
```
roster/
  backend/    Spring Boot 3.3 API (Java 17, Maven)
    src/main/java/com/example/roster/
      config/    SecurityConfig
      security/  JwtService, JwtAuthFilter
      domain/    entities (Employee, Department, AppUser) and the status enum
      repo/      Spring Data repositories (search, report queries)
      service/   EmployeeService, ReportService, ExcelReportBuilder, PdfReportBuilder
      web/       controllers, DTOs, exception handling
      seed/      DataSeeder (demo users and employees)
    src/main/resources/db/migration/   V1__create_schema.sql (the database schema)
    src/test/    40 tests
  frontend/   Angular 19 app
    src/app/core/      auth, interceptor, guard, API services, idle service, download helper
    src/app/features/  login, shell, employees, report
```

## 3. Build and run
**Database.** Install MySQL 8 and make sure the MySQL service is running on port 3306. On first start the backend creates the `roster_db` database and Flyway runs `backend/src/main/resources/db/migration/V1__create_schema.sql` (tables, foreign key, constraints, indexes). **If you ran an earlier version of this project, run `DROP DATABASE roster_db;` first**, because that older database was created by Hibernate and Flyway will not use it.

**Backend** (terminal 1):
```
cd backend
mvn test                  # 40 tests should pass
mvn spring-boot:run       # wait for "Started RosterApplication"
```
Other MySQL login: `DB_USER=myuser DB_PASSWORD=mypass mvn spring-boot:run`
(PowerShell: `$env:DB_USER="myuser"; $env:DB_PASSWORD="mypass"; mvn spring-boot:run`).

**Frontend** (terminal 2):
```
cd frontend
npm install
npm start                 # http://localhost:4200
```

**Production-style build (optional)**
```
cd backend  && mvn -DskipTests package     # target/roster-api-1.0.0.jar
cd frontend && npm run build               # dist/roster-web (needs internet to inline the Google font)
```
Run the jar with real secrets: `JWT_SECRET` (32+ characters), `DB_USER`, `DB_PASSWORD`.

## 4. Check that everything works
1. Open http://localhost:4200 and log in as `admin` / `Admin@123`. A wrong password shows a red message.
2. Employees: 13 rows over 2 pages (6 departments are created from the seed data). Search "eng", sort a column, add, edit and delete an employee. Adding a duplicate email shows an error on the Email field.
3. Report: the default range is the current month, so it may be empty. Click **All time**: the cards, the department bars, the hires-by-month bars and the table fill in. Sort a column, change the status, pick a `from` date later than `to` to see the error.
4. Exports: **Export Excel** gives a sheet with a frozen header and real dates; **Export PDF** gives a title, filters, table and a page footer.
5. Sign out, log in as `viewer` / `Viewer@123`: no Add, Edit or Delete buttons, and Report still works.
6. Visit http://localhost:4200/employees while signed out: you are sent to the login page.

**API status codes (PowerShell).** A forbidden action must return 403 and a missing token 401:
```powershell
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login `
  -ContentType 'application/json' -Body '{"username":"viewer","password":"Viewer@123"}'
$h = @{ Authorization = "Bearer $($login.token)" }

# viewer deleting -> expect 403
try { Invoke-RestMethod -Method Delete -Uri http://localhost:8080/api/employees/1 -Headers $h }
catch { $_.Exception.Response.StatusCode.value__ }

# no token -> expect 401
try { Invoke-RestMethod -Uri http://localhost:8080/api/employees }
catch { $_.Exception.Response.StatusCode.value__ }
```

## 5. Common problems
| Problem | Likely cause and fix |
|---|---|
| `mvn` or `java` not found | PATH or `JAVA_HOME` not set. Open a new terminal after installing; set `JAVA_HOME` to the JDK folder. |
| Maven says the Java version is wrong | `mvn -v` must show Java 17 or newer. Point `JAVA_HOME` at JDK 17+. |
| `Access denied for user 'root'` | Wrong DB password. Set `DB_USER` and `DB_PASSWORD`. |
| `Communications link failure` | MySQL is not running on port 3306. Start the MySQL service (`services.msc`, service name `MySQL80`). |
| `npm : ... cannot be loaded because running scripts is disabled` | PowerShell blocks scripts by default on a new Windows install. Run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` once, then open a new terminal. |
| `mvn` is not recognized | Maven is not on PATH. Add the Maven `bin` folder (for example `C:\tools\apache-maven-3.10.2\bin`) to the user PATH, then open a new terminal. |
| Windows on ARM (for example Snapdragon laptops) | Download the ARM64 build of Node. MySQL has no Windows ARM build, so install the normal x64 MySQL; Windows runs it under emulation. An x64 JDK also works the same way. |
| Port 8080 or 4200 already in use | Stop the other program using it, or change `server.port` / `ng serve --port 4300` (if you change the frontend port, also change `app.cors.allowed-origin`). |
| Login works but the page shows CORS errors | The frontend must run on http://localhost:4200, or `app.cors.allowed-origin` must match. |
| Report page is empty | The default range is the current month and the seed hire dates are older. Click **All time**. |
| First Maven run is slow or fails | It downloads dependencies (Spring, POI, OpenPDF). Check the internet connection and retry. |
| `npm run build` fails fetching fonts | The production build inlines the Google font and needs internet. `npm start` does not. |
| `Found non-empty schema(s) ... without schema history table` | The database was created by an older version (Hibernate). Run `DROP DATABASE roster_db;` and restart the backend. |
| Seed data looks old after code changes | The seeder only runs on empty tables. Run `DROP DATABASE roster_db;` in MySQL and restart the backend. |
| `Flyway ... newer than this version of Flyway` warning | A warning only; the migration still runs. |
| Everything signs out after 30 minutes | By design: the token lasts 30 minutes and the idle timer signs out after 30 minutes without activity. |

## 6. How the project was built (order)
1. Template: backend (entities, repositories, DTOs, services, controllers, JWT security, seeder, tests) and frontend (login, shell, employees, dialogs).
   The duplicate-email check was corrected here, before the first commit.
2. Fix: allow `/error` in `SecurityConfig` so a forbidden action returns 403, not 401.
3. Report: summary endpoint and cards, then the filtered paged table, then Excel export, then PDF export.
4. Tests: report service, Excel and PDF builders, and MockMvc tests for 400, 401, 403, 404 and 409.
5. Improvements: seeder cleanup, the 409 handler reports the real field, `IdleService` extracted.
6. Review against the assessment document, which found the missing foreign key and the missing date-based aggregation.
7. Database design: a `departments` table with a foreign key from employees, and the schema as a Flyway SQL migration instead of `ddl-auto=update`.
8. Hires-per-month aggregation in the report summary.
9. Docs and AI usage log. Every AI use is recorded in `AI_USAGE_LOG.md`.
