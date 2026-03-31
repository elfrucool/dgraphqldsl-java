/**
 * A type-safe DSL (Domain Specific Language) for building DQL queries in Java.
 *
 * <p>This library provides a fluent, builder-style API for constructing Dgraph DQL
 * queries, mutations, and schema alterations. All classes use immutable records
 * with "with" methods for creating modified copies.</p>
 *
 * <h2>Core Concepts</h2>
 *
 * <ul>
 *   <li>{@link com.github.elfrucool.dgraphql.dsl.Query} - Main entry point for building queries</li>
 *   <li>{@link com.github.elfrucool.dgraphql.dsl.Block} - Query blocks (predicates, nested, functions)</li>
 *   <li>{@link com.github.elfrucool.dgraphql.dsl.Mutation} - Data modification operations</li>
 *   <li>{@link com.github.elfrucool.dgraphql.dsl.Filter} - Query filters</li>
 *   <li>{@link com.github.elfrucool.dgraphql.dsl.DqlElement} - Base interface for all DSL elements</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 *
 * <pre>
 * import static com.github.elfrucool.dgraphql.dsl.Query.*;
 * import static com.github.elfrucool.dgraphql.dsl.Block.*;
 * import static com.github.elfrucool.dgraphql.dsl.Func.*;
 * import static com.github.elfrucool.dgraphql.dsl.Filter.*;
 *
 * // Build a query
 * Query query = query(
 *     queryBlock("all", func(eq("name", "Alice")),
 *         predicate("name"),
 *         predicate("email"),
 *         nested("friend",
 *             predicate("name"),
 *             predicate("age")
 *         )
 *     )
 * );
 *
 * // Generate DQL string
 * String dql = query.dql().query();
 * </pre>
 *
 * <h2>Key Features</h2>
 *
 * <ul>
 *   <li><b>Type-safe</b> - All DQL constructs are represented as Java types</li>
 *   <li><b>Immutable</b> - All records use "with" methods for modifications</li>
 *   <li><b>Fluent API</b> - Builder pattern for intuitive query construction</li>
 *   <li><b>Complete coverage</b> - Supports all DQL features (queries, mutations, schema)</li>
 * </ul>
 *
 * <h2>Package Structure</h2>
 *
 * <ul>
 *   <li>{@code Query.java} - Query building and execution</li>
 *   <li>{@code QueryBlock.java} - Query blocks (Named/Anonymous)</li>
 *   <li>{@code Block.java} - Block types (Predicate, Nested, FuncBlock, etc.)</li>
 *   <li>{@code Func.java} - DQL functions (eq, has, count, etc.)</li>
 *   <li>{@code Filter.java} - Filter expressions (AND, OR, NOT)</li>
 *   <li>{@code Mutation.java} - Mutation types (Set, Delete, Update, etc.)</li>
 *   <li>{@code Directive.java} - Query directives (@filter, @cascade, etc.)</li>
 *   <li>{@code Variable.java} - Query variables</li>
 * </ul>
 *
 * @see <a href="https://dgraph.io/docs/query-language/">Dgraph DQL Documentation</a>
 */
package com.github.elfrucool.dgraphql.dsl;
