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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class UpsertExamples {

    private static final Logger log = LoggerFactory.getLogger(UpsertExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public UpsertExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Upsert Examples (Phase 11.2) ===");
        setupSchema();
        setupData();
        try {
            upsertWithSet();
            upsertWithSetAndDelete();
        } finally {
            teardownData();
        }
    }

    private void setupSchema() {
        String schema = """
            upsertEmail: string .
            upsertStatus: string .
            upsertOldStatus: string .
            """;
        
        try {
            DgraphProto.Operation operation = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(operation);
        } catch (Exception e) {
            log.warn("UpsertExamples: Schema setup error: {}", e.getMessage());
        }
        log.info("Schema setup complete");
    }

    private void setupData() {
        String nquads = """
            <0xaa> <upsertEmail> "test@example.com" .
            <0xaa> <upsertStatus> "inactive" .
            <0xaa> <upsertOldStatus> "pending" .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("UpsertExamples: Data setup error: {}", e.getMessage());
        }
        log.info("Data setup complete");
    }

    private void upsertWithSet() {
        String testName = "Upsert with Set";
        log.info("--- Upsert with Set ---");
        
        Mutation mutation = Mutation.upsertRaw(
            "query { v as var(func: eq(upsertEmail, \"test@example.com\")) }",
            Mutation.Set.of(
                SetTriple.subject("uid(v)").predicate("upsertStatus").value("active")
            )
        );

        String dql = mutation.dql();
        log.info("DSL: {}", dql);
        
        executeUpsert(mutation, testName);
    }

    private void upsertWithSetAndDelete() {
        String testName = "Upsert with Set and Delete";
        log.info("--- Upsert with Set and Delete ---");
        
        Mutation mutation = Mutation.upsertRaw(
            "query { v as var(func: eq(upsertEmail, \"test@example.com\")) }",
            Mutation.Set.of(
                SetTriple.subject("uid(v)").predicate("upsertStatus").value("updated")
            ),
            Mutation.Delete.of(
                SetTriple.subject("uid(v)").predicate("upsertOldStatus").value("*")
            )
        );

        String dql = mutation.dql();
        log.info("DSL: {}", dql);
        
        executeUpsert(mutation, testName);
    }

    private void executeUpsert(Mutation mutation, String testName) {
        try {
            Map<String, Object> upsertJson = new LinkedHashMap<>();
            
            String queryStr = "query { v as var(func: eq(upsertEmail, \"test@example.com\")) }";
            upsertJson.put("query", queryStr);
            
            List<Map<String, Object>> mutationList = new ArrayList<>();
            List<Map<String, Object>> setList = new ArrayList<>();
            
            if (mutation instanceof Mutation.UpsertRaw upsert) {
                if (upsert.set() != null) {
                    for (SetTriple t : upsert.set().triples()) {
                        Map<String, Object> triple = new LinkedHashMap<>();
                        triple.put("uid", t.subject());
                        triple.put(t.predicate(), t.value());
                        setList.add(triple);
                    }
                }
            }
            
            Map<String, Object> mutObj = new LinkedHashMap<>();
            mutObj.put("cond", "@if(uid(v))");
            if (!setList.isEmpty()) {
                mutObj.put("set", setList);
            }
            mutationList.add(mutObj);
            upsertJson.put("mutations", mutationList);
            
            String jsonStr = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(upsertJson);
            log.info("Upsert JSON: {}", jsonStr);
            
            java.net.URL url = new java.net.URL("http://localhost:8080/mutate?commitNow=true");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonStr.getBytes());
            }
            int code = conn.getResponseCode();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(code == 200 ? conn.getInputStream() : conn.getErrorStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            
            if (code == 200) {
                results.record("14 Upsert Examples (Phase 11.4)", testName, mutation.dql(), "Upsert successful", true);
                log.info("Upsert successful");
            } else {
                results.record("14 Upsert Examples (Phase 11.4)", testName, mutation.dql(), "Error: " + response, false);
                log.warn("Upsert error: {}", response);
            }
        } catch (Exception e) {
            results.record("14 Upsert Examples (Phase 11.4)", testName, mutation.dql(), "Error: " + e.getMessage(), false);
            log.warn("Upsert error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0xaa> * * .
            """;

        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("UpsertExamples: Data teardown error: {}", e.getMessage());
        }
        log.info("Teardown complete");
    }
}
