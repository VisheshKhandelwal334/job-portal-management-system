package com.jobportal.dao;

import com.jobportal.config.DBConnection;
import com.jobportal.exception.PortalException;
import com.jobportal.model.Company;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CompanyDAO {

    private static final String COLS =
            "company_id, name, email, password_hash, industry, location, website, description, status";

    private Company map(ResultSet rs) throws SQLException {
        Company c = new Company();
        c.setId(rs.getInt("company_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setIndustry(rs.getString("industry"));
        c.setLocation(rs.getString("location"));
        c.setWebsite(rs.getString("website"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        return c;
    }

    public int register(Company co) throws PortalException {
        String sql = "INSERT INTO companies (name, email, password_hash, industry, location, website, description) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, co.getName());
            ps.setString(2, co.getEmail());
            ps.setString(3, co.getPasswordHash());
            ps.setString(4, co.getIndustry());
            ps.setString(5, co.getLocation());
            ps.setString(6, co.getWebsite());
            ps.setString(7, co.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new PortalException("This email is already registered.");
            throw new PortalException("Registration failed: " + e.getMessage(), e);
        }
    }

    public Company login(String email, String passwordHash) throws PortalException {
        String sql = "SELECT " + COLS + " FROM companies WHERE email=? AND password_hash=?";
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

    public Company findById(int id) throws PortalException {
        String sql = "SELECT " + COLS + " FROM companies WHERE company_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new PortalException("Could not load company: " + e.getMessage(), e);
        }
    }

    public void updateProfile(Company co) throws PortalException {
        String sql = "UPDATE companies SET name=?, industry=?, location=?, website=?, description=? WHERE company_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, co.getName());
            ps.setString(2, co.getIndustry());
            ps.setString(3, co.getLocation());
            ps.setString(4, co.getWebsite());
            ps.setString(5, co.getDescription());
            ps.setInt(6, co.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PortalException("Profile update failed: " + e.getMessage(), e);
        }
    }

    public void setStatus(int companyId, String status) throws PortalException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE companies SET status=? WHERE company_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, companyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PortalException("Status update failed: " + e.getMessage(), e);
        }
    }

    public boolean delete(int companyId) throws PortalException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM companies WHERE company_id=?")) {
            ps.setInt(1, companyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PortalException("Delete failed: " + e.getMessage(), e);
        }
    }

    public List<Company> findAll() throws PortalException {
        List<Company> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM companies ORDER BY company_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new PortalException("Could not list companies: " + e.getMessage(), e);
        }
        return list;
    }
}
