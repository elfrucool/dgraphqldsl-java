package org.frunix.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

public sealed interface Mutation extends DqlElement 
    permits Mutation.Set, Mutation.Delete, Mutation.Update, Mutation.Conditional {

    List<Directive> directives();

    default Mutation withDirective(Directive directive) {
        return switch (this) {
            case Set s -> s.withDirective(directive);
            case Delete d -> d;
            case Update u -> u;
            case Conditional c -> c;
        };
    }

    default Mutation withDirectives(List<Directive> directives) {
        return switch (this) {
            case Set s -> s.withDirectives(directives);
            case Delete d -> d;
            case Update u -> u;
            case Conditional c -> c;
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
}