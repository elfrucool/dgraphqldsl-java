# Dgraph DQL Java DSL

A type-safe Java DSL for building Dgraph DQL queries. Build queries programmatically with immutability and compile-time safety.

## Features

- **Type-safe query building** - Compile-time verification of query structure
- **Immutable objects** - All components use immutable records with `with*` methods
- **Fluent API** - Chain methods for readable query construction
- **No string concatenation** - Build queries using Java objects
- **Variables & bindings** - Support for query variables with runtime bindings
- **Complete DQL coverage** - Functions, filters, directives, fragments, recurse, and more
- **Mutations** - Set, Delete, Update, and conditional mutations
- **GroupBy aggregation** - Group and aggregate query results
- **Schema mutations (ALTER)** - Define types, predicates, indexes
- **JSON mutations** - Set/delete with JSON objects
- **Facet filtering** - Filter edges by facet values

## Quick Start

```java
import org.frunix.dgraphql.dsl.*;

Query query = Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.eq("name", "Alice"))
            .withBlocks(List.of(
                Block.predicate("name"),
                Block.predicate("age"),
                Block.nested("friend")
                    .withBlocks(List.of(Block.predicate("name")))
            ))
    ));

DqlResult result = query.dql();
System.out.println(result.query());
// { me(func: eq(name, "Alice")) { name age friend { name } } }
```

## Core Components

### Query

The root container for DQL queries.

```java
Query.query()                                    // Anonymous query
Query.query("getUser")                          // Named query
Query.query("getUser", List.of(param))          // With parameters

query.withBlocks(List.of(...))                  // Add query blocks
query.withVarBlock(VarBlock.var(...))           // Add variable blocks
query.withFragment(Fragment.fragment(...))      // Add fragments
query.withRecurseBlock(RecurseBlock.recurse())  // Add recurse
query.dql()                                     // Generate DQL
query.dql(Map.of("name", "Alice"))              // With bindings
```

### QueryBlock

Named query blocks with root functions.

```java
QueryBlock.block("me", Func.eq("name", "Alice"))
    .withBlocks(List.of(...))
    .withFirst(10)
    .withOffset(20)
    .withOrderasc("name")
    .withDirective(Directive.filter(...));
```

### Block

Predicates, nested blocks, and relationships.

```java
Block.predicate("name")                    // Simple predicate
Block.predicate("name", "fullName")         // With alias
Block.nested("friend")                       // Nested relationship
Block.reverse("friend")                      // Reverse edge (~friend)
Block.var("count", "uid")                    // Variable assignment
Block.predicate(Func.expandAll())            // Expand all edges
Block.predicate(Func.val("score"))           // Value variable reference
```

### Func

Root and inline functions.

```java
Func.eq("name", "Alice")
Func.neq("age", 18)
Func.allofterms("name", "alice bob")
Func.has("friend")
Func.uid("0x1", "0x2")
Func.type("Person")
Func.count("friend")
Func.val("score")
Func.math("friend_count + age")
Func.expandAll()
Func.expandReverse()
```

### Filter

Boolean filter expressions.

```java
Filter.eq("age", 18)
Filter.has("friend")
Filter.neq("state", "completed")
Filter.allofterms("name", "alice")

Filter.and(Filter.eq("a", 1), Filter.eq("b", 2))     // Varargs
Filter.or(Filter.eq("a", 1), Filter.eq("b", 2))     // Varargs
Filter.not(Filter.has("friend"))
Filter.func(Func.uid("blocked"))
```

### Directive

Query directives.

```java
Directive.filter(Filter.eq("name", "Alice"))
Directive.facets("since")
Directive.cascade()
Directive.normalize()
Directive.ignorereflex()
Directive.groupby("age")
Directive.recurse(3)
```

### Variable

Query variables with optional defaults.

```java
Variable.queryVar("name", "string")              // $name: string
Variable.queryVar("name", "string", "Default")  // $name: string = "Default"
Variable.param("name")                           // Reference to $name
```

### VarBlock & VarAssignment

Value and query variables.

```java
VarBlock.var(Func.eq("name", "Alice"))
    .withAssignment(VarAssignment.queryVar("friends", Func.count("friend")))
    .withAssignment(VarAssignment.valueVar("score", Func.math("friend_count * 10")));
```

### Fragment

Query fragments for reuse.

```java
Fragment.fragment("PersonDetails")
    .withBlocks(List.of(
        Block.predicate("name"),
        Block.predicate("age")
    ))
// Usage: Block.predicate("... PersonDetails") or FragmentRef
```

### RecurseBlock

Recursive query traversal (as directive).

```java
// Using directive syntax
Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.has("friend"))
            .withDirective(Directive.recurse(3))
            .withBlocks(List.of(Block.predicate("name"), Block.predicate("friend")))
    ));
// => { me(func: has(friend)) @recurse(depth: 3) { name friend } }
```

### GroupBy

GroupBy aggregation using directive syntax.

```java
// Using directive syntax
Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.has("friend"))
            .withDirective(Directive.groupby("age"))
            .withBlocks(List.of(Block.predicate("count(uid)")))
    ));
// => { me(func: has(friend)) @groupby(age) { count(uid) } }
```

### MathExpr

Mathematical expressions for value variables.

```java
MathExpr.add("friend_count", 10)
MathExpr.multiply("a", "b")
MathExpr.cond(MathExpr.gt("age", 18), 10, 0)
MathExpr.sqrt("value")
MathExpr.ln("x")
```

### Mutation

DQL mutations for modifying data.

```java
// Set mutation - add new triples
Mutation.set(SetTriple.subject("0x123").predicate("name").value("Alice"))
// => { set { <0x123> name "Alice" . } }

// Delete mutation - remove triples
Mutation.delete(SetTriple.subject("0x123").predicate("name").value(null))
// => { delete { <0x123> name _: . } }

// Update mutation - set + delete combined
Mutation.update(setMutation, deleteMutation)

// Upsert mutation - query + conditional mutation
String query = "{ alice as var(func: eq(name, \"Alice\")) }";
Mutation.upsertRaw(query, setMutation)
// Generates: upsert { { alice as var(func: eq(name, "Alice")) } mutation @if(uid(alice)) { set { ... } } }
```

### SetTriple

RDF-like triples for mutations.

```java
SetTriple.subject("0x123").predicate("name").value("Alice")       // UID subject
SetTriple.subject("_:newNode").predicate("name").value("Bob")    // Blank node
SetTriple.subject("0x123").predicate("friend").value("0x456")   // UID reference
SetTriple.subject("0x123").predicate("name").value(null)        // Delete predicate
SetTriple.subject("0x123").predicate("name").value("*")         // Delete all values
```

### Alter

Schema mutations for modifying types and predicates.

```java
// Type definition
Alter.type("Person", "name", "age")
// => type Person { name age }

// Predicate with index
Alter.predicate("name", "string").withIndex("exact")
// => name: string @index(exact) .

// Multiple indexes
Alter.predicate("email", "string").withIndexes(List.of("exact", "hash"))

// Drop operations
Alter.dropAll()           // => drop all
Alter.dropType("Person")   // => drop type Person
Alter.dropPredicate("name") // => drop name

// Multiple operations
Alter.all(List.of(alter1, alter2))
```

### ShortestPath

K-shortest path queries for finding multiple paths between nodes.

```java
// Basic shortest path
ShortestPath.shortest("path", "0x1", "0x5")
    .withPredicate(Block.predicate("friend"))
// => path as shortest(from: 0x1, to: 0x5) { friend }

// K-shortest paths (find multiple paths)
ShortestPath.kShortest("path", "0x1", "0x5", 2)
    .withPredicate(Block.predicate("friend"))
// => path as shortest(from: 0x1, to: 0x5, numpaths: 2) { friend }

// With depth limit
ShortestPath.kShortest("path", "0x1", "0x5", 1)
    .withDepth(3)
    .withPredicate(Block.predicate("friend"))
// => path as shortest(from: 0x1, to: 0x5, numpaths: 1, depth: 3) { friend }

// With weight constraints
ShortestPath.kShortest("path", "0x1", "0x5", 2)
    .withWeightRange(2.0f, 4.0f)
    .withPredicate(Block.predicate("friend"))
// => path as shortest(from: 0x1, to: 0x5, numpaths: 2, minweight: 2.0, maxweight: 4.0) { friend }

// Full query with path result
Query.query()
    .withShortestPath(
        ShortestPath.kShortest("path", "0x1", "0x5", 2)
            .withPredicate(Block.predicate("friend"))
    )
    .withBlocks(List.of(
        QueryBlock.block("me", Func.uid("path"))
            .withBlocks(List.of(Block.predicate("name")))
    ));
// => { path as shortest(from: 0x1, to: 0x5, numpaths: 2) { friend } me(func: uid(path)) { name } }
```

### ExpandPredicates

Expand predicates from type definitions.

```java
// Expand by type name
Block.expand("Person")
// => expand(Person)

// Expand all types
Block.expandAll()
// => expand(_all_)

// With nested predicates
Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.uid("0x1"))
            .withBlocks(List.of(
                Block.expand("Person")
                    .withBlocks(List.of(Block.predicate("name")))
            ))
    ));
// => { me(func: uid(0x1)) { expand(Person) { name } } }
```

## Examples

### Basic Query

```java
Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.eq("name", "Alice"))
            .withBlocks(List.of(
                Block.predicate("name"),
                Block.predicate("age")
            ))
    ))
    .dql().query();
// { me(func: eq(name, "Alice")) { name age } }
```

### With Parameters

```java
Query query = Query.query("getPerson")
    .withParameters(List.of(Variable.queryVar("name", "string")))
    .withBlocks(List.of(
        QueryBlock.block("me", Func.eq("name", Variable.param("name")))
            .withBlocks(List.of(Block.predicate("name")))
    ));

DqlResult result = query.dql(Map.of("name", "Alice"));
// query: query getPerson($name: string) { me(func: eq(name, $name)) { name } }
// variables: {name: "Alice"}
```

### With Filters

```java
Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.has("friend"))
            .withDirective(Directive.filter(Filter.and(
                Filter.ge("age", 18),
                Filter.not(Filter.has("banned"))
            )))
            .withBlocks(List.of(Block.predicate("name")))
    ))
    .dql().query();
// { me(func: has(friend)) @filter(ge(age, 18) AND NOT has(banned)) { name } }
```

### With Reverse Edges

```java
Query.query()
    .withBlocks(List.of(
        QueryBlock.block("me", Func.has("top_level"))
            .withBlocks(List.of(
                Block.reverse("top_level")
                    .withDirective(Directive.filter(Filter.neq("state", "completed")))
                    .withBlocks(List.of(Block.predicate("name")))
            ))
    ))
    .dql().query();
// { me(func: has(top_level)) { ~top_level @filter(neq(state, "completed")) { name } } }
```

### Complex Query with Variables

This example demonstrates a real-world query that filters candidates based on blocked sources:

```java
Query.query()
    .withVarBlock(
        VarBlock.var(Func.has("top_level"))
            .withBlock(
                Block.reverse("top_level")
                    .withDirective(Directive.filter(Filter.neq("state", "completed")))
                    .withBlock(Block.var("blocked_top", "uid"))
            )
    )
    .withVarBlock(
        VarBlock.var(Func.has("premium"))
            .withBlock(
                Block.reverse("premium")
                    .withDirective(Directive.filter(Filter.neq("state", "completed")))
                    .withBlock(Block.var("blocked_prem", "uid"))
            )
    )
    .withBlocks(List.of(
        QueryBlock.block("candidates", Func.type("a_label"))
            .withDirective(Directive.filter(Filter.and(
                Filter.eq("is_draft", false),
                Filter.eq("compilation_id", "d8002dbc-fb9d-4acf-9b79-bb1d4237323f"),
                Filter.eq("target", "marker"),
                Filter.or(
                    Filter.eq("node_state", "built"),
                    Filter.eq("node_state", "started")
                ),
                Filter.not(Filter.func(Func.uid("blocked_top"))),
                Filter.not(Filter.func(Func.uid("blocked_prem")))
            )))
            .withBlocks(List.of(
                Block.predicate("uid"),
                Block.predicate(Func.expandAll())
            ))
    ))
    .dql().query();
```

Generated DQL:

```
{ var(func: has(top_level)) { ~top_level @filter(neq(state, "completed")) { blocked_top as uid } } 
  var(func: has(premium)) { ~premium @filter(neq(state, "completed")) { blocked_prem as uid } } 
  candidates(func: type(a_label)) @filter(...) { uid expand(_all_) } }
```

Equivalent Gremlin:

```java
g.V()
    .hasLabel("a_label")
    .has("is_draft", false)
    .has("compilation_id", "d8002dbc-fb9d-4acf-9b79-bb1d4237323f")
    .has("target", "marker")
    .has("node_state", P.within("built", "started"))
    .where(not(
        inE("top_level", "premium")
            .outV()
            .has("state", P.neq("completed"))
    ))
```

## Build

```bash
./gradlew build
```

## Test

```bash
./gradlew test
./gradlew test --tests DslTest              # Run DSL tests
./gradlew test --tests DslTest.testBasicQuery  # Run single test
```

## Examples

The project includes an `examples/` subproject with a Spring Boot application that tests the DSL against a live Dgraph instance.

> **Note**: The `task` CLI tool is required for Option 1. Install via [taskfile.dev](https://taskfile.dev).

### Option 1: Taskfile (Recommended)

```bash
task up     # Start Dgraph container
task down   # Stop and remove Dgraph container
task run    # Run examples application
task ps     # Show Docker container status

# Or all at once
task up run down
```

### Option 2: Direct Commands

```bash
# Start Dgraph
docker compose -f examples/docker-compose.yaml up --wait

# Run examples
./gradlew :examples:bootRun

# Stop Dgraph
docker compose -f examples/docker-compose.yaml down -v
```

See [examples/README.md](examples/README.md) for more details and troubleshooting.

## Project Structure

```
src/main/java/org/frunix/dgraphql/dsl/
├── DqlElement.java       # Base interface
├── DqlResult.java        # Query + variables result
├── Query.java            # Root query container
├── QueryBlock.java       # Named query block
├── Block.java            # Predicates, nested, reverse
├── Func.java             # Functions (eq, has, uid, etc.)
├── Filter.java           # Boolean filters (AND, OR, NOT)
├── Directive.java       # @filter, @facets, @cascade, @ignorereflex
├── Variable.java         # Query variables ($var)
├── VarBlock.java         # Variable blocks
├── VarAssignment.java    # Variable assignments
├── Fragment.java         # Query fragments
├── FragmentRef.java      # Fragment references
├── RecurseBlock.java     # Recursive queries
├── MathExpr.java         # Math expressions
├── GeoValue.java         # Geo values
├── Mutation.java         # Set, Delete, Update mutations
├── SetTriple.java       # RDF triples for mutations
├── GroupBy.java         # GroupBy aggregation
├── Alter.java           # Schema mutations (ALTER)
└── JsonMutation.java   # JSON mutations
```

## License

See [LICENSE.md](LICENSE.md) for details.
