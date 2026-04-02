package io.github.elfrucool.dgraphql.examples;

import io.github.elfrucool.dgraphql.examples.result.ResultsCollector;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Application runner that prints the test results report after all examples complete.
 * <p>
 * This component is invoked after the Spring Boot application starts and all example
 * components have executed. It prints a formatted report showing which examples
 * passed or failed.
 *
 * @see io.github.elfrucool.dgraphql.examples.result.ResultsCollector
 */
@Component
public class AppRunner implements ApplicationRunner {

    private final ResultsCollector results;

    public AppRunner(ResultsCollector results) {
        this.results = results;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        results.printReport();
        System.exit(0);
    }
}
