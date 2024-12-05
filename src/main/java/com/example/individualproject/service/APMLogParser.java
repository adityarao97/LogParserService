package com.example.individualproject.service;

import com.example.individualproject.utils.StatsUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class APMLogParser extends LogParser {

    private final Map<String, List<Double>> metrics = new HashMap<>();

    @Override
    protected boolean canHandleLog(String logLine) {
        return logLine.contains("metric="); // Specific to APM logs
    }

    @Override
    public void parse(String logLine) {
        if (logLine.contains("metric=")) {
            String[] values = logLine.split(" ");
            String metricValue = null;
            Double valueDouble = null;

            for (String value : values) {
                if (value.startsWith("metric=")) {
                    metricValue = value.split("=")[1];
                } else if (value.startsWith("value=")) {
                    try {
                        valueDouble = Double.parseDouble(value.split("=")[1]);
                    } catch (NumberFormatException ignored) {
                        // Ignore invalid values
                    }
                }
            }

            if (metricValue != null && valueDouble != null) {
                metrics.computeIfAbsent(metricValue, k -> new java.util.ArrayList<>()).add(valueDouble);
            }
        }
    }

    @Override
    public ObjectNode getAggregatedData() {
        ObjectMapper mapper = new ObjectMapper(); // Declare the ObjectMapper

        // TreeMap for storing keys in alphabetical order
        Map<String, ObjectNode> sortedMetricsMapData = new TreeMap<>();

        for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
            String metric = entry.getKey();
            List<Double> values = entry.getValue();
            values.sort(Double::compareTo);

            ObjectNode stats = mapper.createObjectNode();
            double[] valuesArray = values.stream().mapToDouble(Double::doubleValue).toArray();
            Percentile percentile = new Percentile();

            stats.put("minimum", (int) StatsUtils.castToIntegerIfPossible(valuesArray[0]));
            stats.put("median", StatsUtils.roundToDouble(percentile.evaluate(valuesArray, 50)));
            stats.put("average", StatsUtils.roundToDouble(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)));
            stats.put("max", (int) StatsUtils.castToIntegerIfPossible(valuesArray[valuesArray.length - 1]));

            sortedMetricsMapData.put(metric, stats);
        }

        ObjectNode result = mapper.createObjectNode();
        sortedMetricsMapData.forEach(result::set);
        return result;
    }


    @Override
    public String getOutputFileName() {
        return "apm.json";
    }
}
