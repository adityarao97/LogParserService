package com.example.individualproject.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChainOfResponsibilityTest {
    @Test
    void testLogProcessingChain() {
        // Set up the chain
        LogParser apmParser = new APMLogParser();
        LogParser appParser = new AppLogParser();
        LogParser requestParser = new RequestLogParser();

        apmParser.setNextParser(appParser);
        appParser.setNextParser(requestParser);

        // Sample log lines
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=54",
                "timestamp=2024-12-12T13:20:05 level=INFO message=Application started",
                "timestamp=2024-12-12T13:20:05 request_url=/api/update response_time_ms=35 response_status=200"
        );

        // Process the logs through the chain
        logLines.forEach(apmParser::processLogLine);

        // Verify results for APM logs
        ObjectNode apmData = apmParser.getAggregatedData();
        assertTrue(apmData.has("cpu_usage_percent"));
        assertEquals(54.0, apmData.get("cpu_usage_percent").get("minimum").asDouble());

        // Verify results for Application logs
        ObjectNode appData = appParser.getAggregatedData();
        assertTrue(appData.has("INFO"));
        assertEquals(1, appData.get("INFO").asInt());

        // Verify results for Request logs
        ObjectNode requestData = requestParser.getAggregatedData();
        assertTrue(requestData.has("/api/update"));
        assertEquals(35, requestData.get("/api/update").get("response_times").get("min").asInt());
    }

    @Test
    void testEmptyLogProcessingChain() {
        LogParser apmParser = new APMLogParser();
        LogParser appParser = new AppLogParser();
        LogParser requestParser = new RequestLogParser();

        apmParser.setNextParser(appParser);
        appParser.setNextParser(requestParser);

        // Empty log lines
        List<String> logLines = List.of();

        logLines.forEach(apmParser::processLogLine);

        assertEquals(0, apmParser.getAggregatedData().size());
        assertEquals(5, appParser.getAggregatedData().size());
        assertEquals(0, requestParser.getAggregatedData().size());

        appParser.getAggregatedData().fields().forEachRemaining(entry -> {
            assertEquals(0, entry.getValue().asInt(), "Value for " + entry.getKey() + " is not 0");
        });
    }

    @Test
    void testUnrecognizedLogLines() {
        LogParser apmParser = new APMLogParser();
        LogParser appParser = new AppLogParser();
        LogParser requestParser = new RequestLogParser();

        apmParser.setNextParser(appParser);
        appParser.setNextParser(requestParser);

        // Logs that don't match any parser
        List<String> logLines = List.of(
                "this is a random log",
                "completely unrelated log line"
        );

        logLines.forEach(apmParser::processLogLine);

        assertEquals(0, apmParser.getAggregatedData().size());
        assertEquals(5, appParser.getAggregatedData().size());
        assertEquals(0, requestParser.getAggregatedData().size());

        // Assert that all values in appData are 0
        appParser.getAggregatedData().fields().forEachRemaining(entry -> {
            assertEquals(0, entry.getValue().asInt(), "Value for " + entry.getKey() + " is not 0");
        });
    }

    @Test
    void testMultipleParserLogs() {
        LogParser apmParser = new APMLogParser();
        LogParser appParser = new AppLogParser();
        LogParser requestParser = new RequestLogParser();

        apmParser.setNextParser(appParser);
        appParser.setNextParser(requestParser);

        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=67", // APM log
                "timestamp=2024-12-12T13:20:05 level=INFO message=Application started", // Application log
                "timestamp=2024-12-12T13:20:05 request_url=/api/update response_time_ms=50 response_status=200", // Request log
                "invalid"
        );

        logLines.forEach(apmParser::processLogLine);

        ObjectNode apmData = apmParser.getAggregatedData();
        assertTrue(apmData.has("cpu_usage_percent"));

        ObjectNode appData = appParser.getAggregatedData();
        assertTrue(appData.has("INFO"));

        ObjectNode requestData = requestParser.getAggregatedData();
        assertTrue(requestData.has("/api/update"));
    }

    @Test
    void testChainOrder() {
        LogParser apmParser = new APMLogParser();
        LogParser appParser = new AppLogParser();
        LogParser requestParser = new RequestLogParser();

        apmParser.setNextParser(appParser);
        appParser.setNextParser(requestParser);

        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=67 level=INFO"
        );

        logLines.forEach(apmParser::processLogLine);

        // Verify APM logs handled the metric
        ObjectNode apmData = apmParser.getAggregatedData();
        assertTrue(apmData.has("cpu_usage_percent"));

        // Verify Application logs ignored the log
        ObjectNode appData = appParser.getAggregatedData();
        assertEquals(5, appData.size());

        // Assert that all values in appData are 0
        appData.fields().forEachRemaining(entry -> {
            assertEquals(0, entry.getValue().asInt(), "Value for " + entry.getKey() + " is not 0");
        });
    }
}
