package com.example.individualproject.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppLogParserTest {

    @Test
    void testParseAndAggregateApplicationLogs() {
        AppLogParser appLogParser = new AppLogParser();
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 level=INFO message=Application started",
                "timestamp=2024-12-12T13:20:05 level=ERROR message=Unhandled exception",
                "timestamp=2024-12-12T13:20:05 level=INFO message=Processed request"
        );

        logLines.forEach(appLogParser::processLogLine);

        ObjectNode aggregatedData = appLogParser.getAggregatedData();
        assertEquals(1, aggregatedData.get("ERROR").asInt());
        assertEquals(2, aggregatedData.get("INFO").asInt());
    }

    @Test
    void testEmptyApplicationLogs() {
        AppLogParser appLogParser = new AppLogParser();
        List<String> logLines = List.of();

        logLines.forEach(appLogParser::processLogLine);

        ObjectNode aggregatedData = appLogParser.getAggregatedData();
        assertEquals(5, aggregatedData.size());
        aggregatedData.fields().forEachRemaining(entry -> {
            assertEquals(0, entry.getValue().asInt(), "Value for " + entry.getKey() + " is not 0");
        });
    }

    @Test
    void testMixedApplicationLogs() {
        AppLogParser appLogParser = new AppLogParser();
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 level=INFO message=Application started",
                "invalid data",
                "timestamp=2024-12-12T13:20:05 level=ERROR message=Unhandled exception"
        );

        logLines.forEach(appLogParser::processLogLine);

        ObjectNode aggregatedData = appLogParser.getAggregatedData();
        assertEquals(1, aggregatedData.get("ERROR").asInt());
        assertEquals(1, aggregatedData.get("INFO").asInt());
    }
}
