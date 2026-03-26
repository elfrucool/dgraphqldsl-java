package org.frunix.dgraphql.dsl;

public sealed interface Filter extends DqlElement 
    permits Filter.And, Filter.Or, Filter.Not, Filter.FuncFilter {

    record And(Filter left, Filter right) implements Filter {
        @Override
        public String dql() {
            return "(" + left.dql() + " AND " + right.dql() + ")";
        }
    }

    record Or(Filter left, Filter right) implements Filter {
        @Override
        public String dql() {
            return "(" + left.dql() + " OR " + right.dql() + ")";
        }
    }

    record Not(Filter inner) implements Filter {
        @Override
        public String dql() {
            return "(NOT " + inner.dql() + ")";
        }
    }

    record FuncFilter(Func func) implements Filter {
        @Override
        public String dql() {
            return func.dql();
        }
    }

    public static Filter and(Filter... filters) {
        if (filters.length == 1) return filters[0];
        Filter result = filters[0];
        for (int i = 1; i < filters.length; i++) {
            result = new And(result, filters[i]);
        }
        return result;
    }

    public static Filter or(Filter... filters) {
        if (filters.length == 1) return filters[0];
        Filter result = filters[0];
        for (int i = 1; i < filters.length; i++) {
            result = new Or(result, filters[i]);
        }
        return result;
    }

    public static Filter not(Filter inner) {
        return new Not(inner);
    }

    public static Filter func(Func func) {
        return new FuncFilter(func);
    }

    public static Filter eq(String predicate, Object value) {
        return new FuncFilter(Func.eq(predicate, value));
    }

    public static Filter has(String predicate) {
        return new FuncFilter(Func.has(predicate));
    }

    public static Filter ge(String predicate, Object value) {
        return new FuncFilter(Func.ge(predicate, value));
    }

    public static Filter gt(String predicate, Object value) {
        return new FuncFilter(Func.gt(predicate, value));
    }

    public static Filter le(String predicate, Object value) {
        return new FuncFilter(Func.le(predicate, value));
    }

    public static Filter lt(String predicate, Object value) {
        return new FuncFilter(Func.lt(predicate, value));
    }

    public static Filter neq(String predicate, Object value) {
        return new FuncFilter(Func.neq(predicate, value));
    }

    public static Filter allofterms(String predicate, String terms) {
        return new FuncFilter(Func.allofterms(predicate, terms));
    }

    public static Filter anyofterms(String predicate, String terms) {
        return new FuncFilter(Func.anyofterms(predicate, terms));
    }
}
