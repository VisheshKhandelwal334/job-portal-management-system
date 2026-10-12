package com.jobportal.ui;

import com.jobportal.dao.AdminDAO;
import com.jobportal.dao.CompanyDAO;
import com.jobportal.dao.JobDAO;
import com.jobportal.dao.StudentDAO;
import com.jobportal.exception.PortalException;
import com.jobportal.model.Job;
import com.jobportal.util.FileUtil;
import com.jobportal.util.InputUtil;
import com.jobportal.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminMenu {

    private final AdminDAO adminDAO = new AdminDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final JobDAO jobDAO = new JobDAO();

    public void login() {
        Display.title("ADMIN LOGIN");
        try {
            String user = InputUtil.required("Username : ");
            String pass = InputUtil.password("Password : ");
            if (!adminDAO.login(user, PasswordUtil.hash(pass))) {
                System.out.println("\n  ! Invalid admin credentials.");
                return;
            }
            FileUtil.log("Admin login user=" + user);
            dashboard();
        } catch (PortalException e) {
            System.out.println("\n  ! " + e.getMessage());
        }
    }

    private void dashboard() {
        while (true) {
            Display.title("ADMIN MENU");
            System.out.println("  1. Manage Students");
            System.out.println("  2. Manage Companies");
            System.out.println("  3. Manage Jobs");
            System.out.println("  4. Reports");
            System.out.println("  0. Logout");
            int choice = InputUtil.integer("Choose: ");
            try {
                switch (choice) {
                    case 1: manageStudents(); break;
                    case 2: manageCompanies(); break;
                    case 3: manageJobs(); break;
                    case 4: reports(); break;
                    case 0: return;
                    default: System.out.println("  ! Invalid option.");
                }
            } catch (PortalException e) {
                System.out.println("\n  ! " + e.getMessage());
            }
        }
    }

    private void manageStudents() throws PortalException {
        while (true) {
            Display.title("MANAGE STUDENTS");
            Display.students(studentDAO.findAll());
            System.out.println("\n  1. Block   2. Unblock   3. Delete   0. Back");
            int c = InputUtil.integer("Choose: ");
            if (c == 0) return;
            if (c < 1 || c > 3) {
                System.out.println("  ! Invalid option.");
                continue;
            }
            int id = InputUtil.integer("Student ID: ");
            if (studentDAO.findById(id) == null) {
                System.out.println("  ! Student not found.");
                continue;
            }
            if (c == 1) {
                studentDAO.setStatus(id, "BLOCKED");
                System.out.println("  Student blocked.");
            } else if (c == 2) {
                studentDAO.setStatus(id, "ACTIVE");
                System.out.println("  Student unblocked.");
            } else if (InputUtil.confirm("Permanently delete this student and their applications?")) {
                studentDAO.delete(id);
                System.out.println("  Student deleted.");
            }
            FileUtil.log("Admin action on student id=" + id + " option=" + c);
        }
    }

    private void manageCompanies() throws PortalException {
        while (true) {
            Display.title("MANAGE COMPANIES");
            Display.companies(companyDAO.findAll());
            System.out.println("\n  1. Approve   2. Block   3. Delete   0. Back");
            int c = InputUtil.integer("Choose: ");
            if (c == 0) return;
            if (c < 1 || c > 3) {
                System.out.println("  ! Invalid option.");
                continue;
            }
            int id = InputUtil.integer("Company ID: ");
            if (companyDAO.findById(id) == null) {
                System.out.println("  ! Company not found.");
                continue;
            }
            if (c == 1) {
                companyDAO.setStatus(id, "APPROVED");
                System.out.println("  Company approved.");
            } else if (c == 2) {
                companyDAO.setStatus(id, "BLOCKED");
                System.out.println("  Company blocked.");
            } else if (InputUtil.confirm("Permanently delete this company, its jobs and applications?")) {
                companyDAO.delete(id);
                System.out.println("  Company deleted.");
            }
            FileUtil.log("Admin action on company id=" + id + " option=" + c);
        }
    }

    private void manageJobs() throws PortalException {
        while (true) {
            Display.title("MANAGE JOBS");
            List<Job> jobs = jobDAO.findAll();
            Display.jobs(jobs);
            System.out.println("\n  1. Close   2. Reopen   3. Delete   0. Back");
            int c = InputUtil.integer("Choose: ");
            if (c == 0) return;
            if (c < 1 || c > 3) {
                System.out.println("  ! Invalid option.");
                continue;
            }
            int id = InputUtil.integer("Job ID: ");
            if (jobDAO.findById(id) == null) {
                System.out.println("  ! Job not found.");
                continue;
            }
            if (c == 1) {
                jobDAO.setStatus(id, "CLOSED");
                System.out.println("  Job closed.");
            } else if (c == 2) {
                jobDAO.setStatus(id, "OPEN");
                System.out.println("  Job reopened.");
            } else if (InputUtil.confirm("Permanently delete this job?")) {
                jobDAO.delete(id);
                System.out.println("  Job deleted.");
            }
            FileUtil.log("Admin action on job id=" + id + " option=" + c);
        }
    }

    private void reports() throws PortalException {
        List<String> lines = new ArrayList<>();
        lines.add("JOB PORTAL - SUMMARY REPORT");
        lines.add("=".repeat(50));
        for (Map.Entry<String, Long> e : adminDAO.summaryCounts().entrySet()) {
            lines.add(String.format("%-30s : %d", e.getKey(), e.getValue()));
        }
        lines.add("");
        lines.add("TOP JOBS BY APPLICATIONS");
        lines.add("-".repeat(50));
        for (String[] r : adminDAO.topJobsByApplications(5)) {
            lines.add(String.format("%-28s %-18s %s applications", r[0], r[1], r[2]));
        }
        lines.add("");
        lines.add("MOST COMMON STUDENT SKILLS");
        lines.add("-".repeat(50));
        for (String[] r : adminDAO.topSkills(5)) {
            lines.add(String.format("%-28s %s students", r[0], r[1]));
        }

        Display.title("REPORT");
        lines.forEach(System.out::println);
        if (InputUtil.confirm("\nSave this report to a file?")) {
            System.out.println("  Saved to: " + FileUtil.exportReport("summary_report", lines));
        }
        InputUtil.pause();
    }
}
