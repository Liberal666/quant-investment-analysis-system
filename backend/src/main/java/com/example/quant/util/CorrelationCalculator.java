package com.example.quant.util;

import java.util.List;

public final class CorrelationCalculator {
    private CorrelationCalculator() {
    }

    public static double pearson(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.size() < 2) {
            return 0;
        }
        double avgX = average(x);
        double avgY = average(y);
        double numerator = 0;
        double sumX = 0;
        double sumY = 0;
        for (int i = 0; i < x.size(); i++) {
            double dx = x.get(i) - avgX;
            double dy = y.get(i) - avgY;
            numerator += dx * dy;
            sumX += dx * dx;
            sumY += dy * dy;
        }
        double denominator = Math.sqrt(sumX * sumY);
        return denominator == 0 ? 0 : numerator / denominator;
    }

    private static double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
}
