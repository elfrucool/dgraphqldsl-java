package org.frunix.dgraphql.dsl;

import java.util.Map;

public record DqlResult(String query, Map<String, Object> variables) {
    public static DqlResult of(String query) {
        return new DqlResult(query, Map.of());
    }

    public static DqlResult of(String query, Map<String, Object> variables) {
        return new DqlResult(query, variables);
    }
}
