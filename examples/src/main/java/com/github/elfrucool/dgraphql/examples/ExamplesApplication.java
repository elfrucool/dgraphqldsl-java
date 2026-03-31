package com.github.elfrucool.dgraphql.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application for running DSL examples.
 *
 * <p>This application demonstrates how to use the dgraphqldsl-java library
 * to build DQL queries and mutations. Each example class is a Spring component
 * that runs on application startup.</p>
 *
 * <p>To run examples:</p>
 * <pre>
 * ./gradlew :examples:bootRun
 * </pre>
 *
 * <p>Requires a running Dgraph instance. Use {@code task up} to start Dgraph
 * and {@code task down} to stop it.</p>
 *
 * @see com.github.elfrucool.dgraphql.examples.example
 */
@SpringBootApplication
public class ExamplesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamplesApplication.class, args);
    }
}
