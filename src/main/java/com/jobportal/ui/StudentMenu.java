package com.jobportal.ui;

import com.jobportal.dao.ApplicationDAO;
import com.jobportal.dao.JobDAO;
import com.jobportal.dao.SkillDAO;
import com.jobportal.dao.StudentDAO;
import com.jobportal.exception.PortalException;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.Student;
import com.jobportal.util.FileUtil;
import com.jobportal.util.InputUtil;
import com.jobportal.util.PasswordUtil;

import java.util.List;

public class StudentMenu {

    private final StudentDAO studentDAO = new StudentDAO();
    private final SkillDAO skillDAO = new SkillDAO();
    private final JobDAO jobDAO = new JobDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();

    // ---------- Register / Login ----------
    public void register() {
        Display.title("STUDENT REGISTRATION");
        try {
            Student s = new Student();
            s.setName(InputUtil.required("Full name   : "));
            s.setEmail(InputUtil.email("Email       : "));
            s.setPasswordHash(PasswordUtil.hash(InputUtil.password("Password (min 6 chars): ")));
            s.setPhone(InputUtil.phone("Phone (10 digits): "));
            s.setBranch(InputUtil.required("Branch      : "));
            s.setCgpa(InputUtil.decimal("CGPA (0-10)  : ", 0, 10));
            int id = studentDAO.register(s);
            FileUtil.log("Student registered id=" + id + " email=" + s.getEmail());
            System.out.println("\n  Registration successful! Your Student ID is " + id + ". Please login.");
        } catch (PortalException e) {
            System.out.println("\n  ! " + e.getMessage());
        }
    }

    public void login() {
        Display.title("STUDENT LOGIN");
        try {
            String email = InputUtil.email("Email    : ");
            String pass = InputUtil.password("Password : ");
            Student s = studentDAO.login(email, PasswordUtil.hash(pass));
            if (s == null) {
                System.out.println("\n  ! Invalid email or password.");
                return;
            }
            if ("BLOCKED".equals(s.getStatus())) {
                System.out.println("\n  ! Your account has been blocked by the admin.");
                return;
            }
            FileUtil.log("Student login id=" + s.getId());
            System.out.println("\n  Welcome, " + s.getName() + "!");
            dashboard(s);
        } catch (PortalException e) {
            System.out.println("\n  ! " + e.getMessage());
        }
    }

    // ---------- Dashboard ----------
    private void dashboard(Student student) {
        while (true) {
            Display.title("STUDENT MENU  -  " + student.getName());
            System.out.println("  1. View Profile");
            System.out.println("  2. Edit Profile");
            System.out.println("  3. Manage Skills");
            System.out.println("  4. Set Resume Path");
            System.out.println("  5. Search Jobs");
            System.out.println("  6. Apply for Job");
            System.out.println("  7. View Application Status");
            System.out.println("  8. Change Password");
            System.out.println("  0. Logout");
            int choice = InputUtil.integer("Choose: ");
            try {
                switch (choice) {
                    case 1: viewProfile(student); break;
                    case 2: editProfile(student); break;
                    case 3: manageSkills(student); break;
                    case 4: setResume(student); break;
                    case 5: searchJobs(); break;
                    case 6: applyJob(student); break;
                    case 7: Display.myApplications(applicationDAO.findByStudent(student.getId())); break;
                    case 8: changePassword(student); break;
                    case 0: return;
                    default: System.out.println("  ! Invalid option.");
                }
            } catch (PortalException e) {
                System.out.println("\n  ! " + e.getMessage());
            }
            InputUtil.pause();
        }
    }

    private void viewProfile(Student student) throws PortalException {
        Student fresh = studentDAO.findById(student.getId());
        fresh.setSkills(skillDAO.findByStudent(student.getId()));
        Display.student(fresh);
    }

    private void editProfile(Student student) throws PortalException {
        Student fresh = studentDAO.findById(student.getId());
        System.out.println("  (press Enter to keep the current value)");
        fresh.setName(InputUtil.optional("Name", fresh.getName()));
        String phone = InputUtil.optional("Phone", fresh.getPhone());
        while (!phone.matches("[0-9]{10}")) {
            System.out.println("  ! Phone must be exactly 10 digits.");
            phone = InputUtil.optional("Phone", fresh.getPhone());
        }
        fresh.setPhone(phone);
        fresh.setBranch(InputUtil.optional("Branch", fresh.getBranch()));
        String cg = InputUtil.optional("CGPA", String.format("%.2f", fresh.getCgpa()));
        try {
            double v = Double.parseDouble(cg);
            if (v < 0 || v > 10) throw new NumberFormatException();
            fresh.setCgpa(v);
        } catch (NumberFormatException e) {
            System.out.println("  ! Invalid CGPA, keeping the old value.");
        }
        studentDAO.updateProfile(fresh);
        student.setName(fresh.getName());
        FileUtil.log("Student profile updated id=" + student.getId());
        System.out.println("  Profile updated.");
    }

    private void manageSkills(Student student) throws PortalException {
        while (true) {
            List<String> skills = skillDAO.findByStudent(student.getId());
            System.out.println("\n  Your skills: " + (skills.isEmpty() ? "(none)" : String.join(", ", skills)));
            System.out.println("  1. Add skill   2. Remove skill   0. Back");
            int c = InputUtil.integer("Choose: ");
            if (c == 0) return;
            if (c == 1) {
                skillDAO.add(student.getId(), InputUtil.required("Skill name: "));
                System.out.println("  Skill added.");
            } else if (c == 2) {
                boolean ok = skillDAO.remove(student.getId(), InputUtil.required("Skill to remove: "));
                System.out.println(ok ? "  Skill removed." : "  ! Skill not found.");
            } else {
                System.out.println("  ! Invalid option.");
            }
        }
    }

    private void setResume(Student student) throws PortalException {
        String path = InputUtil.required("Full path to your resume (.pdf/.doc/.docx): ");
        String valid = FileUtil.validateResumePath(path);
        studentDAO.updateResumePath(student.getId(), valid);
        FileUtil.log("Resume path set student=" + student.getId());
        System.out.println("  Resume path saved.");
    }

    private void searchJobs() throws PortalException {
        Display.title("SEARCH JOBS  (press Enter to skip a filter)");
        String keyword = InputUtil.text("Keyword (title / skill / company): ");
        String location = InputUtil.text("Location: ");
        String type = InputUtil.text("Type (FULL_TIME / INTERNSHIP / PART_TIME / CONTRACT): ").toUpperCase();
        if (!type.isEmpty() && !List.of("FULL_TIME", "INTERNSHIP", "PART_TIME", "CONTRACT").contains(type)) {
            System.out.println("  ! Unknown job type, ignoring that filter.");
            type = "";
        }
        List<Job> jobs = jobDAO.search(keyword, location, type);
        Display.jobs(jobs);
        if (!jobs.isEmpty() && InputUtil.confirm("\nView details of a job?")) {
            Job j = jobDAO.findById(InputUtil.integer("Job ID: "));
            if (j == null) System.out.println("  ! Job not found.");
            else Display.jobDetail(j);
        }
    }

    private void applyJob(Student student) throws PortalException {
        Display.jobs(jobDAO.search("", "", ""));
        int jobId = InputUtil.integer("\nEnter Job ID to apply (0 to cancel): ");
        if (jobId == 0) return;
        Job job = jobDAO.findById(jobId);
        if (job == null) throw new PortalException("Job not found.");
        if (!"OPEN".equals(job.getStatus())) throw new PortalException("This job is closed.");

        Student fresh = studentDAO.findById(student.getId());
        if (fresh.getResumePath() == null || fresh.getResumePath().isEmpty()) {
            throw new PortalException("Set your resume path (option 4) before applying.");
        }
        if (fresh.getCgpa() < job.getMinCgpa()) {
            throw new PortalException(String.format("Your CGPA %.2f is below the required %.2f.",
                    fresh.getCgpa(), job.getMinCgpa()));
        }
        applicationDAO.apply(jobId, student.getId());
        FileUtil.log("Application student=" + student.getId() + " job=" + jobId);
        System.out.println("  Applied successfully to '" + job.getTitle() + "' at " + job.getCompanyName() + ".");
    }

    private void changePassword(Student student) throws PortalException {
        String old = InputUtil.password("Current password: ");
        Student check = studentDAO.login(student.getEmail(), PasswordUtil.hash(old));
        if (check == null) throw new PortalException("Current password is incorrect.");
        String neu = InputUtil.password("New password: ");
        studentDAO.updatePassword(student.getId(), PasswordUtil.hash(neu));
        System.out.println("  Password changed.");
    }
}
