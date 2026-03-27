package org.frunix.dgraphql.examples.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResultsCollector {

    private static final Logger log = LoggerFactory.getLogger(ResultsCollector.class);
    private final List<ExampleResult> results = new ArrayList<>();

    public void record(String example, String test, String query, String response, boolean success) {
        results.add(new ExampleResult(example, test, query, response, success));
    }

    public void printReport() {
        StringBuilder output = new StringBuilder();
        output.append(""
                + "\n╔════════════════════════════════════════════════════════════════════════════"
                + "\n║                           EXAMPLES REPORT"
                + "\n╠════════════════════════════════════════════════════════════════════════════");
        
        String currentSection = "";
        int passed = 0;
        int failed = 0;
        
        for (ExampleResult r : results) {
            if (!r.example().equals(currentSection)) {
                currentSection = r.example();
                output.append("\n║");
                output.append("\n║  %s".formatted(String.format("%-74s", currentSection)));
                output.append("\n║");
            }
            
            String status = r.success() ? "✓ PASS" : "✗ FAIL";
            if (r.success()) passed++; else failed++;
            
            output.append("\n║    %s: %s".formatted(String.format("%-10s", status), String.format("%-60s", r.test())));
        }
        
        output.append("\n║");
        output.append("\n╠════════════════════════════════════════════════════════════════════════════");
        output.append("\n║  Total: %s | Passed: %s | Failed: %s".formatted( 
            String.format("%-6s", passed + failed),
            String.format("%-6s", passed),
            String.format("%-6s", failed)));
        output.append("\n╚════════════════════════════════════════════════════════════════════════════");
        output.append("\n");
        log.info("{}", output);
    }

    public record ExampleResult(String example, String test, String query, String response, boolean success) {}
}
