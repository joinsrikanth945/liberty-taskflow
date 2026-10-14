package com.taskflow.repository;

import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores tasks in memory (data resets when the server restarts).
 *
 * This is the ONLY class that knows how tasks are stored. To move to a real
 * database later, rewrite this class with JPA; the REST layer stays the same.
 */
@ApplicationScoped
public class TaskRepository {

    /** The ways a task list can be sorted (step 3). */
    public enum SortBy {
        ID(Comparator.comparing(Task::getId)),
        // HIGH first, then MEDIUM, then LOW; ties broken by soonest due date
        PRIORITY(Comparator.comparing(Task::getPriority)
                .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))),
        // soonest due date first; tasks with no due date go last
        DUE_DATE(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getPriority)),
        // newest first
        CREATED_AT(Comparator.comparing(Task::getCreatedAt).reversed());

        private final Comparator<Task> comparator;

        SortBy(Comparator<Task> comparator) {
            this.comparator = comparator;
        }
    }

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @PostConstruct
    void seed() {
        LocalDate today = LocalDate.now();
        save(new Task("Install WebSphere Liberty", "Run mvn liberty:dev", Priority.HIGH, today.minusDays(1)));
        save(new Task("Try the API", "Open http://localhost:9080/openapi/ui", Priority.MEDIUM, today.plusDays(2)));
        save(new Task("Read about MicroProfile", "Health, Config, Metrics", Priority.LOW, null));
    }

    /** status and priority are optional filters (null = no filter). */
    public List<Task> findAll(TaskStatus status, Priority priority, SortBy sortBy) {
        return tasks.values().stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> priority == null || t.getPriority() == priority)
                .sorted(sortBy == null ? SortBy.ID.comparator : sortBy.comparator)
                .toList();
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(nextId.getAndIncrement());
            task.setCreatedAt(Instant.now());
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(Priority.MEDIUM);
        }
        tasks.put(task.getId(), task);
        return task;
    }

    public boolean delete(Long id) {
        return tasks.remove(id) != null;
    }

    public int count() {
        return tasks.size();
    }
}
