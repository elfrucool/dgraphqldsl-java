# DQL DSL Implementation Plan

## Overview

Build a type-safe Java DSL for constructing Dgraph DQL queries, using immutable records with `with*` methods.

## Package Structure

```
src/main/java/org/frunix/dgraphql/dsl/
├── DqlElement.java          # Base interface with String dql()
├── DqlResult.java          # Record: query string + variables map
├── Query.java               # Root query container
├── QueryBlock.java          # Named query block
├── Func.java                # Root function (eq, allofterms, etc.)
├── Block.java               # Nested block (predicate traversal)
├── Filter.java              # @filter expressions
├── Directive.java           # @filter, @facets, etc.
├── Variable.java            # Query variables ($var)
└── Facet.java               # @facets support
```

## Core Interface

```java
public interface DqlElement {
    String dql();
}
```

**Note:** Root `Query.dql()` returns `DqlResult` containing both the query string and binding variables map.

## Implementation Phases

### ✓ Phase 1: Basic Query Structure

- `Query` - root container with parameterization + blocks
- `QueryBlock` - named query block with root function
- `Func` - root functions: `eq`, `allofterms`, `has`, `uid`
- Basic predicate blocks (no directives)

**Target DQL:**

```dql
{
  me(func: eq(name, "Alice")) {
    name
    age
  }
}
```

### ✓ Phase 2: Nested Blocks & Directives

- Nested `Block` with predicate traversal
- `@filter` directive with simple conditions
- Alias support via `Alias`

**Target DQL:**

```dql
{
  me(func: eq(name, "Alice")) {
    name
    friend @filter(has(birthdate)) {
      name
    }
  }
}
```

### ✓ Phase 3: Filter Expressions

- Boolean connectives: `AND`, `OR`, `NOT`
- Comparison: `eq`, `ge`, `gt`, `le`, `lt`, `between`
- String functions: `allofterms`, `anyofterms`, `regexp`, `match`
- Node tests: `has`, `uid`, `type()`

**Target DQL:**

```dql
{
  me(func: allofterms(name, "Alice")) @filter(has(friend) AND ge(age, 18)) {
    name
  }
}
```

### ✓ Phase 4: Variables (Query & Value Vars)

- `var` blocks for pre-computation
- `$variable` syntax for parameterized queries
- Value variables with `val()` function

**Target DQL:**

```dql
query getPerson($name: string) {
  me(func: eq(name, $name)) {
    name
  }
}
```

### ✓ Phase 5: Facets

- `@facets` directive
- Facet filtering

**Target DQL:**

```dql
{
  me(func: eq(name, "Alice")) {
    friend @facets(since) {
      name
    }
  }
}
```

### ✓ Phase 6: Sorting & Pagination

- `orderasc`, `orderdesc`
- `first`, `offset`, `after`

**Target DQL:**

```dql
{
  me(func: has(name), orderasc: name, first: 10) {
    name
  }
}
```

### ✓ Phase 7: Aggregations & Math

- `count`
- `sum`, `avg`, `min`, `max`
- `math()` expressions

### ✓ Phase 8: Advanced Features

- `@cascade`, `@normalize` directives
- Recurse queries
- Fragment syntax

## Naming Conventions (match DQL output)

| DQL Construct          | Factory Method               | Example Output            |
| ---------------------- | ---------------------------- | ------------------------- |
| `query name($v: type)` | `Query.query("name", param)` | `query getPerson(...)`    |
| `func: eq(...)`        | `Func.eq(...)`               | `func: eq(name, "Alice")` |
| `{ ... }` block        | `QueryBlock.block(...)`      | `me(func: ...) { }`       |
| predicate              | `Block.predicate("name")`    | `name`                    |
| nested block           | `Block.nested("friend")`     | `friend { }`              |
| `@filter(...)`         | `Directive.filter(...)`      | `@filter(has(name))`      |
| `@facets(...)`         | `Directive.facets(...)`      | `@facets(since)`          |
| `orderasc: name`       | `Block.orderasc("name")`     | `orderasc: name`          |
| `first: 10`            | `Block.first(10)`            | `first: 10`               |
| `var { }`              | `Query.varBlock(...)`        | `var(func: ...) { }`      |

## Key Design Decisions

### Immutability Pattern

All records have `with*` methods returning new instances:

```java
Query q = Query.query().withBlocks(List.of(block));
Query q2 = q.withBlocks(List.of(block2)); // q unchanged
```

### Factory Methods

- Static factory methods on each type
- Names match DQL keywords: `eq()`, `allofterms()`, `has()`, `and()`, `or()`, `not()`

### DqlResult Return Type

```java
public record DqlResult(String query, Map<String, Object> variables) {}
```

Only `Query.dql()` returns `DqlResult`; all other elements return `String`.

## Example Usage (Phase 1-2)

```java
// Build: { me(func: eq(name, "Alice")) { name friend { name } } }
Query query = Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.eq("name", "Alice"))
            .withBlocks(List.of(
                Block.predicate("name"),
                Block.nested("friend")
                    .withBlocks(List.of(Block.predicate("name")))
            ))
    ));

DqlResult result = query.dql();
// result.query() => "{ me(func: eq(name, \"Alice\")) { name friend { name } } }"
// result.variables() => {}
```

## Example Usage (Phase 4 - Variables)

```java
// Build: query getPerson($name: string) { me(func: eq(name, $name)) { name } }
Query query = Query.query("getPerson")
    .withParameters(List.of(
        Variable.queryVar("name", "string")
    ))
    .withBlocks(List.of(
        QueryBlock.block("me", Func.eq("name", Variable.param("name")))
            .withBlocks(List.of(Block.predicate("name")))
    ));

DqlResult result = query.dql();
// result.query() => "query getPerson($name: string) { me(func: eq(name, $name)) { name } }"
// result.variables() => {"name": <value>}
```

## Dependencies

- None (pure Java, no external libraries)
- Java 25 toolchain (per project requirements)

## Testing Strategy

- Unit tests for each phase
- Compare `.dql()` output to expected DQL strings
- Test immutability (original objects unchanged after `with*`)

## Implementation Status

### Completed Phases

- Phase 1: Basic Query Structure ✅
- Phase 2: Nested Blocks & Directives ✅
- Phase 3: Filter Expressions ✅
- Phase 4: Variables (Query & Value Vars) ✅
- Phase 5: Facets ✅
- Phase 6: Sorting & Pagination ✅
- Phase 7: Aggregations & Math ✅
- Phase 8: Advanced Features ✅
- Phase 9: Mutations ✅

### Phase 10: Missing Features (Future)

See below for details.

### ✓ Phase 9: Mutations

Dgraph DQL mutations include Set, Delete, Update, and Conditional mutations.

#### Core Types

```java
// Mutation - root container for mutations
public sealed interface Mutation extends DqlElement
    permits Mutation.Set, Mutation.Delete, Mutation.Update, Mutation.Conditional {}

public record Set(List<SetTriple> triples, List<Directive> directives) implements Mutation {}
public record Delete(List<QueryBlock> queryBlocks, List<QueryBlock> valuePatterns) implements Mutation {}
public record Update(Set set, Delete delete) implements Mutation {}
public record Conditional(String ifCondition, Set set, Delete delete) implements Mutation {}
```

#### Set Triple

```java
// RDF-like triple for setting values
public record SetTriple(String subject, String predicate, Object value) {}
```

#### Target DQL:

```dql
{
  set {
    <0x123> <name> "Alice" .
    <0x123> <age> 30 .
  }
}
```

```dql
{
  delete {
    <0x123> <name> * .
  }
}
```

```dql
{
  update {
    set {
      <0x123> <name> "Alice" .
    }
    delete {
      <0x123> <age> * .
    }
  }
}
```

#### Key Design Decisions

1. **Separate Set vs Delete** - Clear distinction between adding and removing data
2. **Conditional mutations** - Support `@if` conditions for upsert-like behavior
3. **Multiple set triples** - Batch multiple triples in single mutation
4. **UID generation** - Support `_:var` syntax for blank nodes

#### Usage Examples

```java
// Set mutation
Mutation mutation = Mutation.set(List.of(
    SetTriple.subject("0x123").predicate("name").value("Alice"),
    SetTriple.subject("0x123").predicate("age").value(30)
));
mutation.dql(); // "{ set { <0x123> <name> \"Alice\" . <0x123> <age> 30 . } }"

// Delete mutation  
Mutation mutation = Mutation.delete(QueryBlock.block("me", Func.uid("0x123")));

// Conditional mutation
Mutation mutation = Mutation.ifCondition(
    "cond(func: uid(0x123))",
    Set.of(SetTriple.subject("0x123").predicate("name").value("Bob")),
    null
);
```

#### Factory Methods

| DQL Construct                     | Factory Method                                     | Example Output                |
| --------------------------------- | -------------------------------------------------- | ----------------------------- |
| `set { }`                         | `Mutation.set(...)`                                | `set { ... }`                 |
| `delete { }`                      | `Mutation.delete(...)`                             | `delete { ... }`              |
| `update { set {} delete {} }`     | `Mutation.update(...)`                             | `update { set {} delete {} }` |
| `@if(...)`                        | `Mutation.ifCondition(...)`                        | `@if(...) { set {} }`         |
| `<subject> <predicate> <value> .` | `SetTriple.subject(...).predicate(...).value(...)` | RDF triple                    |

#### Package Structure Update

```
src/main/java/org/frunix/dgraphql/dsl/
├── Query.java               # (existing)
├── Block.java               # (existing)
├── Mutation.java            # NEW - root mutation container
├── SetTriple.java           # NEW - RDF-like triples for set operations
├── DeletePattern.java       # NEW - patterns for delete operations
├── Upsert.java              # NEW - query + mutation combination (optional)
└── (existing files...)
```

### Phase 10: Missing Features (Future)

The following features are not yet implemented but are identified as gaps for complete DQL coverage.

#### ✓ 10.1 GroupBy Aggregation

DQL's `groupby` for aggregating results by predicate values.

```java
// Target DQL:
query {
  me(func: has(name)) {
    age groupby(age) {
      count(uid)
    }
  }
}
```

#### ✓ 10.2 @ignorereflex Directive

Used with recurse to avoid circular traversals.

```java
// Target DQL:
{
  me(func: uid(0x123)) @recurse(depth: 5) @ignorereflex {
    friend
  }
}
```

#### ✓ 10.3 Schema Mutations (ALTER)

Operations for modifying schema: adding types, predicates, indexes.

```java
// Target DQL:
alter {
  type Person {
    name
    age
  }
}

alter {
  name: string @index(exact) .
}
```

#### ✓ 10.4 Multiple Query Blocks

Running multiple named queries in one request.

```java
// Target DQL:
{
  getUser1(func: eq(email, "a@b.com")) { name }
  getUser2(func: eq(email, "c@d.com")) { name }
}
```

#### ✓ 10.5 JSON Mutations

JSON-based mutations for complex objects.

```java
// Target DQL:
{
  set {
    _:user <name> "Alice" .
    _:user <age> "30" .
  }
}
```

#### ✓ 10.6 Facet Filtering

Advanced facet filtering with conditions.

```java
// Target DQL:
{
  me(func: eq(name, "Alice")) {
    friend @facets(eq(since, "2024")) {
      name
    }
  }
}
```

#### Proposed Package Updates

```
src/main/java/org/frunix/dgraphql/dsl/
├── GroupBy.java             # NEW - groupby aggregation
├── Alter.java               # NEW - schema mutations
├── QueryGroup.java          # NEW - grouping multiple queries
├── JsonMutation.java        # NEW - JSON-based mutations
├── FacetFilter.java        # NEW - facet filtering conditions
└── (existing files...)
```
