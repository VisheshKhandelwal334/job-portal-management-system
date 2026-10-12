package com.jobportal.dao;

import com.jobportal.config.DBConnection;
import com.jobportal.exception.PortalException;
import com.jobportal.model.Job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {

    private static final String SELECT =
            "SELECT j.job_id, j.company_id, c.name AS company_name, j.title, j.description, j.location, "
          + "j.salary, j.job_type, j.required_skills, j.min_cgpa, j.status, j.posted_at "
          + "FROM jobs j JOIN companies c ON c.company_id = j.company_id ";

    private Job map(ResultSet rs) throws SQLException {
        Job j = new Job();
        j.setId(rs.getInt("job_id"));
        j.setCompanyId(rs.getInt("company_id"));
        j.setCompanyName(rs.getString("company_name"));
        j.setTitle(rs.getString("title"));
        j.setDescription(rs.getString("description"));
        j.setLocation(rs.getString("location"));
        j.setSalary(rs.getDouble("salary"));
        j.setJobType(rs.getString("job_type"));
        j.setRequiredSkills(rs.getString("required_skills"));
        j.setMinCgpa(rs.getDouble("min_cgpa"));
        j.setStatus(rs.getString("status"));
        j.setPostedAt(String.valueOf(rs.getTimestamp("posted_at")));
        return j;
    }

    public int create(Job j) throws PortalException {
        String sql = "INSERT INTO jobs (company_id, title, description, location, salary, job_type, required_skills, min_cgpa) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, j.getCompanyId());
            ps.setString(2, j.getTitle());
            ps.setString(3, j.getDescription());
            ps.setString(4, j.getLocation());
            ps.setDouble(5, j.getSalary());
            ps.setString(6, j.getJobType());
            ps.setString(7, j.getRequiredSkills());
            ps.setDouble(8, j.getMinCgpa());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new PortalException("Could not post job: " + e.getMessage(), e);
        }
    }

    public Job findById(int jobId) throws PortalException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE j.job_id=?")) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new PortalException("Could not load job: " + e.getMessage(), e);
        }
    }

    public List<Job> findByCompany(int companyId) throws PortalException {
        return query(SELECT + "WHERE j.company_id=? ORDER BY j.job_id DESC", List.of(companyId));
    }

    public List<Job> findAll() throws PortalException {
        return query(SELECT + "ORDER BY j.job_id DESC", List.of());
    }

    /** Search OPEN jobs. Any blank filter is ignored. */
    public List<Job> search(String keyword, String location, String jobType) throws PortalException {
        StringBuilder sql = new StringBuilder(SELECT)
                .append("WHERE j.status='OPEN' AND c.status='APPROVED' ");
        List<Object> params = new ArrayList<>();
        if (!keyword.isEmpty()) {
            sql.append("AND (j.title LIKE ? OR j.required_skills LIKE ? OR c.name LIKE ?) ");
            String like = "%" + keyword + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (!location.isEmpty()) {
            sql.append("AND j.location LIKE ? ");
            params.add("%" + location + "%");
        }
        if (!jobType.isEmpty()) {
            sql.append("AND j.job_type = ? ");
            params.add(jobType);
        }
        sql.append("ORDER BY j.posted_at DESC");
        return query(sql.toString(), params);
    }

    public void setStatus(int jobId, String status) throws PortalException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE jobs SET status=? WHERE job_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, jobId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PortalException("Could not update job: " + e.getMessage(), e);
        }
    }

    public boolean delete(int jobId) throws PortalException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM jobs WHERE job_id=?")) {
            ps.setInt(1, jobId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PortalException("Delete failed: " + e.getMessage(), e);
        }
    }

    private List<Job> query(String sql, List<?> params) throws PortalException {
        List<Job> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new PortalException("Job query failed: " + e.getMessage(), e);
        }
        return list;
    }
}
