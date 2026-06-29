package com.example.quant.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockProduct(String code,
                           String name,
                           LocalDate latestDate,
                           BigDecimal latestClose,
                           BigDecimal previousClose,
                           BigDecimal weekClose,
                           BigDecimal firstClose,
                           long rows) {
}
