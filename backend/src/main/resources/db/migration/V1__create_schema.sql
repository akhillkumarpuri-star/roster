-- Roster schema, version 1 (MySQL 8).
-- Flyway runs this once on an empty database; the application never changes the schema on its own.
--
--   departments 1 ---- * employees        (employees.department_id -> departments.id)
--   users                                 (login accounts, stands alone)

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,            -- BCrypt hash, never the plain password
    full_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'USER'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE departments (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_department_name UNIQUE (name)     -- case-insensitive: the collation is *_ci
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE employees (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    first_name    VARCHAR(80)  NOT NULL,
    last_name     VARCHAR(80)  NOT NULL,
    email         VARCHAR(150) NOT NULL,
    phone         VARCHAR(30)  NOT NULL,
    department_id BIGINT       NOT NULL,
    job_title     VARCHAR(100) NOT NULL,
    hire_date     DATE         NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    -- Last line of defence against duplicate emails (two simultaneous requests can both pass the service check).
    CONSTRAINT uk_employee_email UNIQUE (email),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT ck_employee_status CHECK (status IN ('ACTIVE', 'ON_LEAVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Indexes for the queries the app actually runs:
CREATE INDEX idx_employee_hire_status ON employees (hire_date, status);  -- report: date range + status, hires per month
CREATE INDEX idx_employee_last_name   ON employees (last_name);          -- default sort of the employee list
-- (MySQL creates an index on employees.department_id automatically for the foreign key.)
