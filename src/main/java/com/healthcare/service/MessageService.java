package com.healthcare.service;

import com.healthcare.dao.*;
import com.healthcare.model.*;
import com.healthcare.util.*;

/** Messaging is only allowed between a patient and a doctor they are connected with. */
public class MessageService {
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public void send(User sender, int receiverId, String body) throws DAOException, ValidationException {
        String text = ValidationUtil.required(body, "Message");
        if (text.length() > 2000) throw new ValidationException("Message is too long (max 2000 characters).");
        User receiver = userDAO.findById(receiverId);
        if (receiver == null) throw new ValidationException("Recipient not found.");
        boolean allowed;
        if (sender.getRole() == Role.PATIENT)
            allowed = receiver.getRole() == Role.DOCTOR;
        else if (sender.getRole() == Role.DOCTOR)
            allowed = receiver.getRole() == Role.PATIENT && appointmentDAO.hasRelationship(sender.getId(), receiverId);
        else
            allowed = false;
        if (!allowed) throw new ValidationException("You cannot message this user.");
        messageDAO.send(sender.getId(), receiverId, text);
    }
}
