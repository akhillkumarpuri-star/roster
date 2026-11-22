# Roster: Employee Management

A small full-stack application: **Angular 19** frontend, **Spring Boot 3** REST API, **MySQL 8** database, with **JWT** login and an employee CRUD screen.

## Contents
1. [What it does](#1-what-it-does)
2. [Tech stack](#2-tech-stack)
3. [Project structure](#3-project-structure)
4. [Install the tools (Windows 11)](#4-install-the-tools-windows-11)
5. [Get the project](#5-get-the-project)
6. [Run the application](#6-run-the-application)
7. [Demo accounts](#7-demo-accounts)
8. [Run the tests](#8-run-the-tests)
9. [API reference](#9-api-reference)
10. [Get a JWT token manually](#10-get-a-jwt-token-manually)
11. [Configuration](#11-configuration)
12. [Security notes](#12-security-notes)
13. [Use-case coverage](#13-use-case-coverage)
14. [Troubleshooting](#14-troubleshooting)
15. [AI usage](#15-ai-usage)

---

## 1. What it does
- **Login and logout** with JWT. Form validation, route guard, automatic sign-out when the token expires, 30-minute idle timeout with a warning.
- **Employee management** (the transaction screen): server-side paging (10 per page), sorting, search with a 300 ms debounce, add and edit in a dialog, delete with confirmation, toast messages, duplicate-email protection.
- **Two roles:** `ADMIN` can add, edit and delete. `USER` (viewer) is read-only, and the server enforces this, not just the UI.
- **Not built yet:** the Report module (filters, summary cards, Excel and PDF export).

## 2. Tech stack
| Layer | Technology |
|---|---|
| Frontend | Angular 19 (standalone components, signals), Reactive Forms, Angular Material |
| Backend | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA (Hibernate), jjwt 0.12 |
| Database | MySQL 8.0 |
| Tests | JUnit 5, Mockito |

## 3. Project structure
```
roster-fullstack/
  backend/                      Spring Boot API (Maven)
    pom.xml
    src/main/java/com/example/roster/
      config/SecurityConfig.java       security rules, CORS, password encoder
      security/JwtService.java         create and validate tokens
      security/JwtAuthFilter.java      reads the Bearer token on each request
      domain/                          JPA entities (AppUser, Employee, EmployeeStatus)
      repo/                            Spring Data repositories
      service/EmployeeService.java     business rules
      web/                             controllers, DTOs, error handling
      seed/DataSeeder.java             demo users and employees
    src/main/resources/application.properties
    src/test/java/...                  unit tests
  frontend/                     Angular app
    package.json, angular.json
    src/app/
      core/                            auth service, interceptor, guard, API client, models
      features/login/                  login page
      features/shell/                  toolbar and idle timer
      features/employees/              employee table, add/edit dialog, confirm dialog
  docker-compose.yml            optional MySQL in Docker
  AI_USAGE_LOG.md               where AI was used, why, and results
  REFACTOR_CHECKLIST.md         improvements to the AI draft
  WALKTHROUGH.md                demo and explanation notes
```

---

## 4. Install the tools (Windows 11)
You need four things: **Java 17**, **Maven**, **Node.js** and **MySQL Server**. Do them one at a time and verify each. After installing anything, **close every Command Prompt window and open a new one**, otherwise Windows will not see it.

Open **Command Prompt** (Start menu, type `cmd`).

### Step 1: Java 17 (JDK)
1. Check first: `java -version`. If it prints version 17 or higher, skip to Step 2.
2. Install with one command: `winget install EclipseAdoptium.Temurin.17.JDK`
   - No `winget`? Download **Temurin 17 (LTS), Windows x64, .msi** from https://adoptium.net and run it. On the "Custom Setup" screen, set **Add to PATH** and **Set JAVA_HOME variable** to "Will be installed on local hard drive".
3. Open a **new** Command Prompt and verify:
   ```
   java -version
   echo %JAVA_HOME%
   ```
   Expected: `openjdk version "17.0.x"`, and a path like `C:\Program Files\Eclipse Adoptium\jdk-17...\`.

### Step 2: Maven
1. Check first: `mvn -v`. If it prints Maven 3.9 or higher, skip to Step 3.
2. Download the **Binary zip archive** (`apache-maven-3.x.x-bin.zip`) from https://maven.apache.org/download.cgi
3. Create the folder `C:\tools` and extract the zip into it. You should end up with a folder like `C:\tools\apache-maven-3.10.2` (the numbers depend on the version you downloaded).
4. Confirm the exact folder name and that it contains `mvn.cmd`:
   ```
   dir C:\tools
   dir C:\tools\apache-maven-3.10.2\bin
   ```
   Replace `3.10.2` with the folder name you see. The second command must list `mvn.cmd`.
5. Add Maven to your PATH:
   1. Press the Windows key, type **Edit the system environment variables**, open it, click **Environment Variables**.
   2. Under **User variables**, select **Path**, click **Edit**, click **New**, and paste `C:\tools\apache-maven-3.10.2\bin` (your real folder name).
   3. Click **OK** on all three windows.
6. Open a **new** Command Prompt and verify:
   ```
   mvn -v
   ```
   Expected: `Apache Maven 3.x.x` and `Java version: 17...`.

### Step 3: Node.js 20 or newer
1. Check first: `node -v`. If it prints v20 or higher, skip to Step 4.
2. Install: `winget install OpenJS.NodeJS.LTS` (or download the LTS installer from https://nodejs.org).
3. Open a **new** Command Prompt and verify:
   ```
   node -v
   npm -v
   ```

### Step 4: MySQL Server 8.0
You need the **Server**, not only MySQL Workbench (Workbench is just a viewer and cannot run the database).

1. Download **MySQL Installer** from https://dev.mysql.com/downloads/installer/ and choose the larger file, `mysql-installer-community-8.0.xx.msi`. On the next page click **"No thanks, just start my download"**.
2. Run it and choose the setup type **Server only**, then **Next**.
3. On the installation page click **Execute** and wait for the green tick, then **Next**.
4. Keep the defaults on the configuration pages: **Standalone MySQL Server**, port **3306**, **Use Strong Password Encryption**.
5. On **Accounts and Roles**, set the **MySQL Root Password** to `root` (enter it twice). The project defaults to `root`/`root`.
   - If this page says **"Current Root Password"** instead, there is old MySQL data on the machine. See [Troubleshooting](#14-troubleshooting).
6. Keep the Windows Service defaults, click **Next**, then **Execute** until everything has a green tick, then **Finish**.
7. Verify the server files exist:
   ```
   dir "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe"
   ```
8. Verify the service is running: Start menu, type **Services**, find **MySQL80**, and check that Status says **Running**. If it is blank, right-click it and choose **Start**.
9. Test the login:
   ```
   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```
   Enter `root`. If you see the `mysql>` prompt, it works. Type `exit` to leave.

> **No MySQL installer? Use Docker instead.** Install Docker Desktop, then in the project folder run `docker compose up -d`. This starts MySQL 8 with `root`/`root` and creates `roster_db`.

### Step 5 (recommended): VS Code
Install VS Code from https://code.visualstudio.com and add the extensions **Extension Pack for Java**, **Spring Boot Extension Pack** and **Angular Language Service**.

---

## 5. Get the project
1. Unzip `roster-fullstack.zip`, for example to `C:\Users\<you>\Downloads\roster-fullstack`. Avoid OneDrive folders.
2. Check the layout:
   ```
   dir C:\Users\<you>\Downloads\roster-fullstack
   ```
   You should see `backend`, `frontend` and `README.md` directly inside (not another `roster-fullstack` folder).
3. Open the folder in VS Code (File, Open Folder). Open the terminal with **Ctrl + `**.

## 6. Run the application
You need **two terminals**: one for the backend, one for the frontend. MySQL must already be running (Section 4, Step 4).

### Step 1: Start the backend (terminal 1)
```
cd backend
mvn spring-boot:run
```
- The first run downloads dependencies and takes a few minutes.
- Wait for a line containing **`Started RosterApplication`**. Leave this terminal open.
- On first start it creates the database `roster_db`, its tables, two users and 13 sample employees automatically. You do not need to create anything in MySQL.
- If your MySQL root password is not `root`, set it first:
  - Command Prompt: `set DB_PASSWORD=your_password`
  - PowerShell: `$env:DB_PASSWORD="your_password"`

### Step 2: Start the frontend (terminal 2)
Open a second terminal (the **+** icon in VS Code, or a new Command Prompt) and run:
```
cd frontend
npm install
npm start
```
- `npm install` is needed only the first time.
- Wait until it says the app is available at `http://localhost:4200/`.

### Step 3: Open the app
Go to **http://localhost:4200** and sign in (see Section 7). You should see the employee list with 13 people.

### Step 4: Stop the app
Press **Ctrl + C** in each terminal. Next time you only need Step 1 and Step 2 (skip `npm install`).

---

## 7. Demo accounts
Created automatically on first start.

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `Admin@123` | ADMIN | view, add, edit, delete |
| `viewer` | `Viewer@123` | USER | view only (buttons hidden, API returns 403 for changes) |

## 8. Run the tests
```
cd backend
mvn test
```
Nine unit tests run: six for `EmployeeService` (duplicate email, not found, delete, sort whitelist, page cap, email normalising) and three for `JwtService` (round trip, wrong secret, expired token).

## 9. API reference
Base URL `http://localhost:8080`. Every endpoint except login needs the header `Authorization: Bearer <token>`.

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | `/api/auth/login` | public | body `{username, password}`; returns `{token, username, fullName, role, expiresInSeconds}` |
| GET | `/api/employees` | any signed-in user | query: `q`, `page` (starts at 0), `size` (max 50), `sort`, `dir` (`asc` or `desc`) |
| GET | `/api/employees/{id}` | any signed-in user | 404 if unknown |
| POST | `/api/employees` | ADMIN | 201 created, 400 validation error, 409 duplicate email |
| PUT | `/api/employees/{id}` | ADMIN | 404 unknown id, 409 duplicate email |
| DELETE | `/api/employees/{id}` | ADMIN | 204 no content |

Sortable fields: `firstName`, `lastName`, `email`, `department`, `jobTitle`, `hireDate`, `status`. Anything else falls back to `lastName`.

Employee body:
```json
{
  "firstName": "Test", "lastName": "User", "email": "test@example.com",
  "phone": "+91 98765 43210", "department": "Engineering", "jobTitle": "Engineer",
  "hireDate": "2024-01-15", "status": "ACTIVE"
}
```
`status` is one of `ACTIVE`, `ON_LEAVE`, `INACTIVE`. The hire date cannot be in the future.

Error shape: `{ "message": "...", "errors": { "fieldName": "message" } }`.

| Status | Meaning |
|---|---|
| 400 | Validation failed |
| 401 | Not signed in, or token invalid or expired |
| 403 | Signed in but not allowed (for example a viewer deleting) |
| 404 | Not found |
| 409 | Duplicate email |

## 10. Get a JWT token manually
**PowerShell** (works on every Windows 11 machine):
```powershell
$r = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -ContentType 'application/json' -Body '{"username":"admin","password":"Admin@123"}'
$r.token
Invoke-RestMethod -Uri "http://localhost:8080/api/employees?page=0&size=5" -Headers @{Authorization="Bearer $($r.token)"}
```

**curl** (Command Prompt):
```
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"Admin@123\"}"
curl http://localhost:8080/api/employees -H "Authorization: Bearer PASTE_TOKEN_HERE"
```

**Inspect a token:** paste it at https://jwt.io. The payload shows `sub` (username), `role`, `iat` and `exp` (30 minutes later). The token is signed, not encrypted, so anyone can read it but nobody can change it without the secret.

## 11. Configuration
Settings are in `backend/src/main/resources/application.properties`, and can be overridden with environment variables.

| Setting | Environment variable | Default |
|---|---|---|
| Database user | `DB_USER` | `root` |
| Database password | `DB_PASSWORD` | `root` |
| JWT signing secret (32+ characters) | `JWT_SECRET` | a development value |
| Token lifetime | (property `app.jwt.expiration-minutes`) | 30 |
| Allowed frontend origin | (property `app.cors.allowed-origin`) | `http://localhost:4200` |

The frontend API address is in `frontend/src/app/core/api.ts` (default `http://localhost:8080/api`). Set a real `JWT_SECRET` for anything other than local development.

## 12. Security notes
- Passwords are stored as BCrypt hashes.
- Stateless JWT (HS256, 30 minutes). CSRF protection is off because the token is sent in a header, not a cookie.
- Roles are enforced on the server with `@PreAuthorize`, not just in the UI.
- Every request body is validated. Search uses parameterised JPQL, and sort fields are whitelisted, so there is no string-built SQL.
- CORS allows only `http://localhost:4200`.
- The frontend keeps the token in localStorage, as the assessment specifies. Any XSS bug could read it, so a production app would prefer an httpOnly cookie.

## 13. Use-case coverage
| Requirement | Status |
|---|---|
| Login with validation, JWT, route guard | Done |
| Remember me | Partial (username remembered; no refresh token) |
| 30-minute idle timeout with warning | Done |
| Employee list: paging, sorting, debounced search | Done |
| Add, edit, delete with validation, duplicate check, confirmations, toasts | Done |
| Logout, guard redirect, 401 handling | Done |
| Report module (filters, summary cards, Excel and PDF) | Not built yet |

---

## 14. Troubleshooting
| Problem | Fix |
|---|---|
| `'java'`, `'mvn'` or `'node'` is not recognized | Close all terminals and VS Code, open a new one. If still missing, recheck the installation and PATH entries in Section 4. |
| `mvn` fails with a folder not found path | Use the **real** Maven folder name (for example `apache-maven-3.10.2`), not a placeholder. Confirm with `dir C:\tools`. |
| `'curl'` or `'netstat'` is not recognized | `C:\Windows\System32` is missing from PATH. Add `%SystemRoot%\system32` and `%SystemRoot%` to the **System variables** Path and reopen the terminal. Meanwhile use `C:\Windows\System32\curl.exe` or PowerShell. |
| Service MySQL80 shows an error "Error 2: cannot find the file specified" | MySQL Server was not installed (only Workbench). Open **MySQL Installer**, click **Add**, add **MySQL Server 8.0** and finish the configuration. |
| MySQL Installer asks for **"Current Root Password"** and you do not know it | Old data exists. In an **Administrator** Command Prompt run `net stop MySQL80` then `ren "C:\ProgramData\MySQL\MySQL Server 8.0\Data" Data_old`. Then in MySQL Installer click **Reconfigure** on the server and set the root password again. |
| `Access denied for user 'root'` | The password differs from `root`. Set `DB_PASSWORD` (Section 6, Step 1). |
| `Communications link failure` | MySQL is not running. Open **Services** and start **MySQL80**, or run `docker compose up -d`. |
| `Port 8080 was already in use` | Close the other program, or change `server.port` and the URL in `frontend/src/app/core/api.ts`. |
| Login page says "Cannot reach the server" | The backend is not running or crashed. Check terminal 1 for errors. |
| Browser console shows a CORS error | The frontend must be opened at exactly `http://localhost:4200`. |
| `npm.ps1 cannot be loaded because running scripts is disabled` | In PowerShell run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`, then retry. |
| Signed out unexpectedly | The token lasts 30 minutes (and idle sign-out is 30 minutes). Sign in again. |
| A viewer's forbidden action returns 401 instead of 403 | Make sure `SecurityConfig` has `.requestMatchers("/error").permitAll()` in `authorizeHttpRequests`, then restart the backend. |

## 15. AI usage
AI was used only to produce template and boilerplate code, which was then reviewed, tested and improved by hand. Every use is recorded in [`AI_USAGE_LOG.md`](AI_USAGE_LOG.md): where it was used, why AI instead of writing it directly, what it produced, and what was changed afterwards. Planned and completed refinements are tracked in [`REFACTOR_CHECKLIST.md`](REFACTOR_CHECKLIST.md).
