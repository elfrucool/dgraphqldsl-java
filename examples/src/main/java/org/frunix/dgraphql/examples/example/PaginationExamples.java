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
public class PaginationExamples {

    private static final Logger log = LoggerFactory.getLogger(PaginationExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public PaginationExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Pagination Examples (Phase 6) ===");
        setupData();
        try {
            orderAndFirst();
            offsetPagination();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        StringBuilder nquads = new StringBuilder();
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry", "Ivy", "Jack",
                         "Kate", "Leo", "Mike", "Noah", "Olivia", "Paul", "Quinn", "Rose", "Sam", "Tom",
                         "Uma", "Victor", "Wendy", "Xavier", "Yara", "Zack"};
        for (int i = 0; i < names.length; i++) {
            nquads.append(String.format("_:person%d <name> \"%s\" .\n", i, names[i]));
        }
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads.toString()))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("PaginationExamples: Inserted {} persons for pagination testing", names.length);
        } catch (Exception e) {
            log.warn("PaginationExamples: Data setup error: {}", e.getMessage());
        }
    }

    private void teardownData() {
        String nquads = """
            <0x1> * * .
            <0x2> * * .
            <0x3> * * .
            <0x4> * * .
            <0x5> * * .
            <0x6> * * .
            <0x7> * * .
            <0x8> * * .
            <0x9> * * .
            <0xa> * * .
            <0xb> * * .
            <0xc> * * .
            <0xd> * * .
            <0xe> * * .
            <0xf> * * .
            <0x10> * * .
            <0x11> * * .
            <0x12> * * .
            <0x13> * * .
            <0x14> * * .
            <0x15> * * .
            <0x16> * * .
            <0x17> * * .
            <0x18> * * .
            <0x19> * * .
            <0x1a> * * .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setDelNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("PaginationExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("PaginationExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void orderAndFirst() {
        log.info("--- Order and First ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withOrderasc("name")
                    .withFirst(10)
                    .withBlocks(List.of(Block.predicate("name"), Block.predicate("age")))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Order and First");
    }

    private void offsetPagination() {
        log.info("--- Offset Pagination ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withOrderasc("name")
                    .withFirst(10)
                    .withOffset(20)
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Offset Pagination");
    }

    private void executeQuery(String query, String testName) {
        try {
            DgraphProto.Response response = dgraphClient.newReadOnlyTransaction().query(query);
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("05 Pagination Examples (Phase 6)", testName, query, json, success);
        } catch (Exception e) {
            results.record("05 Pagination Examples (Phase 6)", testName, query, "Error: " + e.getMessage(), false);
        }
    }
}
