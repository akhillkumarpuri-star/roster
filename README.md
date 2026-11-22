# Roster - Employee Management (Angular 19 + Spring Boot 3 + MySQL)

Small full-stack app for the Application Developer skill assessment.
**Included:** login / logout (JWT), one transaction module (Employee CRUD), one report module
(filters, summary cards, paginated table, Excel and PDF export).

## Stack
| Layer | Tech |
|---|---|
| Frontend | Angular 19 (standalone components, signals), Reactive Forms, Angular Material |
| Backend | Spring Boot 3.3, Spring Security (JWT), Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Exports | Apache POI 5.3 (Excel), OpenPDF 2.0 (PDF) |
| Tests | JUnit 5, Mockito, Spring MockMvc (34 tests) |

## Prerequisites
Java 17+, Maven 3.9+, Node 20+ (npm), MySQL 8 (or Docker). The first backend start downloads the Maven dependencies, so it needs internet.

## Run it
```bash
# 1. Database (skip if you already run MySQL with root/root)
docker compose up -d

# 2. Backend  ->  http://localhost:8080
cd backend
mvn spring-boot:run            # tests: mvn test

# 3. Frontend  ->  http://localhost:4200
cd frontend
npm install
npm start
```
Different MySQL credentials? `DB_USER=... DB_PASSWORD=... mvn spring-boot:run`.
Always set a real `JWT_SECRET` (32+ chars) outside local development.

## Demo accounts (seeded on first start)
| Username | Password | Role | Can do |
|---|---|---|---|
| admin | Admin@123 | ADMIN | view, add, edit, delete, reports |
| viewer | Viewer@123 | USER | view and reports only (action buttons hidden, write API calls return 403) |

## API
All endpoints except login need `Authorization: Bearer <token>`.

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | /api/auth/login | public | `{username,password}` -> `{token,username,fullName,role,expiresInSeconds}` |
| GET | /api/employees | any | query: `q`, `page` (0-based), `size` (max 50), `sort`, `dir` (asc/desc) |
| GET | /api/employees/{id} | any | 404 if unknown |
| POST | /api/employees | ADMIN | 201; 400 validation; 409 duplicate email |
| PUT | /api/employees/{id} | ADMIN | 404 unknown id; 409 duplicate email |
| DELETE | /api/employees/{id} | ADMIN | 204 |
| GET | /api/reports/employees | any | report table by hire date. query: `from`, `to` (yyyy-MM-dd, default current month), `status`, `page`, `size` (max 50), `sort`, `dir` (default hireDate desc). 400 if `from` > `to` |
| GET | /api/reports/employees/summary | any | same filters; returns `{from,to,total,byStatus,byDepartment}` |
| GET | /api/reports/employees/export.xlsx | any | same filters; every matching row (max 10,000) as an Excel file |
| GET | /api/reports/employees/export.pdf | any | same filters; formatted PDF with title, filters, date and page footer |

Sortable fields: firstName, lastName, email, department, jobTitle, hireDate, status (anything else falls back to the default).
Error shape: `{ "message": "...", "errors": { "field": "message" } }`.

## Use-case coverage
| ID | Status | Where |
|---|---|---|
| UC-LOGIN-01/02 | Done | `features/login`, `AuthController`, `JwtService` |
| UC-LOGIN-03 | Partial | username remembered; token refresh not built |
| UC-LOGIN-04 | Done | `core/idle.service.ts` (warning at 28 min, logout at 30) |
| UC-TXN-01..06 | Done | `features/employees`, `EmployeeService`; search debounced 300 ms |
| UC-RPT-01 | Done | `features/report` table, `ReportService.list` (server-side paging and sorting, date and status filters) |
| UC-RPT-02 | Done | `ExcelReportBuilder`, `GET /api/reports/employees/export.xlsx` |
| UC-RPT-03 | Done | `PdfReportBuilder`, `GET /api/reports/employees/export.pdf` (title, date, table, page footer) |
| UC-RPT-04 | Done | summary cards: total, per status, per department (SQL COUNT + GROUP BY) |
| UC-RPT-05 | Done | date-range picker, default current month, presets, server rejects from > to |
| UC-LOGOUT-01/02 | Done | `AuthService.logout`, `authGuard`, interceptor 401 handling |

## Tests
`cd backend && mvn test` runs 34 tests: employee and report service rules, JWT, the Excel and PDF builders,
and MockMvc tests of the real security chain (400, 401, 403, 404, 409).

## Security notes
BCrypt password hashes; stateless JWT (HS256, 30 min); route guard + HTTP interceptor;
`@PreAuthorize` role checks; Bean Validation on every request body; parameterised JPQL and a sort-field whitelist
(no string-built SQL); export size capped; CORS limited to http://localhost:4200.
Known trade-off: the JWT is kept in localStorage as the assessment specifies, which is readable by any XSS.

## Known limitations
- The PDF uses a built-in Latin font, so non-Latin names (for example Devanagari) would not render; embed a Unicode font to fix.
- Exports stop at 10,000 rows and say so in the file.
- Schema is created by Hibernate (`ddl-auto=update`); production should use Flyway migrations.
- Search uses a leading-wildcard `LIKE`, which cannot use an index on very large tables.
- No refresh token; users are signed out after 30 minutes.
