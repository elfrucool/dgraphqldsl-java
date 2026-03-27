package org.frunix.dgraphql.examples.config;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import io.dgraph.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataSetup {

    private static final Logger log = LoggerFactory.getLogger(DataSetup.class);
    private final DgraphClient dgraphClient;

    public DataSetup(DgraphClient dgraphClient) {
        this.dgraphClient = dgraphClient;
    }

    @PostConstruct
    public void setup() {
        log.info("Setting up schema and data...");
        try {
            setupSchema();
            insertData();
            log.info("Data setup complete");
        } catch (Exception e) {
            log.warn("Data setup error: {}", e.getMessage());
        }
    }

    private void setupSchema() {
        try {
            String schema = """
                name: string @index(exact, hash) .
                age: int .
                email: string @index(hash) .
                friend: uid .
                status: string @index(exact) .
                score: float .
                """;
            DgraphProto.Operation op = DgraphProto.Operation.newBuilder()
                .setSchema(schema)
                .build();
            dgraphClient.alter(op);
            log.info("Schema created");
        } catch (Exception e) {
            log.warn("Schema setup error: {}", e.getMessage());
        }
    }

    private void insertData() {
        String nquads = """
            _:alice <name> "Alice" .
            _:alice <age> "30"^^<http://www.w3.org/2001/XMLSchema#int> .
            _:alice <email> "alice@example.com" .
            _:alice <status> "active" .
            _:alice <score> "95.5"^^<http://www.w3.org/2001/XMLSchema#float> .
            
            _:bob <name> "Bob" .
            _:bob <age> "25"^^<http://www.w3.org/2001/XMLSchema#int> .
            _:bob <email> "bob@example.com" .
            _:bob <status> "active" .
            _:bob <score> "85.0"^^<http://www.w3.org/2001/XMLSchema#float> .
            
            _:charlie <name> "Charlie" .
            _:charlie <age> "28"^^<http://www.w3.org/2001/XMLSchema#int> .
            _:charlie <email> "charlie@example.com" .
            _:charlie <status> "pending" .
            _:charlie <score> "90.0"^^<http://www.w3.org/2001/XMLSchema#float> .
            
            _:diana <name> "Diana" .
            _:diana <age> "35"^^<http://www.w3.org/2001/XMLSchema#int> .
            _:diana <email> "diana@example.com" .
            _:diana <status> "active" .
            _:diana <score> "78.5"^^<http://www.w3.org/2001/XMLSchema#float> .
            
            _:alice <friend> _:bob .
            _:alice <friend> _:charlie .
            _:bob <friend> _:alice .
            _:charlie <friend> _:alice .
            """;
        
        try (Transaction txn = dgraphClient.newTransaction()) {
            DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder()
                .setSetNquads(com.google.protobuf.ByteString.copyFromUtf8(nquads))
                .build();
            txn.mutate(mu);
            txn.commit();
            log.info("Data inserted: 4 persons (Alice, Bob, Charlie, Diana) with friendships");
        } catch (Exception e) {
            log.warn("Data insert error: {}", e.getMessage());
        }
    }
}
