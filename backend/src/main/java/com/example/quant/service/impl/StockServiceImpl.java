package com.example.quant.service.impl;

import com.example.quant.entity.AiAnalysisResult;
import com.example.quant.entity.AiChatResult;
import com.example.quant.entity.CorrelationPoint;
import com.example.quant.entity.CorrelationResult;
import com.example.quant.entity.StockIndicator;
import com.example.quant.entity.StockKline;
import com.example.quant.entity.StockProduct;
import com.example.quant.entity.StockQuote;
import com.example.quant.mapper.StockIndicatorMapper;
import com.example.quant.mapper.StockKlineMapper;
import com.example.quant.mapper.UserStockMapper;
import com.example.quant.service.StockService;
import com.example.quant.util.CorrelationCalculator;
import com.example.quant.util.DeepSeekClient;
import com.example.quant.util.IndicatorCalculator;
import com.example.quant.util.MockStockData;
import com.example.quant.util.SinaFinanceClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class StockServiceImpl implements StockService {
    private static final String BENCHMARK_CODE = "000300";

    private final StockKlineMapper klineMapper;
    private final StockIndicatorMapper indicatorMapper;
    private final UserStockMapper userStockMapper;
    private final SinaFinanceClient sinaFinanceClient;
    private final DeepSeekClient deepSeekClient;

    public StockServiceImpl(StockKlineMapper klineMapper,
                            StockIndicatorMapper indicatorMapper,
                            UserStockMapper userStockMapper,
                            SinaFinanceClient sinaFinanceClient,
                            DeepSeekClient deepSeekClient) {
        this.klineMapper = klineMapper;
        this.indicatorMapper = indicatorMapper;
        this.userStockMapper = userStockMapper;
        this.sinaFinanceClient = sinaFinanceClient;
        this.deepSeekClient = deepSeekClient;
    }

    @Override
    public List<StockProduct> getProducts(String username) {
        List<String> codes = userStockMapper.findCodesByUsername(username);
        if (codes.isEmpty()) {
            codes = klineMapper.findCodes().stream()
                    .filter(code -> !BENCHMARK_CODE.equals(code))
                    .toList();
        }
        return codes.stream()
                .map(this::buildProduct)
                .toList();
    }

    @Override
    public StockProduct addUserStock(String username, String code) {
        String normalizedCode = normalizeCode(code);
        sync(normalizedCode);
        userStockMapper.upsert(username, normalizedCode);
        return buildProduct(normalizedCode);
    }

    @Override
    public List<StockKline> getKlines(String code) {
        return getKlines(code, true);
    }

    @Override
    public List<StockKline> getKlines(String code, boolean autoSync) {
        if (autoSync) {
            syncIfStale(code);
        }
        List<StockKline> klines = klineMapper.findByCode(code);
        if (autoSync && klines.isEmpty()) {
            sync(code);
            klines = klineMapper.findByCode(code);
        }
        return klines.isEmpty() ? MockStockData.klines(code) : klines;
    }

    @Override
    public List<StockIndicator> getIndicators(String code) {
        return getIndicators(code, true);
    }

    @Override
    public List<StockIndicator> getIndicators(String code, boolean autoSync) {
        if (autoSync) {
            syncIfStale(code);
        }
        List<StockIndicator> indicators = indicatorMapper.findByCode(code);
        if (autoSync && indicators.isEmpty()) {
            sync(code);
            indicators = indicatorMapper.findByCode(code);
        }
        return indicators.isEmpty() ? IndicatorCalculator.calculate(code, MockStockData.klines(code)) : indicators;
    }

    @Override
    public StockQuote getQuote(String code) {
        String normalizedCode = normalizeCode(code);
        StockQuote quote = sinaFinanceClient.fetchQuote(normalizedCode);
        return quote != null ? quote : fallbackQuote(normalizedCode);
    }

    @Override
    public CorrelationResult getCorrelation(String code, String benchmarkCode) {
        return getCorrelation(code, benchmarkCode, true);
    }

    @Override
    public CorrelationResult getCorrelation(String code, String benchmarkCode, boolean autoSync) {
        List<StockKline> stock = getKlines(code, autoSync);
        String normalizedBenchmark = normalizeCode(benchmarkCode);
        List<StockKline> benchmark = loadBenchmark(normalizedBenchmark, autoSync);
        int size = Math.min(stock.size(), benchmark.size());
        List<Double> stockReturns = new ArrayList<>();
        List<Double> indexReturns = new ArrayList<>();
        List<CorrelationPoint> points = new ArrayList<>();

        for (int i = 1; i < size; i++) {
            double stockReturn = returnRate(stock.get(i - 1), stock.get(i));
            double indexReturn = returnRate(benchmark.get(i - 1), benchmark.get(i));
            stockReturns.add(stockReturn);
            indexReturns.add(indexReturn);
            points.add(new CorrelationPoint(stock.get(i).getDate(), stockReturn, indexReturn));
        }
        double coefficient = CorrelationCalculator.pearson(stockReturns, indexReturns);
        return new CorrelationResult(code, coefficient, benchmarkName(normalizedBenchmark), points);
    }

    @Override
    public int sync(String code) {
        List<StockKline> klines = sinaFinanceClient.fetchDailyKlines(code);
        if (klines.isEmpty()) {
            klines = MockStockData.klines(code);
        }
        indicatorMapper.deleteByCode(code);
        klineMapper.deleteByCode(code);
        for (StockKline kline : klines) {
            klineMapper.upsert(kline);
        }
        List<StockIndicator> indicators = IndicatorCalculator.calculate(code, klines);
        for (StockIndicator indicator : indicators) {
            indicatorMapper.upsert(indicator);
        }
        return klines.size();
    }

    @Override
    public AiAnalysisResult analyze(String code) {
        return analyze(code, true);
    }

    @Override
    public AiAnalysisResult analyze(String code, boolean autoSync) {
        List<StockKline> klines = getKlines(code, autoSync);
        CorrelationResult correlation = getCorrelation(code, BENCHMARK_CODE, autoSync);
        StockKline first = klines.get(0);
        StockKline last = klines.get(klines.size() - 1);
        double totalReturn = returnRate(first, last);
        return deepSeekClient.analyze(code, totalReturn, correlation.coefficient());
    }

    @Override
    public AiChatResult chat(String code, String question, String context) {
        if (question == null || question.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "请输入对话内容");
        }
        StringBuilder prompt = new StringBuilder();
        if (code != null && code.matches("\\d{6}")) {
            prompt.append("股票代码：").append(code).append('\n');
        }
        if (context != null && !context.isBlank()) {
            prompt.append("页面参考信息：").append(context.trim()).append('\n');
        }
        prompt.append("用户问题：").append(question.trim());
        return new AiChatResult("deepseek", deepSeekClient.chat(prompt.toString()));
    }

    private List<StockKline> loadBenchmark(String benchmarkCode, boolean autoSync) {
        if (autoSync) {
            syncIfStale(benchmarkCode);
        }
        List<StockKline> benchmark = klineMapper.findByCode(benchmarkCode);
        if (!benchmark.isEmpty()) {
            return benchmark;
        }
        if (autoSync) {
            benchmark = sinaFinanceClient.fetchDailyKlines(benchmarkCode);
            return benchmark.isEmpty() ? MockStockData.benchmarkKlines(benchmarkCode) : benchmark;
        }
        return MockStockData.benchmarkKlines(benchmarkCode);
    }

    private StockProduct buildProduct(String code) {
        List<StockKline> klines = getKlines(code);
        int lastIndex = klines.size() - 1;
        StockKline latest = klines.get(lastIndex);
        StockKline previous = klines.get(Math.max(lastIndex - 1, 0));
        StockKline week = klines.get(Math.max(lastIndex - 5, 0));
        StockKline first = klines.get(0);
        return new StockProduct(
                code,
                productNameForProduct(code),
                latest.getDate(),
                latest.getClose(),
                previous.getClose(),
                week.getClose(),
                first.getClose(),
                klines.size());
    }

    private StockQuote fallbackQuote(String code) {
        List<StockKline> klines = getKlines(code);
        StockKline latest = klines.get(klines.size() - 1);
        StockKline previous = klines.get(Math.max(klines.size() - 2, 0));
        BigDecimal previousClose = previous.getClose();
        BigDecimal change = latest.getClose().subtract(previousClose);
        BigDecimal changePercent = previousClose.signum() == 0
                ? BigDecimal.ZERO
                : change.multiply(BigDecimal.valueOf(100)).divide(previousClose, 4, RoundingMode.HALF_UP);
        return new StockQuote(
                code,
                productName(code),
                latest.getClose(),
                latest.getVolume(),
                change,
                changePercent,
                latest.getHigh(),
                latest.getLow(),
                previousClose,
                latest.getDate().toString());
    }

    private static String productName(String code) {
        return switch (code) {
            case "600519" -> "贵州茅台";
            case "000001" -> "平安银行";
            case "000858" -> "五粮液";
            case "601318" -> "中国平安";
            case "000300" -> "沪深300";
            case "399001" -> "深证成指";
            case "399006" -> "创业板指";
            default -> "股票";
        };
    }

    private String productNameForProduct(String code) {
        String knownName = productName(code);
        if (!"股票".equals(knownName)) {
            return knownName;
        }
        StockQuote quote = sinaFinanceClient.fetchQuote(code);
        if (quote != null && quote.name() != null && !quote.name().isBlank()) {
            return quote.name();
        }
        return knownName;
    }

    private static String benchmarkName(String code) {
        return switch (code) {
            case "000300" -> "沪深300";
            case "399001" -> "深证成指";
            case "399006" -> "创业板指";
            default -> "基准 " + code;
        };
    }

    private static String normalizeCode(String code) {
        if (code == null || !code.matches("\\d{6}")) {
            throw new ResponseStatusException(BAD_REQUEST, "股票代码必须是6位数字");
        }
        return code;
    }

    private void syncIfStale(String code) {
        LocalDate latestDate = klineMapper.findLatestDate(code);
        if (latestDate == null || latestDate.isBefore(latestTradingDate())) {
            sync(code);
        }
    }

    private static LocalDate latestTradingDate() {
        LocalDate today = LocalDate.now();
        DayOfWeek day = today.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return today.minusDays(1);
        }
        if (day == DayOfWeek.SUNDAY) {
            return today.minusDays(2);
        }
        return today;
    }

    private static double returnRate(StockKline previous, StockKline current) {
        double before = previous.getClose().doubleValue();
        if (before == 0) {
            return 0;
        }
        return (current.getClose().doubleValue() - before) / before;
    }
}
