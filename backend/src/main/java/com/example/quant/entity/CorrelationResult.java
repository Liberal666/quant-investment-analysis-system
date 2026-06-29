package com.example.quant.entity;

import java.util.List;

public record CorrelationResult(String code, double coefficient, String benchmark, List<CorrelationPoint> points) {
}
