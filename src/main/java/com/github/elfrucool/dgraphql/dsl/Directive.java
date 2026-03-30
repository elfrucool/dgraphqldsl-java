package com.github.elfrucool.dgraphql.dsl;

/**
 * A DQL directive for query modifiers.
 *
 * <p>Directives provide additional query functionality such as filtering,
 * pagination, and result transformation:</p>
 *
 * <ul>
 *   <li>{@link #filter(Filter)} - Apply a filter to the query</li>
 *   <li>{@link #facets(String...)} - Include facet data</li>
 *   <li>{@link #cascade()} - Cascade results up the tree</li>
 *   <li>{@link #normalize()} - Normalize JSON output</li>
 *   <li>{@link #recurse(int)} - Recursive query traversal</li>
 *   <li>{@link #groupby(String)} - Group results</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * Directive.filter(Filter.eq("name", "Alice"))
 * Directive.cascade()
 * Directive.recurse(5)
 * </pre>
 */
public record Directive(String name, String content) implements DqlElement {

    /**
     * Filter directive with a string condition.
     *
     * <p>Example: {@code @filter(eq(name, "Alice"))}</p>
     */
    public static Directive filter(String condition) {
        return new Directive("filter", condition);
    }

    /**
     * Filter directive with a Filter object.
     *
     * <p>Example: {@code @filter(eq(name, "Alice"))}</p>
     *
     * @see Filter
     */
    public static Directive filter(Filter filter) {
        return new Directive("filter", filter.dql());
    }

    /**
     * Facets directive to include specific facets.
     *
     * <p>Example: {@code @facets(friendCount, since)}</p>
     */
    public static Directive facets(String... facetNames) {
        return new Directive("facets", String.join(", ", facetNames));
    }

    /**
     * Facets directive with a filter.
     *
     * <p>Example: {@code @facets(eq(status, "active"))}</p>
     *
     * @see Filter
     */
    public static Directive facets(Filter filter) {
        return new Directive("facets", filter.dql());
    }

    /**
     * Cascade directive to return nodes that have all predicates.
     *
     * <p>Example: {@code @cascade}</p>
     */
    public static Directive cascade() {
        return new Directive("cascade", null);
    }

    /**
     * Normalize directive to normalize JSON output.
     *
     * <p>Example: {@code @normalize}</p>
     */
    public static Directive normalize() {
        return new Directive("normalize", null);
    }

    /**
     * Generate directive with a JSON generator.
     *
     * <p>Example: {@code @generate(myGenerator)}</p>
     */
    public static Directive generate(String generator) {
        return new Directive("generate", generator);
    }

    /**
     * With subscription directive for real-time updates.
     *
     * <p>Example: {@code @withSubscription}</p>
     */
    public static Directive withSubscription() {
        return new Directive("withSubscription", null);
    }

    /**
     * Ignore reflex directive to exclude self-references.
     *
     * <p>Example: {@code @ignorereflex}</p>
     */
    public static Directive ignorereflex() {
        return new Directive("ignorereflex", null);
    }

    /**
     * Group by directive for aggregation.
     *
     * <p>Example: {@code @groupby(name)}</p>
     */
    public static Directive groupby(String predicate) {
        return new Directive("groupby", predicate);
    }

    /**
     * Recurse directive for recursive traversal.
     *
     * <p>Example: {@code @recurse(depth: 5)}</p>
     */
    public static Directive recurse(int depth) {
        return new Directive("recurse", "depth: " + depth);
    }

    @Override
    public String dql() {
        if (content == null || content.isEmpty()) {
            return "@" + name;
        }
        return "@" + name + "(" + content + ")";
    }
}
