package org.frunix.dgraphql.dsl;

public sealed interface ExpandPredicates extends DqlElement
    permits ExpandPredicates.ByType, ExpandPredicates.All, ExpandPredicates.WithFilter {

    static ByType type(String typeName) {
        return new ByType(typeName);
    }

    static All all() {
        return new All();
    }

    static WithFilter allWithFilter(Filter filter) {
        return new WithFilter("_all_", filter);
    }

    static WithFilter typeWithFilter(String typeName, Filter filter) {
        return new WithFilter(typeName, filter);
    }

    record ByType(String typeName) implements ExpandPredicates {
        @Override
        public String dql() {
            return "expand(" + typeName + ")";
        }
    }

    record All() implements ExpandPredicates {
        @Override
        public String dql() {
            return "expand(_all_)";
        }
    }

    record WithFilter(String typeName, Filter filter) implements ExpandPredicates {
        @Override
        public String dql() {
            return "expand(" + typeName + ") @filter(" + filter.dql() + ")";
        }
    }
}
