package com.example.quant.entity;

import java.math.BigDecimal;

public record StockQuote(String code,
                         String name,
                         BigDecimal price,
                         Long volume,
                         BigDecimal change,
                         BigDecimal changePercent,
                         BigDecimal high,
                         BigDecimal low,
                         BigDecimal previousClose,
                         String updateTime) {
}
