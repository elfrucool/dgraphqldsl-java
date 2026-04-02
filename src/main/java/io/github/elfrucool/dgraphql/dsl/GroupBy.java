package io.github.elfrucool.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * A groupBy aggregation for grouping query results.
 *
 * <p>GroupBy is a sealed interface with two variants:</p>
 * <ul>
 *   <li>{@link Aggregation} - Simple groupBy on a predicate</li>
 *   <li>{@link Nested} - Nested groupBy with multiple grouping levels</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * GroupBy.groupBy("genre").withBlock(Block.predicate("count"))
 * GroupBy.nested(List.of("genre", "year")).withBlock(Block.predicate("count"))
 * </pre>
 *
 * @see Block#groupBy(String)
 */
public sealed interface GroupBy extends DqlElement permits GroupBy.Aggregation, GroupBy.Nested {

    String predicate();

    List<Block> blocks();

    List<Directive> directives();

    default GroupBy withBlock(Block block) {
        return switch (this) {
            case Aggregation a -> a.withBlock(block);
            case Nested n -> n.withBlock(block);
        };
    }

    default GroupBy withBlocks(List<Block> blocks) {
        return switch (this) {
            case Aggregation a -> a.withBlocks(blocks);
            case Nested n -> n.withBlocks(blocks);
        };
    }

    default GroupBy withDirective(Directive directive) {
        return switch (this) {
            case Aggregation a -> a.withDirective(directive);
            case Nested n -> n.withDirective(directive);
        };
    }

    default GroupBy withDirectives(List<Directive> directives) {
        return switch (this) {
            case Aggregation a -> a.withDirectives(directives);
            case Nested n -> n.withDirectives(directives);
        };
    }

    /**
     * Simple groupBy on a single predicate.
     *
     * <p>Example: {@code GroupBy.groupBy("genre")}</p>
     */
    record Aggregation(String predicate, List<Block> blocks, List<Directive> directives) implements GroupBy {

        public static Aggregation of(String predicate) {
            return new Aggregation(predicate, List.of(), List.of());
        }

        public Aggregation withBlocks(List<Block> blocks) {
            return new Aggregation(this.predicate, blocks, this.directives);
        }

        public Aggregation withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        public Aggregation withDirectives(List<Directive> directives) {
            return new Aggregation(this.predicate, this.blocks, directives);
        }

        public Aggregation withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append(predicate);

            if (!directives.isEmpty()) {
                for (Directive d : directives) {
                    sb.append(" ").append(d.dql());
                }
            }

            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }

            return sb.toString();
        }
    }

    /**
     * Nested groupBy with multiple grouping levels.
     *
     * <p>Example: {@code GroupBy.nested("genre", "year")}</p>
     */
    record Nested(String predicate, String groupByPredicate, List<Block> blocks, List<Directive> directives)
            implements GroupBy {

        public static Nested of(String predicate, String groupByPredicate) {
            return new Nested(predicate, groupByPredicate, List.of(), List.of());
        }

        public Nested withBlocks(List<Block> blocks) {
            return new Nested(this.predicate, this.groupByPredicate, blocks, this.directives);
        }

        public Nested withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        public Nested withDirectives(List<Directive> directives) {
            return new Nested(this.predicate, this.groupByPredicate, this.blocks, directives);
        }

        public Nested withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append(predicate).append(" @groupby(").append(groupByPredicate).append(")");

            if (!directives.isEmpty()) {
                for (Directive d : directives) {
                    sb.append(" ").append(d.dql());
                }
            }

            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }

            return sb.toString();
        }
    }

    static GroupBy groupBy(String predicate) {
        return Aggregation.of(predicate);
    }

    static GroupBy groupBy(String predicate, String groupByPredicate) {
        return Nested.of(predicate, groupByPredicate);
    }
}
