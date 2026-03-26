package org.frunix.dgraphql.dsl;

public record Func(String name, Object... args) implements DqlElement {

    public static Func eq(String predicate, Object value) {
        return new Func("eq", predicate, value);
    }

    public static Func allofterms(String predicate, String terms) {
        return new Func("allofterms", predicate, terms);
    }

    public static Func anyofterms(String predicate, String terms) {
        return new Func("anyofterms", predicate, terms);
    }

    public static Func has(String predicate) {
        return new Func("has", predicate);
    }

    public static Func uid(String... uids) {
        return new Func("uid", (Object[]) uids);
    }

    public static Func ge(String predicate, Object value) {
        return new Func("ge", predicate, value);
    }

    public static Func gt(String predicate, Object value) {
        return new Func("gt", predicate, value);
    }

    public static Func le(String predicate, Object value) {
        return new Func("le", predicate, value);
    }

    public static Func lt(String predicate, Object value) {
        return new Func("lt", predicate, value);
    }

    public static Func neq(String predicate, Object value) {
        return new Func("neq", predicate, value);
    }

    public static Func between(String predicate, Object start, Object end) {
        return new Func("between", predicate, start, end);
    }

    public static Func match(String predicate, String value) {
        return new Func("match", predicate, value);
    }

    public static Func regexp(String predicate, String pattern) {
        return new Func("regexp", predicate, pattern);
    }

    public static Func regexp(String predicate, String pattern, boolean caseInsensitive) {
        return new Func("regexp", predicate, pattern, caseInsensitive ? "i" : "");
    }

    public static Func type(String typeName) {
        return new Func("type", typeName);
    }

    public static Func alloftext(String predicate, String text) {
        return new Func("alloftext", predicate, text);
    }

    public static Func anyoftext(String predicate, String text) {
        return new Func("anyoftext", predicate, text);
    }

    public static Func uidIn(String predicate, String... uids) {
        Object[] args = new Object[uids.length + 1];
        args[0] = predicate;
        System.arraycopy(uids, 0, args, 1, uids.length);
        return new Func("uid_in", args);
    }

    public static Func near(String predicate, GeoValue geo) {
        return new Func("near", predicate, geo);
    }

    public static Func within(String predicate, GeoValue geo) {
        return new Func("within", predicate, geo);
    }

    public static Func contains(String predicate, GeoValue geo) {
        return new Func("contains", predicate, geo);
    }

    public static Func intersects(String predicate, GeoValue geo) {
        return new Func("intersects", predicate, geo);
    }

    public static Func ngram(String predicate, String terms) {
        return new Func("ngram", predicate, terms);
    }

    public static Func similarTo(String predicate, int k, String vector) {
        return new Func("similar_to", predicate, k, vector);
    }

    public static Func count(String predicate) {
        return new Func("count", predicate);
    }

    public static Func min(String predicate) {
        return new Func("min", predicate);
    }

    public static Func max(String predicate) {
        return new Func("max", predicate);
    }

    public static Func sum(String predicate) {
        return new Func("sum", predicate);
    }

    public static Func avg(String predicate) {
        return new Func("avg", predicate);
    }

    public static Func val(String variableName) {
        return new Func("val", variableName);
    }

    public static Func math(String expression) {
        return new Func("math", expression);
    }

    public static Func expand(String edgeName) {
        return new Func("expand", edgeName);
    }

    public static Func expandAll() {
        return new Func("expand", "_all_");
    }

    public static Func expandReverse() {
        return new Func("expand", "_reverse_");
    }

    public static Func debug() {
        return new Func("debug", (Object) null);
    }

    @Override
    public String dql() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        boolean isUidIn = "uid_in".equals(name);
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatValue(args[i], i == 0, isUidIn));
        }
        sb.append(")");
        return sb.toString();
    }

    private String formatValue(Object value, boolean isPredicate, boolean isUidContext) {
        if (value == null) return "null";
        if (value instanceof String s) {
            if (isPredicate || isUid(s) || (isUidContext && isUid(s))) {
                return s;
            }
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Variable) {
            return ((Variable) value).dql();
        }
        if (value instanceof GeoValue) {
            return ((GeoValue) value).dql();
        }
        if (value instanceof MathExpr m) {
            return m.dql();
        }
        return value.toString();
    }

    private boolean isUid(String s) {
        return s.startsWith("0x") || s.startsWith("0X");
    }
}
