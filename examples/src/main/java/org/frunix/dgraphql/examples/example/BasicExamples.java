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

@Component
public class BasicExamples {

    private static final Logger log = LoggerFactory.getLogger(BasicExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public BasicExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Basic Examples (Phase 1-2) ===");
        setupData();
        try {
            basicQuery();
            nestedBlocks();
            aliasExample();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <age> "30" .
            _:alice <friend> _:bob .
            _:bob <name> "Bob" .
            _:bob <age> "25" .
            _:bob <friend> _:alice .
            _:charlie <name> "Charlie" .
            _:charlie <age> "28" .
            _:diana <name> "Diana" .
            _:diana <age> "35" .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("BasicExamples: Test data inserted (Alice, Bob, Charlie, Diana with friendships)");
        } catch (Exception e) {
            log.warn("BasicExamples: Data setup error: {}", e.getMessage());
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
            log.info("BasicExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("BasicExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void basicQuery() {
        log.info("--- Basic Query ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate("age")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Basic Query");
    }

    private void nestedBlocks() {
        log.info("--- Nested Blocks ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Nested Blocks");
    }

    private void aliasExample() {
        log.info("--- Alias Example ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name", "userName"),
                        Block.predicate("age", "userAge")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Alias Example");
    }

    private void executeQuery(String query, String testName) {
        try {
            DgraphProto.Response response = dgraphClient.newReadOnlyTransaction().query(query);
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("Basic Examples (Phase 1-2)", testName, query, json, success);
        } catch (Exception e) {
            results.record("Basic Examples (Phase 1-2)", testName, query, "Error: " + e.getMessage(), false);
        }
    }
}
