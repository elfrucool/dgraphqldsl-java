package io.github.elfrucool.dgraphql.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The root container for building DQL queries.
 *
 * <p>Use the static factory methods to create queries:</p>
 *
 * <pre>
 * Query.query()                                    // Anonymous query
 * Query.query("getUser")                          // Named query
 * Query.query("getUser", List.of(param))          // With parameters
 * </pre>
 *
 * <p>Then add query blocks, variables, fragments, etc.:</p>
 *
 * <pre>
 * query.withBlocks(List.of(...))
 *      .withVarBlock(VarBlock.var(...))
 *      .withFragment(Fragment.fragment(...))
 *      .dql();
 * </pre>
 *
 * @see QueryBlock
 * @see VarBlock
 * @see Fragment
 * @see RecurseBlock
 * @see ShortestPath
 */
public record Query(
    String name,
    List<Variable> parameters,
    List<QueryBlock> blocks,
    List<VarBlock> varBlocks,
    List<Fragment> fragments,
    List<RecurseBlock> recurseBlocks,
    List<ShortestPath> shortestPaths
) {

    /**
     * Creates an anonymous query with no name or parameters.
     *
     * @return a new Query with no blocks
     */
    public static Query query() {
        return new Query(null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Creates a named query.
     *
     * @param name the query name
     * @return a new Query with the given name
     */
    public static Query query(String name) {
        return new Query(name, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Creates a named query with query variables as parameters.
     *
     * @param name       the query name
     * @param parameters list of query variables
     * @return a new Query with name and parameters
     */
    public static Query query(String name, List<Variable> parameters) {
        return new Query(name, parameters, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Adds query blocks to this query.
     *
     * @param blocks list of QueryBlock to add
     * @return a new Query with the given blocks
     */
    public Query withBlocks(List<QueryBlock> blocks) {
        return new Query(this.name, this.parameters, blocks, this.varBlocks, this.fragments, this.recurseBlocks, this.shortestPaths);
    }

    /**
     * Adds a single query block to this query.
     *
     * @param block the QueryBlock to add
     * @return a new Query with the added block
     */
    public Query withBlock(QueryBlock block) {
        List<QueryBlock> newBlocks = new ArrayList<>(blocks);
        newBlocks.add(block);
        return withBlocks(newBlocks);
    }

    /**
     * Sets the query parameters (variables) for this query.
     *
     * @param parameters list of query variables
     * @return a new Query with the given parameters
     */
    public Query withParameters(List<Variable> parameters) {
        return new Query(this.name, parameters, this.blocks, this.varBlocks, this.fragments, this.recurseBlocks, this.shortestPaths);
    }

    /**
     * Adds a single query parameter to this query.
     *
     * @param parameter the Variable to add
     * @return a new Query with the added parameter
     */
    public Query withParameter(Variable parameter) {
        List<Variable> newParams = new ArrayList<>(parameters);
        newParams.add(parameter);
        return withParameters(newParams);
    }

    /**
     * Adds variable blocks to this query.
     *
     * @param varBlocks list of VarBlock to add
     * @return a new Query with the given variable blocks
     */
    public Query withVarBlocks(List<VarBlock> varBlocks) {
        return new Query(this.name, this.parameters, this.blocks, varBlocks, this.fragments, this.recurseBlocks, this.shortestPaths);
    }

    /**
     * Adds a single variable block to this query.
     *
     * @param varBlock the VarBlock to add
     * @return a new Query with the added variable block
     */
    public Query withVarBlock(VarBlock varBlock) {
        List<VarBlock> newVarBlocks = new ArrayList<>(varBlocks);
        newVarBlocks.add(varBlock);
        return withVarBlocks(newVarBlocks);
    }

    /**
     * Adds fragments to this query.
     *
     * @param fragments list of Fragment to add
     * @return a new Query with the given fragments
     */
    public Query withFragments(List<Fragment> fragments) {
        return new Query(this.name, this.parameters, this.blocks, this.varBlocks, fragments, this.recurseBlocks, this.shortestPaths);
    }

    /**
     * Adds a single fragment to this query.
     *
     * @param fragment the Fragment to add
     * @return a new Query with the added fragment
     */
    public Query withFragment(Fragment fragment) {
        List<Fragment> newFragments = new ArrayList<>(fragments);
        newFragments.add(fragment);
        return withFragments(newFragments);
    }

    /**
     * Adds recursive query blocks to this query.
     *
     * @param recurseBlocks list of RecurseBlock to add
     * @return a new Query with the given recurse blocks
     */
    public Query withRecurseBlocks(List<RecurseBlock> recurseBlocks) {
        return new Query(this.name, this.parameters, this.blocks, this.varBlocks, this.fragments, recurseBlocks, this.shortestPaths);
    }

    /**
     * Adds a single recursive query block to this query.
     *
     * @param recurseBlock the RecurseBlock to add
     * @return a new Query with the added recurse block
     */
    public Query withRecurseBlock(RecurseBlock recurseBlock) {
        List<RecurseBlock> newRecurseBlocks = new ArrayList<>(recurseBlocks);
        newRecurseBlocks.add(recurseBlock);
        return withRecurseBlocks(newRecurseBlocks);
    }

    /**
     * Adds shortest path queries to this query.
     *
     * @param shortestPaths list of ShortestPath to add
     * @return a new Query with the given shortest paths
     */
    public Query withShortestPaths(List<ShortestPath> shortestPaths) {
        return new Query(this.name, this.parameters, this.blocks, this.varBlocks, this.fragments, this.recurseBlocks, shortestPaths);
    }

    /**
     * Adds a single shortest path query to this query.
     *
     * @param shortestPath the ShortestPath to add
     * @return a new Query with the added shortest path
     */
    public Query withShortestPath(ShortestPath shortestPath) {
        List<ShortestPath> newShortestPaths = new ArrayList<>(shortestPaths);
        newShortestPaths.add(shortestPath);
        return withShortestPaths(newShortestPaths);
    }

    /**
     * Generates the DQL query string with no variable bindings.
     *
     * @return DqlResult containing the DQL query string and empty variables map
     */
    public DqlResult dql() {
        return dql(Map.of());
    }

    /**
     * Generates the DQL query string with variable bindings.
     *
     * @param bindings map of variable names to their values
     * @return DqlResult containing the DQL query string and filled variables
     */
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
                    String key = param.name();
                    Object value = bindings.get(key);
                    if (value == null) {
                        value = bindings.get("$" + key);
                    }
                    if (value != null) {
                        variables.put("$" + key, value);
                    } else if (param.defaultValue() != null) {
                        variables.put("$" + key, param.defaultValue());
                    }
                }
                sb.append(")");
            }
            sb.append(" ");
        }

        sb.append("{ ");

        boolean first = true;

        for (VarBlock varBlock : varBlocks) {
            if (!first) sb.append(" ");
            sb.append(varBlock.dql());
            first = false;
        }

        for (ShortestPath sp : shortestPaths) {
            if (!first) sb.append(" ");
            sb.append(sp.dql());
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

        for (Fragment fragment : fragments) {
            sb.append(" ");
            sb.append(fragment.dql());
        }

        return new DqlResult(sb.toString(), variables);
    }
}
