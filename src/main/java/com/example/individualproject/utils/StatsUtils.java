package com.example.individualproject.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.List;

public class StatsUtils {

    public static ObjectNode calculateStats(List<Double> sortedValues, ObjectMapper mapper) {
        ObjectNode stats = mapper.createObjectNode();

        // Convert list to array for Apache Commons Percentile
        double[] valuesArray = sortedValues.stream().mapToDouble(Double::doubleValue).toArray();

        // Use Percentile from Apache Commons Math
        Percentile percentile = new Percentile();

        stats.put("minimum", (int) castToIntegerIfPossible(valuesArray[0]));
        stats.put("median", roundToDouble(percentile.evaluate(valuesArray, 50)));
        stats.put("average", roundToDouble(sortedValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)));
        stats.put("max", (int) castToIntegerIfPossible(valuesArray[valuesArray.length - 1]));

        return stats;
    }

    public static double castToIntegerIfPossible(double value) {
        // If the value is effectively an integer, return it as an integer, otherwise keep it as a double
        return (value % 1 == 0) ? (int) value : value;
    }

    public static double roundToDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
