package com.jobportal.dao;

import com.jobportal.config.DBConnection;
import com.jobportal.exception.PortalException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Admin login and report queries. */
public class AdminDAO {

    public boolean login(String username, String passwordHash) throws PortalException {
        String sql = "SELECT 1 FROM admins WHERE username=? AND password_hash=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new PortalException("Login failed: " + e.getMessage(), e);
        }
    }

    /** Key figures for the dashboard / report, insertion-ordered. */
    public Map<String, Long> summaryCounts() throws PortalException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("Total students",         "SELECT COUNT(*) FROM students");
        queries.put("Blocked students",       "SELECT COUNT(*) FROM students WHERE status='BLOCKED'");
        queries.put("Total companies",        "SELECT COUNT(*) FROM companies");
        queries.put("Companies pending approval", "SELECT COUNT(*) FROM companies WHERE status='PENDING'");
        queries.put("Total jobs",             "SELECT COUNT(*) FROM jobs");
        queries.put("Open jobs",              "SELECT COUNT(*) FROM jobs WHERE status='OPEN'");
        queries.put("Total applications",     "SELECT COUNT(*) FROM applications");
        queries.put("Shortlisted candidates", "SELECT COUNT(*) FROM applications WHERE status='SHORTLISTED'");

        Map<String, Long> result = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection()) {
            for (Map.Entry<String, String> q : queries.entrySet()) {
                try (PreparedStatement ps = c.prepareStatement(q.getValue());
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    result.put(q.getKey(), rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new PortalException("Report failed: " + e.getMessage(), e);
        }
        return result;
    }

    /** Jobs ranked by number of applications. Each row: title, company, count. */
    public List<String[]> topJobsByApplications(int limit) throws PortalException {
        String sql = "SELECT j.title, c.name, COUNT(a.application_id) AS cnt "
                + "FROM jobs j JOIN companies c ON c.company_id=j.company_id "
                + "LEFT JOIN applications a ON a.job_id=j.job_id "
                + "GROUP BY j.job_id, j.title, c.name ORDER BY cnt DESC LIMIT ?";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{rs.getString(1), rs.getString(2), String.valueOf(rs.getInt(3))});
                }
            }
        } catch (SQLException e) {
            throw new PortalException("Report failed: " + e.getMessage(), e);
        }
        return rows;
    }

    /** Most requested skills across all students. Each row: skill, count. */
    public List<String[]> topSkills(int limit) throws PortalException {
        String sql = "SELECT skill_name, COUNT(*) AS cnt FROM skills GROUP BY skill_name ORDER BY cnt DESC LIMIT ?";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new String[]{rs.getString(1), String.valueOf(rs.getInt(2))});
            }
        } catch (SQLException e) {
            throw new PortalException("Report failed: " + e.getMessage(), e);
        }
        return rows;
    }
}
