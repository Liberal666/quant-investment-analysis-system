package com.example.quant.controller;

import com.example.quant.entity.AiAnalysisResult;
import com.example.quant.entity.AiChatResult;
import com.example.quant.entity.CorrelationResult;
import com.example.quant.entity.StockIndicator;
import com.example.quant.entity.StockKline;
import com.example.quant.entity.StockProduct;
import com.example.quant.entity.StockQuote;
import com.example.quant.entity.SysUser;
import com.example.quant.service.AuthSessionService;
import com.example.quant.service.StockService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/stock")
public class StockController {
    private final StockService stockService;
    private final AuthSessionService authSessionService;

    public StockController(StockService stockService, AuthSessionService authSessionService) {
        this.stockService = stockService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/products")
    public List<StockProduct> products(@RequestHeader(value = "Authorization", required = false) String authorization) {
        SysUser user = authSessionService.requireUser(authorization);
        return stockService.getProducts(user.getUsername());
    }

    @PostMapping("/user-stock")
    public StockProduct addUserStock(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @RequestParam String code) {
        SysUser user = authSessionService.requireUser(authorization);
        return stockService.addUserStock(user.getUsername(), code);
    }

    @GetMapping("/kline")
    public List<StockKline> kline(@RequestParam String code,
                                  @RequestParam(defaultValue = "true") boolean sync) {
        return stockService.getKlines(code, sync);
    }

    @GetMapping("/indicator")
    public List<StockIndicator> indicator(@RequestParam String code,
                                          @RequestParam(defaultValue = "true") boolean sync) {
        return stockService.getIndicators(code, sync);
    }

    @GetMapping("/quote")
    public StockQuote quote(@RequestParam String code) {
        return stockService.getQuote(code);
    }

    @GetMapping("/correlation")
    public CorrelationResult correlation(@RequestParam String code,
                                         @RequestParam(defaultValue = "000300") String benchmarkCode,
                                         @RequestParam(defaultValue = "true") boolean sync) {
        return stockService.getCorrelation(code, benchmarkCode, sync);
    }

    @PostMapping("/sync")
    public Map<String, Object> sync(@RequestParam String code) {
        int count = stockService.sync(code);
        return Map.of("code", code, "count", count, "message", "sync completed");
    }

    @GetMapping("/analysis")
    public AiAnalysisResult analysis(@RequestParam String code,
                                     @RequestParam(defaultValue = "true") boolean sync) {
        return stockService.analyze(code, sync);
    }

    @PostMapping("/chat")
    public AiChatResult chat(@RequestBody AiChatRequest request) {
        return stockService.chat(request.code(), request.question(), request.context());
    }

    public record AiChatRequest(String code, String question, String context) {
    }
}
