package com.taskflow.rest;

import jakarta.json.Json;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Returns API errors as clean JSON, e.g. {"status": 404, "error": "Task 7 not found"}.
 * Step 3: now handles every HTTP error (404 not found, 400 bad request, ...), not just 404.
 */
@Provider
public class ErrorMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        String message = e.getMessage() == null ? "HTTP " + status : e.getMessage();
        String body = Json.createObjectBuilder()
                .add("status", status)
                .add("error", message)
                .build()
                .toString();
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
