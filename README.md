# Demo Project

> [!IMPORTANT]
> Spring Boot with Kotlin and ScyllaDB in Docker

This demo project showcases the integration of Spring Boot v3.2 with Kotlin and ScyllaDB, utilizing a two-node cluster setup in Docker. It features OAuth2 social login and signup functionalities, demonstrating the use of opaque tokens and their storage in ScyllaDB.

## Features
- **Spring Boot v3.2 with Kotlin**
- **ScyllaDB Cluster:** Distributed NoSQL database with a two-node ScyllaDB setup.
- **OAuth2 Authentication:** Implement OAuth2 social login and signup processes.
- **Token Storage:** Manage opaque tokens within ScyllaDB.

## Prerequisites
Before you start, ensure you have the following installed:
- Docker and Docker Compose
- Java Development Kit (JDK) 17 or later
- Gradle (if not using the included Gradle wrapper)

## Usage Instructions

### Step 1: Build the Spring Boot Application
Build the Spring Boot project and create a JAR file. This process involves compiling the Kotlin code and packaging it into an executable JAR, which includes all the necessary dependencies.

```bash
./gradlew clean bootJar build -x test
```

This command cleans any previous builds, generates a new bootable JAR file, and builds the project while skipping unit tests.

### Step 2: Prepare Docker Environment
Set up the Docker environment for running the Spring Boot application alongside the ScyllaDB cluster. This involves copying the necessary Dockerfile and using Docker Compose to orchestrate the multi-container setup.

```bash
# Copy the Spring Boot Dockerfile to the build directory
cp Dockerfile.spring build/libs/

# Start the Docker containers for the application and ScyllaDB nodes
docker-compose -f scylla.yml up --build -d
```

The `docker-compose` command will build the Docker image for the Spring Boot application and start the containers as defined in the `scylla.yml` file. The `-d` flag runs the containers in detached mode, allowing them to run in the background.

### Step 3: Accessing the Application
Once the containers are up and running, the Spring Boot application will be accessible at `http://localhost:8080`.

You can interact with the application's APIs and authenticate using the OAuth2 features.

## Contributing
Contributions to this demo project are welcome! Please read our contributing guidelines for details on how to submit changes or report issues.
