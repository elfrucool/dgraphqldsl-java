package org.frunix.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public sealed interface JsonMutation extends DqlElement 
    permits JsonMutation.Set, JsonMutation.Delete {

    List<Map<String, Object>> getJson();

    record Set(List<Map<String, Object>> jsonObjects) implements JsonMutation {

        public static Set of(Map<String, Object>... jsonObjects) {
            return new Set(List.of(jsonObjects));
        }

        public static Set of(List<Map<String, Object>> jsonObjects) {
            return new Set(jsonObjects);
        }

        public Set withJson(Map<String, Object> json) {
            List<Map<String, Object>> newList = new ArrayList<>(jsonObjects);
            newList.add(json);
            return new Set(newList);
        }

        @Override
        public List<Map<String, Object>> getJson() {
            return jsonObjects;
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ \"set\": [");
            for (int i = 0; i < jsonObjects.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(toJsonString(jsonObjects.get(i)));
            }
            sb.append("] }");
            return sb.toString();
        }

        private String toJsonString(Map<String, Object> obj) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            int count = 0;
            for (Map.Entry<String, Object> entry : obj.entrySet()) {
                if (count > 0) sb.append(", ");
                sb.append("\"").append(entry.getKey()).append("\": ");
                sb.append(formatValue(entry.getValue()));
                count++;
            }
            sb.append("}");
            return sb.toString();
        }

        private String formatValue(Object value) {
            if (value == null) return "null";
            if (value instanceof String s) {
                return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            }
            if (value instanceof Number || value instanceof Boolean) {
                return value.toString();
            }
            if (value instanceof Map m) {
                return toJsonString(m);
            }
            if (value instanceof List l) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < l.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(formatValue(l.get(i)));
                }
                sb.append("]");
                return sb.toString();
            }
            return "\"" + value.toString() + "\"";
        }
    }

    record Delete(List<Map<String, Object>> jsonObjects) implements JsonMutation {

        public static Delete of(Map<String, Object>... jsonObjects) {
            return new Delete(List.of(jsonObjects));
        }

        public static Delete of(List<Map<String, Object>> jsonObjects) {
            return new Delete(jsonObjects);
        }

        public Delete withJson(Map<String, Object> json) {
            List<Map<String, Object>> newList = new ArrayList<>(jsonObjects);
            newList.add(json);
            return new Delete(newList);
        }

        @Override
        public List<Map<String, Object>> getJson() {
            return jsonObjects;
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ \"delete\": [");
            for (int i = 0; i < jsonObjects.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(toJsonString(jsonObjects.get(i)));
            }
            sb.append("] }");
            return sb.toString();
        }

        private String toJsonString(Map<String, Object> obj) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            int count = 0;
            for (Map.Entry<String, Object> entry : obj.entrySet()) {
                if (count > 0) sb.append(", ");
                sb.append("\"").append(entry.getKey()).append("\": ");
                sb.append(formatValue(entry.getValue()));
                count++;
            }
            sb.append("}");
            return sb.toString();
        }

        private String formatValue(Object value) {
            if (value == null) return "null";
            if (value instanceof String s) {
                return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            }
            if (value instanceof Number || value instanceof Boolean) {
                return value.toString();
            }
            if (value instanceof Map m) {
                return toJsonString(m);
            }
            if (value instanceof List l) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < l.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(formatValue(l.get(i)));
                }
                sb.append("]");
                return sb.toString();
            }
            return "\"" + value.toString() + "\"";
        }
    }

    static JsonMutation set(Map<String, Object>... jsonObjects) {
        return Set.of(jsonObjects);
    }

    static JsonMutation set(List<Map<String, Object>> jsonObjects) {
        return Set.of(jsonObjects);
    }

    static JsonMutation delete(Map<String, Object>... jsonObjects) {
        return Delete.of(jsonObjects);
    }

    static JsonMutation delete(List<Map<String, Object>> jsonObjects) {
        return Delete.of(jsonObjects);
    }
}