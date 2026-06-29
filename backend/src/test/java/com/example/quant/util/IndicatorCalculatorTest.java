package com.example.quant.util;

import com.example.quant.entity.StockIndicator;
import com.example.quant.entity.StockKline;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndicatorCalculatorTest {
    @Test
    void calculatesMovingAveragesMacdAndRsi() {
        List<StockKline> klines = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            StockKline kline = new StockKline();
            kline.setCode("600519");
            kline.setDate(LocalDate.of(2024, 1, 1).plusDays(i));
            kline.setOpen(BigDecimal.valueOf(i));
            kline.setHigh(BigDecimal.valueOf(i + 1));
            kline.setLow(BigDecimal.valueOf(i - 1));
            kline.setClose(BigDecimal.valueOf(i));
            kline.setVolume(1000L + i);
            klines.add(kline);
        }

        List<StockIndicator> indicators = IndicatorCalculator.calculate("600519", klines);

        assertThat(indicators).hasSize(30);
        assertThat(indicators.get(3).getMa5()).isNull();
        assertThat(indicators.get(4).getMa5()).isEqualByComparingTo("3.0000");
        assertThat(indicators.get(9).getMa10()).isEqualByComparingTo("5.5000");
        assertThat(indicators.get(19).getMa20()).isEqualByComparingTo("10.5000");
        assertThat(indicators.get(29).getDif()).isPositive();
        assertThat(indicators.get(29).getMacd()).isNotNull();
        assertThat(indicators.get(14).getRsi()).isEqualByComparingTo("100.0000");
    }
}
