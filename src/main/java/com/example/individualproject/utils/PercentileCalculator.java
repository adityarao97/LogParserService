package com.example.individualproject.utils;

import java.util.List;

public class PercentileCalculator {

    public static double calculatePercentile(List<Double> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) {
            throw new IllegalArgumentException("List is empty, cannot calculate percentile.");
        }

        // Calculate the index
        double index = (percentile / 100.0) * (sortedValues.size() - 1);

        if (index < 0) {
            return sortedValues.get(0); // Handle cases where percentile < 1
        }

        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);

        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }

        // Interpolate between the two nearest values
        double lower = sortedValues.get(lowerIndex);
        double upper = sortedValues.get(upperIndex);
        return lower + (upper - lower) * (index - lowerIndex);
    }

}
