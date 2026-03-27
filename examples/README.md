# Examples

This subproject demonstrates the DSL library features by running queries against a live Dgraph instance.

## Status: ✅ All 25 Examples Passing

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
| AdditionalExamples  | 10    | GroupBy, ALTER, JSON mutations         |

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

- **Total**: 25 examples
- **Passing**: 25
- **Failing**: 0

All examples passing! See [docs/examples-issues.md](../docs/examples-issues.md) for details on how issues were resolved.

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
