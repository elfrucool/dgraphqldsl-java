package io.github.elfrucool.dgraphql.dsl;

import io.github.elfrucool.dgraphql.dsl.GeoValue;
import io.github.elfrucool.dgraphql.dsl.MathExpr;
import io.github.elfrucool.dgraphql.dsl.Variable;

/**
 * A DQL function call with a name and arguments.
 *
 * <p>Functions are used in query filters, aggregations, and transformations.
 * This class provides factory methods for all DQL built-in functions:</p>
 *
 * <ul>
 *   <li><b>Comparison</b>: eq, ge, gt, le, lt, neq, between</li>
 *   <li><b>Text</b>: allofterms, anyofterms, alloftext, anyoftext, match, regexp</li>
 *   <li><b>UID</b>: uid, uid_in, type, has</li>
 *   <li><b>Geo</b>: near, within, contains, intersects</li>
 *   <li><b>Aggregation</b>: count, min, max, sum, avg</li>
 *   <li><b>Math</b>: math, val</li>
 *   <li><b>Expand</b>: expand</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * Func.eq("name", "Alice")          // eq(name, "Alice")
 * Func.count("friend")              // count(friend)
 * Func.near("location", geoValue)   // near(location, {...})
 * </pre>
 *
 * @see Filter
 * @see Block#predicate(Func)
 */
public record Func(String name, Object... args) implements DqlElement {

    /**
     * Equality function: matches nodes where predicate equals value.
     *
     * <p>Example: {@code eq(name, "Alice")}</p>
     */
    public static Func eq(String predicate, Object value) {
        return new Func("eq", predicate, value);
    }

    /**
     * All of terms: matches nodes where all terms are present (full text search).
     *
     * <p>Example: {@code allofterms(name, "Alice Smith")}</p>
     */
    public static Func allofterms(String predicate, String terms) {
        return new Func("allofterms", predicate, terms);
    }

    /**
     * Any of terms: matches nodes where any term is present (full text search).
     *
     * <p>Example: {@code anyofterms(name, "Alice Bob")}</p>
     */
    public static Func anyofterms(String predicate, String terms) {
        return new Func("anyofterms", predicate, terms);
    }

    /**
     * Has: matches nodes that have the specified predicate.
     *
     * <p>Example: {@code has(email)}</p>
     */
    public static Func has(String predicate) {
        return new Func("has", predicate);
    }

    /**
     * UID: matches nodes with the specified UID(s).
     *
     * <p>Example: {@code uid("0x123", "0x456")}</p>
     */
    public static Func uid(String... uids) {
        return new Func("uid", (Object[]) uids);
    }

    /**
     * Greater than or equal: matches nodes where predicate >= value.
     *
     * <p>Example: {@code ge(age, 18)}</p>
     */
    public static Func ge(String predicate, Object value) {
        return new Func("ge", predicate, value);
    }

    /**
     * Greater than: matches nodes where predicate > value.
     *
     * <p>Example: {@code gt(age, 17)}</p>
     */
    public static Func gt(String predicate, Object value) {
        return new Func("gt", predicate, value);
    }

    /**
     * Less than or equal: matches nodes where predicate <= value.
     *
     * <p>Example: {@code le(age, 65)}</p>
     */
    public static Func le(String predicate, Object value) {
        return new Func("le", predicate, value);
    }

    /**
     * Less than: matches nodes where predicate < value.
     *
     * <p>Example: {@code lt(age, 18)}</p>
     */
    public static Func lt(String predicate, Object value) {
        return new Func("lt", predicate, value);
    }

    /**
     * Not equal: matches nodes where predicate != value.
     *
     * <p>Example: {@code neq(status, "deleted")}</p>
     */
    public static Func neq(String predicate, Object value) {
        return new Func("neq", predicate, value);
    }

    /**
     * Between: matches nodes where predicate is within a range.
     *
     * <p>Example: {@code between(age, 18, 65)}</p>
     */
    public static Func between(String predicate, Object start, Object end) {
        return new Func("between", predicate, start, end);
    }

    /**
     * Match: matches nodes using fuzzy string matching.
     *
     * <p>Example: {@code match(name, "Alice")}</p>
     */
    public static Func match(String predicate, String value) {
        return new Func("match", predicate, value);
    }

    /**
     * Regexp: matches nodes using regular expression pattern.
     *
     * <p>Example: {@code regexp(email, ".*@example.com")}</p>
     */
    public static Func regexp(String predicate, String pattern) {
        return new Func("regexp", predicate, pattern);
    }

    /**
     * Regexp with case sensitivity control.
     *
     * <p>Example: {@code regexp(name, "alice", true)} for case-insensitive</p>
     */
    public static Func regexp(String predicate, String pattern, boolean caseInsensitive) {
        return new Func("regexp", predicate, pattern, caseInsensitive ? "i" : "");
    }

    /**
     * Type: matches nodes of the specified type.
     *
     * <p>Example: {@code type(Person)}</p>
     */
    public static Func type(String typeName) {
        return new Func("type", typeName);
    }

    /**
     * All of text: matches nodes where all text matches (full-text search).
     *
     * <p>Example: {@code alloftext(bio, "developer engineer")}</p>
     */
    public static Func alloftext(String predicate, String text) {
        return new Func("alloftext", predicate, text);
    }

    /**
     * Any of text: matches nodes where any text matches (full-text search).
     *
     * <p>Example: {@code anyoftext(bio, "java python")}</p>
     */
    public static Func anyoftext(String predicate, String text) {
        return new Func("anyoftext", predicate, text);
    }

    /**
     * UID in: checks if a predicate contains any of the given UIDs.
     *
     * <p>Example: {@code uidIn(friend, "0x123", "0x456")}</p>
     */
    public static Func uidIn(String predicate, String... uids) {
        Object[] args = new Object[uids.length + 1];
        args[0] = predicate;
        System.arraycopy(uids, 0, args, 1, uids.length);
        return new Func("uid_in", args);
    }

    /**
     * Near: matches nodes within a radius of a geo point.
     *
     * <p>Example: {@code near(location, geoValue)}</p>
     *
     * @see GeoValue
     */
    public static Func near(String predicate, GeoValue geo) {
        return new Func("near", predicate, geo);
    }

    /**
     * Within: matches nodes within a geo polygon.
     *
     * <p>Example: {@code within(location, polygon)}</p>
     *
     * @see GeoValue
     */
    public static Func within(String predicate, GeoValue geo) {
        return new Func("within", predicate, geo);
    }

    /**
     * Contains: matches nodes that contain the geo point.
     *
     * <p>Example: {@code contains(location, point)}</p>
     *
     * @see GeoValue
     */
    public static Func contains(String predicate, GeoValue geo) {
        return new Func("contains", predicate, geo);
    }

    /**
     * Intersects: matches nodes that intersect with the geo area.
     *
     * <p>Example: {@code intersects(area1, area2)}</p>
     *
     * @see GeoValue
     */
    public static Func intersects(String predicate, GeoValue geo) {
        return new Func("intersects", predicate, geo);
    }

    /**
     * Ngram: matches using n-gram index.
     *
     * <p>Example: {@code ngram(description, "search term")}</p>
     */
    public static Func ngram(String predicate, String terms) {
        return new Func("ngram", predicate, terms);
    }

    /**
     * Similar to: finds similar vectors using k-NN search.
     *
     * <p>Example: {@code similarTo(embedding, 5, vectorString)}</p>
     */
    public static Func similarTo(String predicate, int k, String vector) {
        return new Func("similar_to", predicate, k, vector);
    }

    /**
     * Count: returns the count of edges for a predicate.
     *
     * <p>Example: {@code count(friend)}</p>
     */
    public static Func count(String predicate) {
        return new Func("count", predicate);
    }

    /**
     * Min: returns the minimum value of a numeric predicate.
     *
     * <p>Example: {@code min(age)}</p>
     */
    public static Func min(String predicate) {
        return new Func("min", predicate);
    }

    /**
     * Max: returns the maximum value of a numeric predicate.
     *
     * <p>Example: {@code max(age)}</p>
     */
    public static Func max(String predicate) {
        return new Func("max", predicate);
    }

    /**
     * Sum: returns the sum of a numeric predicate.
     *
     * <p>Example: {@code sum(amount)}</p>
     */
    public static Func sum(String predicate) {
        return new Func("sum", predicate);
    }

    /**
     * Avg: returns the average of a numeric predicate.
     *
     * <p>Example: {@code avg(rating)}</p>
     */
    public static Func avg(String predicate) {
        return new Func("avg", predicate);
    }

    /**
     * Val: references a variable's value in expressions.
     *
     * <p>Example: {@code val(countVar)}</p>
     */
    public static Func val(String variableName) {
        return new Func("val", variableName);
    }

    /**
     * Math: evaluates a mathematical expression.
     *
     * <p>Example: {@code math("age * 2 + 10")}</p>
     *
     * @see MathExpr
     */
    public static Func math(String expression) {
        return new Func("math", expression);
    }

    /**
     * Expand: expands all predicates of a specific edge.
     *
     * <p>Example: {@code expand(friend)}</p>
     */
    public static Func expand(String edgeName) {
        return new Func("expand", edgeName);
    }

    /**
     * Expand all: expands all predicates for all edges.
     *
     * <p>Example: {@code expandAll()}</p>
     */
    public static Func expandAll() {
        return new Func("expand", "_all_");
    }

    /**
     * Expand reverse: expands all reverse edges.
     *
     * <p>Example: {@code expandReverse()}</p>
     */
    public static Func expandReverse() {
        return new Func("expand", "_reverse_");
    }

    /**
     * Debug: includes debug information in query results.
     *
     * <p>Example: {@code debug()}</p>
     */
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
