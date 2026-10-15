package com.healthcare.model;

public class Doctor extends User {
    private String specialization;
    @Override public Role getRole() { return Role.DOCTOR; }
    @Override public String getExtra() { return specialization == null ? "" : specialization; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
