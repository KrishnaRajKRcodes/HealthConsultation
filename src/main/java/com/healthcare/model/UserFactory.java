package com.healthcare.model;

/** Factory: creates the right User subclass for a Role. */
public final class UserFactory {
    private UserFactory() { }

    public static User create(Role role) {
        switch (role) {
            case ADMIN:   return new Admin();
            case DOCTOR:  return new Doctor();
            case PATIENT: return new Patient();
            default: throw new IllegalArgumentException("Unknown role " + role);
        }
    }
}
