# AGENTS.md - Developer Guidelines for dgraphql

## Project Overview

This is a pure Java library using Gradle with Java 25 toolchain.

## Build Commands

```bash
# Build the project
./gradlew build

# Clean and build
./gradlew clean build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests DslTest                    # DSL tests

# Run a single test method
./gradlew test --tests DslTest.testBasicQuery

# Run tests with verbose output
./gradlew test --info

# Full verification (build + tests)
./gradlew check
```

## Git Commit Messages

Follow Linus Torvalds' commit message style (from subsurface project):

- **Header line**: Explain the commit in one line (use imperative mood)
  - Good: `Fix bug that causes NPE on empty input`
  - Bad: `Fixed bug`, `Adding new feature`
- **Body**: Explain _why_, not just _what_ - describe the solution and reasoning
- **Width**: Keep lines under 74 characters
- **Signed-off-by**: Add at end of each commit: `Signed-off-by: Name <email>`

Example:

```
Fix bug that causes NPE on empty input

The issue occurred because we called .getName() without checking
for null. Added a null check and return early.

Reported-by: jane@example.com
Signed-off-by: John Doe <john@example.com>
```

## Code Style Guidelines

### General Principles

- Follow Spring Boot conventions and Java idioms
- Keep classes focused and single-responsibility
- Use dependency injection for loose coupling
- Write meaningful Javadoc for public APIs

### Naming Conventions

- **Classes**: PascalCase (e.g., `GraphQLService`, `UserResolver`)
- **Methods**: camelCase (e.g., `fetchUserById`, `executeQuery`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages**: lowercase, single words or dot-separated (e.g., `org.frunix.dgraphql.resolvers`)
- **Test Classes**: `*Test.java` or `*Tests.java` (e.g., `UserServiceTest.java`)

### File Organization

```
src/main/java/org/frunix/dgraphql/
├── dsl/                # DSL classes
├── config/             # Configuration classes (if needed)
├── service/            # Business logic (if needed)
├── controller/         # REST controllers (if needed)
├── model/              # DTOs and domain objects (if needed)
└── repository/         # Data access (if needed)
```

### Imports

- Use wildcard imports sparingly (`java.util.*` is acceptable)
- Group imports in order: static, java, javax, third-party, project
- Sort alphabetically within groups

### Formatting

- Indent with 4 spaces (no tabs)
- Max line length: 120 characters
- Opening brace on same line for classes/methods
- One blank line between top-level elements
- Use meaningful variable names (avoid single letters except loop indices)

### Types

- Use interfaces for dependencies when possible
- Prefer immutable objects where practical
- Use `List` over `ArrayList`, `Map` over `HashMap` in declarations
- Avoid primitive wrappers unless nullability is needed
- Use `Optional` for optional return values

### Error Handling

- Use specific exception types (not generic `Exception`)
- Include meaningful error messages
- Log exceptions with appropriate level (error for fatal, warn for recoverable)

### Testing

- Test class per production class (`UserService` -> `UserServiceTest`)
- Use descriptive test method names: `shouldReturnUser_WhenIdExists()`
- Use AAA pattern (Arrange, Act, Assert)
- Test both success and failure cases
- **DSL Bug Fixes**: When fixing a DSL bug, always add or update a unit test in `DslTest.java` to prove the fix works. Either reuse existing test or create new one.

### Configuration

- Use `application.yaml` for configuration (not properties)
- Environment-specific config: `application-{profile}.yaml`
- Use `@ConfigurationProperties` for type-safe config
- Avoid hardcoding values; use configuration where possible

### Dependencies

- Keep dependencies minimal
- Use stable, well-maintained libraries
- Avoid duplicate transitive dependencies

## Database (Future)

- Use Spring Data JPA or similar for persistence
- Follow JPA naming conventions for entities
- Use transactions appropriately

## GraphQL (Future)

- Define schema in `schema.graphqls`
- Use `@Query`, `@Mutation` annotations for resolvers
- Follow GraphQL best practices for schema design

## Java Specific Guidelines

### Class Structure

- One public class per file
- Fields should be private with getters/setters or records for immutable data
- Use constructors for required dependencies, builder pattern for optional
- Keep methods small and focused (max 30-40 lines)

### Annotations

- Place annotations directly above class/method (no empty line between)
- Common order: @Override, then others
- Keep annotations minimal and meaningful

### Logging

- Use SLF4J for logging (if needed)
- Log at appropriate levels: error (exceptions), warn (recoverable), info (important events), debug (development)
- Never log sensitive data (passwords, tokens, PII)
- Use parameterized logging: `log.info("User {} logged in", userId)` not `log.info("User " + userId + " logged in")`

### Concurrency

- Prefer immutable objects for shared state
- Use thread-safe collections (ConcurrentHashMap, CopyOnWriteArrayList) when needed
- Avoid synchronized blocks; use java.util.concurrent utilities instead

### Performance

- Avoid unnecessary object creation in loops
- Use StringBuilder for string concatenation in loops
- Consider lazy initialization for expensive resources
- Profile before optimizing

## Common Patterns

### Builder Pattern (for complex objects)

```java
User user = User.builder()
    .id(1)
    .name("John")
    .email("john@example.com")
    .build();
```

### Optional Return Pattern

```java
public Optional<User> findById(Long id) {
    return userRepository.findById(id);
}
```

## Code Review Checklist

- [ ] No hardcoded values (use configuration)
- [ ] Proper error handling with specific exceptions
- [ ] Logging at appropriate levels
- [ ] Tests cover both success and failure paths
- [ ] No sensitive data in logs
- [ ] Interfaces used for dependencies
- [ ] Meaningful variable and method names
- [ ] No commented-out code
- [ ] Javadoc for public APIs

## Tools and Versions

- Java: 25
- Build Tool: Gradle (via gradlew wrapper)
- Test Framework: JUnit 5 (Jupiter)

## DSL Library State (Completed)

The DSL library in `org.frunix.dgraphql.dsl` is fully implemented with:

### Implemented Features

- **Query**: Basic queries, nested blocks, multiple query blocks
- **QueryBlock**: Named/anonymous blocks with pagination & ordering
- **Block**: Predicate, FuncBlock, Nested, Reverse, Var, GroupByBlock, Expand cases
- **Func**: All DQL functions (eq, has, uid, count, min, max, sum, avg, expand, etc.)
- **Filter**: Boolean filters (AND, OR, NOT) with comparison functions
- **Directive**: filter, facets, cascade, normalize, ignorereflex, groupby, recurse
- **Variable**: Query variables with defaults
- **VarBlock/VarAssignment**: Query and value variables
- **Fragment**: Query fragment syntax
- **RecurseBlock**: Recursive queries with depth
- **MathExpr**: Mathematical expressions
- **GeoValue**: Geo queries
- **ShortestPath**: K-shortest path queries with depth, weight constraints
- **LanguageTag**: Language-tagged values (@en, @fr, etc.)
- **Mutation**: Set, Delete, Update, Conditional mutations
- **SetTriple**: RDF triples for mutations
- **GroupBy**: GroupBy aggregation
- **Alter**: Schema mutations (types, predicates, indexes)
- **JsonMutation**: JSON-based mutations

### Design Patterns

- **Sealed interfaces** for type-safe variants (Block, QueryBlock, Filter, Mutation, GroupBy, Alter, JsonMutation, ShortestPath, ExpandPredicates)
- **Immutable records** with `with*` methods
- **Factory methods** matching DQL keywords
- **DqlElement** base interface with `dql()` method

### DSL Coverage

The DSL covers 100% of DQL features:
- Query: All features (functions, filters, aliases, pagination, sorting, variables, aggregation, expand, shortest path, fragments, language tags, directives, recurse, facets)
- Mutations: Set, Delete, Update, Upsert, JSON
- Schema: Types, predicates, indexes

See [README.md](../README.md#dql-coverage) for detailed coverage table with documentation links.

### Implementation Status (from docs/dql-dsl-plan.md)

- Phase 1-9: ✅ Complete
- Phase 10: ✅ Complete (GroupBy, ignorereflex, ALTER, Multiple Queries, JSON, Facet Filtering)
- Phase 11: 7/7 Complete (all done)

### Key Files

- Tests: `src/test/java/org/frunix/dgraphql/dsl/DslTest.java` (75 tests)
- Documentation: `README.md`, `docs/dql-dsl-plan.md`, `docs/examples-issues.md`, `examples/README.md`

## Examples Subproject

The project includes an `examples/` subproject for testing the DSL against a live Dgraph instance.

### Prerequisites

- **Task CLI**: Install from [taskfile.dev](https://taskfile.dev)
- **Docker**: Running Dgraph container

### Taskfile.yaml Commands

```bash
task up     # Start Dgraph container with --wait
task down   # Stop and remove Dgraph container
task run    # Run examples application via ./gradlew :examples:bootRun
task ps     # Show Docker container status
```

### Running Examples

```bash
# Start Dgraph, run examples, stop Dgraph
task up run down
```

### Alternative: Direct Commands

```bash
# Start Dgraph
docker compose -f examples/docker-compose.yaml up --wait

# Run examples
./gradlew :examples:bootRun

# Stop Dgraph
docker compose -f examples/docker-compose.yaml down -v
```

### Documentation

- Main: [examples/README.md](examples/README.md)
- Known issues: [docs/examples-issues.md](docs/examples-issues.md)

### Known Issues (3 Failing Examples)

| Example                  | Issue                                                | Type               |
| ------------------------ | ---------------------------------------------------- | ------------------ |
| **Normalize Directive**  | Returns no data                                      | Dgraph v24/v25 bug |
| **Math Expression**      | DSL generates unsupported syntax (math on predicate) | DSL issue          |
| **Conditional Mutation** | `@if` not supported in Dgraph standalone             | Dgraph limitation  |

### DSL Bug Fixes Applied

- **VarBlock**: Changed `(func: ...)` to `var(func: ...)` in `VarBlock.java`
- **Query**: Removed duplicate "var" prefix in `Query.java` (was generating `varvar`)
- **Query Variable**: Support both `$name` and `name` in bindings (auto-adds `$`)
- **Fragment**: Render fragments AFTER query block (not inside) in `Query.java`
