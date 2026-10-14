package com.taskflow.rest;

import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.TaskRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

/**
 * REST endpoints for tasks:
 *   GET    /api/tasks            list (optional ?status=TODO|IN_PROGRESS|DONE)
 *   GET    /api/tasks/{id}       get one
 *   POST   /api/tasks            create
 *   PUT    /api/tasks/{id}       update
 *   DELETE /api/tasks/{id}       delete
 */
@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    @Inject
    TaskRepository repository;

    @GET
    public List<Task> list(@QueryParam("status") TaskStatus status) {
        return repository.findAll(status);
    }

    @GET
    @Path("/{id}")
    public Task get(@PathParam("id") Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task " + id + " not found"));
    }

    @POST
    public Response create(@Valid Task task, @Context UriInfo uriInfo) {
        task.setId(null); // the server assigns ids
        Task saved = repository.save(task);
        URI location = uriInfo.getAbsolutePathBuilder().path(saved.getId().toString()).build();
        return Response.created(location).entity(saved).build();
    }

    @PUT
    @Path("/{id}")
    public Task update(@PathParam("id") Long id, @Valid Task changes) {
        Task existing = get(id);
        existing.setTitle(changes.getTitle());
        existing.setDescription(changes.getDescription());
        if (changes.getStatus() != null) {
            existing.setStatus(changes.getStatus());
        }
        return repository.save(existing);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (!repository.delete(id)) {
            throw new NotFoundException("Task " + id + " not found");
        }
        return Response.noContent().build();
    }
}
