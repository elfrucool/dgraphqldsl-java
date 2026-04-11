package tutorial;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Tutorial {
    public static void main(String[] args) throws Exception {
        // Create a gRPC channel to the Dgraph server
        // Port 9080 is where Dgraph listens for gRPC connections
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9080)
            .usePlaintext() // no TLS - use plaintext encoding
            .build();

        // Dgraph uses gRPC for client-server communication
        // Create a stub (client-side object that sends requests)
        DgraphGrpc.DgraphStub stub = DgraphGrpc.newStub(channel);
        // Wrap the stub in a DgraphClient for easier use
        DgraphClient client = new DgraphClient(stub);

        System.out.println("Connected to Dgraph!");

        // Clean shutdown
        channel.shutdownNow();
    }
}