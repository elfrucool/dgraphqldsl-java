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
