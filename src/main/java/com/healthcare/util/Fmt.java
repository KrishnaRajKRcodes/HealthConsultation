package com.healthcare.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Fmt {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private Fmt() { }
    public static String dateTime(LocalDateTime t) { return t == null ? "" : t.format(DT); }
}
