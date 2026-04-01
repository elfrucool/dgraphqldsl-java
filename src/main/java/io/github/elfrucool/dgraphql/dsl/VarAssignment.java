package io.github.elfrucool.dgraphql.dsl;

/**
 * A variable assignment within a VarBlock.
 *
 * <p>Assigns a computed value to a variable name that can be referenced
 * later in the query:</p>
 * <ul>
 *   <li>Query variables: Store UIDs for later use</li>
 *   <li>Value variables: Store computed values (counts, sums, etc.)</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * VarAssignment.queryVar("user", Func.uid("0x123"))   // user as uid(0x123)
 * VarAssignment.valueVar("cnt", Func.count("friend")) // cnt as count(friend)
 * </pre>
 *
 * @see VarBlock
 */
public record VarAssignment(String name, Func func, boolean isValueVar) implements DqlElement {

    /**
     * Creates a query variable assignment (stores UIDs).
     *
     * <p>Example: {@code VarAssignment.queryVar("user", Func.uid("0x123"))}</p>
     *
     * @see VarBlock#withAssignment(VarAssignment)
     */
    public static VarAssignment queryVar(String name, Func func) {
        return new VarAssignment(name, func, false);
    }

    /**
     * Creates a value variable assignment (stores computed values).
     *
     * <p>Example: {@code VarAssignment.valueVar("cnt", Func.count("friend"))}</p>
     *
     * @see VarBlock#withAssignment(VarAssignment)
     */
    public static VarAssignment valueVar(String name, Func func) {
        return new VarAssignment(name, func, true);
    }

    @Override
    public String dql() {
        return name + " as " + func.dql();
    }
}
