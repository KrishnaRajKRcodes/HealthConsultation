package com.healthcare.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Abstract base of the three user types (inheritance + polymorphism). */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected String phone;
    protected boolean active = true;
    protected LocalDateTime createdAt;

    public abstract Role getRole();

    public String getRoleName() { return getRole().name(); }
    public String getRoleLabel() { String n = getRole().name(); return n.charAt(0) + n.substring(1).toLowerCase(); }
    /** Extra role-specific value shown in forms (specialization for doctors, DOB for patients). */
    public String getExtra() { return ""; }

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
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
