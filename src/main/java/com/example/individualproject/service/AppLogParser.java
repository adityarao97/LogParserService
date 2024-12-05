package com.example.individualproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppLogParser extends LogParser {

    private final Map<String, Integer> severityCounts = new HashMap<>();

    @Override
    protected boolean canHandleLog(String logLine) {
        return logLine.contains("level="); // Specific to Application logs
    }

    @Override
    public void parse(String logLine) {
        if (logLine.contains("level=") && logLine.split("level=").length > 1) {
            // Extract the severity level from the log
            String level = logLine.split("level=")[1].split(" ")[0];
            severityCounts.put(level, severityCounts.getOrDefault(level, 0) + 1);
        }
    }

    @Override
    public ObjectNode getAggregatedData() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        List<String> desiredOrder = List.of("INFO", "DEBUG", "ERROR", "WARNING", "TRACE");

        for (String severity : desiredOrder) {
            int count = severityCounts.getOrDefault(severity, 0);
            result.put(severity, count);
        }

        return result;
    }


    @Override
    public String getOutputFileName() {
        return "application.json";
    }
}
