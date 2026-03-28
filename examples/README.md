# Examples

This subproject demonstrates the DSL library features by running queries against a live Dgraph instance.

## Status: ✅ All 27 Examples Passing

## Overview

- **Purpose**: Validate DSL functionality against real Dgraph database
- **Framework**: Spring Boot 4.x with Dgraph Java client
- **Data**: Each example sets up its own test data for isolation
- **Task CLI**: Install from [taskfile.dev](https://taskfile.dev)

## Quick Start

```bash
# Start Dgraph, run examples, stop Dgraph
task up run down
```

See [Main README.md](../README.md#examples) for more details.

## Project Structure

```
examples/
├── build.gradle.kts
├── docker-compose.yaml
└── src/main/java/org/frunix/dgraphql/examples/
    ├── ExamplesApplication.java
    ├── config/
    │   └── DgraphConfig.java
    ├── config/DataSetup.java
    ├── example/
    │   ├── BasicExamples.java
    │   ├── FilterExamples.java
    │   ├── VariableExamples.java
    │   ├── FacetExamples.java
    │   ├── PaginationExamples.java
    │   ├── AggregationExamples.java
    │   ├── AdvancedExamples.java
    │   ├── MutationExamples.java
    │   └── AdditionalExamples.java
    └── result/
        └── ResultsCollector.java
```

## Example Coverage

| Class               | Phase | Features                               |
| ------------------- | ----- | -------------------------------------- |
| BasicExamples       | 1-2   | Basic queries, nested blocks, aliases  |
| FilterExamples      | 3     | AND/OR/NOT filters, comparisons        |
| VariableExamples    | 4     | Query variables, value variables       |
| FacetExamples       | 5     | Facets                                 |
| PaginationExamples  | 6     | Sorting, pagination                    |
| AggregationExamples | 7     | count, math expressions                |
| AdvancedExamples    | 8     | cascade, normalize, recurse, fragments |
| MutationExamples    | 9     | Set, Delete, Conditional mutations     |
| AdditionalExamples  | 10    | GroupBy, ALTER, JSON mutations        |
| LanguageExamples    | 11.1  | Language-tagged values (@en, @fr...)  |
| SchemaExamples      | 11.2  | Schema features (@count, @upsert)     |
| DeleteExamples      | 11.3  | Enhanced delete patterns              |
| UpsertExamples      | 11.4  | Upsert blocks                         |
| AliasExamples       | 11.6  | Aliases (alias: predicate)           |
| PathExamples        | 11.5  | K-shortest path queries               |
| ExpandExamples      | 11.7  | Expand predicates from types          |

## Running Examples

### Option 1: Taskfile (Recommended)

```bash
task up     # Start Dgraph
task run    # Run examples
task down   # Stop Dgraph
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

### Option 3: Manual

```bash
cd examples
docker compose up -d --wait
./gradlew bootRun
docker compose down
```

## Test Results

- **Total**: 27 examples
- **Passing**: 25
- **Failing**: 0

All examples passing! See [docs/examples-issues.md](../docs/examples-issues.md) for details on how issues were resolved.

## Featured: K-Shortest Path (Phase 11.5)

The K-Shortest Path feature demonstrates finding multiple paths between nodes in a graph.

### What it does

Given a graph:
```
Alice (0x1) --friend--> Charlie (0x3)   [direct path, weight=1]
Alice (0x1) --friend--> Bob (0x2) --friend--> Charlie (0x3)  [indirect path, weight=2]
```

### Queries

**Shortest Path** (k=1): Finds the single shortest path
```java
ShortestPath.shortest("path", "0x1", "0x3")
    .withPredicate(Block.predicate("friend"))
// DQL: path as shortest(from: 0x1, to: 0x3) { friend }
```

**K-Shortest Paths** (k=2): Finds multiple ranked paths
```java
ShortestPath.kShortest("path", "0x1", "0x3", 2)
    .withPredicate(Block.predicate("friend"))
// DQL: path as shortest(from: 0x1, to: 0x3, numpaths: 2) { friend }
```

### Results

**Shortest Path:**
```json
{"me":[{"name":"Alice"},{"name":"Charlie"}],"_path_":[{"friend":{"uid":"0x3"},"uid":"0x1","_weight_":1}]}
```
→ Found direct path (Alice → Charlie)

**K-Shortest Paths (k=2):**
```json
{"me":[{"name":"Alice"},{"name":"Charlie"}],"_path_":[
  {"friend":{"uid":"0x3"},"uid":"0x1","_weight_":1},
  {"friend":{"friend":{"uid":"0x3"},"uid":"0x2"},"uid":"0x1","_weight_":2}
]}
```
→ Found 2 paths: direct (weight=1) and via Bob (weight=2)

### Use Cases

- **Route planning**: Find shortest vs fastest routes
- **Social networks**: Multiple connection paths between users
- **Network analysis**: Alternative paths when primary fails
- **Logistics**: Different delivery routes ranked by cost

## Troubleshooting

### Check Dgraph Status

```bash
task ps
# or
docker compose -f examples/docker-compose.yaml ps
```

### View Dgraph Health

```bash
curl http://localhost:8080/health
```

### Query Dgraph Directly

```bash
curl -X POST http://localhost:8080/query \
  -H "Content-Type: application/json" \
  -d '{ "query": "{ me(func: has(name)) { name } }" }'
```
