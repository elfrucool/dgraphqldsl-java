package com.github.elfrucool.dgraphql.dsl;

import java.util.Map;

/**
 * The result of building a DQL query.
 *
 * <p>Contains the generated DQL query string and any variables for parameterized queries:</p>
 *
 * <pre>
 * DqlResult result = query.dql();
 * String dql = result.query();
 * Map&lt;String, Object&gt; vars = result.variables();
 * </pre>
 *
 * @see Query#dql()
 */
public record DqlResult(String query, Map<String, Object> variables) {

    /**
     * Creates a DqlResult with just a query (no variables).
     */
    public static DqlResult of(String query) {
        return new DqlResult(query, Map.of());
    }

    /**
     * Creates a DqlResult with query and variables.
     */
    public static DqlResult of(String query, Map<String, Object> variables) {
        return new DqlResult(query, variables);
    }
}
