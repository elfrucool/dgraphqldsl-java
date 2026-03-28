package org.frunix.dgraphql.examples.example;

import com.google.protobuf.ByteString;
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
public class PathExamples {

    private static final Logger log = LoggerFactory.getLogger(PathExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public PathExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Path Examples (Phase 11.5) ===");
        setupSchema();
        setupData();
        try {
            shortestPath();
            kShortestPath();
        } finally {
            teardownData();
        }
    }

    private void setupSchema() {
        String schema = """
            name: string @index(exact) .
            friend: [uid] .
            """;
        
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
        } catch (Exception e) {
            log.warn("PathExamples: Schema setup error: {}", e.getMessage());
        }
        log.info("Schema setup complete: name, friend");
    }

    private void setupData() {
        String nquads = """
            <0x1> <name> "Alice" .
            <0x2> <name> "Bob" .
            <0x3> <name> "Charlie" .
            <0x4> <name> "Diana" .
            <0x1> <friend> <0x2> .
            <0x2> <friend> <0x3> .
            <0x1> <friend> <0x3> .
            <0x3> <friend> <0x4> .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("PathExamples: Data setup error: {}", e.getMessage());
        }
        log.info("Data setup complete: Alice->Bob->Charlie, Alice->Charlie, Charlie->Diana");
    }

    private void shortestPath() {
        String testName = "Shortest Path";
        log.info("--- Shortest Path (Alice to Charlie) ---");
        
        Query query = Query.query()
            .withShortestPath(
                ShortestPath.shortest("path", "0x1", "0x3")
                    .withPredicate(Block.predicate("friend"))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("path"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("16 Path Examples (Phase 11.5)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("16 Path Examples (Phase 11.5)", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void kShortestPath() {
        String testName = "K-Shortest Path";
        log.info("--- K-Shortest Path (Alice to Charlie, k=2) ---");
        
        Query query = Query.query()
            .withShortestPath(
                ShortestPath.kShortest("path", "0x1", "0x3", 2)
                    .withPredicate(Block.predicate("friend"))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("path"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("16 Path Examples (Phase 11.5)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("16 Path Examples (Phase 11.5)", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
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
                .setDelNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("PathExamples: Data teardown error: {}", e.getMessage());
        }
        log.info("Teardown complete");
    }
}
