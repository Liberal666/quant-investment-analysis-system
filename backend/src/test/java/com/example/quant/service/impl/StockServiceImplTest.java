package com.example.quant.service.impl;

import com.example.quant.entity.StockKline;
import com.example.quant.mapper.StockIndicatorMapper;
import com.example.quant.mapper.StockKlineMapper;
import com.example.quant.mapper.UserStockMapper;
import com.example.quant.util.SinaFinanceClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class StockServiceImplTest {
    @Test
    void syncReplacesOldKlinesBeforeSavingLatestRows() {
        StockKlineMapper klineMapper = mock(StockKlineMapper.class);
        StockIndicatorMapper indicatorMapper = mock(StockIndicatorMapper.class);
        StockKline kline = kline("000001", LocalDate.of(2026, 7, 1));
        SinaFinanceClient sinaFinanceClient = new SinaFinanceClient() {
            @Override
            public List<StockKline> fetchDailyKlines(String code) {
                return List.of(kline);
            }
        };
        StockServiceImpl service = new StockServiceImpl(
                klineMapper,
                indicatorMapper,
                mock(UserStockMapper.class),
                sinaFinanceClient,
                null
        );

        service.sync("000001");

        var ordered = inOrder(klineMapper, indicatorMapper);
        ordered.verify(indicatorMapper).deleteByCode("000001");
        ordered.verify(klineMapper).deleteByCode("000001");
        ordered.verify(klineMapper).upsert(kline);
        ordered.verify(indicatorMapper).upsert(any());
    }

    private static StockKline kline(String code, LocalDate date) {
        StockKline kline = new StockKline();
        kline.setCode(code);
        kline.setDate(date);
        kline.setOpen(BigDecimal.TEN);
        kline.setHigh(BigDecimal.TEN);
        kline.setLow(BigDecimal.TEN);
        kline.setClose(BigDecimal.TEN);
        kline.setVolume(1000L);
        return kline;
    }
}
