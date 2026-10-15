package com.healthcare.service;

import com.healthcare.dao.ScheduleDAO;
import com.healthcare.model.Schedule;
import com.healthcare.util.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleService {
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    public void addSlot(int doctorId, String day, String start, String end) throws DAOException, ValidationException {
        DayOfWeek d;
        try { d = DayOfWeek.valueOf(ValidationUtil.required(day, "Day")); }
        catch (IllegalArgumentException e) { throw new ValidationException("Invalid day."); }
        LocalTime s = ValidationUtil.time(start, "Start time");
        LocalTime e = ValidationUtil.time(end, "End time");
        if (!s.isBefore(e)) throw new ValidationException("End time must be after start time.");
        boolean overlaps = scheduleDAO.findByDoctorAndDay(doctorId, d).stream()
                .anyMatch(x -> s.isBefore(x.getEndTime()) && e.isAfter(x.getStartTime()));
        if (overlaps) throw new ValidationException("This overlaps with an existing working period.");
        Schedule sc = new Schedule();
        sc.setDoctorId(doctorId);
        sc.setDay(d);
        sc.setStartTime(s);
        sc.setEndTime(e);
        scheduleDAO.insert(sc);
    }
}
