# Job Portal Management System

A console-based job portal built with **Java, JDBC and MySQL**. Students build a profile and apply for jobs, companies post jobs and shortlist applicants, and an admin manages the whole platform.

**Technologies:** Java 11+, JDBC, MySQL, Collections, OOP, Exception Handling, File Handling, Git

## Features

| Module  | Features |
|---------|----------|
| Student | Register, Login, Edit Profile, Skills (add/remove), Resume Path, Search Jobs (keyword/location/type), Apply Job, View Application Status, Change Password |
| Company | Register/Login (admin approval required), Company Profile, Post Jobs, Close/Reopen/Delete Jobs, View Applicants, Shortlist/Reject, Export Applicants to file |
| Admin   | Manage Students (block/unblock/delete), Manage Companies (approve/block/delete), Manage Jobs (close/reopen/delete), Reports (summary, top jobs, top skills, save to file) |

Business rules enforced: duplicate email/application blocked, CGPA must meet a job's minimum, a resume is required before applying, closed jobs cannot be applied to, companies can only touch their own jobs/applicants, blocked/unapproved accounts cannot log in.

## Database (5 tables + admins)

`students`, `companies`, `jobs`, `applications`, `skills` (plus `admins` for admin login).

```
students 1--* skills
students 1--* applications *--1 jobs *--1 companies
```

## Project structure

```
job-portal/
├── pom.xml
├── db.properties.example      <- copy to db.properties
├── sql/schema.sql             <- tables + admin + demo data
└── src/main/java/com/jobportal/
    ├── Main.java
    ├── config/DBConnection.java
    ├── model/       Student, Company, Job, Application      (OOP entities)
    ├── dao/         StudentDAO, CompanyDAO, JobDAO, ApplicationDAO, SkillDAO, AdminDAO  (JDBC)
    ├── ui/          StudentMenu, CompanyMenu, AdminMenu, Display
    ├── util/        InputUtil, PasswordUtil (SHA-256), FileUtil (resume check, reports, log)
    └── exception/   PortalException
```

## Setup

1. Install JDK 11+ and MySQL 8.
2. Create the database:
   ```
   mysql -u root -p < sql/schema.sql
   ```
3. Configure the connection:
   ```
   cp db.properties.example db.properties     # then put your MySQL password in it
   ```
4. Run (Maven):
   ```
   mvn compile exec:java
   ```
   Or without Maven: download `mysql-connector-j-8.4.0.jar` into a `lib/` folder, then
   ```
   javac -d out -cp "lib/*" $(find src -name "*.java")
   java -cp "out:lib/*" com.jobportal.Main        # on Windows use ; instead of :
   ```
   Run from the project root so `db.properties` is found.

## Demo logins (from schema.sql)

| Role    | Login | Password |
|---------|-------|----------|
| Admin   | `admin` | `admin123` |
| Student | `aarav@demo.com` | `pass123` |
| Company | `hr@technova.com` | `pass123` |

New companies register as PENDING; log in as admin → Manage Companies → Approve.

## Generated files

- `reports/` – exported reports and applicant lists
- `logs/activity.log` – audit trail of logins and actions

## Git

```
git init
git add .
git commit -m "Job Portal Management System"
```
