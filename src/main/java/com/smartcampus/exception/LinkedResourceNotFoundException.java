package com.smartcampus.exception;

/**
 * Thrown when a request references a non-existent resource (e.g., invalid roomId).
 * Results in HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
