package com.example.quant.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationCalculatorTest {
    @Test
    void calculatesPearsonCorrelation() {
        double positive = CorrelationCalculator.pearson(List.of(1.0, 2.0, 3.0), List.of(2.0, 4.0, 6.0));
        double negative = CorrelationCalculator.pearson(List.of(1.0, 2.0, 3.0), List.of(6.0, 4.0, 2.0));

        assertThat(positive).isCloseTo(1.0, within(0.0001));
        assertThat(negative).isCloseTo(-1.0, within(0.0001));
    }

    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
