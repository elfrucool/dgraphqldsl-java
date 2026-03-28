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

@Component
public class DeleteExamples {

    private static final Logger log = LoggerFactory.getLogger(DeleteExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public DeleteExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Delete Examples (Phase 11.4) ===");
        setupSchema();
        setupData();
        try {
            deletePredicateValue();
            deleteAllPredicates();
        } finally {
            teardownData();
        }
    }

    private void setupSchema() {
        String schema = """
            deleteName: string .
            deleteAge: int .
            deleteEmail: string .
            """;
        
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
        } catch (Exception e) {
            log.warn("DeleteExamples: Schema setup error: {}", e.getMessage());
        }
        log.info("Schema setup complete");
    }

    private void setupData() {
        String nquads = """
            <0x99> <deleteName> "Alice" .
            <0x99> <deleteAge> "30" .
            <0x99> <deleteEmail> "alice@test.com" .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("DeleteExamples: Data setup error: {}", e.getMessage());
        }
        log.info("Data setup complete");
    }

    private void deletePredicateValue() {
        String testName = "Delete Predicate Value";
        log.info("--- Delete Predicate Value (using JSON) ---");
        
        try {
            String jsonDelete = """
                [{"uid": "0x99", "deleteName": null}]
                """;
            
            try (Transaction txn = dgraphClient.newTransaction()) {
                DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                    .setDeleteJson(ByteString.copyFromUtf8(jsonDelete))
                    .build();
                txn.mutate(mu);
                txn.commit();
            }
            results.record("13 Delete Examples (Phase 11.3)", testName, "JSON: deleteName = null", "Mutation successful", true);
            log.info("Mutation successful (JSON format)");
        } catch (Exception e) {
            results.record("13 Delete Examples (Phase 11.3)", testName, "JSON: deleteName = null", "Error: " + e.getMessage(), false);
            log.warn("Mutation error: {}", e.getMessage());
        }
    }

    private void deleteAllPredicates() {
        String testName = "Delete All Predicates";
        log.info("--- Delete All Predicates ---");
        
        Mutation mutation = Mutation.delete(
            SetTriple.subject("0x99").predicate("*").value("*")
        );

        String dql = mutation.dql();
        log.info("DSL: {}", dql);
        
        executeMutation(dql, testName);
    }

    private void executeMutation(String mutationDql, String testName) {
        try {
            String nquads = mutationDql.replace("{ delete { ", "").replace(" } }", "");
            try (Transaction txn = dgraphClient.newTransaction()) {
                DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                    .setDelNquads(ByteString.copyFromUtf8(nquads))
                    .build();
                txn.mutate(mu);
                txn.commit();
            }
            results.record("13 Delete Examples (Phase 11.3)", testName, mutationDql, "Mutation successful", true);
            log.info("Mutation successful");
        } catch (Exception e) {
            results.record("13 Delete Examples (Phase 11.3)", testName, mutationDql, "Error: " + e.getMessage(), false);
            log.warn("Mutation error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x99> * * .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("DeleteExamples: Data teardown error: {}", e.getMessage());
        }
        log.info("Teardown complete");
    }
}
