package com.jobportal.dao;

import com.jobportal.config.DBConnection;
import com.jobportal.exception.PortalException;
import com.jobportal.model.Application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    public void apply(int jobId, int studentId) throws PortalException {
        String sql = "INSERT INTO applications (job_id, student_id) VALUES (?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new PortalException("You have already applied to this job.");
            throw new PortalException("Could not apply: " + e.getMessage(), e);
        }
    }

    /** All applications made by one student (View Application Status). */
    public List<Application> findByStudent(int studentId) throws PortalException {
        String sql = "SELECT a.application_id, a.job_id, a.student_id, a.status, a.applied_at, "
                + "j.title AS job_title, c.name AS company_name "
                + "FROM applications a JOIN jobs j ON j.job_id=a.job_id "
                + "JOIN companies c ON c.company_id=j.company_id "
                + "WHERE a.student_id=? ORDER BY a.applied_at DESC";
        List<Application> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Application a = base(rs);
                    a.setJobTitle(rs.getString("job_title"));
                    a.setCompanyName(rs.getString("company_name"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new PortalException("Could not load applications: " + e.getMessage(), e);
        }
        return list;
    }

    /** Applicants of one job, only if the job belongs to the given company. */
    public List<Application> findApplicants(int jobId, int companyId) throws PortalException {
        String sql = "SELECT a.application_id, a.job_id, a.student_id, a.status, a.applied_at, "
                + "s.name AS student_name, s.email AS student_email, s.cgpa, s.resume_path, "
                + "(SELECT GROUP_CONCAT(k.skill_name SEPARATOR ', ') FROM skills k WHERE k.student_id=s.student_id) AS skills "
                + "FROM applications a JOIN jobs j ON j.job_id=a.job_id "
                + "JOIN students s ON s.student_id=a.student_id "
                + "WHERE a.job_id=? AND j.company_id=? ORDER BY s.cgpa DESC";
        List<Application> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            ps.setInt(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Application a = base(rs);
                    a.setStudentName(rs.getString("student_name"));
                    a.setStudentEmail(rs.getString("student_email"));
                    a.setCgpa(rs.getDouble("cgpa"));
                    a.setResumePath(rs.getString("resume_path"));
                    a.setSkills(rs.getString("skills"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new PortalException("Could not load applicants: " + e.getMessage(), e);
        }
        return list;
    }

    /** Shortlist / reject; the join guarantees a company can only touch its own applicants. */
    public boolean updateStatus(int applicationId, int companyId, String status) throws PortalException {
        String sql = "UPDATE applications a JOIN jobs j ON j.job_id=a.job_id "
                + "SET a.status=? WHERE a.application_id=? AND j.company_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, applicationId);
            ps.setInt(3, companyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PortalException("Could not update application: " + e.getMessage(), e);
        }
    }

    private Application base(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setId(rs.getInt("application_id"));
        a.setJobId(rs.getInt("job_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setStatus(rs.getString("status"));
        a.setAppliedAt(String.valueOf(rs.getTimestamp("applied_at")));
        return a;
    }
}
