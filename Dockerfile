# Build the war first:  mvn clean package
# Then:                 docker build -t liberty-taskflow .
#                       docker run -p 9080:9080 liberty-taskflow
FROM icr.io/appcafe/websphere-liberty:kernel-java17-openj9-ubi

# Server configuration, then install only the features server.xml lists
COPY --chown=1001:0 src/main/liberty/config/server.xml /config/
RUN features.sh

# The application
COPY --chown=1001:0 target/liberty-taskflow.war /config/apps/

# Pre-warm caches for faster container start
RUN configure.sh
