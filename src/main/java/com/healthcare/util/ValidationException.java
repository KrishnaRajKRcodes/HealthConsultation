package com.healthcare.util;

/** Checked exception for invalid user input / business-rule violations. */
public class ValidationException extends Exception {
    public ValidationException(String message) { super(message); }
}
