package com.example.individualproject.service;

import com.example.individualproject.utils.PercentileCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class RequestLogParser extends LogParser {

    private final Map<String, List<Integer>> responseTimes = new HashMap<>();
    private final Map<String, Map<String, Integer>> statusCounts = new HashMap<>();

    @Override
    protected boolean canHandleLog(String logLine) {
        return logLine.contains("request_url="); // Specific to Request logs
    }
    @Override
    public void parse(String logLine) {
        if (logLine.contains("request_url=")) {
            String[] values = logLine.split(" ");
            String apiUrl = null;
            Integer responseTimeValue = null;
            String httpStatusCode = null;

            for (String value : values) {
                if (value.startsWith("request_url=")) {
                    apiUrl = value.split("=")[1];
                } else if (value.startsWith("response_time_ms=")) {
                    try {
                        responseTimeValue = Integer.parseInt(value.split("=")[1]);
                    } catch (NumberFormatException e) {
                        // Invalid response time, skip this log
                        responseTimeValue = null;
                    }
                } else if (value.startsWith("response_status=")) {
                    httpStatusCode = value.split("=")[1];
                }
            }

            // Validate that all required fields are present
            if (apiUrl == null || responseTimeValue == null || httpStatusCode == null) {
                return; // Skip invalid log
            }

            // Add response time to the list
            responseTimes.computeIfAbsent(apiUrl, k -> new ArrayList<>()).add(responseTimeValue);

            // Count status codes by category (e.g., 2xx, 4xx, 5xx)
            String category = httpStatusCode.charAt(0) + "xx";
            statusCounts.putIfAbsent(apiUrl, new HashMap<>());
            statusCounts.get(apiUrl).put(category, statusCounts.get(apiUrl).getOrDefault(category, 0) + 1);
        }
    }

    @Override
    public ObjectNode getAggregatedData() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        // TreeMap for storing keys in alphabetical order
        Map<String, List<Integer>> sortedResponseTimeMap = new TreeMap<>(responseTimes);

        for (String urlKey : sortedResponseTimeMap.keySet()) {
            List<Integer> timesValue = sortedResponseTimeMap.get(urlKey);
            timesValue.sort(Integer::compareTo);

            //remove unnecessary quotes
            String updatedUrl = urlKey.replaceAll("^\"|\"$", ""); // Remove leading and trailing quotes

            // Response time stats
            ObjectNode responseStatsNode = mapper.createObjectNode();
            responseStatsNode.put("min", timesValue.get(0));
            responseStatsNode.put("50_percentile", roundToOneDecimal(PercentileCalculator.calculatePercentile(asDoubleList(timesValue), 50)));
            responseStatsNode.put("90_percentile", roundToOneDecimal(PercentileCalculator.calculatePercentile(asDoubleList(timesValue), 90)));
            responseStatsNode.put("95_percentile", roundToOneDecimal(PercentileCalculator.calculatePercentile(asDoubleList(timesValue), 95)));
            responseStatsNode.put("99_percentile", roundToOneDecimal(PercentileCalculator.calculatePercentile(asDoubleList(timesValue), 99)));
            responseStatsNode.put("max", timesValue.get(timesValue.size() - 1));

            // Status codes stats
            ObjectNode statusStatsNode = mapper.createObjectNode();
            statusStatsNode.put("2XX", statusCounts.getOrDefault(urlKey, new HashMap<>()).getOrDefault("2xx", 0));
            statusStatsNode.put("4XX", statusCounts.getOrDefault(urlKey, new HashMap<>()).getOrDefault("4xx", 0));
            statusStatsNode.put("5XX", statusCounts.getOrDefault(urlKey, new HashMap<>()).getOrDefault("5xx", 0));

            // Combine into one object
            ObjectNode apiStatsNode = mapper.createObjectNode();
            apiStatsNode.set("response_times", responseStatsNode);
            apiStatsNode.set("status_codes", statusStatsNode);

            result.set(updatedUrl, apiStatsNode); // Add sanitized key
        }

        return result;
    }

    private List<Double> asDoubleList(List<Integer> intList) {
        return intList.stream().map(Integer::doubleValue).toList();
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


    @Override
    public String getOutputFileName() {
        return "request.json";
    }
}
