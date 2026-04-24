package com.smartcampus.exception.mapper;

import com.smartcampus.exception.RoomNotEmptyException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException to HTTP 409 Conflict with structured JSON error body.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Conflict");
        error.put("status", 409);
        error.put("message", exception.getMessage());
        error.put("details", "The room contains active hardware and cannot be decommissioned.");

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
