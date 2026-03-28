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
public class ExpandExamples {

    private static final Logger log = LoggerFactory.getLogger(ExpandExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public ExpandExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Expand Examples (Phase 11.7) ===");
        setupSchema();
        setupData();
        try {
            expandByType();
            expandAll();
        } finally {
            teardownData();
        }
    }

    private void setupSchema() {
        String schema = """
            name: string @index(exact) .
            age: int .
            email: string .
            type Person {
              name
              age
              email
            }
            """;
        
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
        } catch (Exception e) {
            log.warn("ExpandExamples: Schema setup error: {}", e.getMessage());
        }
        log.info("Schema setup complete: Person type with name, age, email");
    }

    private void setupData() {
        String nquads = """
            <0x1> <name> "Alice" .
            <0x1> <age> "30" .
            <0x1> <email> "alice@example.com" .
            <0x1> <dgraph.type> "Person" .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("ExpandExamples: Data setup error: {}", e.getMessage());
        }
        log.info("Data setup complete: Alice as Person type");
    }

    private void expandByType() {
        String testName = "Expand by Type";
        log.info("--- Expand by Type (Person) ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.expand("Person")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("17 Expand Examples (Phase 11.7)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("17 Expand Examples (Phase 11.7)", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void expandAll() {
        String testName = "Expand All Types";
        log.info("--- Expand All Types ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.expandAll()
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("17 Expand Examples (Phase 11.7)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("17 Expand Examples (Phase 11.7)", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x1> * * .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("ExpandExamples: Data teardown error: {}", e.getMessage());
        }
        log.info("Teardown complete");
    }
}
