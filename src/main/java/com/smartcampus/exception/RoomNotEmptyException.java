package com.smartcampus.exception;

/**
 * Thrown when attempting to delete a room that still contains sensors.
 * Results in HTTP 409 Conflict.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
