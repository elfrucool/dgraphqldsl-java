package org.frunix.dgraphql.examples.config;

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
