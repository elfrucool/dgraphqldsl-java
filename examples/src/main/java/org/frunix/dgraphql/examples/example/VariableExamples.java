package org.frunix.dgraphql.examples.example;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import io.dgraph.Transaction;
import org.frunix.dgraphql.dsl.*;
import org.frunix.dgraphql.examples.result.ResultsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class VariableExamples {

    private static final Logger log = LoggerFactory.getLogger(VariableExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public VariableExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Variable Examples (Phase 4) ===");
        setupData();
        try {
            queryVariable();
            valueVariable();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <age> "30" .
            _:bob <name> "Bob" .
            _:bob <age> "25" .
            _:alice <friend> _:bob .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("VariableExamples: Test data inserted");
        } catch (Exception e) {
            log.warn("VariableExamples: Data setup error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x1> * * .
            <0x2> * * .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("VariableExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("VariableExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void queryVariable() {
        log.info("--- Query Variable ---");
        
        setupData();
        
        Query query = Query.query("getPerson")
            .withParameters(List.of(Variable.queryVar("name", "string")))
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", Variable.param("name")))
                    .withBlocks(List.of(Block.predicate("name"), Block.predicate("age")))
            ));

        DqlResult result = query.dql(Map.of("name", "Alice"));
        log.info("Query: {}", result.query());
        log.info("Variables: {}", result.variables());
        
        executeQuery(result.query(), result.variables(), "Query Variable");
    }

    private void valueVariable() {
        log.info("--- Value Variable ---");
        
        Query query = Query.query()
            .withVarBlock(
                VarBlock.var(Func.has("friend"))
                    .withBlock(Block.var("friendCount", "count(friend)"))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate(Func.val("friendCount"))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Value Variable");
    }

    private void executeQuery(String query, String testName) {
        executeQuery(query, null, testName);
    }

    private void executeQuery(String query, Map<String, Object> variables, String testName) {
        try {
            DgraphProto.Response response;
            if (variables != null && !variables.isEmpty()) {
                Map<String, String> stringVars = new java.util.HashMap<>();
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    stringVars.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                response = dgraphClient.newReadOnlyTransaction().queryWithVars(query, stringVars);
            } else {
                response = dgraphClient.newReadOnlyTransaction().query(query);
            }
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("03 Variable Examples (Phase 4)", testName, query, json, success);
        } catch (Exception e) {
            results.record("03 Variable Examples (Phase 4)", testName, query, "Error: " + e.getMessage(), false);
        }
    }
}
