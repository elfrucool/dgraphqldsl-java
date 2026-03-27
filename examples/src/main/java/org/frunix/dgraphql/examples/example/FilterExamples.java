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
public class FilterExamples {

    private static final Logger log = LoggerFactory.getLogger(FilterExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public FilterExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Filter Examples (Phase 3) ===");
        setupData();
        try {
            andFilter();
            orFilter();
            notFilter();
            hasFilter();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <age> "30" .
            _:alice <status> "active" .
            _:alice <friend> _:bob .
            _:bob <name> "Bob" .
            _:bob <age> "25" .
            _:bob <status> "active" .
            _:bob <friend> _:alice .
            _:charlie <name> "Charlie" .
            _:charlie <age> "28" .
            _:charlie <status> "pending" .
            _:charlie <friend> _:alice .
            _:diana <name> "Diana" .
            _:diana <age> "35" .
            _:diana <status> "active" .
            _:diana <email> "diana@example.com" .
            _:eve <name> "Eve" .
            _:eve <age> "22" .
            _:eve <status> "pending" .
            _:eve <email> "eve@example.com" .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("FilterExamples: Test data inserted (5 persons with age, status, friend, email)");
        } catch (Exception e) {
            log.warn("FilterExamples: Data setup error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x1> * * .
            <0x2> * * .
            <0x3> * * .
            <0x4> * * .
            <0x5> * * .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("FilterExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("FilterExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void andFilter() {
        log.info("--- AND Filter ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.filter(Filter.and(
                        Filter.ge("age", 18),
                        Filter.eq("status", "active")
                    )))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "AND Filter");
    }

    private void orFilter() {
        log.info("--- OR Filter ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.filter(Filter.or(
                        Filter.eq("status", "active"),
                        Filter.eq("status", "pending")
                    )))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "OR Filter");
    }

    private void notFilter() {
        log.info("--- NOT Filter ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.filter(Filter.not(Filter.has("banned"))))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "NOT Filter");
    }

    private void hasFilter() {
        log.info("--- Has Filter ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("email"))
                    .withBlocks(List.of(Block.predicate("name"), Block.predicate("email")))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Has Filter");
    }

    private void executeQuery(String query, String testName) {
        try {
            DgraphProto.Response response = dgraphClient.newReadOnlyTransaction().query(query);
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("Filter Examples (Phase 3)", testName, query, json, success);
        } catch (Exception e) {
            results.record("Filter Examples (Phase 3)", testName, query, "Error: " + e.getMessage(), false);
        }
    }
}
