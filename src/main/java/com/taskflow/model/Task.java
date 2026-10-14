package com.taskflow.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** A single task. JSON-B turns this into JSON automatically. */
public class Task {

    private Long id;

    @NotBlank(message = "title is required")
    @Size(max = 100, message = "title must be 100 characters or fewer")
    private String title;

    @Size(max = 500, message = "description must be 500 characters or fewer")
    private String description;

    private TaskStatus status = TaskStatus.TODO;

    private Instant createdAt;

    public Task() {
        // needed by JSON-B
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
