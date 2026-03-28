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
public class LanguageExamples {

    private static final Logger log = LoggerFactory.getLogger(LanguageExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LanguageExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Language Examples (Phase 11.3) ===");
        setupSchema();
        setupData();
        try {
            queryLanguageTags();
            queryLanguageFallback();
        } finally {
            teardownData();
        }
    }

    private void setupSchema() {
        String schema = "localename: string @lang .";
        
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
        } catch (Exception e) {
            log.warn("LanguageExamples: Schema setup error: {}", e.getMessage());
        }
        log.info("Schema setup complete: localename@lang");
    }

    private void setupData() {
        String nquads = """
            <0x1> <localename@en> "Alice" .
            <0x1> <localename@fr> "Alice" .
            <0x1> <localename@es> "Alicia" .
            <0x1> <age> "30" .
            <0x2> <localename@en> "Bob" .
            <0x2> <localename@fr> "Robert" .
            <0x2> <age> "25" .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("LanguageExamples: Data setup error: {}", e.getMessage());
        }
        log.info("Data setup complete");
    }

    private void queryLanguageTags() {
        String testName = "Single Language Tag";
        log.info("--- Query with single language tag ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.predicate("localename", LanguageTag.en()),
                        Block.predicate("age")
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("11 Language Examples (Phase 11.1)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("11 Language Examples (Phase 11.1)", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void queryLanguageFallback() {
        String testName = "Language Fallback";
        log.info("--- Query with language fallback ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1"))
                    .withBlocks(List.of(
                        Block.predicate("localename", LanguageTag.of("en", "fr")),
                        Block.predicate("localename", LanguageTag.es())
                    ))
            ));

        DqlResult result = query.dql();
        log.info("DSL: {}", result.query());

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            DgraphProto.Response response = txn.query(result.query());
            String json = response.getJson().toStringUtf8();
            results.record("11 Language Examples (Phase 11.1)", testName, result.query(), json, true);
            log.info("Response: {}", json);
        } catch (Exception e) {
            results.record("11 Language Examples (Phase 11.1)", testName, result.query(), "Error: " + e.getMessage(), false);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x1> * * .
            <0x2> * * .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("LanguageExamples: Data teardown error: {}", e.getMessage());
        }
        log.info("Teardown complete");
    }
}
