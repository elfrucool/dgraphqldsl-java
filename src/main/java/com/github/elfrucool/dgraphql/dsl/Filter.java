package com.github.elfrucool.dgraphql.dsl;

/**
 * A filter expression for filtering query results.
 *
 * <p>Filter is a sealed interface with four variants:</p>
 * <ul>
 *   <li>{@link And} - Boolean AND of two filters</li>
 *   <li>{@link Or} - Boolean OR of two filters</li>
 *   <li>{@link Not} - Boolean NOT of a filter</li>
 *   <li>{@link FuncFilter} - A function-based filter (eq, has, ge, etc.)</li>
 * </ul>
 *
 * <p>Use factory methods to create filters:</p>
 *
 * <pre>
 * Filter.eq("name", "Alice")                               // Simple filter
 * Filter.and(Filter.eq("age", 18), Filter.has("email"))    // AND
 * Filter.or(Filter.has("friend"), Filter.has("enemy"))     // OR
 * Filter.not(Filter.eq("status", "deleted"))              // NOT
 * </pre>
 *
 * @see Func
 * @see Directive#filter(Filter)
 */
public sealed interface Filter extends DqlElement 
    permits Filter.And, Filter.Or, Filter.Not, Filter.FuncFilter {

    /**
     * Boolean AND of two filters.
     *
     * <p>Example: {@code (age >= 18 AND has(email))}</p>
     */
    record And(Filter left, Filter right) implements Filter {
        @Override
        public String dql() {
            return "(" + left.dql() + " AND " + right.dql() + ")";
        }
    }

    /**
     * Boolean OR of two filters.
     *
     * <p>Example: {@code (type(Person) OR type(Organization))}</p>
     */
    record Or(Filter left, Filter right) implements Filter {
        @Override
        public String dql() {
            return "(" + left.dql() + " OR " + right.dql() + ")";
        }
    }

    /**
     * Boolean NOT of a filter.
     *
     * <p>Example: {@code (NOT eq(status, "deleted"))}</p>
     */
    record Not(Filter inner) implements Filter {
        @Override
        public String dql() {
            return "(NOT " + inner.dql() + ")";
        }
    }

    /**
     * A function-based filter.
     *
     * <p>Wraps a Func for use as a filter.</p>
     *
     * @see Func
     */
    record FuncFilter(Func func) implements Filter {
        @Override
        public String dql() {
            return func.dql();
        }
    }

    /**
     * Boolean AND of multiple filters.
     *
     * <p>Example: {@code Filter.and(Filter.eq("age", 18), Filter.has("email"))}</p>
     */
    public static Filter and(Filter... filters) {
        if (filters.length == 1) return filters[0];
        Filter result = filters[0];
        for (int i = 1; i < filters.length; i++) {
            result = new And(result, filters[i]);
        }
        return result;
    }

    /**
     * Boolean OR of multiple filters.
     *
     * <p>Example: {@code Filter.or(Filter.has("friend"), Filter.has("enemy"))}</p>
     */
    public static Filter or(Filter... filters) {
        if (filters.length == 1) return filters[0];
        Filter result = filters[0];
        for (int i = 1; i < filters.length; i++) {
            result = new Or(result, filters[i]);
        }
        return result;
    }

    /**
     * Boolean NOT of a filter.
     *
     * <p>Example: {@code Filter.not(Filter.eq("status", "deleted"))}</p>
     */
    public static Filter not(Filter inner) {
        return new Not(inner);
    }

    /**
     * Wraps a Func as a Filter.
     *
     * <p>Example: {@code Filter.func(Func.gt("age", 21))}</p>
     *
     * @see Func
     */
    public static Filter func(Func func) {
        return new FuncFilter(func);
    }

    /**
     * Equality filter.
     *
     * <p>Example: {@code Filter.eq("name", "Alice")}</p>
     */
    public static Filter eq(String predicate, Object value) {
        return new FuncFilter(Func.eq(predicate, value));
    }

    /**
     * Has filter - matches nodes that have the predicate.
     *
     * <p>Example: {@code Filter.has("email")}</p>
     */
    public static Filter has(String predicate) {
        return new FuncFilter(Func.has(predicate));
    }

    /**
     * Greater than or equal filter.
     *
     * <p>Example: {@code Filter.ge("age", 18)}</p>
     */
    public static Filter ge(String predicate, Object value) {
        return new FuncFilter(Func.ge(predicate, value));
    }

    /**
     * Greater than filter.
     *
     * <p>Example: {@code Filter.gt("age", 17)}</p>
     */
    public static Filter gt(String predicate, Object value) {
        return new FuncFilter(Func.gt(predicate, value));
    }

    /**
     * Less than or equal filter.
     *
     * <p>Example: {@code Filter.le("age", 65)}</p>
     */
    public static Filter le(String predicate, Object value) {
        return new FuncFilter(Func.le(predicate, value));
    }

    /**
     * Less than filter.
     *
     * <p>Example: {@code Filter.lt("age", 18)}</p>
     */
    public static Filter lt(String predicate, Object value) {
        return new FuncFilter(Func.lt(predicate, value));
    }

    /**
     * Not equal filter.
     *
     * <p>Example: {@code Filter.neq("status", "deleted")}</p>
     */
    public static Filter neq(String predicate, Object value) {
        return new FuncFilter(Func.neq(predicate, value));
    }

    /**
     * All of terms filter (full-text search).
     *
     * <p>Example: {@code Filter.allofterms("name", "Alice Smith")}</p>
     */
    public static Filter allofterms(String predicate, String terms) {
        return new FuncFilter(Func.allofterms(predicate, terms));
    }

    /**
     * Any of terms filter (full-text search).
     *
     * <p>Example: {@code Filter.anyofterms("name", "Alice Bob")}</p>
     */
    public static Filter anyofterms(String predicate, String terms) {
        return new FuncFilter(Func.anyofterms(predicate, terms));
    }
}
