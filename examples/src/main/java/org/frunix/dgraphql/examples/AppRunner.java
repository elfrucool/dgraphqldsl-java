package org.frunix.dgraphql.examples;

import org.frunix.dgraphql.examples.result.ResultsCollector;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
