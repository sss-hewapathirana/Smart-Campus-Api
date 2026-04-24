package com.smartcampus.exception.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Global safety net - catches all unhandled exceptions.
 * Returns HTTP 500 with safe generic message (never exposes stack traces).
 * Delegates WebApplicationException subtypes back to Jersey for proper status codes.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // Let Jersey handle its own exceptions (NotFoundException, BadRequestException, etc.)
        if (exception instanceof WebApplicationException) {
            Response original = ((WebApplicationException) exception).getResponse();
            Map<String, Object> error = new HashMap<>();
            error.put("error", original.getStatusInfo().getReasonPhrase());
            error.put("status", original.getStatus());
            error.put("message", exception.getMessage());
            return Response.status(original.getStatus())
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        Map<String, Object> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("status", 500);
        error.put("message", "An unexpected error occurred. Please contact support.");

        // Log server-side only
        exception.printStackTrace();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
