package com.smartcampus.exception.mapper;

import com.smartcampus.exception.SensorUnavailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException to HTTP 403 Forbidden with structured JSON error body.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("status", 403);
        error.put("message", exception.getMessage());
        error.put("details", "The sensor is physically disconnected or in maintenance mode.");

        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
