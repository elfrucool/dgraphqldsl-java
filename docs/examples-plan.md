# Examples Subproject Plan

## Status: ✅ COMPLETE

## Status: ✅ COMPLETE

## Overview

Create a `examples` subproject to showcase and validate the DSL library features with Spring Boot and Dgraph.

## Project Structure ✅ COMPLETE

```
examples/
├── build.gradle.kts
├── settings.gradle.kts
├── docker-compose.yaml
└── src/main/
    ├── java/org/frunix/dgraphql/examples/
    │   ├── ExamplesApplication.java
    │   ├── config/
    │   │   └── DgraphConfig.java          # Dgraph client bean
    │   └── example/
    │       ├── BasicExamples.java         # Phase 1-2
    │       ├── FilterExamples.java        # Phase 3
    │       ├── VariableExamples.java       # Phase 4
    │       ├── FacetExamples.java          # Phase 5
    │       ├── PaginationExamples.java    # Phase 6
    │       ├── AggregationExamples.java    # Phase 7
    │       ├── AdvancedExamples.java       # Phase 8 (cascade, recurse, fragments)
    │       ├── MutationExamples.java       # Phase 9
    │       └── AdditionalExamples.java    # Phase 10 (GroupBy, ALTER, JSON)
    └── resources/
        └── application.yaml
```

## Configuration ✅ COMPLETE

### Java Version

- Java 21 toolchain (compatible with Spring 4.x and parent project)

### Spring Boot Version

- Spring Boot 4.x (latest stable)

### Build: examples/build.gradle.kts

```kotlin
plugins {
    id("org.springframework.boot") version "4.0.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.frunix"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    mavenLocal()  // To use parent project
}

dependencies {
    implementation(project(":"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("io.dgraph:dgraph4j:24.2.0")
    implementation("io.grpc:grpc-api:1.71.0")
    implementation("io.grpc:grpc-stub:1.71.0")
    implementation("com.google.protobuf:protobuf-java:4.30.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### Build: examples/settings.gradle.kts

```kotlin
rootProject.name = "examples"
```

### Docker Compose: examples/docker-compose.yaml

```yaml
services:
  dgraph:
    image: dgraph/standalone:v24.0.0
    ports:
      - "8080:8080"
      - "9080:9080"
```

## Example Classes

### 1. BasicExamples (Phase 1-2) ✅ COMPLETE

- Basic queries with `Query.query()`
- Named and anonymous query blocks with `QueryBlock.block()`
- Root functions: `eq`, `allofterms`, `has`, `uid`
- Nested blocks with `Block.nested()`
- Alias support
- `@filter` directive

### 2. FilterExamples (Phase 3) ✅ COMPLETE

- Boolean connectives: `AND`, `OR`, `NOT`
- Comparison: `eq`, `ge`, `gt`, `le`, `lt`, `between`
- String functions: `allofterms`, `anyofterms`, `regexp`, `match`
- Node tests: `has`, `uid`, `type()`

### 3. VariableExamples (Phase 4) ✅ COMPLETE

### 4. FacetExamples (Phase 5) ✅ COMPLETE

### 5. PaginationExamples (Phase 6) ✅ COMPLETE

### 6. AggregationExamples (Phase 7) ✅ COMPLETE

### 7. AdvancedExamples (Phase 8) ✅ COMPLETE

### 8. MutationExamples (Phase 9) ✅ COMPLETE

### 9. AdditionalExamples (Phase 10) ✅ COMPLETE

## Dgraph Connection ✅ COMPLETE

### DgraphConfig.java

- Use `DgraphClient` from dgraph4j
- Connect to `localhost:9080` (gRPC)
- Configure as a Spring `@Bean`

Example:

```java
@Bean
public DgraphClient dgraphClient() {
    return DgraphClient.connect("localhost:9080");
}
```

## Execution Flow

1. Start Docker Compose: `docker compose up -d`
2. Run Spring Boot application
3. Each example class:
   - `@Component` - Spring-managed
   - `@PostConstruct` - runs after Dgraph is ready
   - Performs write (if needed), then read
   - Prints results to console
4. Application exits after all examples run

## Data Management

Options for handling data state:

- **Option A**: Drop schema/data before each run (clean state)
- **Option B**: Assume fresh Dgraph instance
- **Option C**: Check if data exists, skip if present

Recommended: Option A - drop and recreate schema/data to ensure deterministic results.

## Usage

### Start Dgraph

```bash
cd examples
docker compose up -d
```

### Run Examples

```bash
./gradlew :examples:bootRun
```

### Stop Dgraph

```bash
docker compose down
```

## Testing ✅ COMPLETE

- [x] Start Dgraph with Docker Compose (`docker compose up -d --wait`)
- [x] Run examples with `./gradlew :examples:bootRun`
- [x] Verify output shows queries and responses
- [x] Fix any issues found during testing

**Test Results:**

- All queries execute successfully
- Errors are expected (empty database - no data to query)
- The DSL correctly generates DQL strings
- Health check works with `curl`

## Known Issues

✅ **All issues resolved!** All 25 examples now passing.

See [examples-issues.md](examples-issues.md) for the complete history of fixes and solutions.
