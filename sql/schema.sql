-- Job Portal Management System : database schema
CREATE DATABASE IF NOT EXISTS job_portal CHARACTER SET utf8mb4;
USE job_portal;

DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS admins;

CREATE TABLE admins (
    admin_id      INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL
);

CREATE TABLE students (
    student_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL,
    phone         VARCHAR(15),
    branch        VARCHAR(80),
    cgpa          DECIMAL(4,2) DEFAULT 0.00,
    resume_path   VARCHAR(255),
    status        ENUM('ACTIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skills (
    skill_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    skill_name VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_student_skill (student_id, skill_name),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

CREATE TABLE companies (
    company_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL,
    industry      VARCHAR(80),
    location      VARCHAR(80),
    website       VARCHAR(150),
    description   VARCHAR(500),
    status        ENUM('PENDING','APPROVED','BLOCKED') NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE jobs (
    job_id          INT AUTO_INCREMENT PRIMARY KEY,
    company_id      INT NOT NULL,
    title           VARCHAR(120) NOT NULL,
    description     VARCHAR(1000),
    location        VARCHAR(80),
    salary          DECIMAL(10,2) DEFAULT 0.00,
    job_type        ENUM('FULL_TIME','INTERNSHIP','PART_TIME','CONTRACT') NOT NULL DEFAULT 'FULL_TIME',
    required_skills VARCHAR(255),
    min_cgpa        DECIMAL(4,2) DEFAULT 0.00,
    status          ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
    posted_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
);

CREATE TABLE applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id         INT NOT NULL,
    student_id     INT NOT NULL,
    status         ENUM('APPLIED','SHORTLISTED','REJECTED') NOT NULL DEFAULT 'APPLIED',
    applied_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_job_student (job_id, student_id),
    FOREIGN KEY (job_id)     REFERENCES jobs(job_id)         ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- Default admin  (username: admin / password: admin123)  -- change after first login
INSERT INTO admins (username, password_hash) VALUES ('admin', SHA2('admin123', 256));

-- Demo data (all demo passwords: pass123)
INSERT INTO students (name, email, password_hash, phone, branch, cgpa) VALUES
 ('Aarav Sharma', 'aarav@demo.com', SHA2('pass123',256), '9876543210', 'Artificial Intelligence', 7.80),
 ('Riya Verma',   'riya@demo.com',  SHA2('pass123',256), '9123456780', 'Computer Science',        8.40);
INSERT INTO skills (student_id, skill_name) VALUES (1,'Java'),(1,'MySQL'),(2,'Java'),(2,'Python');

INSERT INTO companies (name, email, password_hash, industry, location, website, description, status) VALUES
 ('TechNova Pvt Ltd', 'hr@technova.com', SHA2('pass123',256), 'Software', 'Jaipur', 'https://technova.example', 'Product startup', 'APPROVED');
INSERT INTO jobs (company_id, title, description, location, salary, job_type, required_skills, min_cgpa) VALUES
 (1, 'Java Developer Intern', 'Work on backend services with Java and MySQL', 'Jaipur', 15000, 'INTERNSHIP', 'Java,MySQL', 6.00),
 (1, 'Junior Java Developer', 'Build and maintain REST APIs', 'Remote', 400000, 'FULL_TIME', 'Java,JDBC,Git', 7.00);
