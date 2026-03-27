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
public class AggregationExamples {

    private static final Logger log = LoggerFactory.getLogger(AggregationExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public AggregationExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Aggregation Examples (Phase 7) ===");
        setupData();
        try {
            countAggregation();
            mathExpression();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <score> "95.5" .
            _:alice <friend> _:bob .
            _:alice <friend> _:charlie .
            _:bob <name> "Bob" .
            _:bob <score> "85.0" .
            _:bob <friend> _:alice .
            _:charlie <name> "Charlie" .
            _:charlie <score> "90.0" .
            _:charlie <friend> _:alice .
            _:diana <name> "Diana" .
            _:diana <score> "78.5" .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("AggregationExamples: Test data inserted (Alice, Bob, Charlie, Diana with score and friend)");
        } catch (Exception e) {
            log.warn("AggregationExamples: Data setup error: {}", e.getMessage());
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
            log.info("AggregationExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("AggregationExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void countAggregation() {
        log.info("--- Count Aggregation ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate("count(friend)")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Count Aggregation");
    }

    private void mathExpression() {
        log.info("--- Math Expression ---");
        
        Query query = Query.query()
            .withVarBlock(
                VarBlock.var(Func.has("friend"))
                    .withAssignment(VarAssignment.valueVar("friendCount", Func.count("friend")))
                    .withAssignment(VarAssignment.valueVar("computedScore", Func.math("friendCount * 10")))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate(Func.val("friendCount"), "friendCount"),
                        Block.predicate(Func.val("computedScore"), "computedScore")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Math Expression");
    }

    private void executeQuery(String query, String testName) {
        try {
            DgraphProto.Response response = dgraphClient.newReadOnlyTransaction().query(query);
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("Aggregation Examples (Phase 7)", testName, query, json, success);
        } catch (Exception e) {
            results.record("Aggregation Examples (Phase 7)", testName, query, "Error: " + e.getMessage(), false);
        }
    }
}
