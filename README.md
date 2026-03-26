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

Recursive query traversal.

```java
RecurseBlock.recurse("me")
    .withDepth(5)
    .withBlocks(List.of(Block.predicate("name")));
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

// Conditional mutation - upsert-like behavior
Mutation.ifCondition("eq(name, \"Bob\")", setMutation, null)
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

## Project Structure

```
src/main/java/org/frunix/dgraphql/dsl/
├── DqlElement.java       # Base interface
├── DqlResult.java       # Query + variables result
├── Query.java           # Root query container
├── QueryBlock.java      # Named query block
├── Block.java           # Predicates, nested, reverse
├── Func.java            # Functions (eq, has, uid, etc.)
├── Filter.java          # Boolean filters (AND, OR, NOT)
├── Directive.java       # @filter, @facets, @cascade
├── Variable.java        # Query variables ($var)
├── VarBlock.java        # Variable blocks
├── VarAssignment.java   # Variable assignments
├── Fragment.java        # Query fragments
├── FragmentRef.java     # Fragment references
├── RecurseBlock.java    # Recursive queries
├── MathExpr.java        # Math expressions
├── GeoValue.java        # Geo values
├── Mutation.java        # Set, Delete, Update mutations
└── SetTriple.java       # RDF triples for mutations
```

## License

MIT
