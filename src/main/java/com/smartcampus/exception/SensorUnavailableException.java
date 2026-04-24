package com.smartcampus.exception;

/**
 * Thrown when attempting to add a reading to a sensor in MAINTENANCE status.
 * Results in HTTP 403 Forbidden.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
