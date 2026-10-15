package com.healthcare.model;

public class Admin extends User {
    @Override public Role getRole() { return Role.ADMIN; }
}
