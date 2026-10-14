# TaskFlow — task manager on WebSphere Liberty

A small REST API for managing tasks (create, list, update, delete), running on
IBM WebSphere Liberty with Jakarta EE 10 and MicroProfile.

What you get out of the box:

| URL | What it is |
|---|---|
| http://localhost:9080/ | Welcome page with links |
| http://localhost:9080/api/tasks | The REST API (JSON) |
| http://localhost:9080/openapi/ui | Swagger UI: click-to-test every endpoint |
| http://localhost:9080/health | Health check (used by Docker/Kubernetes) |

---

## Part 1 — Install the tools (one time)

You need three things. Everything else (including Liberty itself) is downloaded by Maven.

### 1. Java JDK 17 or 21
Download **IBM Semeru Runtime** (IBM's free JDK, recommended for Liberty) from
https://developer.ibm.com/languages/java/semeru-runtimes/downloads, or any JDK
such as Eclipse Temurin (https://adoptium.net).

- **Windows:** run the `.msi` installer and tick *"Set JAVA_HOME"* and *"Add to PATH"*.
- **macOS:** `brew install --cask semeru-jdk-open` (or use the `.pkg` installer).
- **Linux:** use the `.tar.gz` or your package manager (`sudo apt install openjdk-21-jdk`).

Check: open a **new** terminal and run
```bash
java -version      # should say 17 or 21
```

### 2. Apache Maven 3.8+
Download from https://maven.apache.org/download.cgi (the "Binary zip archive").

- **Windows:** unzip to e.g. `C:\tools\maven`, then add `C:\tools\maven\bin` to your
  PATH (Start → "Edit the system environment variables" → Environment Variables → Path → New).
- **macOS:** `brew install maven`
- **Linux:** `sudo apt install maven`

Check:
```bash
mvn -version
```

### 3. An editor
- **VS Code** with the *Extension Pack for Java* and *Liberty Tools* extensions, or
- **IntelliJ IDEA Community** with the *Liberty Tools* plugin, or
- **Eclipse IDE for Enterprise Java** with *Liberty Tools*.

Liberty Tools adds "Start in dev mode" buttons, so you don't have to type commands.

### 4. (Optional, for Part 5) Docker Desktop
https://www.docker.com/products/docker-desktop

---

## Part 2 — Run the project locally

1. Unzip this project and open a terminal in the `liberty-taskflow` folder (the one with `pom.xml`).
2. Start Liberty in **dev mode**:
   ```bash
   mvn liberty:dev
   ```
   The first run downloads WebSphere Liberty and dependencies (a few minutes).
   Later runs start in seconds.
3. Wait for this line:
   ```
   [INFO] CWWKF0011I: The taskflowServer server is ready to run a smarter planet.
   ```
4. Open http://localhost:9080/openapi/ui and try the endpoints, or http://localhost:9080/api/tasks.

**Dev mode is live:** edit any `.java` file or `server.xml`, save, and Liberty reloads
in a second or two. No restart needed.

Keys while dev mode is running:
- `Enter` — run tests
- `r` — restart the server
- `q` — stop

### Try it from the command line
```bash
# list
curl http://localhost:9080/api/tasks

# create
curl -X POST http://localhost:9080/api/tasks \
     -H "Content-Type: application/json" \
     -d '{"title":"Learn Liberty","description":"Finish part 2"}'

# update (mark as done)
curl -X PUT http://localhost:9080/api/tasks/1 \
     -H "Content-Type: application/json" \
     -d '{"title":"Install WebSphere Liberty","status":"DONE"}'

# filter
curl "http://localhost:9080/api/tasks?status=DONE"

# delete
curl -X DELETE http://localhost:9080/api/tasks/2
```
On Windows PowerShell, use `curl.exe` instead of `curl`, or use the file
`requests.http` from VS Code (REST Client extension) or IntelliJ.

---

## Part 3 — Understand the project

```
liberty-taskflow/
├── pom.xml                              Build file: dependencies + Liberty plugin
├── Dockerfile                           Container image (Part 5)
├── requests.http                        Ready-made test requests
└── src/main/
    ├── liberty/config/server.xml        Liberty server configuration (features, ports, app)
    ├── webapp/index.html                Welcome page
    └── java/com/taskflow/
        ├── RestApplication.java         Puts all endpoints under /api
        ├── model/Task.java              The data (id, title, description, status, createdAt)
        ├── model/TaskStatus.java        TODO / IN_PROGRESS / DONE
        ├── repository/TaskRepository.java  Stores tasks (in memory for now)
        ├── rest/TaskResource.java       The HTTP endpoints
        ├── rest/ErrorMapper.java        Turns "not found" into a JSON 404
        └── health/TaskHealthCheck.java  Readiness check at /health/ready
```

How a request flows:

```
HTTP request → TaskResource (endpoints) → TaskRepository (storage) → Task (data)
```

The two files to know in Liberty:

- **`server.xml`** — Liberty loads *only* the features listed in `<featureManager>`.
  That's why it starts fast. Need a new capability? Add a feature line.
- **`pom.xml`** — the `liberty-maven-plugin` downloads WebSphere Liberty, creates a
  server called `taskflowServer`, copies your `server.xml`, and deploys your `.war`.
  The Liberty install lives in `target/liberty/wlp/`.

Logs: `target/liberty/wlp/usr/servers/taskflowServer/logs/messages.log`

---

## Part 4 — Deploy to a standalone Liberty server (no dev mode)

This is how you'd run it "for real" on a machine, outside your IDE.

### Option A — Let Maven build and run it
```bash
mvn clean package liberty:run      # foreground, Ctrl+C to stop
```
or in the background:
```bash
mvn clean package liberty:create liberty:install-feature liberty:deploy liberty:start
mvn liberty:stop
```

### Option B — Package a runnable server you can copy anywhere
```bash
mvn clean package liberty:package -Dinclude=runnable
```
This creates `target/liberty-taskflow.jar` that contains Liberty + your app. Run it on any
machine with Java:
```bash
java -jar target/liberty-taskflow.jar
```

### Option C — Deploy the `.war` into a Liberty you installed yourself
1. Download WebSphere Liberty (Jakarta EE 10) from
   https://www.ibm.com/support/pages/websphere-liberty-developers and unzip it,
   e.g. to `C:\wlp` or `~/wlp`.
2. Create a server:
   ```bash
   cd ~/wlp
   bin/server create taskflowServer          # Windows: bin\server.bat create taskflowServer
   ```
3. Copy `src/main/liberty/config/server.xml` over `usr/servers/taskflowServer/server.xml`.
4. Install the features it lists:
   ```bash
   bin/installUtility install taskflowServer
   ```
5. Build and copy the app:
   ```bash
   mvn clean package
   cp target/liberty-taskflow.war ~/wlp/usr/servers/taskflowServer/apps/
   ```
6. Start / stop:
   ```bash
   bin/server start taskflowServer
   bin/server status taskflowServer
   bin/server stop taskflowServer
   ```

---

## Part 5 — Deploy with Docker

```bash
mvn clean package
docker build -t liberty-taskflow .
docker run -d --name liberty-taskflow -p 9080:9080 liberty-taskflow
```
Open http://localhost:9080/api/tasks. Logs: `docker logs -f liberty-taskflow`.
Stop: `docker stop liberty-taskflow && docker rm liberty-taskflow`.

The same image can be deployed to Kubernetes, OpenShift, or any cloud container service.
Point the liveness/readiness probes at `/health/live` and `/health/ready`.

---

## Part 6 — Ideas for adding features (in order)

The code is split so each of these only touches one or two places.

| Feature | What to change |
|---|---|
| **Due dates & priority** | Add fields to `Task.java` (e.g. `LocalDate dueDate`, `int priority`). JSON and Swagger update automatically. |
| **Search & sorting** | Add `@QueryParam("q")` / `@QueryParam("sort")` to `list()` in `TaskResource`, filter in `TaskRepository.findAll`. |
| **Real database** | Add `persistence-3.1` + a `<dataSource>` to `server.xml`, annotate `Task` with `@Entity`, rewrite `TaskRepository` with `EntityManager`. Start with embedded Derby, then PostgreSQL. The REST layer doesn't change. |
| **Configuration** | Add `mpConfig-3.1`, inject values with `@ConfigProperty(name="tasks.max")`. |
| **Metrics** | Add `mpMetrics-5.1`, annotate methods with `@Counted` / `@Timed`; view at `/metrics`. |
| **Users & login** | Add `appSecurity-5.0` (basic auth) then `mpJwt-2.1` (tokens). Protect endpoints with `@RolesAllowed("admin")`. |
| **Tests** | Add JUnit 5 + REST client, put tests in `src/test/java`; press Enter in dev mode to run them. |
| **Web UI** | Put an HTML/JS (or React) front end in `src/main/webapp` that calls `/api/tasks`. |
| **Second service** | e.g. a "notifications" service called via `mpRestClient-3.0` when a task is marked DONE. |

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `mvn` or `java` "not recognized" | Close and reopen the terminal after installing; check PATH and JAVA_HOME. |
| `Address already in use` / port 9080 busy | Change `liberty.var.http.port` in `pom.xml` (e.g. to 9081). |
| `CWWKF0001E: A feature definition could not be found` | Typo in a `<feature>` line, or run `mvn liberty:install-feature`. |
| Download errors behind a company proxy | Configure the proxy in `~/.m2/settings.xml`. |
| Changes not picked up | Make sure you're running `mvn liberty:dev`, not `liberty:run`. Press `r` to restart. |
| Anything else | Read `target/liberty/wlp/usr/servers/taskflowServer/logs/messages.log` — Liberty message codes (CWWK…) are searchable online. |

To use a newer WebSphere Liberty release, change `<wlp.version>` in `pom.xml`.
