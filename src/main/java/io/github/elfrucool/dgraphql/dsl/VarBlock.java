package io.github.elfrucool.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * A variable block for defining query variables.
 *
 * <p>VarBlock creates a query variable by executing a function and storing
 * the results. It can be used to:</p>
 * <ul>
 *   <li>Define variables from query results</li>
 *   <li>Pre-filter data for reuse</li>
 *   <li>Create complex variable expressions</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * VarBlock.var(Func.eq("name", "Alice"))
 * VarBlock.var(Func.uid("0x123")).withBlock(Block.predicate("name"))
 * </pre>
 *
 * @see VarAssignment
 * @see Query#withVarBlocks(List)
 */
public record VarBlock(
    Func func,
    List<VarAssignment> assignments,
    List<Directive> directives,
    List<Block> blocks
) implements DqlElement {

    /**
     * Creates a variable block from a function.
     *
     * <p>Example: {@code VarBlock.var(Func.eq("name", "Alice"))}</p>
     *
     * @see Func
     */
    public static VarBlock var(Func func) {
        return new VarBlock(func, List.of(), List.of(), List.of());
    }

    public VarBlock withAssignments(List<VarAssignment> assignments) {
        return new VarBlock(this.func, assignments, this.directives, this.blocks);
    }

    public VarBlock withAssignment(VarAssignment assignment) {
        List<VarAssignment> newAssignments = new ArrayList<>(assignments);
        newAssignments.add(assignment);
        return withAssignments(newAssignments);
    }

    public VarBlock withDirectives(List<Directive> directives) {
        return new VarBlock(this.func, this.assignments, directives, this.blocks);
    }

    public VarBlock withDirective(Directive directive) {
        List<Directive> newDirectives = new ArrayList<>(directives);
        newDirectives.add(directive);
        return withDirectives(newDirectives);
    }

    public VarBlock withBlocks(List<Block> blocks) {
        return new VarBlock(this.func, this.assignments, this.directives, blocks);
    }

    public VarBlock withBlock(Block block) {
        List<Block> newBlocks = new ArrayList<>(blocks);
        newBlocks.add(block);
        return withBlocks(newBlocks);
    }

    @Override
    public String dql() {
        StringBuilder sb = new StringBuilder();
        sb.append("var(func: ").append(func.dql()).append(")");

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
        } else if (!assignments.isEmpty()) {
            sb.append(" { ");
            for (int i = 0; i < assignments.size(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(assignments.get(i).dql());
            }
            sb.append(" }");
        }

        return sb.toString();
    }
}
