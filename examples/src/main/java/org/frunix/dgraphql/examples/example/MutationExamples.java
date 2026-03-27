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
import java.util.Map;

@Component
public class MutationExamples {

    private static final Logger log = LoggerFactory.getLogger(MutationExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public MutationExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Mutation Examples (Phase 9) ===");
        setupData();
        try {
            setMutation();
            deleteMutation();
            conditionalMutation();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        log.info("Setting up test data for mutations...");
        Mutation mutation = Mutation.set(List.of(
            SetTriple.subject("_:alice").predicate("name").value("Alice"),
            SetTriple.subject("_:bob").predicate("name").value("Bob")
        ));
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetJson(com.google.protobuf.ByteString.copyFromUtf8("{\"set\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]}"))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("Created Alice and Bob for mutation testing");
        } catch (Exception e) {
            log.warn("Setup data warning: {}", e.getMessage());
        }
    }

    private void teardownData() {
        log.info("Cleaning up mutation test data...");
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDeleteJson(com.google.protobuf.ByteString.copyFromUtf8(
                    "{\"delete\":[{\"name\":null}]}"))
                .build();
            txn.mutate(mu);
            txn.commit();
        } catch (Exception e) {
            log.warn("Teardown warning: {}", e.getMessage());
        }
    }

    private void setMutation() {
        log.info("--- Set Mutation ---");
        
        Mutation mutation = Mutation.set(List.of(
            SetTriple.subject("_:newPerson1").predicate("name").value("NewPerson1"),
            SetTriple.subject("_:newPerson2").predicate("name").value("NewPerson2")
        ));

        log.info("Mutation (JSON): {}", mutation.toJsonList());
        executeMutation(mutation, "Set Mutation");
    }

    private void deleteMutation() {
        log.info("--- Delete Mutation ---");
        
        Mutation mutation = Mutation.Delete.of(List.of(
            SetTriple.subject("0x1").predicate("name").value("*")
        ));

        log.info("Mutation (JSON): {}", mutation.toJsonList());
        executeMutation(mutation, "Delete Mutation");
    }

    private void conditionalMutation() {
        log.info("--- Conditional Mutation ---");
        
        Mutation.Set setMutation = Mutation.Set.of(List.of(
            SetTriple.subject("uid(alice)").predicate("status").value("active")
        ));
        
        String query = "{ alice as var(func: eq(name, \"Alice\")) }";
        
        Mutation mutation = Mutation.upsertRaw(query, setMutation);

        log.info("Mutation (JSON): {}", mutation.toJsonList());
        executeMutation(mutation, "Conditional Mutation");
    }

    private void executeMutation(Mutation mutation, String testName) {
        try {
            String jsonStr;
            
            if (mutation instanceof org.frunix.dgraphql.dsl.Mutation.UpsertRaw upsert) {
                Map<String, Object> upsertJson = new java.util.LinkedHashMap<>();
                upsertJson.put("query", upsert.query());
                
                List<Map<String, Object>> mutationList = new java.util.ArrayList<>();
                List<Map<String, Object>> setList = new java.util.ArrayList<>();
                for (var t : upsert.set().triples()) {
                    Map<String, Object> triple = new java.util.LinkedHashMap<>();
                    triple.put("uid", t.subject());
                    triple.put(t.predicate(), t.value());
                    setList.add(triple);
                }
                Map<String, Object> mutObj = new java.util.LinkedHashMap<>();
                mutObj.put("cond", "@if(uid(" + extractVarName(upsert.query()) + "))");
                mutObj.put("set", setList);
                mutationList.add(mutObj);
                upsertJson.put("mutations", mutationList);
                
                jsonStr = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(upsertJson);
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
                reader.close();
                conn.disconnect();
                
                log.info("Upsert response code: {}, body: {}", code, response);
                if (code == 200) {
                    results.record("Mutation Examples (Phase 9)", testName, mutation.dql(), "Upsert successful", true);
                } else {
                    results.record("Mutation Examples (Phase 9)", testName, mutation.dql(), "Error: " + response, false);
                }
                return;
            }
            
            try (Transaction txn = dgraphClient.newTransaction()) {
                Map<String, Object> jsonBody = Map.of("set", mutation.toJsonList());
                jsonStr = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(jsonBody);
                
                DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                    .setSetJson(com.google.protobuf.ByteString.copyFromUtf8(jsonStr))
                    .build();
                DgraphProto.Response response = txn.mutate(mu);
                txn.commit();
                log.info("Mutation successful - response: {}", response);
                results.record("Mutation Examples (Phase 9)", testName, mutation.dql(), "Mutation successful", true);
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
            log.error("Mutation failed - DQL: {}", mutation.dql());
            log.error("JSON: {}", mutation.toJsonList());
            log.error("Error: {}", errorMsg);
            if (cause != null && cause.getCause() != null) {
                log.error("Cause: {}", cause.getCause().getMessage());
            }
            results.record("Mutation Examples (Phase 9)", testName, mutation.dql(), "Error: " + errorMsg, false);
        }
    }
    
    private String extractVarName(String query) {
        int asPos = query.indexOf(" as ");
        int start = asPos;
        for (int i = asPos - 1; i >= 0; i--) {
            char c = query.charAt(i);
            if (c == ' ' || c == '{' || c == '\n') {
                start = i + 1;
                break;
            }
        }
        return query.substring(start, asPos);
    }
}
