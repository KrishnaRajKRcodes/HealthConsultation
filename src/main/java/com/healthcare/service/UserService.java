package com.healthcare.service;

import com.healthcare.dao.UserDAO;
import com.healthcare.model.*;
import com.healthcare.util.*;

/** Business rules for accounts: validation, hashing, authentication. */
public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String email, String password) throws DAOException, ValidationException {
        String mail = ValidationUtil.email(email);
        User u = userDAO.findByEmail(mail);
        if (u == null || password == null || !PasswordUtil.verify(password, u.getPasswordHash()))
            throw new ValidationException("Invalid email or password.");
        if (!u.isActive()) throw new ValidationException("This account has been disabled. Contact an administrator.");
        return u;
    }

    /** extra = specialization (doctor) or date of birth yyyy-MM-dd (patient, optional). */
    public User createUser(String name, String email, String password, Role role, String phone, String extra)
            throws DAOException, ValidationException {
        User u = build(role, name, email, phone, extra);
        u.setPasswordHash(PasswordUtil.hash(ValidationUtil.password(password)));
        u.setId(userDAO.insert(u));
        return u;
    }

    public void updateUser(int id, String name, String email, Role role, String phone, String extra, boolean active)
            throws DAOException, ValidationException {
        User u = build(role, name, email, phone, extra);
        u.setId(id);
        u.setActive(active);
        if (!userDAO.update(u)) throw new ValidationException("User not found.");
    }

    /** Self-service profile update; optional password change. Returns the refreshed user. */
    public User updateProfile(User current, String name, String phone, String extra, String newPassword)
            throws DAOException, ValidationException {
        User u = build(current.getRole(), name, current.getEmail(), phone, extra);
        u.setId(current.getId());
        u.setActive(current.isActive());
        userDAO.update(u);
        if (newPassword != null && !newPassword.isEmpty())
            userDAO.updatePassword(current.getId(), PasswordUtil.hash(ValidationUtil.password(newPassword)));
        return userDAO.findById(current.getId());
    }

    private User build(Role role, String name, String email, String phone, String extra) throws ValidationException {
        User u = UserFactory.create(role);
        u.setName(ValidationUtil.required(name, "Name"));
        u.setEmail(ValidationUtil.email(email));
        u.setPhone(ValidationUtil.optionalPhone(phone));
        if (u instanceof Doctor) ((Doctor) u).setSpecialization(ValidationUtil.required(extra, "Specialization"));
        if (u instanceof Patient) {
            java.time.LocalDate dob = ValidationUtil.optionalDate(extra, "Date of birth");
            if (dob != null && dob.isAfter(java.time.LocalDate.now()))
                throw new ValidationException("Date of birth cannot be in the future.");
            ((Patient) u).setDateOfBirth(dob);
        }
        return u;
    }
}
