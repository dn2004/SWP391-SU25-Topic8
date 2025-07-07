package com.fu.swp391.schoolhealthmanagementsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an operation is attempted but cannot be completed due to the current state of the object
 * or other business logic constraints.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
