package com.smartcampus.exception.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity with structured JSON error body.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Unprocessable Entity");
        error.put("status", 422);
        error.put("message", exception.getMessage());
        error.put("details", "The request body contains a reference to a non-existent resource.");

        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
