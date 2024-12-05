package com.example.individualproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class APMLogParserTest {

    @Test
    void testParseAndAggregateAPMLogs() {
        APMLogParser apmLogParser = new APMLogParser();
        List<String> logLines = List.of(
                "timestamp=2024-11-24T10:01:35Z metric=memory_usage_percent host=webserver2 value=31",
                "timestamp=2024-11-24T10:01:35Z metric=memory_usage_percent host=webserver2 value=45",
                "timestamp=2024-11-24T10:01:35Z metric=memory_usage_percent host=webserver2 value=75"
        );

        logLines.forEach(apmLogParser::processLogLine);

        ObjectNode aggregatedData = apmLogParser.getAggregatedData();
        JsonNode memoryUsage = aggregatedData.get("memory_usage_percent");
        assertEquals(31, memoryUsage.get("minimum").asInt());
        assertEquals(75, memoryUsage.get("max").asInt());
        assertEquals(50.33, memoryUsage.get("average").asDouble(), 0.01);
    }

    @Test
    void testEmptyAPMLogs() {
        APMLogParser apmLogParser = new APMLogParser();
        List<String> logLines = List.of();

        logLines.forEach(apmLogParser::processLogLine);

        ObjectNode aggregatedData = apmLogParser.getAggregatedData();
        assertEquals(0, aggregatedData.size()); // No data should be aggregated
    }

    @Test
    void testMixedAPMLogs() {
        APMLogParser apmLogParser = new APMLogParser();
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=67",
                "invalid data",
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=35"
        );

        logLines.forEach(apmLogParser::processLogLine);

        ObjectNode aggregatedData = apmLogParser.getAggregatedData();
        JsonNode cpuUsage = aggregatedData.get("cpu_usage_percent");
        assertEquals(35, cpuUsage.get("minimum").asInt());
        assertEquals(67, cpuUsage.get("max").asInt());
    }
}
