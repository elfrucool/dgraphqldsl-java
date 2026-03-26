package org.frunix.dgraphql.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Query(
    String name,
    List<Variable> parameters,
    List<QueryBlock> blocks,
    List<VarBlock> varBlocks,
    List<Fragment> fragments,
    List<RecurseBlock> recurseBlocks
) {

    public static Query query() {
        return new Query(null, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public static Query query(String name) {
        return new Query(name, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public static Query query(String name, List<Variable> parameters) {
        return new Query(name, parameters, List.of(), List.of(), List.of(), List.of());
    }

    public Query withBlocks(List<QueryBlock> blocks) {
        return new Query(this.name, this.parameters, blocks, this.varBlocks, this.fragments, this.recurseBlocks);
    }

    public Query withBlock(QueryBlock block) {
        List<QueryBlock> newBlocks = new ArrayList<>(blocks);
        newBlocks.add(block);
        return withBlocks(newBlocks);
    }

    public Query withParameters(List<Variable> parameters) {
        return new Query(this.name, parameters, this.blocks, this.varBlocks, this.fragments, this.recurseBlocks);
    }

    public Query withParameter(Variable parameter) {
        List<Variable> newParams = new ArrayList<>(parameters);
        newParams.add(parameter);
        return withParameters(newParams);
    }

    public Query withVarBlocks(List<VarBlock> varBlocks) {
        return new Query(this.name, this.parameters, this.blocks, varBlocks, this.fragments, this.recurseBlocks);
    }

    public Query withVarBlock(VarBlock varBlock) {
        List<VarBlock> newVarBlocks = new ArrayList<>(varBlocks);
        newVarBlocks.add(varBlock);
        return withVarBlocks(newVarBlocks);
    }

    public Query withFragments(List<Fragment> fragments) {
        return new Query(this.name, this.parameters, this.blocks, this.varBlocks, fragments, this.recurseBlocks);
    }

    public Query withFragment(Fragment fragment) {
        List<Fragment> newFragments = new ArrayList<>(fragments);
        newFragments.add(fragment);
        return withFragments(newFragments);
    }

    public Query withRecurseBlocks(List<RecurseBlock> recurseBlocks) {
        return new Query(this.name, this.parameters, this.blocks, this.varBlocks, this.fragments, recurseBlocks);
    }

    public Query withRecurseBlock(RecurseBlock recurseBlock) {
        List<RecurseBlock> newRecurseBlocks = new ArrayList<>(recurseBlocks);
        newRecurseBlocks.add(recurseBlock);
        return withRecurseBlocks(newRecurseBlocks);
    }

    public DqlResult dql() {
        return dql(Map.of());
    }

    public DqlResult dql(Map<String, Object> bindings) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> variables = new HashMap<>();

        if (name != null || !parameters.isEmpty()) {
            sb.append("query ");
            if (name != null && !name.isEmpty()) {
                sb.append(name);
            }
            if (!parameters.isEmpty()) {
                sb.append("(");
                for (int i = 0; i < parameters.size(); i++) {
                    if (i > 0) sb.append(", ");
                    Variable param = parameters.get(i);
                    sb.append(param.declaration());
                    Object value = bindings.get(param.name());
                    if (value != null) {
                        variables.put(param.name(), value);
                    } else if (param.defaultValue() != null) {
                        variables.put(param.name(), param.defaultValue());
                    }
                }
                sb.append(")");
            }
            sb.append(" ");
        }

        sb.append("{ ");

        boolean first = true;
        for (Fragment fragment : fragments) {
            if (!first) sb.append(" ");
            sb.append(fragment.dql());
            first = false;
        }

        for (VarBlock varBlock : varBlocks) {
            if (!first) sb.append(" ");
            sb.append("var").append(varBlock.dql());
            first = false;
        }

        for (int i = 0; i < blocks.size(); i++) {
            if (!first) sb.append(" ");
            sb.append(blocks.get(i).dql());
            first = false;
        }

        for (RecurseBlock recurseBlock : recurseBlocks) {
            if (!first) sb.append(" ");
            sb.append(recurseBlock.dql());
            first = false;
        }

        sb.append(" }");

        return new DqlResult(sb.toString(), variables);
    }
}
