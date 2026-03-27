package org.frunix.dgraphql.dsl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public sealed interface Mutation extends DqlElement 
    permits Mutation.Set, Mutation.Delete, Mutation.Update, Mutation.Conditional, Mutation.Upsert, Mutation.UpsertRaw {

    List<Directive> directives();

    default Mutation withDirective(Directive directive) {
        return switch (this) {
            case Set s -> s.withDirective(directive);
            case Delete d -> d;
            case Update u -> u;
            case Conditional c -> c;
            case Upsert u -> u;
            case UpsertRaw u -> u;
        };
    }

    default Mutation withDirectives(List<Directive> directives) {
        return switch (this) {
            case Set s -> s.withDirectives(directives);
            case Delete d -> d;
            case Update u -> u;
            case Conditional c -> c;
            case Upsert u -> u;
            case UpsertRaw u -> u;
        };
    }

    default List<Map<String, Object>> toJsonList() {
        return switch (this) {
            case Set s -> s.toJsonList();
            case Delete d -> d.toJsonList();
            case Update u -> u.toJsonList();
            case Conditional c -> c.toJsonList();
            case Upsert u -> u.toJsonList();
            case UpsertRaw u -> u.toJsonList();
        };
    }

    record Set(
        List<SetTriple> triples,
        List<Directive> directives
    ) implements Mutation {

        public static Set of(List<SetTriple> triples) {
            return new Set(triples, List.of());
        }

        public static Set of(SetTriple... triples) {
            return new Set(List.of(triples), List.of());
        }

        public Set withTriples(List<SetTriple> triples) {
            return new Set(triples, this.directives);
        }

        public Set withTriple(SetTriple triple) {
            List<SetTriple> newTriples = new ArrayList<>(triples);
            newTriples.add(triple);
            return withTriples(newTriples);
        }

        public Set withDirectives(List<Directive> directives) {
            return new Set(this.triples, directives);
        }

        public Set withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        @Override
        public List<Directive> directives() {
            return directives;
        }

        @Override
        public List<Map<String, Object>> toJsonList() {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> currentObj = null;
            String currentSubject = null;
            
            for (SetTriple triple : triples) {
                if (!triple.subject().equals(currentSubject)) {
                    if (currentObj != null) {
                        result.add(currentObj);
                    }
                    currentObj = new LinkedHashMap<>();
                    currentSubject = triple.subject();
                    
                    String subjectKey = triple.subject();
                    if (subjectKey.startsWith("_:")) {
                        currentObj.put("uid", subjectKey);
                    } else {
                        currentObj.put("uid", subjectKey);
                    }
                }
                
                if (currentObj != null && triple.predicate() != null) {
                    currentObj.put(triple.predicate(), triple.value());
                }
            }
            
            if (currentObj != null) {
                result.add(currentObj);
            }
            
            return result;
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ set { ");
            for (int i = 0; i < triples.size(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(triples.get(i).dql());
            }
            sb.append(" }");
            
            if (!directives.isEmpty()) {
                for (Directive d : directives) {
                    sb.append(" ").append(d.dql());
                }
            }
            
            sb.append(" }");
            return sb.toString();
        }
    }

    record Delete(
        List<SetTriple> triples
    ) implements Mutation {

        public static Delete of(List<SetTriple> triples) {
            return new Delete(triples);
        }

        public static Delete of(SetTriple... triples) {
            return new Delete(List.of(triples));
        }

        @Override
        public List<Map<String, Object>> toJsonList() {
            List<Map<String, Object>> result = new ArrayList<>();
            for (SetTriple triple : triples) {
                Map<String, Object> obj = new LinkedHashMap<>();
                if (triple.subject().startsWith("_:") || triple.subject().startsWith("0x")) {
                    obj.put("uid", triple.subject());
                } else {
                    obj.put("uid", triple.subject());
                }
                if (triple.predicate() != null && triple.value() != null) {
                    if (triple.value().equals("*")) {
                        obj.put(triple.predicate(), "*");
                    } else {
                        obj.put(triple.predicate(), triple.value());
                    }
                }
                result.add(obj);
            }
            return result;
        }

        @Override
        public List<Directive> directives() {
            return List.of();
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ delete { ");
            for (int i = 0; i < triples.size(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(triples.get(i).dql());
            }
            sb.append(" } }");
            return sb.toString();
        }
    }

    record Update(
        Set set,
        Delete delete
    ) implements Mutation {

        public static Update of(Set set, Delete delete) {
            return new Update(set, delete);
        }

        @Override
        public List<Directive> directives() {
            List<Directive> result = new ArrayList<>();
            if (set != null) result.addAll(set.directives());
            return result;
        }

        @Override
        public List<Map<String, Object>> toJsonList() {
            List<Map<String, Object>> result = new ArrayList<>();
            if (set != null) {
                result.addAll(set.toJsonList());
            }
            return result;
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ update { ");
            if (set != null) {
                sb.append("set { ");
                for (SetTriple triple : set.triples()) {
                    sb.append(triple.dql()).append(" ");
                }
                sb.append("} ");
            }
            if (delete != null) {
                sb.append("delete { ");
                for (SetTriple triple : delete.triples()) {
                    sb.append(triple.dql()).append(" ");
                }
                sb.append("}");
            }
            sb.append(" } }");
            return sb.toString();
        }
    }

    record Conditional(
        String condition,
        Set set,
        Delete delete,
        List<Directive> directives
    ) implements Mutation {

        public static Conditional ifCondition(String condition, Set set, Delete delete) {
            return new Conditional(condition, set, delete, List.of());
        }

        public Conditional withDirectives(List<Directive> directives) {
            return new Conditional(this.condition, this.set, this.delete, directives);
        }

        public Conditional withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        @Override
        public List<Directive> directives() {
            return directives;
        }

        @Override
        public List<Map<String, Object>> toJsonList() {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> upsertObj = new LinkedHashMap<>();
            upsertObj.put("@if", condition);
            
            if (set != null) {
                List<Map<String, Object>> setJson = set.toJsonList();
                if (!setJson.isEmpty()) {
                    upsertObj.put("set", setJson);
                }
            }
            if (delete != null) {
                List<Map<String, Object>> deleteJson = delete.toJsonList();
                if (!deleteJson.isEmpty()) {
                    upsertObj.put("delete", deleteJson);
                }
            }
            
            result.add(upsertObj);
            return result;
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("@if(").append(condition).append(") { ");
            if (set != null) {
                sb.append("set { ");
                for (int i = 0; i < set.triples().size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(set.triples().get(i).dql());
                }
                sb.append(" }");
            }
            if (delete != null) {
                if (set != null) sb.append(" ");
                sb.append("delete { ");
                for (int i = 0; i < delete.triples().size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(delete.triples().get(i).dql());
                }
                sb.append(" }");
            }
            sb.append(" }");
            return sb.toString();
        }
    }

    record Upsert(
        Query query,
        Set set,
        Delete delete
    ) implements Mutation {

        @Override
        public List<Directive> directives() {
            return List.of();
        }

        @Override
        public String dql() {
            return "upsert { " + query.dql().query() + " " + renderMutations() + " }";
        }

        private String renderMutations() {
            StringBuilder sb = new StringBuilder();
            String cond = "@if(uid(" + getQueryVar() + "))";
            sb.append("mutation ").append(cond).append(" { ");
            if (set != null) {
                sb.append("set { ");
                for (int i = 0; i < set.triples().size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(set.triples().get(i).dql());
                }
                sb.append(" }");
            }
            if (delete != null) {
                if (set != null) sb.append(" ");
                sb.append("delete { ");
                for (int i = 0; i < delete.triples().size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(delete.triples().get(i).dql());
                }
                sb.append(" }");
            }
            sb.append(" }");
            return sb.toString();
        }

        private String getQueryVar() {
            String q = query.dql().query();
            int start = q.indexOf(" as ") + 4;
            int end = q.indexOf(" ", start);
            if (end == -1) end = q.indexOf(")", start);
            return q.substring(start, end);
        }

        @Override
        public List<Map<String, Object>> toJsonList() {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> upsertObj = new LinkedHashMap<>();
            
            String q = query.dql().query();
            int start = q.indexOf(" as ") + 4;
            int end = q.indexOf(" ", start);
            if (end == -1) end = q.indexOf(")", start);
            String varName = q.substring(start, end);
            
            upsertObj.put("@if", "uid(" + varName + ")");
            
            if (set != null) {
                List<Map<String, Object>> setJson = new ArrayList<>();
                for (SetTriple t : set.triples()) {
                    Map<String, Object> triple = new LinkedHashMap<>();
                    triple.put("uid", t.subject());
                    triple.put(t.predicate(), t.value());
                    setJson.add(triple);
                }
                upsertObj.put("set", setJson);
            }
            if (delete != null) {
                List<Map<String, Object>> delJson = new ArrayList<>();
                for (SetTriple t : delete.triples()) {
                    Map<String, Object> triple = new LinkedHashMap<>();
                    triple.put("uid", t.subject());
                    triple.put(t.predicate(), null);
                    delJson.add(triple);
                }
                upsertObj.put("delete", delJson);
            }
            
            result.add(upsertObj);
            return result;
        }
    }

    record UpsertRaw(
        String query,
        Set set,
        Delete delete
    ) implements Mutation {

        @Override
        public List<Directive> directives() {
            return List.of();
        }

        @Override
        public String dql() {
            return "upsert { " + query + " " + renderMutations() + " }";
        }

        private String renderMutations() {
            StringBuilder sb = new StringBuilder();
            String varName = extractVarName();
            String cond = "@if(uid(" + varName + "))";
            sb.append("mutation ").append(cond).append(" { ");
            if (set != null) {
                sb.append("set { ");
                for (int i = 0; i < set.triples().size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(set.triples().get(i).dql());
                }
                sb.append(" }");
            }
            if (delete != null) {
                if (set != null) sb.append(" ");
                sb.append("delete { ");
                for (int i = 0; i < delete.triples().size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(delete.triples().get(i).dql());
                }
                sb.append(" }");
            }
            sb.append(" }");
            return sb.toString();
        }

        private String extractVarName() {
            int asPos = query.indexOf(" as ");
            int start = asPos;
            int end = asPos;
            for (int i = asPos - 1; i >= 0; i--) {
                char c = query.charAt(i);
                if (c == ' ' || c == '{' || c == '\n') {
                    start = i + 1;
                    break;
                }
            }
            return query.substring(start, end);
        }

        @Override
        public List<Map<String, Object>> toJsonList() {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> upsertObj = new LinkedHashMap<>();
            
            String varName = extractVarName();
            upsertObj.put("@if", "uid(" + varName + ")");
            
            if (set != null) {
                List<Map<String, Object>> setJson = new ArrayList<>();
                for (SetTriple t : set.triples()) {
                    Map<String, Object> triple = new LinkedHashMap<>();
                    triple.put("uid", t.subject());
                    triple.put(t.predicate(), t.value());
                    setJson.add(triple);
                }
                upsertObj.put("set", setJson);
            }
            if (delete != null) {
                List<Map<String, Object>> delJson = new ArrayList<>();
                for (SetTriple t : delete.triples()) {
                    Map<String, Object> triple = new LinkedHashMap<>();
                    triple.put("uid", t.subject());
                    triple.put(t.predicate(), null);
                    delJson.add(triple);
                }
                upsertObj.put("delete", delJson);
            }
            
            result.add(upsertObj);
            return result;
        }
    }

    static Mutation set(List<SetTriple> triples) {
        return Set.of(triples);
    }

    static Mutation set(SetTriple... triples) {
        return Set.of(triples);
    }

    static Mutation delete(List<SetTriple> triples) {
        return Delete.of(triples);
    }

    static Mutation delete(SetTriple... triples) {
        return Delete.of(triples);
    }

    static Mutation update(Set set, Delete delete) {
        return Update.of(set, delete);
    }

    static Mutation ifCondition(String condition, Set set, Delete delete) {
        return Conditional.ifCondition(condition, set, delete);
    }

    static Mutation upsert(Query query, Set set) {
        return new Upsert(query, set, null);
    }

    static Mutation upsert(Query query, Set set, Delete delete) {
        return new Upsert(query, set, delete);
    }

    static Mutation upsertRaw(String query, Set set) {
        return new UpsertRaw(query, set, null);
    }

    static Mutation upsertRaw(String query, Set set, Delete delete) {
        return new UpsertRaw(query, set, delete);
    }
}