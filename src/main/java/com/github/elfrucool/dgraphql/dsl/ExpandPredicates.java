package com.github.elfrucool.dgraphql.dsl;

/**
 * Expansion of predicates for a type.
 *
 * <p>ExpandPredicates is a sealed interface with three variants:</p>
 * <ul>
 *   <li>{@link ByType} - Expand predicates for a specific type</li>
 *   <li>{@link All} - Expand all predicates</li>
 *   <li>{@link WithFilter} - Expand with a filter</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * ExpandPredicates.type("Person")
 * ExpandPredicates.all()
 * ExpandPredicates.allWithFilter(Filter.has("active"))
 * </pre>
 */
public sealed interface ExpandPredicates extends DqlElement
    permits ExpandPredicates.ByType, ExpandPredicates.All, ExpandPredicates.WithFilter {

    /**
     * Expand predicates for a specific type.
     *
     * <p>Example: {@code ExpandPredicates.type("Person")}</p>
     */
    static ByType type(String typeName) {
        return new ByType(typeName);
    }

    /**
     * Expand all predicates.
     */
    static All all() {
        return new All();
    }

    /**
     * Expand all predicates with a filter.
     */
    static WithFilter allWithFilter(Filter filter) {
        return new WithFilter("_all_", filter);
    }

    /**
     * Expand predicates for a type with a filter.
     */
    static WithFilter typeWithFilter(String typeName, Filter filter) {
        return new WithFilter(typeName, filter);
    }

    /**
     * Expand predicates for a specific type.
     */
    record ByType(String typeName) implements ExpandPredicates {
        @Override
        public String dql() {
            return "expand(" + typeName + ")";
        }
    }

    /**
     * Expand all predicates.
     */
    record All() implements ExpandPredicates {
        @Override
        public String dql() {
            return "expand(_all_)";
        }
    }

    /**
     * Expand predicates with a filter.
     */
    record WithFilter(String typeName, Filter filter) implements ExpandPredicates {
        @Override
        public String dql() {
            return "expand(" + typeName + ") @filter(" + filter.dql() + ")";
        }
    }
}
