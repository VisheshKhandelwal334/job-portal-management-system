package com.jobportal.ui;

import com.jobportal.model.Application;
import com.jobportal.model.Company;
import com.jobportal.model.Job;
import com.jobportal.model.Student;

import java.util.List;

/** Console table printing shared by all menus. */
public final class Display {

    private Display() { }

    public static void title(String text) {
        String line = "=".repeat(Math.max(text.length() + 4, 50));
        System.out.println("\n" + line);
        System.out.println("  " + text);
        System.out.println(line);
    }

    public static String cut(String s, int max) {
        if (s == null) return "-";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }

    public static void jobs(List<Job> jobs) {
        if (jobs.isEmpty()) {
            System.out.println("  (no jobs found)");
            return;
        }
        System.out.printf("%-5s %-26s %-20s %-12s %-12s %-10s %-7s %-7s%n",
                "ID", "Title", "Company", "Location", "Type", "Salary", "MinCGPA", "Status");
        System.out.println("-".repeat(108));
        for (Job j : jobs) {
            System.out.printf("%-5d %-26s %-20s %-12s %-12s %-10.0f %-7.2f %-7s%n",
                    j.getId(), cut(j.getTitle(), 26), cut(j.getCompanyName(), 20), cut(j.getLocation(), 12),
                    j.getJobType(), j.getSalary(), j.getMinCgpa(), j.getStatus());
        }
    }

    public static void jobDetail(Job j) {
        System.out.println("\n  Job ID       : " + j.getId());
        System.out.println("  Title        : " + j.getTitle());
        System.out.println("  Company      : " + j.getCompanyName());
        System.out.println("  Location     : " + j.getLocation());
        System.out.println("  Type         : " + j.getJobType());
        System.out.printf("  Salary       : %.2f%n", j.getSalary());
        System.out.println("  Skills       : " + j.getRequiredSkills());
        System.out.printf("  Minimum CGPA : %.2f%n", j.getMinCgpa());
        System.out.println("  Description  : " + j.getDescription());
        System.out.println("  Posted       : " + j.getPostedAt());
    }

    public static void student(Student s) {
        System.out.println("\n  Student ID : " + s.getId());
        System.out.println("  Name       : " + s.getName());
        System.out.println("  Email      : " + s.getEmail());
        System.out.println("  Phone      : " + s.getPhone());
        System.out.println("  Branch     : " + s.getBranch());
        System.out.printf("  CGPA       : %.2f%n", s.getCgpa());
        System.out.println("  Resume     : " + (s.getResumePath() == null ? "(not set)" : s.getResumePath()));
        System.out.println("  Skills     : " + (s.getSkills().isEmpty() ? "(none)" : String.join(", ", s.getSkills())));
    }

    public static void company(Company c) {
        System.out.println("\n  Company ID  : " + c.getId());
        System.out.println("  Name        : " + c.getName());
        System.out.println("  Email       : " + c.getEmail());
        System.out.println("  Industry    : " + c.getIndustry());
        System.out.println("  Location    : " + c.getLocation());
        System.out.println("  Website     : " + c.getWebsite());
        System.out.println("  About       : " + c.getDescription());
        System.out.println("  Status      : " + c.getStatus());
    }

    public static void students(List<Student> list) {
        if (list.isEmpty()) {
            System.out.println("  (no students)");
            return;
        }
        System.out.printf("%-5s %-22s %-30s %-12s %-6s %-8s%n", "ID", "Name", "Email", "Branch", "CGPA", "Status");
        System.out.println("-".repeat(88));
        for (Student s : list) {
            System.out.printf("%-5d %-22s %-30s %-12s %-6.2f %-8s%n", s.getId(), cut(s.getName(), 22),
                    cut(s.getEmail(), 30), cut(s.getBranch(), 12), s.getCgpa(), s.getStatus());
        }
    }

    public static void companies(List<Company> list) {
        if (list.isEmpty()) {
            System.out.println("  (no companies)");
            return;
        }
        System.out.printf("%-5s %-24s %-30s %-14s %-9s%n", "ID", "Name", "Email", "Location", "Status");
        System.out.println("-".repeat(86));
        for (Company c : list) {
            System.out.printf("%-5d %-24s %-30s %-14s %-9s%n", c.getId(), cut(c.getName(), 24),
                    cut(c.getEmail(), 30), cut(c.getLocation(), 14), c.getStatus());
        }
    }

    public static void myApplications(List<Application> list) {
        if (list.isEmpty()) {
            System.out.println("  (you have not applied to any job yet)");
            return;
        }
        System.out.printf("%-6s %-28s %-22s %-12s %-20s%n", "AppID", "Job", "Company", "Status", "Applied At");
        System.out.println("-".repeat(92));
        for (Application a : list) {
            System.out.printf("%-6d %-28s %-22s %-12s %-20s%n", a.getId(), cut(a.getJobTitle(), 28),
                    cut(a.getCompanyName(), 22), a.getStatus(), cut(a.getAppliedAt(), 19));
        }
    }

    public static void applicants(List<Application> list) {
        if (list.isEmpty()) {
            System.out.println("  (no applicants yet)");
            return;
        }
        System.out.printf("%-6s %-20s %-28s %-6s %-12s %-24s%n", "AppID", "Name", "Email", "CGPA", "Status", "Skills");
        System.out.println("-".repeat(102));
        for (Application a : list) {
            System.out.printf("%-6d %-20s %-28s %-6.2f %-12s %-24s%n", a.getId(), cut(a.getStudentName(), 20),
                    cut(a.getStudentEmail(), 28), a.getCgpa(), a.getStatus(), cut(a.getSkills(), 24));
        }
    }
}
