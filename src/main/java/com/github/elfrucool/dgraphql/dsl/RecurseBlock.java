package com.github.elfrucool.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * A recurse block for recursive query traversal.
 *
 * <p>Used to traverse relationships recursively to a specified depth:</p>
 *
 * <pre>
 * RecurseBlock.recurse("friend").withDepth(3).withBlock(Block.predicate("name"))
 * RecurseBlock.recurse("path", "friend").withBlock(Block.predicate("uid"))
 * </pre>
 *
 * <p>Example DQL output:</p>
 * <pre>
 * friend @recurse(depth: 3) { name }
 * </pre>
 */
public record RecurseBlock(
    String name,
    String queryVariable,
    Integer depth,
    List<Directive> directives,
    List<Block> blocks
) implements DqlElement {

    /**
     * Creates a recurse block on a query variable.
     *
     * <p>Example: {@code RecurseBlock.recurse("friend")}</p>
     */
    public static RecurseBlock recurse(String queryVariable) {
        return new RecurseBlock(null, queryVariable, null, List.of(), List.of());
    }

    /**
     * Creates a named recurse block.
     *
     * <p>Example: {@code RecurseBlock.recurse("path", "friend")}</p>
     *
     * @param name Alias for the result
     * @param queryVariable The variable to recurse on
     */
    public static RecurseBlock recurse(String name, String queryVariable) {
        return new RecurseBlock(name, queryVariable, null, List.of(), List.of());
    }

    public RecurseBlock withDepth(int depth) {
        return new RecurseBlock(this.name, this.queryVariable, depth, this.directives, this.blocks);
    }

    public RecurseBlock withDirectives(List<Directive> directives) {
        return new RecurseBlock(this.name, this.queryVariable, this.depth, directives, this.blocks);
    }

    public RecurseBlock withDirective(Directive directive) {
        List<Directive> newDirectives = new ArrayList<>(directives);
        newDirectives.add(directive);
        return withDirectives(newDirectives);
    }

    public RecurseBlock withBlocks(List<Block> blocks) {
        return new RecurseBlock(this.name, this.queryVariable, this.depth, this.directives, blocks);
    }

    public RecurseBlock withBlock(Block block) {
        List<Block> newBlocks = new ArrayList<>(blocks);
        newBlocks.add(block);
        return withBlocks(newBlocks);
    }

    @Override
    public String dql() {
        StringBuilder sb = new StringBuilder();

        if (name != null && !name.isEmpty()) {
            sb.append(name);
        }

        sb.append(queryVariable);

        if (depth != null) {
            sb.append(" @recurse(depth: ").append(depth).append(")");
        } else {
            sb.append(" @recurse");
        }

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
