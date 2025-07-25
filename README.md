# rinha-backend-2025

This project is a backend application built using Quarkus, a supersonic, subatomic Java framework designed for building cloud-native applications.

## Project Overview

- **Group ID:** br.com.caiqueborges
- **Artifact ID:** rinha-backend-2025
- **Version:** 1.0.0-SNAPSHOT
- **Java Version:** 21

## Features

- RESTful API development with Quarkus RESTEasy Reactive
- JSON serialization and deserialization with Jackson
- Dependency injection with Arc
- Redis client integration
- REST client support
- Caching support

## Prerequisites

- Java 21 or higher
- Maven 3.6.3 or higher
- Docker (optional, for containerization)

## Running the Application

### In Development Mode

You can run the application in development mode with live reload support:

```bash
./mvnw quarkus:dev
```

This will start the application on `http://localhost:8080` and enable hot deployment.

### Packaging the Application

To package the application into a runnable JAR file:

```bash
./mvnw package
```

The packaged application will be located in the `target/` directory.

### Running the Packaged Application

Run the packaged JAR with:

```bash
java -jar target/rinha-backend-2025-1.0.0-SNAPSHOT.jar
```

### Building an Uber-JAR

To build an uber-jar (fat jar) that includes all dependencies:

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

## Docker

A `docker-compose.yml` file is included for containerized deployment.

## Testing

The project includes JUnit 5 and Rest Assured for testing.

Run tests with:

```bash
./mvnw test
```

## Further Reading

- [Quarkus Official Website](https://quarkus.io/)
- [Quarkus RESTEasy Reactive](https://quarkus.io/guides/rest-json)
- [Quarkus Redis Client](https://quarkus.io/guides/redis)

## License

Specify your project license here.

---

This README was generated based on the project configuration in the `pom.xml` file.