package com.example.quant.entity;

import java.time.LocalDate;

public record CorrelationPoint(LocalDate date, double stockReturn, double indexReturn) {
}
