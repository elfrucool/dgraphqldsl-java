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
public class AdvancedExamples {

    private static final Logger log = LoggerFactory.getLogger(AdvancedExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public AdvancedExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Advanced Examples (Phase 8) ===");
        setupData();
        try {
            cascadeDirective();
            normalizeDirective();
            recurseQuery();
            fragmentExample();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <age> "30" .
            _:alice <friend> _:bob .
            _:alice <friend> _:charlie .
            _:bob <name> "Bob" .
            _:bob <age> "25" .
            _:bob <friend> _:alice .
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
            log.info("AdvancedExamples: Test data inserted (Alice, Bob, Charlie, Diana with friendships)");
        } catch (Exception e) {
            log.warn("AdvancedExamples: Data setup error: {}", e.getMessage());
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
            log.info("AdvancedExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("AdvancedExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void cascadeDirective() {
        log.info("--- Cascade Directive ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.cascade())
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Cascade Directive");
    }

    private void normalizeDirective() {
        log.info("--- Normalize Directive ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withDirective(Directive.normalize())
                    .withBlocks(List.of(
                        Block.predicate("name", "personName"),
                        Block.nested("friend")
                            .withBlocks(List.of(Block.predicate("name", "friendName")))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Normalize Directive");
    }

    private void recurseQuery() {
        log.info("--- Recurse Query ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.recurse(3))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate("friend")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Recurse Query");
    }

    private void fragmentExample() {
        log.info("--- Fragment Example ---");
        
        Query query = Query.query()
            .withFragment(
                Fragment.fragment("PersonDetails")
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate("age")
                    ))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("... PersonDetails")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Fragment Example");
    }

    private void executeQuery(String query, String testName) {
        try {
            DgraphProto.Response response = dgraphClient.newReadOnlyTransaction().query(query);
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("07 Advanced Examples (Phase 8)", testName, query, json, success);
        } catch (Exception e) {
            results.record("07 Advanced Examples (Phase 8)", testName, query, "Error: " + e.getMessage(), false);
        }
    }
}
