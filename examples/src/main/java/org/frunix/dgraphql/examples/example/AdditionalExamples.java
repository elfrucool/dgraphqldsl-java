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

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AdditionalExamples {

    private static final Logger log = LoggerFactory.getLogger(AdditionalExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdditionalExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Additional Examples (Phase 10) ===");
        setupData();
        try {
            groupByAggregation();
            alterSchema();
            jsonMutation();
            multipleQueryBlocks();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <age> "30" .
            _:alice <friend> _:bob .
            _:alice <email> "alice@example.com" .
            _:bob <name> "Bob" .
            _:bob <age> "25" .
            _:bob <friend> _:alice .
            _:bob <email> "bob@example.com" .
            _:charlie <name> "Charlie" .
            _:charlie <age> "28" .
            _:charlie <friend> _:alice .
            _:diana <name> "Diana" .
            _:diana <age> "35" .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("AdditionalExamples: Test data inserted (Alice, Bob, Charlie, Diana with friend, age, email)");
        } catch (Exception e) {
            log.warn("AdditionalExamples: Data setup error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x1> * * .
            <0x2> * * .
            <0x3> * * .
            <0x4> * * .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("AdditionalExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("AdditionalExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void groupByAggregation() {
        log.info("--- GroupBy Aggregation ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withBlocks(List.of(
                        Block.predicate("age"),
                        Block.nested("friend")
                            .withDirective(Directive.groupby("age"))
                            .withBlocks(List.of(Block.predicate("count(uid)")))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "GroupBy Aggregation");
    }

    private void alterSchema() {
        log.info("--- ALTER Schema ---");
        
        Alter alter = Alter.predicate("email", "string")
            .withIndex("hash");

        String dql = alter.dql();
        log.info("Schema: {}", dql);
        executeAlter(dql, "ALTER Schema");
    }

    private void jsonMutation() {
        log.info("--- JSON Mutation ---");
        
        JsonMutation mutation = JsonMutation.Set.of(
            Map.of("name", "TestJsonPerson", "age", 99)
        );

        log.info("Mutation (JSON): {}", mutation.getJson());
        executeJsonMutation(mutation, "JSON Mutation");
    }

    private void multipleQueryBlocks() {
        log.info("--- Multiple Query Blocks ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("user1", Func.eq("email", "alice@example.com"))
                    .withBlocks(List.of(Block.predicate("name"))),
                QueryBlock.block("user2", Func.eq("email", "bob@example.com"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Multiple Query Blocks");
    }

    private void executeQuery(String query, String testName) {
        try {
            DgraphProto.Response response = dgraphClient.newReadOnlyTransaction().query(query);
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("Additional Examples (Phase 10)", testName, query, json, success);
        } catch (Exception e) {
            results.record("Additional Examples (Phase 10)", testName, query, "Error: " + e.getMessage(), false);
        }
    }

    private void executeAlter(String schema, String testName) {
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
            results.record("Additional Examples (Phase 10)", testName, schema, "Schema update successful", true);
            log.info("Schema update successful");
        } catch (Exception e) {
            results.record("Additional Examples (Phase 10)", testName, schema, "Error: " + e.getMessage(), false);
            log.warn("Schema update error: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    private void executeJsonMutation(JsonMutation mutation, String testName) {
        try (Transaction txn = dgraphClient.newTransaction()) {
            Map<String, Object> jsonBody = Map.of("set", mutation.getJson());
            String jsonStr = objectMapper.writeValueAsString(jsonBody);
            
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetJson(com.google.protobuf.ByteString.copyFromUtf8(jsonStr))
                .build();
            txn.mutate(mu);
            txn.commit();
            results.record("Additional Examples (Phase 10)", testName, mutation.dql(), "Mutation successful", true);
            log.info("Mutation successful");
        } catch (Exception e) {
            results.record("Additional Examples (Phase 10)", testName, mutation.dql(), "Error: " + e.getMessage(), false);
            log.warn("Mutation error: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }
}
