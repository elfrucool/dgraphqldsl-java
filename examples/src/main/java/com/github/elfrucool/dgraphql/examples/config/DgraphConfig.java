package com.github.elfrucool.dgraphql.examples.config;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.DgraphProto.Operation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Dgraph client connection.
 * <p>
 * This configuration creates a {@link io.dgraph.DgraphClient} bean that connects
 * to a Dgraph instance. The connection uses gRPC with plaintext authentication.
 * <p>
 * Configuration properties:
 * <ul>
 *   <li>{@code dgraph.host} - Dgraph server hostname (default: localhost)</li>
 *   <li>{@code dgraph.port} - Dgraph gRPC port (default: 9080)</li>
 * </ul>
 */
@Configuration
public class DgraphConfig {

    private static final Logger log = LoggerFactory.getLogger(DgraphConfig.class);

    @Value("${dgraph.host:localhost}")
    private String host;

    @Value("${dgraph.port:9080}")
    private int port;

    @Bean
    public DgraphClient dgraphClient() {
        log.info("Connecting to Dgraph at {}:{}", host, port);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
        DgraphGrpc.DgraphStub stub = DgraphGrpc.newStub(channel);
        return new DgraphClient(stub);
    }
}
