package com.taskflow.rest;

import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.TaskRepository.SortBy;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.net.URI;
import java.util.List;

/**
 * REST endpoints for tasks:
 *   GET    /api/tasks            list, with optional filters and sorting:
 *                                 ?status=TODO|IN_PROGRESS|DONE
 *                                 ?priority=HIGH|MEDIUM|LOW
 *                                 ?sort=id|priority|dueDate|createdAt
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
    @Operation(summary = "List tasks", description = "Optionally filter by status and priority, and sort the result.")
    public List<Task> list(
            @QueryParam("status") TaskStatus status,
            @QueryParam("priority") Priority priority,
            @Parameter(description = "id (default), priority, dueDate or createdAt")
            @QueryParam("sort") String sort) {
        return repository.findAll(status, priority, parseSort(sort));
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get one task")
    public Task get(@PathParam("id") Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task " + id + " not found"));
    }

    @POST
    @Operation(summary = "Create a task")
    public Response create(@Valid Task task, @Context UriInfo uriInfo) {
        task.setId(null); // the server assigns ids
        Task saved = repository.save(task);
        URI location = uriInfo.getAbsolutePathBuilder().path(saved.getId().toString()).build();
        return Response.created(location).entity(saved).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a task")
    public Task update(@PathParam("id") Long id, @Valid Task changes) {
        Task existing = get(id);
        existing.setTitle(changes.getTitle());
        existing.setDescription(changes.getDescription());
        existing.setDueDate(changes.getDueDate());
        if (changes.getStatus() != null) {
            existing.setStatus(changes.getStatus());
        }
        if (changes.getPriority() != null) {
            existing.setPriority(changes.getPriority());
        }
        return repository.save(existing);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a task")
    public Response delete(@PathParam("id") Long id) {
        if (!repository.delete(id)) {
            throw new NotFoundException("Task " + id + " not found");
        }
        return Response.noContent().build();
    }

    /** Turns ?sort=dueDate into SortBy.DUE_DATE. Missing = sort by id. */
    private SortBy parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return SortBy.ID;
        }
        return switch (sort.trim().toLowerCase()) {
            case "id" -> SortBy.ID;
            case "priority" -> SortBy.PRIORITY;
            case "duedate", "due_date" -> SortBy.DUE_DATE;
            case "createdat", "created_at" -> SortBy.CREATED_AT;
            default -> throw new BadRequestException(
                    "Unknown sort '" + sort + "'. Use id, priority, dueDate or createdAt.");
        };
    }
}
