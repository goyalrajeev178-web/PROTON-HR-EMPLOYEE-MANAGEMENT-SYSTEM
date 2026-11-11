-- =============================================================================
-- Proton HR - Comprehensive Database Schema (PostgreSQL / MySQL Compatible)
-- =============================================================================

-- Enable extension for UUID generation if using PostgreSQL
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -----------------------------------------------------------------------------
-- 1. DEPARTMENTS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 2. ROLES & POSITIONS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    department_id INT NOT NULL,
    salary_min DECIMAL(12, 2),
    salary_max DECIMAL(12, 2),
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 3. EMPLOYEES & USERS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE employees (
    employee_id VARCHAR(20) PRIMARY KEY, -- e.g. 'EMP-1001'
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE CHECK (email LIKE '%@gmail.com'),
    password_hash VARCHAR(255) NOT NULL DEFAULT 'password123',
    phone VARCHAR(30) NOT NULL,
    dob DATE,
    age INT,
    gender VARCHAR(20),
    address TEXT,
    department_id INT,
    role_id INT,
    manager_id VARCHAR(20),
    location VARCHAR(100) NOT NULL DEFAULT 'Bengaluru, KA',
    status ENUM('Active', 'On Leave', 'Remote', 'Terminated') DEFAULT 'Active',
    hire_date DATE NOT NULL,
    salary DECIMAL(12, 2) NOT NULL,
    bank_account VARCHAR(50),
    emergency_contact VARCHAR(255),
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id) REFERENCES employees(employee_id) ON DELETE SET NULL
);

-- -----------------------------------------------------------------------------
-- 4. LEAVE TYPES TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE leave_types (
    leave_type_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE, -- e.g. Annual Leave, Sick Leave, Casual Leave
    default_allowance_days DECIMAL(5, 1) NOT NULL
);

-- -----------------------------------------------------------------------------
-- 5. LEAVE BALANCES TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE leave_balances (
    balance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    leave_type_id INT NOT NULL,
    year INT NOT NULL DEFAULT 2026,
    total_days DECIMAL(5, 1) NOT NULL,
    used_days DECIMAL(5, 1) DEFAULT 0.0,
    remaining_days DECIMAL(5, 1) GENERATED ALWAYS AS (total_days - used_days) STORED,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id) ON DELETE CASCADE,
    CONSTRAINT unique_emp_leave_year UNIQUE (employee_id, leave_type_id, year)
);

-- -----------------------------------------------------------------------------
-- 6. LEAVE REQUESTS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE leave_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    leave_type_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration_days DECIMAL(5, 1) NOT NULL,
    reason TEXT,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    reviewer_id VARCHAR(20),
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES employees(employee_id) ON DELETE SET NULL
);

-- -----------------------------------------------------------------------------
-- 7. PAYROLL & COMPENSATION TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE payroll (
    payroll_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    base_salary DECIMAL(12, 2) NOT NULL,
    bonuses DECIMAL(12, 2) DEFAULT 0.00,
    deductions DECIMAL(12, 2) DEFAULT 0.00,
    net_pay DECIMAL(12, 2) GENERATED ALWAYS AS (base_salary + bonuses - deductions) STORED,
    payment_status ENUM('Pending', 'Processed', 'Failed') DEFAULT 'Pending',
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 8. SYSTEM & USER PREFERENCES TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE system_settings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    theme_mode ENUM('Light', 'Dark') DEFAULT 'Light',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 9. AUDIT LOGS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE SET NULL
);

-- -----------------------------------------------------------------------------
-- INDEXES FOR OPTIMIZED PERFORMANCE
-- -----------------------------------------------------------------------------
CREATE INDEX idx_emp_email ON employees(email);
CREATE INDEX idx_emp_dept ON employees(department_id);
CREATE INDEX idx_emp_status ON employees(status);
CREATE INDEX idx_leave_emp_status ON leave_requests(employee_id, status);
CREATE INDEX idx_payroll_emp_period ON payroll(employee_id, pay_period_start, pay_period_end);

-- =============================================================================
-- SAMPLE DATA INSERTION (DML SCRIPT WITH INDIAN PROFILES)
-- =============================================================================

INSERT INTO departments (department_name, location) VALUES
('Engineering', 'Bengaluru, KA'),
('Human Resources', 'Mumbai, MH'),
('Design', 'Bengaluru, KA'),
('Sales', 'Delhi NCR'),
('Marketing', 'Hyderabad, TS'),
('Finance', 'Mumbai, MH'),
('Operations', 'Hyderabad, TS');

INSERT INTO roles (title, department_id, salary_min, salary_max) VALUES
('VP of Engineering', 1, 3000000.00, 5000000.00),
('Senior Software Engineer', 1, 1500000.00, 2500000.00),
('DevOps Engineer', 1, 1400000.00, 2200000.00),
('Security Consultant', 1, 1600000.00, 2400000.00),
('HR Business Partner', 2, 1000000.00, 1800000.00),
('Talent Acquisition Specialist', 2, 800000.00, 1400000.00),
('Product Designer', 3, 1200000.00, 2000000.00),
('Regional Sales Director', 4, 1800000.00, 3000000.00),
('Marketing Lead', 5, 1200000.00, 2000000.00),
('Content Strategist', 5, 900000.00, 1500000.00),
('Financial Analyst', 6, 1000000.00, 1700000.00),
('Operations Specialist', 7, 800000.00, 1300000.00);

INSERT INTO employees (employee_id, full_name, email, password_hash, phone, dob, age, gender, address, department_id, role_id, manager_id, location, status, hire_date, salary, bank_account, emergency_contact) VALUES
('EMP-1005', 'Kavita Rao', 'kavita.rao@gmail.com', 'password123', '+91 98765 45678', '1988-04-12', 38, 'Female', '45 Indiranagar, Bengaluru, KA', 1, 1, NULL, 'Bengaluru, KA', 'Active', '2019-02-20', 3500000.00, 'HDFC Bank ****9900', 'Rohan Rao - 98765-00000'),
('EMP-1001', 'Rajesh Sharma', 'rajesh.sharma@gmail.com', 'password123', '+91 98765 43210', '1994-08-22', 32, 'Male', '123 Tech Park, Whitefield, Bengaluru, KA', 1, 2, 'EMP-1005', 'Bengaluru, KA', 'Active', '2021-03-15', 1800000.00, 'HDFC Bank ****1234', 'Sunita Sharma (Spouse) - 98765-43210'),
('EMP-1002', 'Priya Patel', 'priya.patel@gmail.com', 'password123', '+91 98765 12345', '1992-11-05', 33, 'Female', '78 Bandra West, Mumbai, MH', 2, 5, 'EMP-1005', 'Mumbai, MH', 'Active', '2020-01-10', 1450000.00, 'ICICI Bank ****5678', 'Amit Patel - 98765-54321'),
('EMP-1003', 'Ananya Iyer', 'ananya.iyer@gmail.com', 'password123', '+91 98765 23456', '1996-03-18', 30, 'Female', '12 Koramangala, Bengaluru, KA', 3, 7, 'EMP-1005', 'Bengaluru, KA', 'Active', '2022-06-01', 1600000.00, 'Axis Bank ****2345', 'Suresh Iyer - 98765-67890'),
('EMP-1004', 'Vikram Verma', 'v.verma@gmail.com', 'password123', '+91 98765 34567', '1985-07-29', 41, 'Male', '99 Connaught Place, New Delhi', 4, 8, NULL, 'Delhi NCR', 'Active', '2018-11-12', 2200000.00, 'SBI Bank ****3456', 'Pooja Verma - 98765-99999');

INSERT INTO leave_types (name, default_allowance_days) VALUES
('Annual Leave', 25.0),
('Sick Leave', 10.0),
('Casual Leave', 8.0),
('Maternity / Paternity Leave', 90.0);

INSERT INTO leave_balances (employee_id, leave_type_id, year, total_days, used_days) VALUES
('EMP-1001', 1, 2026, 25.0, 6.5),
('EMP-1001', 2, 2026, 10.0, 6.8),
('EMP-1002', 1, 2026, 25.0, 10.0),
('EMP-1003', 1, 2026, 25.0, 5.0);

INSERT INTO leave_requests (employee_id, leave_type_id, start_date, end_date, duration_days, reason, status, reviewer_id) VALUES
('EMP-1001', 1, '2026-10-10', '2026-10-12', 3.0, 'Family vacation', 'Approved', 'EMP-1005'),
('EMP-1002', 2, '2026-10-15', '2026-10-15', 1.0, 'Medical appointment', 'Pending', 'EMP-1005'),
('EMP-1003', 1, '2026-11-01', '2026-11-05', 5.0, 'Personal leave', 'Rejected', 'EMP-1005');
