package com.taskflow.rest;

import jakarta.json.Json;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Returns 404s as clean JSON: {"error": "Task 7 not found"} */
@Provider
public class ErrorMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException e) {
        String body = Json.createObjectBuilder()
                .add("error", e.getMessage() == null ? "Not found" : e.getMessage())
                .build()
                .toString();
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
