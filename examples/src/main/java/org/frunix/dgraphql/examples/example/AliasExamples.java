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

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AliasExamples {

    private static final Logger log = LoggerFactory.getLogger(AliasExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AliasExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Alias Examples (Phase 11.6) ===");
        setupSchema();
        setupData();
        try {
            aliasBasic();
            aliasUid();
            aliasCount();
            aliasNested();
        } finally {
            teardownData();
        }
    }

    private void setupSchema() {
        String schema = """
            name: string @index(exact) .
            age: int .
            friend: [uid] .
            """;
        
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
        } catch (Exception e) {
            log.warn("AliasExamples: Schema setup error: {}", e.getMessage());
        }
        log.info("Schema setup complete: name, age, friend");
    }

    private void setupData() {
        String nquads = """
            <0x1> <name> "Alice" .
            <0x1> <age> "30" .
            <0x1> <friend> <0x2> .
            <0x2> <name> "Bob" .
            <0x2> <age> "25" .
            <0x2> <friend> <0x3> .
            <0x3> <name> "Charlie" .
            <0x3> <age> "35" .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("AliasExamples: Data setup error: {}", e.getMessage());
        }
        log.info("Data setup complete");
    }

    private void aliasBasic() {
        String testName = "Basic Alias";
        log.info("--- Query with basic alias ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.predicate("name", "userName"),
                        Block.predicate("age", "userAge")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("16 Alias Examples (Phase 11.6)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("11.6 Alias Examples", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void aliasUid() {
        String testName = "Alias UID";
        log.info("--- Query with UID alias ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.predicate("uid", "userId"),
                        Block.predicate("name")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("16 Alias Examples (Phase 11.6)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("11.6 Alias Examples", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void aliasCount() {
        String testName = "Alias with Count";
        log.info("--- Query with count alias ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                            .withBlocks(List.of(
                                Block.predicate("name", "friendName")
                            ))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("16 Alias Examples (Phase 11.6)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("11.6 Alias Examples", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void aliasNested() {
        String testName = "Alias Nested Block";
        log.info("--- Query with alias on nested block ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                            .withBlocks(List.of(
                                Block.predicate("name", "friendName"),
                                Block.predicate("age", "friendAge")
                            ))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("16 Alias Examples (Phase 11.6)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("11.6 Alias Examples", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x1> * * .
            <0x2> * * .
            <0x3> * * .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("AliasExamples: Data teardown error: {}", e.getMessage());
        }
        log.info("Teardown complete");
    }
}
