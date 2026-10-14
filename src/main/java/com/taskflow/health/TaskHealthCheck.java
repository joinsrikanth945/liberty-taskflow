package com.taskflow.health;

import com.taskflow.repository.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/** Shows up at http://localhost:9080/health/ready */
@Readiness
@ApplicationScoped
public class TaskHealthCheck implements HealthCheck {

    @Inject
    TaskRepository repository;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("task-repository")
                .up()
                .withData("taskCount", repository.count())
                .build();
    }
}
