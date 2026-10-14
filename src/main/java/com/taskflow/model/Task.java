package com.taskflow.model;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

/** A single task. JSON-B turns this into JSON automatically. */
@JsonbPropertyOrder({"id", "title", "description", "status", "priority", "dueDate", "overdue", "createdAt"})
public class Task {

    private Long id;

    @NotBlank(message = "title is required")
    @Size(max = 100, message = "title must be 100 characters or fewer")
    private String title;

    @Size(max = 500, message = "description must be 500 characters or fewer")
    private String description;

    private TaskStatus status = TaskStatus.TODO;

    /** NEW in step 3: how urgent the task is (defaults to MEDIUM). */
    private Priority priority = Priority.MEDIUM;

    /** NEW in step 3: optional deadline, sent as JSON "2026-10-20". */
    private LocalDate dueDate;

    private Instant createdAt;

    public Task() {
        // needed by JSON-B
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Task(String title, String description, Priority priority, LocalDate dueDate) {
        this(title, description);
        this.priority = priority;
        this.dueDate = dueDate;
    }

    /**
     * NEW in step 3: calculated, not stored. A task is overdue when it has a due date
     * in the past and isn't DONE. There is no setter, so clients can't change it.
     */
    public boolean isOverdue() {
        return dueDate != null
                && dueDate.isBefore(LocalDate.now())
                && status != TaskStatus.DONE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
