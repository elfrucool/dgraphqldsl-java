# Tutorial: Building a Public Transport Query System with dgraphqldsl-java

## Introduction

In this tutorial, we'll build a query system for a public transport network in a town.

### The Problem

The town has public buses running on routes. Each route:

- Starts at a **base** (where buses begin/end their journey)
- Makes stops at predefined locations along the way
- Ends at another base (then returns)

Some stops are **transfer points** - intersections where two routes meet, allowing passengers to switch buses. Multiple buses work on each route, and each bus reports its current position (at a stop, or in transit between stops).

Users can query:

- All stops on a route
- All buses on a route and their positions
- How to get from stop A to stop B (possibly using transfers)

### About the Library

**dgraphqldsl-java** is a type-safe Java library for building Dgraph DQL queries programmatically.

Instead of writing queries as strings:

```java
String query = "query { stop(func: eq(name, \"Main St\")) { uid name } }";
```

You write in Java:

```java
Query query = Query.query()
    .withRoot(Block.block()
        .withFunc(Func.eq("name", "Main St"))
        .retrieve("uid", "name"))
    .build();
```

This gives you:

- **Compile-time safety** - typos caught early
- **IDE autocompletion** - discover available methods
- **Type safety** - variants are explicit in the code

### What is Dgraph?

[Dgraph](https://dgraph.io/docs/) is a fast, distributed graph database written in Go. DQL (Dgraph Query Language) is similar to GraphQL but optimized for graph operations.

## Prerequisites

Before we start, you'll need:

- Java 21
- Docker (to run Dgraph)
- A way to run the tutorial code (explained in Step 0)

## Tutorial Steps

Let's build this step by step:

- **Step 0**: Set up the environment (Docker, project, hello world)
- **Step 1**: Model a stop and query it
- **Step 2**: Add routes with stops
- **Step 3**: Add bases to routes
- **Step 4**: Add buses and their positions
- **Step 5**: Add transfer points
- **Step 6**: Find paths between stops

Let's begin with Step 0.

## Step 0: Setup (Docker, Project, Hello World)

In this step, we'll set up:

1. Docker with Dgraph
2. A Java project with the dgraphqldsl-java library
3. A hello world to verify everything works

### Why Docker?

Dgraph runs as a container. We use Docker so you don't need to install Dgraph directly on your machine. The container exposes:

- Port 9080 for client API (gRPC)
- Port 8080 for HTTP API

### Docker Compose

Create a file named `docker-compose.yaml` in your project root:

```yaml
services:
  dgraph:
    image: dgraph/standalone:latest
    ports:
      - "8080:8080" # HTTP API
      - "9080:9080" # gRPC API
```

Start Dgraph:

```bash
docker compose up -d
```

Wait a few seconds, then verify:

```bash
curl localhost:8080/health
```

It should return `200 OK` and some data about the server.

It is advisable to add a health check to the service for two purposes:

1. Docker waits for the service to be healthy before reporting success
2. Scripts can wait for the service to be healthy before running tests or queries

So update `docker-compose.yaml` with a health check:

```yaml
services:
  dgraph:
    image: dgraph/standalone:latest
    ports:
      - "8080:8080" # HTTP API
      - "9080:9080" # gRPC API
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/health || exit 1"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s
```

### Project Setup

Create a directory `tutorial` with this structure:

```
tutorial/
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        └── java/
            └── tutorial/
                └── Tutorial.java
```

**build.gradle.kts**:

```kotlin
plugins {
    java
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.elfrucool:dgraphqldsl-java:1.0.0")
    // Dgraph Java client - connects to Dgraph server via gRPC
    implementation("io.dgraph:dgraph4j:24.2.0")
    // gRPC core library - dgraph4j depends on gRPC for client-server communication
    implementation("io.grpc:grpc-core:1.71.0")
    // gRPC stub library - provides async stubs for dgraph4j
    implementation("io.grpc:grpc-stub:1.71.0")
}

application {
    // Tells Gradle which class to run
    mainClass = "tutorial.Tutorial"
}
```

**settings.gradle.kts**:

```kotlin
rootProject.name = "tutorial"
```

**Tutorial.java**:

```java
package tutorial;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Tutorial {
    public static void main(String[] args) throws Exception {
        // Create a gRPC channel to the Dgraph server
        // Port 9080 is where Dgraph listens for gRPC connections
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9080)
            .usePlaintext() // no TLS - use plaintext encoding
            .build();

        // Dgraph uses gRPC for client-server communication
        // Create a stub (client-side object that sends requests)
        DgraphGrpc.DgraphStub stub = DgraphGrpc.newStub(channel);
        // Wrap the stub in a DgraphClient for easier use
        DgraphClient client = new DgraphClient(stub);

        System.out.println("Connected to Dgraph!");

        // Clean shutdown
        channel.shutdownNow();
    }
}
```

### Run Hello World

```bash
./gradlew run
```

You should see:

```
Connected to Dgraph!
```

### Summary

In this step:

- Started Dgraph in Docker
- Created a Java project with dgraphqldsl-java
- Verified connectivity

You are now ready for step 1: Model a stop and query it.
