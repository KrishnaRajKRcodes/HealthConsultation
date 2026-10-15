package com.healthcare.model;

public enum Role {
    ADMIN("/admin"), DOCTOR("/doctor"), PATIENT("/patient");

    private final String home;
    Role(String home) { this.home = home; }
    public String getHome() { return home; }
}
