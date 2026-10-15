package com.healthcare.model;

import java.time.LocalDate;

public class Patient extends User {
    private LocalDate dateOfBirth;
    @Override public Role getRole() { return Role.PATIENT; }
    @Override public String getExtra() { return dateOfBirth == null ? "" : dateOfBirth.toString(); }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}
