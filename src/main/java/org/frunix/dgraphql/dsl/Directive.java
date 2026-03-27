package org.frunix.dgraphql.dsl;

public record Directive(String name, String content) implements DqlElement {

    public static Directive filter(String condition) {
        return new Directive("filter", condition);
    }

    public static Directive filter(Filter filter) {
        return new Directive("filter", filter.dql());
    }

    public static Directive facets(String... facetNames) {
        return new Directive("facets", String.join(", ", facetNames));
    }

    public static Directive facets(Filter filter) {
        return new Directive("facets", filter.dql());
    }

    public static Directive cascade() {
        return new Directive("cascade", null);
    }

    public static Directive normalize() {
        return new Directive("normalize", null);
    }

    public static Directive generate(String generator) {
        return new Directive("generate", generator);
    }

    public static Directive withSubscription() {
        return new Directive("withSubscription", null);
    }

    public static Directive ignorereflex() {
        return new Directive("ignorereflex", null);
    }

    public static Directive groupby(String predicate) {
        return new Directive("groupby", predicate);
    }

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
