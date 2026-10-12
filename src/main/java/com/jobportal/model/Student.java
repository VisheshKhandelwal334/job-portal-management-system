package com.jobportal.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private String branch;
    private double cgpa;
    private String resumePath;
    private String status;
    private List<String> skills = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
