package com.jobportal.ui;

import com.jobportal.dao.ApplicationDAO;
import com.jobportal.dao.CompanyDAO;
import com.jobportal.dao.JobDAO;
import com.jobportal.exception.PortalException;
import com.jobportal.model.Application;
import com.jobportal.model.Company;
import com.jobportal.model.Job;
import com.jobportal.util.FileUtil;
import com.jobportal.util.InputUtil;
import com.jobportal.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

public class CompanyMenu {

    private final CompanyDAO companyDAO = new CompanyDAO();
    private final JobDAO jobDAO = new JobDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();

    // ---------- Register / Login ----------
    public void register() {
        Display.title("COMPANY REGISTRATION");
        try {
            Company c = new Company();
            c.setName(InputUtil.required("Company name : "));
            c.setEmail(InputUtil.email("Official email: "));
            c.setPasswordHash(PasswordUtil.hash(InputUtil.password("Password (min 6 chars): ")));
            c.setIndustry(InputUtil.required("Industry     : "));
            c.setLocation(InputUtil.required("Location     : "));
            c.setWebsite(InputUtil.text("Website      : "));
            c.setDescription(InputUtil.text("About company: "));
            int id = companyDAO.register(c);
            FileUtil.log("Company registered id=" + id + " email=" + c.getEmail());
            System.out.println("\n  Registered! Your Company ID is " + id
                    + ". You can login once the admin approves your account.");
        } catch (PortalException e) {
            System.out.println("\n  ! " + e.getMessage());
        }
    }

    public void login() {
        Display.title("COMPANY LOGIN");
        try {
            String email = InputUtil.email("Email    : ");
            String pass = InputUtil.password("Password : ");
            Company c = companyDAO.login(email, PasswordUtil.hash(pass));
            if (c == null) {
                System.out.println("\n  ! Invalid email or password.");
                return;
            }
            if ("PENDING".equals(c.getStatus())) {
                System.out.println("\n  ! Your account is awaiting admin approval.");
                return;
            }
            if ("BLOCKED".equals(c.getStatus())) {
                System.out.println("\n  ! Your account has been blocked by the admin.");
                return;
            }
            FileUtil.log("Company login id=" + c.getId());
            System.out.println("\n  Welcome, " + c.getName() + "!");
            dashboard(c);
        } catch (PortalException e) {
            System.out.println("\n  ! " + e.getMessage());
        }
    }

    // ---------- Dashboard ----------
    private void dashboard(Company company) {
        while (true) {
            Display.title("COMPANY MENU  -  " + company.getName());
            System.out.println("  1. View / Edit Company Profile");
            System.out.println("  2. Post a Job");
            System.out.println("  3. My Jobs (close / reopen / delete)");
            System.out.println("  4. View Applicants");
            System.out.println("  5. Shortlist / Reject Applicant");
            System.out.println("  6. Export Applicants to File");
            System.out.println("  0. Logout");
            int choice = InputUtil.integer("Choose: ");
            try {
                switch (choice) {
                    case 1: profile(company); break;
                    case 2: postJob(company); break;
                    case 3: myJobs(company); break;
                    case 4: viewApplicants(company); break;
                    case 5: decide(company); break;
                    case 6: exportApplicants(company); break;
                    case 0: return;
                    default: System.out.println("  ! Invalid option.");
                }
            } catch (PortalException e) {
                System.out.println("\n  ! " + e.getMessage());
            }
            InputUtil.pause();
        }
    }

    private void profile(Company company) throws PortalException {
        Company fresh = companyDAO.findById(company.getId());
        Display.company(fresh);
        if (!InputUtil.confirm("\nEdit profile?")) return;
        System.out.println("  (press Enter to keep the current value)");
        fresh.setName(InputUtil.optional("Name", fresh.getName()));
        fresh.setIndustry(InputUtil.optional("Industry", fresh.getIndustry()));
        fresh.setLocation(InputUtil.optional("Location", fresh.getLocation()));
        fresh.setWebsite(InputUtil.optional("Website", fresh.getWebsite()));
        fresh.setDescription(InputUtil.optional("About", fresh.getDescription()));
        companyDAO.updateProfile(fresh);
        company.setName(fresh.getName());
        System.out.println("  Profile updated.");
    }

    private void postJob(Company company) throws PortalException {
        Display.title("POST A JOB");
        Job j = new Job();
        j.setCompanyId(company.getId());
        j.setTitle(InputUtil.required("Job title         : "));
        j.setDescription(InputUtil.required("Description       : "));
        j.setLocation(InputUtil.required("Location          : "));
        j.setSalary(InputUtil.decimal("Salary / stipend   : ", 0, 100_000_000));
        String type;
        while (true) {
            type = InputUtil.required("Type (FULL_TIME / INTERNSHIP / PART_TIME / CONTRACT): ").toUpperCase();
            if (List.of("FULL_TIME", "INTERNSHIP", "PART_TIME", "CONTRACT").contains(type)) break;
            System.out.println("  ! Choose one of the listed types.");
        }
        j.setJobType(type);
        j.setRequiredSkills(InputUtil.required("Required skills (comma separated): "));
        j.setMinCgpa(InputUtil.decimal("Minimum CGPA (0-10): ", 0, 10));
        int id = jobDAO.create(j);
        FileUtil.log("Job posted id=" + id + " company=" + company.getId());
        System.out.println("  Job posted with ID " + id + ".");
    }

    private void myJobs(Company company) throws PortalException {
        List<Job> jobs = jobDAO.findByCompany(company.getId());
        Display.jobs(jobs);
        if (jobs.isEmpty()) return;
        System.out.println("\n  1. Close job   2. Reopen job   3. Delete job   0. Back");
        int c = InputUtil.integer("Choose: ");
        if (c == 0) return;
        if (c < 1 || c > 3) {
            System.out.println("  ! Invalid option.");
            return;
        }
        int id = InputUtil.integer("Job ID: ");
        Job job = ownJob(company, id);
        if (c == 1) {
            jobDAO.setStatus(id, "CLOSED");
            System.out.println("  Job closed.");
        } else if (c == 2) {
            jobDAO.setStatus(id, "OPEN");
            System.out.println("  Job reopened.");
        } else if (InputUtil.confirm("Delete '" + job.getTitle() + "' and all its applications?")) {
            jobDAO.delete(id);
            FileUtil.log("Job deleted id=" + id + " by company=" + company.getId());
            System.out.println("  Job deleted.");
        }
    }

    private void viewApplicants(Company company) throws PortalException {
        List<Job> jobs = jobDAO.findByCompany(company.getId());
        Display.jobs(jobs);
        if (jobs.isEmpty()) return;
        int id = InputUtil.integer("\nJob ID to view applicants: ");
        ownJob(company, id);
        Display.applicants(applicationDAO.findApplicants(id, company.getId()));
    }

    private void decide(Company company) throws PortalException {
        List<Job> jobs = jobDAO.findByCompany(company.getId());
        Display.jobs(jobs);
        if (jobs.isEmpty()) return;
        int jobId = InputUtil.integer("\nJob ID: ");
        ownJob(company, jobId);
        List<Application> apps = applicationDAO.findApplicants(jobId, company.getId());
        Display.applicants(apps);
        if (apps.isEmpty()) return;
        int appId = InputUtil.integer("\nApplication ID: ");
        System.out.println("  1. Shortlist   2. Reject   3. Reset to APPLIED");
        int d = InputUtil.integer("Choose: ");
        String status;
        switch (d) {
            case 1: status = "SHORTLISTED"; break;
            case 2: status = "REJECTED"; break;
            case 3: status = "APPLIED"; break;
            default:
                System.out.println("  ! Invalid option.");
                return;
        }
        boolean ok = applicationDAO.updateStatus(appId, company.getId(), status);
        if (ok) FileUtil.log("Application " + appId + " -> " + status + " by company=" + company.getId());
        System.out.println(ok ? "  Application marked " + status + "." : "  ! Application not found for your jobs.");
    }

    private void exportApplicants(Company company) throws PortalException {
        List<Job> jobs = jobDAO.findByCompany(company.getId());
        Display.jobs(jobs);
        if (jobs.isEmpty()) return;
        int jobId = InputUtil.integer("\nJob ID to export: ");
        Job job = ownJob(company, jobId);
        List<Application> apps = applicationDAO.findApplicants(jobId, company.getId());
        List<String> lines = new ArrayList<>();
        lines.add("Applicants for: " + job.getTitle() + " (Job #" + job.getId() + ") - " + company.getName());
        lines.add("-".repeat(80));
        for (Application a : apps) {
            lines.add(String.format("#%d | %s | %s | CGPA %.2f | %s | Skills: %s | Resume: %s",
                    a.getId(), a.getStudentName(), a.getStudentEmail(), a.getCgpa(), a.getStatus(),
                    a.getSkills(), a.getResumePath()));
        }
        lines.add("Total applicants: " + apps.size());
        System.out.println("  Exported to: " + FileUtil.exportReport("applicants_job" + jobId, lines));
    }

    /** Loads a job and guarantees it belongs to this company. */
    private Job ownJob(Company company, int jobId) throws PortalException {
        Job job = jobDAO.findById(jobId);
        if (job == null || job.getCompanyId() != company.getId()) {
            throw new PortalException("Job not found in your postings.");
        }
        return job;
    }
}
