package com.example.quant.service;

import com.example.quant.entity.AiAnalysisResult;
import com.example.quant.entity.AiChatResult;
import com.example.quant.entity.CorrelationResult;
import com.example.quant.entity.StockIndicator;
import com.example.quant.entity.StockKline;
import com.example.quant.entity.StockProduct;
import com.example.quant.entity.StockQuote;

import java.util.List;

public interface StockService {
    List<StockProduct> getProducts(String username);

    StockProduct addUserStock(String username, String code);

    void removeUserStock(String username, String code);

    List<StockKline> getKlines(String code);

    List<StockKline> getKlines(String code, boolean autoSync);

    List<StockIndicator> getIndicators(String code);

    List<StockIndicator> getIndicators(String code, boolean autoSync);

    StockQuote getQuote(String code);

    CorrelationResult getCorrelation(String code, String benchmarkCode);

    CorrelationResult getCorrelation(String code, String benchmarkCode, boolean autoSync);

    int sync(String code);

    AiAnalysisResult analyze(String code);

    AiAnalysisResult analyze(String code, boolean autoSync);

    AiChatResult chat(String code, String question, String context);
}
