package org.frunix.dgraphql.examples.example;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import org.frunix.dgraphql.dsl.*;
import org.frunix.dgraphql.examples.result.ResultsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class SchemaExamples {

    private static final Logger log = LoggerFactory.getLogger(SchemaExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public SchemaExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Schema Examples (Phase 11.5) ===");
        try {
            predicateWithCount();
            predicateWithUpsert();
            typeDefinition();
        } finally {
            teardownSchema();
        }
    }

    private void predicateWithCount() {
        String testName = "Predicate with @count";
        log.info("--- Predicate with @count ---");
        
        Alter alter = Alter.predicate("schemaFriend", "[uid]")
            .withCount();

        String dql = alter.dql();
        log.info("DSL: {}", dql);
        
        executeAlter(dql, testName);
    }

    private void predicateWithUpsert() {
        String testName = "Predicate with @upsert";
        log.info("--- Predicate with @upsert ---");
        
        Alter alter = Alter.predicate("schemaEmail", "string")
            .withIndex("hash")
            .withUpsert();

        String dql = alter.dql();
        log.info("DSL: {}", dql);
        
        executeAlter(dql, testName);
    }

    private void typeDefinition() {
        String testName = "Type Definition";
        log.info("--- Type Definition ---");
        
        Alter alter = Alter.type("SchemaPerson", "name", "age", "email");

        String dql = alter.dql();
        log.info("DSL: {}", dql);
        
        executeAlter(dql, testName);
    }

    private void executeAlter(String schema, String testName) {
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
            results.record("12 Schema Examples (Phase 11.2)", testName, schema, "Schema update successful", true);
            log.info("Schema update successful");
        } catch (Exception e) {
            results.record("12 Schema Examples (Phase 11.2)", testName, schema, "Error: " + e.getMessage(), false);
            log.warn("Schema update error: {}", e.getMessage());
        }
    }

    private void teardownSchema() {
        try {
            dgraphClient.alter(DgraphProto.Operation.newBuilder()
                .setDropAttr("schemaFriend")
                .build());
            dgraphClient.alter(DgraphProto.Operation.newBuilder()
                .setDropAttr("schemaEmail")
                .build());
            dgraphClient.alter(DgraphProto.Operation.newBuilder()
                .setDropAttr("SchemaPerson")
                .build());
        } catch (Exception e) {
            log.warn("Schema teardown error: {}", e.getMessage());
        }
        log.info("Schema teardown complete");
    }
}
