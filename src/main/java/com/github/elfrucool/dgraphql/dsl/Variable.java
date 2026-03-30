package com.github.elfrucool.dgraphql.dsl;

/**
 * A query variable for parameterized queries.
 *
 * <p>Variables can be used in queries to:</p>
 * <ul>
 *   <li>Define query parameters</li>
 *   <li>Store computed values</li>
 *   <li>Reuse values across the query</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * Variable.param("userName")                                    // $userName
 * Variable.queryVar("count", "int")                            // $count: int
 * Variable.queryVar("name", "string", "Alice")                 // $name: string = "Alice"
 * </pre>
 *
 * @see Query#withVariables(List)
 */
public record Variable(String name, String type, Object defaultValue) implements DqlElement {

    /**
     * Creates a simple parameter variable.
     *
     * <p>Example: {@code Variable.param("userId")} produces {@code $userId}</p>
     */
    public static Variable param(String name) {
        return new Variable(name, null, null);
    }

    /**
     * Creates a typed query variable without a default value.
     *
     * <p>Example: {@code Variable.queryVar("limit", "int")} produces {@code $limit: int}</p>
     */
    public static Variable queryVar(String name, String type) {
        return new Variable(name, type, null);
    }

    /**
     * Creates a typed query variable with a default value.
     *
     * <p>Example: {@code Variable.queryVar("name", "string", "Alice")} produces {@code $name: string = "Alice"}</p>
     */
    public static Variable queryVar(String name, String type, Object defaultValue) {
        return new Variable(name, type, defaultValue);
    }

    @Override
    public String dql() {
        return "$" + name;
    }

    public String declaration() {
        StringBuilder sb = new StringBuilder();
        sb.append("$").append(name);
        if (type != null) {
            sb.append(": ").append(type);
        }
        if (defaultValue != null) {
            sb.append(" = ").append(formatValue(defaultValue));
        }
        return sb.toString();
    }

    private String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) {
            return "\"" + ((String) value).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return value.toString();
    }
}
