package org.frunix.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

public sealed interface ShortestPath extends DqlElement
    permits ShortestPath.KShortest {

    static KShortest shortest(String varName, String from, String to) {
        return new KShortest(varName, from, to, null, null, null, null, null, List.of());
    }

    static KShortest kShortest(String varName, String from, String to, int numpaths) {
        return new KShortest(varName, from, to, numpaths, null, null, null, null, List.of());
    }

    record KShortest(
        String varName,
        String from,
        String to,
        Integer numpaths,
        Integer depth,
        Float minweight,
        Float maxweight,
        Block predicate,
        List<Directive> directives
    ) implements ShortestPath {

        public KShortest withNumpaths(int numpaths) {
            return new KShortest(this.varName, this.from, this.to, numpaths, this.depth, 
                this.minweight, this.maxweight, this.predicate, this.directives);
        }

        public KShortest withDepth(int depth) {
            return new KShortest(this.varName, this.from, this.to, this.numpaths, depth, 
                this.minweight, this.maxweight, this.predicate, this.directives);
        }

        public KShortest withWeightRange(float minweight, float maxweight) {
            return new KShortest(this.varName, this.from, this.to, this.numpaths, this.depth, 
                minweight, maxweight, this.predicate, this.directives);
        }

        public KShortest withPredicate(Block predicate) {
            return new KShortest(this.varName, this.from, this.to, this.numpaths, this.depth, 
                this.minweight, this.maxweight, predicate, this.directives);
        }

        public KShortest withDirectives(List<Directive> directives) {
            return new KShortest(this.varName, this.from, this.to, this.numpaths, this.depth, 
                this.minweight, this.maxweight, this.predicate, directives);
        }

        public KShortest withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            
            if (varName != null && !varName.isEmpty()) {
                sb.append(varName).append(" as ");
            }
            
            sb.append("shortest(");
            
            boolean first = true;
            if (from != null) {
                sb.append("from: ").append(from);
                first = false;
            }
            if (to != null) {
                if (!first) sb.append(", ");
                sb.append("to: ").append(to);
                first = false;
            }
            if (numpaths != null) {
                if (!first) sb.append(", ");
                sb.append("numpaths: ").append(numpaths);
                first = false;
            }
            if (depth != null) {
                if (!first) sb.append(", ");
                sb.append("depth: ").append(depth);
                first = false;
            }
            if (minweight != null) {
                if (!first) sb.append(", ");
                sb.append("minweight: ").append(minweight);
                first = false;
            }
            if (maxweight != null) {
                if (!first) sb.append(", ");
                sb.append("maxweight: ").append(maxweight);
            }
            
            sb.append(") { ");
            
            if (predicate != null) {
                sb.append(predicate.dql());
            }
            
            for (Directive d : directives) {
                sb.append(" ").append(d.dql());
            }
            
            sb.append(" }");
            
            return sb.toString();
        }
    }
}
