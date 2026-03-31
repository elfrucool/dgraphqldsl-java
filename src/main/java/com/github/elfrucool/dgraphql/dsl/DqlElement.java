package com.github.elfrucool.dgraphql.dsl;

/**
 * Base interface for all DSL elements.
 *
 * <p>All DSL classes implement this interface to provide DQL string generation.
 * The {@link #dql()} method produces the DQL query string representation.</p>
 *
 * @see Query
 * @see Mutation
 * @see Block
 */
public interface DqlElement {
    /**
     * Generates the DQL string representation of this element.
     *
     * @return DQL query string
     */
    String dql();
}
