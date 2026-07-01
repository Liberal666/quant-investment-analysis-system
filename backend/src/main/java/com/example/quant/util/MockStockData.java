package com.example.quant.util;

import com.example.quant.entity.StockKline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class MockStockData {
    private MockStockData() {
    }

    public static List<StockKline> klines(String code) {
        return generate(code, 168.0, 0.006, 0.018);
    }

    public static List<StockKline> benchmarkKlines(String code) {
        return switch (code) {
            case "399001" -> generate(code, 15800.0, 0.0022, 0.010);
            case "399006" -> generate(code, 4200.0, 0.0026, 0.014);
            default -> generate(code, 3900.0, 0.002, 0.009);
        };
    }

    private static List<StockKline> generate(String code, double start, double trend, double wave) {
        List<StockKline> result = new ArrayList<>();
        LocalDate date = LocalDate.now().minusDays(119);
        double close = start;
        int tradingDay = 0;
        while (result.size() < 90) {
            if (date.getDayOfWeek().getValue() <= 5) {
                double factor = Math.sin(tradingDay / 5.0) * wave + trend + Math.cos(tradingDay / 11.0) * 0.004;
                double open = close * (1 + Math.sin(tradingDay) * 0.003);
                close = close * (1 + factor);
                double high = Math.max(open, close) * 1.012;
                double low = Math.min(open, close) * 0.988;
                StockKline kline = new StockKline();
                kline.setCode(code);
                kline.setDate(date);
                kline.setOpen(decimal(open));
                kline.setHigh(decimal(high));
                kline.setLow(decimal(low));
                kline.setClose(decimal(close));
                kline.setVolume(1_000_000L + tradingDay * 23_000L);
                result.add(kline);
                tradingDay++;
            }
            date = date.plusDays(1);
        }
        return result;
    }

    private static BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
