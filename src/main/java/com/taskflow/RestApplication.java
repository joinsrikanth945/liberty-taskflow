package com.taskflow;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

/** Every REST endpoint in this app lives under /api */
@ApplicationPath("/api")
@OpenAPIDefinition(info = @Info(title = "TaskFlow", version = "1.0",
        description = "A simple task manager running on WebSphere Liberty"))
public class RestApplication extends Application {
}
