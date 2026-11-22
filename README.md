# Roster - Employee Management (Angular 19 + Spring Boot 3 + MySQL)

Small full-stack app for the Application Developer skill assessment.
**Included:** login / logout (JWT), one transaction module (Employee CRUD).
**Next:** Report module (filters, Excel/PDF export, summary cards).

## Stack
| Layer | Tech |
|---|---|
| Frontend | Angular 19 (standalone components, signals), Reactive Forms, Angular Material |
| Backend | Spring Boot 3.3, Spring Security (JWT), Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Tests | JUnit 5 + Mockito |

## Prerequisites
Java 17+, Maven 3.9+, Node 20+ (npm), MySQL 8 (or Docker).

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
| admin | Admin@123 | ADMIN | view, add, edit, delete |
| viewer | Viewer@123 | USER | view only (action buttons hidden, API returns 403) |

## API
All endpoints except login need `Authorization: Bearer <token>`.

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | /api/auth/login | public | `{username,password}` -> `{token,username,fullName,role,expiresInSeconds}` |
| GET | /api/employees | any | query: `q`, `page` (0-based), `size` (max 50), `sort`, `dir` (asc/desc) |
| GET | /api/employees/{id} | any | |
| POST | /api/employees | ADMIN | 201; 400 validation; 409 duplicate email |
| PUT | /api/employees/{id} | ADMIN | 404 unknown id; 409 duplicate email |
| DELETE | /api/employees/{id} | ADMIN | 204 |

Sortable fields: firstName, lastName, email, department, jobTitle, hireDate, status (anything else falls back to lastName).
Error shape: `{ "message": "...", "errors": { "field": "message" } }`.

## Use-case coverage
| ID | Status | Where |
|---|---|---|
| UC-LOGIN-01/02 | Done | `features/login`, `AuthController`, `JwtService` |
| UC-LOGIN-03 | Partial | username remembered; token refresh not built |
| UC-LOGIN-04 | Done | idle timer in `features/shell` (warning at 28 min, logout at 30) |
| UC-TXN-01..06 | Done | `features/employees`, `EmployeeService`; search debounced 300 ms |
| UC-LOGOUT-01/02 | Done | `AuthService.logout`, `authGuard`, interceptor 401 handling |
| UC-RPT-01..05 | Not yet | next iteration |

## Security notes
BCrypt password hashes; stateless JWT (HS256, 30 min); route guard + HTTP interceptor;
`@PreAuthorize` role checks; Bean Validation on every request body; parameterised JPQL and a sort-field whitelist
(no string-built SQL); CORS limited to http://localhost:4200.
Known trade-off: the JWT is kept in localStorage as the assessment specifies, which is readable by any XSS.
