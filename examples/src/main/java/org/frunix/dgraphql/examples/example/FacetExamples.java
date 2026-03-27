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
public class FacetExamples {

    private static final Logger log = LoggerFactory.getLogger(FacetExamples.class);
    private final DgraphClient dgraphClient;
    private final ResultsCollector results;

    public FacetExamples(DgraphClient dgraphClient, ResultsCollector results) {
        this.dgraphClient = dgraphClient;
        this.results = results;
    }

    @PostConstruct
    public void run() {
        log.info("=== Facet Examples (Phase 5) ===");
        setupData();
        try {
            basicFacets();
        } finally {
            teardownData();
        }
    }

    private void setupData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <friend> _:bob (since=2024-01-15) .
            _:alice <friend> _:charlie (since=2024-03-20) .
            _:bob <name> "Bob" .
            _:charlie <name> "Charlie" .
            """;
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("FacetExamples: Test data inserted (Alice with friends having since facet)");
        } catch (Exception e) {
            log.warn("FacetExamples: Data setup error: {}", e.getMessage());
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
                .setDelNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("FacetExamples: Test data cleaned up");
        } catch (Exception e) {
            log.warn("FacetExamples: Data teardown error: {}", e.getMessage());
        }
    }

    private void basicFacets() {
        log.info("--- Basic Facets ---");
        
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                            .withDirective(Directive.facets("since"))
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        log.info("Query: {}", result.query());
        executeQuery(result.query(), "Basic Facets");
    }

    private void executeQuery(String query, String testName) {
        try {
            DgraphProto.Response response = dgraphClient.newReadOnlyTransaction().query(query);
            String json = response.getJson().toStringUtf8();
            boolean success = !json.isEmpty() && !json.equals("{}") && !json.equals("{\"me\":[]}");
            if (json.isEmpty() || json.equals("{}") || json.equals("{\"me\":[]}")) {
                json = "(no data - empty database)";
            }
            results.record("Facet Examples (Phase 5)", testName, query, json, success);
        } catch (Exception e) {
            results.record("Facet Examples (Phase 5)", testName, query, "Error: " + e.getMessage(), false);
        }
    }
}
