package org.frunix.dgraphql.dsl;

public record VarAssignment(String name, Func func, boolean isValueVar) implements DqlElement {

    public static VarAssignment queryVar(String name, Func func) {
        return new VarAssignment(name, func, false);
    }

    public static VarAssignment valueVar(String name, Func func) {
        return new VarAssignment(name, func, true);
    }

    @Override
    public String dql() {
        return name + " as " + func.dql();
    }
}
