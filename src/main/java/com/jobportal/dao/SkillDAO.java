package com.jobportal.dao;

import com.jobportal.config.DBConnection;
import com.jobportal.exception.PortalException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SkillDAO {

    public void add(int studentId, String skill) throws PortalException {
        String sql = "INSERT INTO skills (student_id, skill_name) VALUES (?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, skill);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new PortalException("You already have this skill.");
            throw new PortalException("Could not add skill: " + e.getMessage(), e);
        }
    }

    public boolean remove(int studentId, String skill) throws PortalException {
        String sql = "DELETE FROM skills WHERE student_id=? AND skill_name=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, skill);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PortalException("Could not remove skill: " + e.getMessage(), e);
        }
    }

    public List<String> findByStudent(int studentId) throws PortalException {
        List<String> skills = new ArrayList<>();
        String sql = "SELECT skill_name FROM skills WHERE student_id=? ORDER BY skill_name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) skills.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new PortalException("Could not load skills: " + e.getMessage(), e);
        }
        return skills;
    }
}
