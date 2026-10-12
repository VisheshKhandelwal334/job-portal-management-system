package com.jobportal.dao;

import com.jobportal.config.DBConnection;
import com.jobportal.exception.PortalException;
import com.jobportal.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private static final String COLS =
            "student_id, name, email, password_hash, phone, branch, cgpa, resume_path, status";

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("student_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setPhone(rs.getString("phone"));
        s.setBranch(rs.getString("branch"));
        s.setCgpa(rs.getDouble("cgpa"));
        s.setResumePath(rs.getString("resume_path"));
        s.setStatus(rs.getString("status"));
        return s;
    }

    public int register(Student s) throws PortalException {
        String sql = "INSERT INTO students (name, email, password_hash, phone, branch, cgpa) VALUES (?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPasswordHash());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getBranch());
            ps.setDouble(6, s.getCgpa());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new PortalException("This email is already registered.");
            throw new PortalException("Registration failed: " + e.getMessage(), e);
        }
    }

    /** Returns null when credentials are wrong. */
    public Student login(String email, String passwordHash) throws PortalException {
        String sql = "SELECT " + COLS + " FROM students WHERE email=? AND password_hash=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new PortalException("Login failed: " + e.getMessage(), e);
        }
    }

    public Student findById(int id) throws PortalException {
        String sql = "SELECT " + COLS + " FROM students WHERE student_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new PortalException("Could not load student: " + e.getMessage(), e);
        }
    }

    public void updateProfile(Student s) throws PortalException {
        String sql = "UPDATE students SET name=?, phone=?, branch=?, cgpa=? WHERE student_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getBranch());
            ps.setDouble(4, s.getCgpa());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PortalException("Profile update failed: " + e.getMessage(), e);
        }
    }

    public void updateResumePath(int studentId, String path) throws PortalException {
        executeUpdate("UPDATE students SET resume_path=? WHERE student_id=?", path, studentId);
    }

    public void updatePassword(int studentId, String hash) throws PortalException {
        executeUpdate("UPDATE students SET password_hash=? WHERE student_id=?", hash, studentId);
    }

    public void setStatus(int studentId, String status) throws PortalException {
        executeUpdate("UPDATE students SET status=? WHERE student_id=?", status, studentId);
    }

    public boolean delete(int studentId) throws PortalException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PortalException("Delete failed: " + e.getMessage(), e);
        }
    }

    public List<Student> findAll() throws PortalException {
        List<Student> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM students ORDER BY student_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new PortalException("Could not list students: " + e.getMessage(), e);
        }
        return list;
    }

    private void executeUpdate(String sql, String value, int id) throws PortalException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PortalException("Update failed: " + e.getMessage(), e);
        }
    }
}
