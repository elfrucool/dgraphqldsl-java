package org.frunix.dgraphql.dsl;

public record Variable(String name, String type, Object defaultValue) implements DqlElement {

    public static Variable param(String name) {
        return new Variable(name, null, null);
    }

    public static Variable queryVar(String name, String type) {
        return new Variable(name, type, null);
    }

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
