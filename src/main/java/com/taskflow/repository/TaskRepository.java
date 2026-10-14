package com.taskflow.repository;

import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.ArrayList;
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

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @PostConstruct
    void seed() {
        save(new Task("Install WebSphere Liberty", "Run mvn liberty:dev"));
        save(new Task("Try the API", "Open http://localhost:9080/openapi/ui"));
    }

    public List<Task> findAll(TaskStatus status) {
        List<Task> result = new ArrayList<>();
        for (Task t : tasks.values()) {
            if (status == null || t.getStatus() == status) {
                result.add(t);
            }
        }
        result.sort(Comparator.comparing(Task::getId));
        return result;
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
