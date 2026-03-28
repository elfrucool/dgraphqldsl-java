# Examples - Known Issues

## Overview

This document tracks known issues and failures when running the examples subproject against a Dgraph standalone instance.

## Test Results Summary

| Total | Passed | Failed |
| ----- | ------ | ------ |
| 34    | 34     | 0      |

**Status:** All 34 examples passing!

## Previously Fixed Issues

These issues were encountered during development and have been FIXED:

### Phase 4 - Basic (Query Variables) ✓

#### 1. Query Variable ✓

| Attribute     | Value                                                                       |
| ------------- | --------------------------------------------------------------------------- |
| File          | `examples/.../example/VariableExamples.java`                                |
| Phase         | Phase 4 (Variables)                                                         |
| Level         | Basic                                                                       |
| Generated DQL | `query getPerson($name: string) { me(func: eq(name, $name)) { name age } }` |

**Issue:** Java client doesn't pass query variables to the server.

**Root Cause:** The code uses `dgraphClient.newReadOnlyTransaction().query(query)` instead of `queryWithVars(query, variables)`.

**Solution:**

1. Update examples to use `dgraphClient.newReadOnlyTransaction().queryWithVars(query, variables)`
2. Pass bindings to `query.dql(bindings)` to populate variables
3. Fix DSL to use `$` prefix in variable keys (`Query.java:100`)

---

### Phase 6 - Basic (Pagination) ✓

#### 2. Offset Pagination ✓

| Attribute     | Value                                                                 |
| ------------- | --------------------------------------------------------------------- |
| File          | `examples/.../example/PaginationExamples.java`                        |
| Phase         | Phase 6 (Sorting & Pagination)                                        |
| Level         | Basic                                                                 |
| Generated DQL | `me(func: has(name), orderasc: name, first: 10, offset: 20) { name }` |

**Issue:** Returns no data (empty array).

**Root Cause:** Database has fewer than 20 results, so offset:20 returns empty.

**Solution:** Added `setupData()` method to insert 26 unique persons before running pagination queries.

**Status:** ✓ FIXED - Now uses own test data (26 persons)

---

### Phase 7 - Advanced (Aggregations)

#### 3. Math Expression ✓

| Attribute     | Value                                                                                                                                                                                              |
| ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| File          | `examples/.../example/AggregationExamples.java`                                                                                                                                                    |
| Phase         | Phase 7 (Aggregations & Math)                                                                                                                                                                      |
| Level         | Advanced                                                                                                                                                                                           |
| Generated DQL | `{ var(func: has(friend)) { friendCount as count(friend) computedScore as math(friendCount * 10) } me(func: has(name)) { name friendCount: val(friendCount) computedScore: val(computedScore) } }` |

**Issue:** Example was not using `Func.math()` on the value variable.

**Root Cause:** The example was just doing a count without any math operation. Dgraph's `math()` function works on value variables.

**Solution:** Updated example to use:

1. `VarAssignment.valueVar("friendCount", Func.count("friend"))` - count friends
2. `VarAssignment.valueVar("computedScore", Func.math("friendCount * 10"))` - math on value variable
3. `Block.predicate(Func.val("computedScore"), "computedScore")` - retrieve the result

**Status:** ✓ FIXED - Example now correctly uses math() on value variable

---

### Phase 8 - Advanced (Advanced Features)

#### 4. Normalize Directive ✓

| Attribute     | Value                                                                                         |
| ------------- | --------------------------------------------------------------------------------------------- |
| File          | `examples/.../example/AdvancedExamples.java`                                                  |
| Phase         | Phase 8 (Advanced Features)                                                                   |
| Level         | Advanced                                                                                      |
| Generated DQL | `{ me(func: eq(name, "Alice")) @normalize { personName: name friend { friendName: name } } }` |

**Issue:** Returns no data.

**Root Cause:** According to Dgraph docs, `@normalize` only returns **aliased predicates**. Non-aliased predicates are excluded from results.

**Solution:** Updated example to use proper aliases:

- `Block.predicate("name", "personName")` - aliased predicate
- `Block.predicate("name", "friendName")` - aliased predicate in nested block
- Changed query to `eq(name, "Alice")` for specific match

**Status:** ✓ FIXED - Example now correctly uses aliases with @normalize

---

#### 5. Fragment Example ✓

| Attribute     | Value                                                                                       |
| ------------- | ------------------------------------------------------------------------------------------- |
| File          | `examples/.../example/AdvancedExamples.java`                                                |
| Phase         | Phase 8 (Advanced Features)                                                                 |
| Level         | Advanced                                                                                    |
| Generated DQL | `{ fragment PersonDetails { name age } me(func: eq(name, "Alice")) { ... PersonDetails } }` |

**Issue:** Dgraph returns error or empty result.

**Root Cause:** DSL generated incorrect fragment syntax - fragments were placed inside the query block instead of outside.

**Solution:** Fixed Query.java to render fragments AFTER the query block (not inside). Correct DQL syntax:

```
{ me(func: eq(name, "Alice")) { ... PersonDetails } }
fragment PersonDetails { name age }
```

**Status:** ✓ FIXED - DSL bug in fragment rendering

---

### Phase 9 - Advanced (Mutations)

#### 6. Conditional Mutation ✓

| Attribute     | Value                                                                                                                    |
| ------------- | ------------------------------------------------------------------------------------------------------------------------ |
| File          | `examples/.../example/MutationExamples.java`                                                                             |
| Phase         | Phase 9 (Mutations)                                                                                                      |
| Level         | Advanced                                                                                                                 |
| Generated DQL | `upsert { { alice as var(func: eq(name, "Alice")) } mutation @if(uid(alice)) { set { uid(alice) status "active" . } } }` |

**Issue:** Example was incorrectly using `@if` without proper query context.

**Root Cause:** The `@if` directive requires a query variable to evaluate against. Example was using `@if(eq(name, "Alice"))` without running a query first to define what `name` refers to.

**Solution:** Updated example to use proper upsert block format:

1. Added `Mutation.UpsertRaw` to DSL for raw query strings
2. Query: `{ alice as var(func: eq(name, "Alice")) }` - defines variable
3. Condition: `@if(uid(alice))` - checks if variable has UIDs
4. Used HTTP directly for upsert mutations (Java client had issues with the format)

**Status:** ✓ FIXED - Example now correctly uses upsert block with query variable

---

### Phase 11 - Extended Features

#### 7. Delete Predicate Value (RDF Format Issue)

| Attribute     | Value                                               |
| ------------- | --------------------------------------------------- |
| File          | `examples/.../example/DeleteExamples.java`          |
| Phase         | Phase 11.4 (Enhanced Delete)                        |
| Level         | Basic                                              |
| Generated DQL | `{ delete { <0x99> deleteName * . } }`           |

**Issue:** Delete specific predicate value using RDF format fails.

**Root Cause:** Dgraph standalone may have issues with RDF format `<uid> <predicate> * .` pattern.

**Solution:** Use JSON format instead: `{"uid": "0x99", "predicate": null}`

**Status:** ✓ FIXED - Now uses JSON format for delete predicate value

---

## Classification Summary

| Level        | Phase   | Issues        |
| ------------ | ------- | ------------- |
| **Basic**    | 4, 6    | 0 - All fixed |
| **Advanced** | 7, 8, 9 | 0 - All fixed |

## DSL Bug Fixes Applied

During development, the following DSL bugs were identified and fixed:

| Bug                    | File                | Fix                                                         |
| ---------------------- | ------------------- | ----------------------------------------------------------- |
| VarBlock syntax        | `VarBlock.java`     | Changed `(func: ...)` to `var(func: ...)`                   |
| Duplicate "var" prefix | `Query.java`        | Removed duplicate "var" prefix (was generating `varvar`)    |
| GroupBy syntax         | `GroupBy.java`      | Changed to use `@groupby()` directive                       |
| Recurse syntax         | `RecurseBlock.java` | Changed to use `@recurse(depth: N)` directive               |
| Query variable prefix  | `Query.java`        | Support both `$name` and `name` in bindings (auto-adds `$`) |
| Fragment syntax        | `Query.java`        | Render fragments AFTER query block (not inside)             |

## Not Bugs (Expected Behavior)

- **Offset Pagination**: Returns empty when offset > result count (correct Dgraph behavior)
- **Fragment**: ✓ FIXED - DSL was generating incorrect syntax
- **Conditional Mutation**: `@if` requires Cluster mode (Dgraph limitation)
- **Math Expression**: DSL generates unsupported syntax (math() only works on value variables, not predicates)
- **Normalize**: Dgraph v24/v25 bug - documented but returns empty

## Recommendations

1. **Query Variable**: Fix in examples to use `queryWithVars()`
2. **Data Setup**: Drop schema/data at start of each run for clean state
3. **Documentation**: Add notes about Dgraph standalone limitations
4. **Testing**: Consider testing against Dgraph Cluster for features requiring Cluster mode

---

## Data Setup Analysis

### Examples WITH Proper Data Setup (own unique data):

| Example               | Data Created                                       |
| --------------------- | -------------------------------------------------- |
| `PaginationExamples`  | 26 unique persons (Alice-Z)                        |
| `VariableExamples`    | Unique Alice, Bob with friend                      |
| `BasicExamples`       | Alice, Bob, Charlie, Diana with friend             |
| `FilterExamples`      | 5 persons with age, status, friend, email          |
| `FacetExamples`       | Alice with friends having since facet              |
| `AdvancedExamples`    | Alice, Bob, Charlie, Diana with friendships        |
| `AggregationExamples` | Alice, Bob, Charlie, Diana with score and friend   |
| `AdditionalExamples`  | Alice, Bob, Charlie, Diana with friend, age, email |

### Status: ✓ ALL EXAMPLES NOW HAVE PROPER DATA SETUP

---

## Data Teardown (Cleanup) ✓ IMPLEMENTED

After completing data setup, each example also cleans up its own test data after running. This ensures complete isolation between examples.

### Implementation:

Each example's `run()` method now follows this pattern:

```java
@PostConstruct
public void run() {
    setupData();
    try {
        // run all example methods
    } finally {
        teardownData();  // clean up after ourselves
    }
}
```

### Status: ✓ COMPLETE - All 8 examples now have teardownData()

The following examples now have proper teardown:

1. `PaginationExamples` ✓
2. `VariableExamples` ✓
3. `BasicExamples` ✓
4. `FilterExamples` ✓
5. `FacetExamples` ✓
6. `AdvancedExamples` ✓
7. `AggregationExamples` ✓
8. `AdditionalExamples` ✓
