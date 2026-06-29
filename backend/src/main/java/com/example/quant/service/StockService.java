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

    List<StockKline> getKlines(String code);

    List<StockIndicator> getIndicators(String code);

    StockQuote getQuote(String code);

    CorrelationResult getCorrelation(String code, String benchmarkCode);

    int sync(String code);

    AiAnalysisResult analyze(String code);

    AiChatResult chat(String code, String question, String context);
}
