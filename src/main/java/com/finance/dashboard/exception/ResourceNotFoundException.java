package com.finance.dashboard.exception;

// Thrown when a record with given ID doesn't exist
// This gives us 404 instead of generic 500
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}