package com.finance.dashboard.exception;

// Thrown when a user tries an action their role doesn't permit
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}