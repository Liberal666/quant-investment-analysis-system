package com.example.quant.util;

import com.example.quant.entity.StockIndicator;
import com.example.quant.entity.StockKline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class IndicatorCalculator {
    private IndicatorCalculator() {
    }

    public static List<StockIndicator> calculate(String code, List<StockKline> klines) {
        List<StockIndicator> indicators = new ArrayList<>();
        double ema12 = 0;
        double ema26 = 0;
        double dea = 0;
        double avgGain = 0;
        double avgLoss = 0;

        for (int i = 0; i < klines.size(); i++) {
            StockKline kline = klines.get(i);
            double close = kline.getClose().doubleValue();
            if (i == 0) {
                ema12 = close;
                ema26 = close;
            } else {
                ema12 = ema(close, ema12, 12);
                ema26 = ema(close, ema26, 26);
            }
            double dif = ema12 - ema26;
            dea = i == 0 ? dif : ema(dif, dea, 9);
            double macd = (dif - dea) * 2;

            StockIndicator indicator = new StockIndicator();
            indicator.setCode(code);
            indicator.setDate(kline.getDate());
            indicator.setMa5(movingAverage(klines, i, 5));
            indicator.setMa10(movingAverage(klines, i, 10));
            indicator.setMa20(movingAverage(klines, i, 20));
            indicator.setDif(decimal(dif));
            indicator.setDea(decimal(dea));
            indicator.setMacd(decimal(macd));

            if (i > 0) {
                double change = close - klines.get(i - 1).getClose().doubleValue();
                double gain = Math.max(change, 0);
                double loss = Math.max(-change, 0);
                if (i <= 14) {
                    avgGain += gain;
                    avgLoss += loss;
                    if (i == 14) {
                        avgGain /= 14;
                        avgLoss /= 14;
                        indicator.setRsi(rsi(avgGain, avgLoss));
                    }
                } else {
                    avgGain = (avgGain * 13 + gain) / 14;
                    avgLoss = (avgLoss * 13 + loss) / 14;
                    indicator.setRsi(rsi(avgGain, avgLoss));
                }
            }
            indicators.add(indicator);
        }
        return indicators;
    }

    private static double ema(double value, double previous, int period) {
        return value * 2 / (period + 1) + previous * (period - 1) / (period + 1);
    }

    private static BigDecimal movingAverage(List<StockKline> klines, int index, int period) {
        if (index + 1 < period) {
            return null;
        }
        double sum = 0;
        for (int i = index - period + 1; i <= index; i++) {
            sum += klines.get(i).getClose().doubleValue();
        }
        return decimal(sum / period);
    }

    private static BigDecimal rsi(double avgGain, double avgLoss) {
        if (avgLoss == 0) {
            return decimal(100);
        }
        return decimal(100 - 100 / (1 + avgGain / avgLoss));
    }

    private static BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
