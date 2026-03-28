# Phase 11: Extended Features Plan

## Overview

This document outlines the implementation plan for Phase 11 extended features, based on official Dgraph documentation.

**Status:** 4/5 Complete (11.5 remaining)

---

## 11.5 K-Shortest Path Queries

### Description

K-shortest path queries find multiple paths between source and destination nodes, ordered by weight.

### DQL Syntax

```dql
{
  path as shortest(from: 0x2, to: 0x5, numpaths: 2) {
    friend
  }
  path(func: uid(path)) {
    name
  }
}
```

### Parameters

| Parameter   | Type      | Description                   |
| ----------- | --------- | ----------------------------- |
| `from`      | UID       | Source node UID               |
| `to`        | UID       | Destination node UID          |
| `numpaths`  | int       | Number of paths to return (k) |
| `depth`     | int       | Maximum path depth            |
| `minweight` | float     | Minimum path weight           |
| `maxweight` | float     | Maximum path weight           |
| `@filter()` | directive | Edge constraint filter        |

### Implementation

```java
public sealed interface ShortestPath extends DqlElement
    permits ShortestPath.KShortest {}

public record KShortest(
    String from,
    String to,
    int numpaths,
    int depth,
    Float minweight,
    Float maxweight,
    Block predicate
) implements ShortestPath {}
```

### Factory Methods

| Method                                       | Output                         |
| -------------------------------------------- | ------------------------------ |
| `ShortestPath.shortest(from, to)`            | `shortest(from: U, to: U) { }` |
| `ShortestPath.kShortest(from, to, numpaths)` | with numpaths parameter        |
| `KShortest.withDepth(n)`                     | adds depth parameter           |
| `KShortest.withWeightRange(min, max)`        | adds weight constraints        |

---

## 11.4 Upsert Block Support

### Description

Upsert blocks combine query and mutation in a single atomic operation. If the query finds nodes, the mutation updates them; otherwise, it creates new nodes.

### DQL Syntax

```dql
upsert {
  query {
    v as var(func: regexp(email, /.*@company1.io$/))
  }
  mutation @if(lt(len(v), 100)) {
    set {
      _:newUser <email> "new@company1.io" .
    }
  }
}
```

### Variations

1. **Query + Conditional Mutation**: Query determines if mutation runs
2. **Query + Set Mutation**: Query results used in set triples
3. **Query + Delete Mutation**: Query results used in delete patterns

### Implementation

```java
public sealed interface Upsert extends DqlElement
    permits Upsert.Block, Upsert.Conditional {}

public record UpsertBlock(
    QueryBlock query,
    Mutation mutation
) implements Upsert {}

public record UpsertConditional(
    QueryBlock query,
    String condition,  // @if(...) string
    Mutation mutation
) implements Upsert {}
```

### Factory Methods

| Method                                           | Output              |
| ------------------------------------------------ | ------------------- |
| `Upsert.upsert(query, mutation)`                 | Full upsert block   |
| `Upsert.ifCondition(query, condition, mutation)` | Conditional upsert  |
| `Upsert.upsert(query, set, delete)`              | With set and delete |

---

## 11.1 Language-Tagged Values

### Description

Support for language-specific string values on predicates. Requires `@lang` directive in schema.

### DQL Syntax

```dql
# Query with language tags
{
  me(func: uid(0x123)) {
    name@en
    name@fr
    name@en:fr  # fallback: en, then fr
  }
}

# Mutation with language tags
{
  set {
    <0x123> <name@en> "Alice" .
    <0x123> <name@fr> "Alice" .
    <0x123> <name@es> "Alicia" .
  }
}

# Delete language-specific value
{
  delete {
    <0x123> <name@es> * .
  }
}
```

### Implementation

```java
public record LanguageTag(String... tags) {
    public String render() {
        return "@" + String.join(":", tags);
    }
}

public record TaggedPredicate(String predicate, LanguageTag tag) {
    public String dql() {
        return predicate + tag.render();
    }
}
```

### Factory Methods

| Method                                      | Output              |
| ------------------------------------------- | ------------------- |
| `LanguageTag.en()`                          | `@en`               |
| `LanguageTag.fr()`                          | `@fr`               |
| `LanguageTag.of("en", "fr")`                | `@en:fr` (fallback) |
| `Block.predicate("name", LanguageTag.en())` | `name@en`           |

---

## 11.3 Enhanced Delete Operations

### Description

Extended delete patterns for more granular data removal.

### DQL Syntax

```dql
{
  delete {
    <0x123> <name> * .           # Delete all values for predicate
    <0x123> * * .                # Delete all typed predicates from node
    <0x123> <name@es> * .       # Delete language-specific value
    uid(v) <status> * .          # Delete from variable results
  }
}
```

### Delete Patterns

| Pattern                 | Description                     |
| ----------------------- | ------------------------------- |
| `<uid> <pred> * .`      | Delete all values for predicate |
| `<uid> * * .`           | Delete all predicates from node |
| `<uid> <pred@lang> * .` | Delete language-specific value  |
| `uid(var) <pred> * .`   | Delete using variable           |

### Implementation

```java
public sealed interface DeletePattern extends DqlElement
    permits DeletePattern.Predicate,
            DeletePattern.AllPredicates,
            DeletePattern.LanguageTagged,
            DeletePattern.FromVariable {}

public record PredicateDelete(String uid, String predicate) {}
public record AllPredicatesDelete(String uid) {}
public record LanguageTaggedDelete(String uid, String predicate, LanguageTag tag) {}
public record FromVariableDelete(String variable, String predicate) {}
```

### Updates to Existing Mutation.Delete

```java
// Extend existing Delete to support multiple pattern types
public record Delete(
    List<DeletePattern> patterns,
    List<QueryBlock> queryBlocks  // existing
) implements Mutation {}
```

### Factory Methods

| Method                                   | Output                  |
| ---------------------------------------- | ----------------------- |
| `DeletePattern.predicate(uid, pred)`     | `<uid> <pred> * .`      |
| `DeletePattern.all(uid)`                 | `<uid> * * .`           |
| `DeletePattern.language(uid, pred, tag)` | `<uid> <pred@lang> * .` |
| `DeletePattern.fromVariable(var, pred)`  | `uid(var) <pred> * .`   |

---

## 11.2 Additional Schema Features

### Description

Additional schema directives for data integrity and performance.

### Features

#### @upsert Directive

Ensures uniqueness on a predicate during mutations.

```dql
email: string @index(hash) @upsert .
```

#### @count Index

Enables efficient counting without loading all results.

```dql
friend: [uid] @count .
```

#### Type Relationships

Define relationships between types.

```dql
type Person {
  name
  friend: [Person]
}
```

### Implementation

```java
public sealed interface SchemaDirective extends DqlElement
    permits SchemaDirective.Upsert,
            SchemaDirective.Count,
            SchemaDirective.Index {}

public record UpsertDirective() implements SchemaDirective {
    public String dql() { return "@upsert"; }
}

public record CountDirective() implements SchemaDirective {
    public String dql() { return "@count"; }
}

public record IndexDirective(List<IndexType> types) implements SchemaDirective {
    public String dql() { return "@index(" + types + ")"; }
}
```

### Updates to Alter

```java
// Extend existing Alter to support new directives
public record TypeDefinition(
    String typeName,
    List<SchemaField> fields
) {}

public record SchemaField(
    String name,
    String type,
    List<SchemaDirective> directives
) {}
```

### Factory Methods

| Method                                                 | Output                 |
| ------------------------------------------------------ | ---------------------- |
| `SchemaDirective.upsert()`                             | `@upsert`              |
| `SchemaDirective.count()`                              | `@count`               |
| `Alter.addType("Person", fields)`                      | `type Person { ... }`  |
| `SchemaField.field("name", "string", List.of(upsert))` | `name: string @upsert` |

---

## Package Structure Updates

```
src/main/java/org/frunix/dgraphql/dsl/
├── ShortestPath.java        # NEW - k-shortest path queries
├── Upsert.java             # NEW - upsert block support
├── LanguageTag.java         # NEW - language-tagged values
├── DeletePattern.java       # UPDATE - enhanced delete patterns
├── SchemaDirective.java     # NEW - @upsert, @count directives
├── Alter.java               # UPDATE - add type definitions
└── (existing files...)
```

---

## Implementation Order

1. **LanguageTag** - Simple value wrapper, low risk
2. **DeletePattern enhancements** - Extend existing Delete
3. **SchemaDirective** - Extend existing Alter
4. **Upsert** - Medium complexity, combines Query + Mutation
5. **ShortestPath** - Most complex, requires new Block type

---

## Integration Testing Guidelines

When testing features against a live Dgraph instance and an example fails, follow this troubleshooting process:

### Step 1: Validate DSL vs Documentation

Compare the DSL-generated DQL output against the official Dgraph documentation to ensure correctness.

- Run the DSL unit test to see the generated DQL
- Compare with Dgraph docs syntax
- If DSL is incorrect → **Fix the DSL** and add/update unit test in `DslTest.java`

### Step 2: Validate Example Data Setup

This is the most common source of failures. Before blaming Dgraph, verify:

- **Schema exists**: Is the predicate defined in the schema?
  - Check if previous examples already created the predicate
  - If not, use `dgraphClient.alter()` with proper schema
- **Node exists**: Does the UID being used actually exist in the database?
  - Use different UID (e.g., `0x99`) that you create in setupData()
- **Type information**: For `* *` deletes, nodes need `dgraph.type` to be set
- **Data integrity**: Was test data actually inserted? Check for setup errors.

### Step 3: Validate Example vs Documentation

If DSL and data setup are correct, verify the example uses the DSL properly.

- Check that the example uses the correct DSL API calls
- Verify query/mutation structure matches Dgraph expectations
- If example is incorrect → **Fix the example** to use DSL correctly

### Step 4: Try Alternative Formats

If standard RDF format fails, try alternatives:

- **JSON format**: For delete mutations, try `{"uid": "0x99", "predicate": null}` instead of RDF
- **Different API**: Some operations work better via HTTP vs Java client
- **Transaction type**: Ensure using correct transaction (read vs write)

### Step 5: Document Unresolved Issues

If DSL, data setup, and example are all correct, but the feature still fails:

- Document the issue in `docs/examples-issues.md`
- Mark as "Dgraph limitation" or "Dgraph bug" if applicable
- Include: feature name, expected behavior, actual behavior, Dgraph version
- Try alternative approaches before giving up

### Common Pitfalls (Lessons Learned)

| Pitfall | Solution |
|---------|----------|
| Schema not defined | Use `dgraphClient.alter()` to define predicates first |
| UID doesn't exist | Create test data with specific UID in `setupData()` |
| Predicate conflict | Use unique predicate names per example (e.g., `deleteName` not `name`) |
| Wrong delete format | Try JSON format `{"pred": null}` instead of RDF `S P * .` |
| Type required | Add `dgraph.type` for `* *` delete operations |
| Shared schema conflict | Don't drop shared predicates (`name`, `age`, `email`) in teardown |

### Summary Table

| Issue Type | Action | Test Required |
|------------|--------|---------------|
| DSL generates wrong DQL | Fix DSL | Add/update unit test in `DslTest.java` |
| Missing schema | Add schema setup in example | Verify schema is created |
| Missing data | Add data setup in example | Verify data exists |
| Example uses DSL incorrectly | Fix example | Verify fix works |
| Wrong format (RDF vs JSON) | Try alternative format | Test both approaches |
| Dgraph limitation/bug | Document in `examples-issues.md` | Mark for follow-up |

---

## Testing Strategy

- Unit tests for each new type
- Compare `.dql()` output to expected DQL strings
- Integration tests with live Dgraph (examples subproject)
- Test immutability (original objects unchanged after `with*`)

### Integration Examples Required

Each Phase 11 feature MUST include an integration example in the examples subproject to be validated against a live Dgraph instance:

| Feature | Example Class | File | Status | Complexity |
|---------|--------------|------|--------|------------|
| 11.1 Language-Tagged Values | `LanguageExamples.java` | `examples/.../example/LanguageExamples.java` | **DONE** | Easy |
| 11.2 Schema Features | `SchemaExamples.java` | `examples/.../example/SchemaExamples.java` | **DONE** | Easy-Medium |
| 11.3 Enhanced Delete | `DeleteExamples.java` | `examples/.../example/DeleteExamples.java` | **DONE** | Medium |
| 11.4 Upsert Block | `UpsertExamples.java` | `examples/.../example/UpsertExamples.java` | **DONE** | Medium |
| 11.5 K-Shortest Path | `PathExamples.java` | `examples/.../example/PathExamples.java` | TODO | Hard |

Each example class must:
1. Have proper `setupData()` with schema and test data
2. Run the query/mutation and print results
3. Have `teardownData()` to clean up test data

---

## Dependencies

- None (pure Java, no external libraries)
- Java 25 toolchain

---

## References

- K-Shortest Path: https://dgraph.io/docs/query-language/kshortest-path-queries/
- Upsert: https://dgraph.io/docs/dql/upserts/
- Language Support: https://dgraph.io/docs/query-language/language-support/
- Mutations: https://dgraph.io/docs/dql/mutations/
- Schema: https://dgraph.io/docs/dql/dql-schema/

---

## Implementation Notes

### 11.1 Language-Tagged Values - COMPLETED

**Implementation:**
- Created `LanguageTag.java` - immutable record with factory methods (`en()`, `fr()`, `de()`, `es()`, `of(...)`)
- Updated `Block.java` - added `languageTag` field to `Predicate`, `Nested`, `Reverse` records
- Updated `SetTriple.java` - added `languageTag` field for mutations

**Files Created:**
- `src/main/java/org/frunix/dgraphql/dsl/LanguageTag.java`

**Files Modified:**
- `src/main/java/org/frunix/dgraphql/dsl/Block.java`
- `src/main/java/org/frunix/dgraphql/dsl/SetTriple.java`
- `src/test/java/org/frunix/dgraphql/dsl/DslTest.java` (9 new unit tests)

**Integration Example:**
- `examples/src/main/java/org/frunix/dgraphql/examples/example/LanguageExamples.java`

**Troubleshooting Applied:**
1. DSL generated correct DQL (`localename@en`) - verified vs Dgraph docs
2. Example had schema setup issue - was using mutation instead of `dgraphClient.alter()`
3. Fixed by using `DgraphProto.Operation` with `setSchema()` for schema definition
4. Used unique predicate name (`localename`) to avoid conflict with global schema

**Test Results:** 27/27 passing

---

### 11.2 Schema Features - COMPLETED

**Implementation:**
- Added convenience methods `withCount()` and `withUpsert()` to `PredicateSchema`
- These build on existing `withDirective()` method

**Files Modified:**
- `src/main/java/org/frunix/dgraphql/dsl/Alter.java`
- `src/test/java/org/frunix/dgraphql/dsl/DslTest.java` (3 new unit tests)

**Integration Example:**
- `examples/src/main/java/org/frunix/dgraphql/examples/example/SchemaExamples.java`

**Test Results:** 30/30 passing (includes 11.3 + 11.5)

---

### 11.3 Enhanced Delete - COMPLETED

**Implementation:**
- Enhanced SetTriple to support:
  - `uid(varName)` as subject for variable-based deletes
  - `*` as predicate for delete all predicates pattern
  - Already supported: LanguageTag for language-specific deletes

**Files Modified:**
- `src/main/java/org/frunix/dgraphql/dsl/SetTriple.java`
- `src/test/java/org/frunix/dgraphql/dsl/DslTest.java` (4 new unit tests)

**Integration Example:**
- `examples/src/main/java/org/frunix/dgraphql/examples/example/DeleteExamples.java`

**Test Results:** 32/32 passing
- "Delete All Predicates" (`<uid> * * .`) - PASS ✓
- "Delete Predicate Value" (JSON format) - PASS ✓

**Troubleshooting Applied:**
1. First tried RDF format `<uid> <pred> * .` - failed
2. Switched to JSON format `{"pred": null}` - works!
3. Used port 8080 instead of 9080 for HTTP calls

---

### 11.4 Upsert Block - COMPLETED

**Implementation:**
- DSL already had `Mutation.Upsert` and `Mutation.UpsertRaw` implemented
- Factory methods: `Mutation.upsert()`, `Mutation.upsertRaw()`

**Files Modified:**
- `src/test/java/org/frunix/dgraphql/dsl/DslTest.java` (3 new unit tests)

**Integration Example:**
- `examples/src/main/java/org/frunix/dgraphql/examples/example/UpsertExamples.java`

**Test Results:** 34/34 passing (all Phase 11 features)

**Troubleshooting Applied:**
1. DSL generates correct upsert syntax - verified
2. First used wrong port (9080) - fixed to 8080
3. Now works on Dgraph standalone via HTTP
