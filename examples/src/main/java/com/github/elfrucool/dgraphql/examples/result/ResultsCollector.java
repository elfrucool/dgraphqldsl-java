package com.github.elfrucool.dgraphql.examples.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects and reports test results from all example classes.
 * <p>
 * This component collects results from each example as they execute, then
 * prints a formatted report when {@link #printReport()} is called.
 * <p>
 * Each result records:
 * <ul>
 *   <li>Example name - the example class this result belongs to</li>
 *   <li>Test name - the specific test within the example</li>
 *   <li>Query/DQL - the DSL query or mutation that was executed</li>
 *   <li>Response - the response from Dgraph or error message</li>
 *   <li>Success - whether the test passed or failed</li>
 * </ul>
 */
@Component
public class ResultsCollector {

    private static final Logger log = LoggerFactory.getLogger(ResultsCollector.class);
    private final List<ExampleResult> results = new ArrayList<>();

    public void record(String example, String test, String query, String response, boolean success) {
        results.add(new ExampleResult(example, test, query, response, success));
    }

    public void printReport() {
        List<ExampleResult> sortedResults = results.stream()
            .sorted((a, b) -> a.example().compareTo(b.example()))
            .collect(Collectors.toList());
        
        StringBuilder output = new StringBuilder();
        output.append(""
                + "\n╔════════════════════════════════════════════════════════════════════════════"
                + "\n║                           EXAMPLES REPORT"
                + "\n╠════════════════════════════════════════════════════════════════════════════");
        
        String currentSection = "";
        int passed = 0;
        int failed = 0;
        
        for (ExampleResult r : sortedResults) {
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
