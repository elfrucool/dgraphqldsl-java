package com.github.elfrucool.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * A schema alteration operation.
 *
 * <p>Alter is a sealed interface with four variants:</p>
 * <ul>
 *   <li>{@link TypeDefinition} - Define a type with fields</li>
 *   <li>{@link PredicateSchema} - Define or modify a predicate schema</li>
 *   <li>{@link Drop} - Drop a type or predicate</li>
 *   <li>{@link All} - Perform multiple alterations</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * Alter.TypeDefinition.of("Person", "name", "email", "age")
 * Alter.PredicateSchema.predicate("owner", "uid").withIndex("exact")
 * </pre>
 */
public sealed interface Alter extends DqlElement 
    permits Alter.TypeDefinition, Alter.PredicateSchema, Alter.Drop, Alter.All {

    String dql();

    /**
     * Defines a type with its fields.
     *
     * <p>Example: {@code Alter.TypeDefinition.of("Person", "name", "email", "age")}</p>
     */
    record TypeDefinition(
        String typeName,
        List<String> fields
    ) implements Alter {

        public static TypeDefinition of(String typeName, String... fields) {
            return new TypeDefinition(typeName, List.of(fields));
        }

        public static TypeDefinition of(String typeName, List<String> fields) {
            return new TypeDefinition(typeName, fields);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("type ").append(typeName).append(" {\n");
            for (int i = 0; i < fields.size(); i++) {
                sb.append("  ").append(fields.get(i));
                if (i < fields.size() - 1) sb.append("\n");
            }
            sb.append("\n}");
            return sb.toString();
        }
    }

    /**
     * Defines or modifies a predicate schema.
     *
     * <p>Example: {@code Alter.PredicateSchema.predicate("name", "string").withIndex("exact")}</p>
     */
    record PredicateSchema(
        String predicate,
        String type,
        List<String> indexes,
        List<String> directives
    ) implements Alter {

        public static PredicateSchema predicate(String predicate, String type) {
            return new PredicateSchema(predicate, type, List.of(), List.of());
        }

        public PredicateSchema withIndex(String index) {
            List<String> newIndexes = new ArrayList<>(indexes);
            newIndexes.add(index);
            return new PredicateSchema(this.predicate, this.type, newIndexes, this.directives);
        }

        public PredicateSchema withIndexes(List<String> indexes) {
            return new PredicateSchema(this.predicate, this.type, indexes, this.directives);
        }

        public PredicateSchema withDirective(String directive) {
            List<String> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return new PredicateSchema(this.predicate, this.type, this.indexes, newDirectives);
        }

        public PredicateSchema withCount() {
            return withDirective("count");
        }

        public PredicateSchema withUpsert() {
            return withDirective("upsert");
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append(predicate).append(": ").append(type);
            
            if (!indexes.isEmpty()) {
                sb.append(" @index(");
                for (int i = 0; i < indexes.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(indexes.get(i));
                }
                sb.append(")");
            }
            
            for (String d : directives) {
                sb.append(" @").append(d);
            }
            
            sb.append(" .");
            return sb.toString();
        }
    }

    /**
     * Drops a type or predicate from the schema.
     *
     * <p>Example: {@code Alter.Drop.dropType("Person")} or {@code Alter.Drop.dropPredicate("owner")}</p>
     */
    record Drop(
        String target,
        boolean isAll
    ) implements Alter {

        public static Drop dropAll() {
            return new Drop(null, true);
        }

        public static Drop dropType(String typeName) {
            return new Drop("type " + typeName, false);
        }

        public static Drop dropPredicate(String predicate) {
            return new Drop(predicate, false);
        }

        @Override
        public String dql() {
            if (isAll) {
                return "drop all";
            }
            return "drop " + target;
        }
    }

    /**
     * Groups multiple schema alterations into one operation.
     *
     * <p>Example: {@code Alter.All.of(List.of(typeDef, predSchema))}</p>
     */
    record All(
        List<Alter> operations
    ) implements Alter {

        public static All of(List<Alter> operations) {
            return new All(operations);
        }

        public All with(Alter alter) {
            List<Alter> newOps = new ArrayList<>(operations);
            newOps.add(alter);
            return new All(newOps);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < operations.size(); i++) {
                if (i > 0) sb.append("\n\n");
                sb.append(operations.get(i).dql());
            }
            return sb.toString();
        }
    }

    static TypeDefinition type(String typeName, String... fields) {
        return TypeDefinition.of(typeName, fields);
    }

    static PredicateSchema predicate(String predicate, String type) {
        return PredicateSchema.predicate(predicate, type);
    }

    static Drop dropAll() {
        return Drop.dropAll();
    }

    static Alter dropType(String typeName) {
        return Drop.dropType(typeName);
    }

    static Alter dropPredicate(String predicate) {
        return Drop.dropPredicate(predicate);
    }

    static Alter all(List<Alter> operations) {
        return All.of(operations);
    }
}