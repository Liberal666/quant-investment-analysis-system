package com.example.quant.util;

import com.example.quant.entity.StockKline;
import com.example.quant.entity.StockQuote;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class SinaFinanceClient {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<StockKline> fetchDailyKlines(String code) {
        try {
            String symbol = toSinaSymbol(code);
            String encoded = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
            String url = "https://money.finance.sina.com.cn/quotes_service/api/json_v2.php/"
                    + "CN_MarketData.getKLineData?symbol=" + encoded + "&scale=240&ma=no&datalen=430";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body().isBlank()) {
                return List.of();
            }
            return fillLastYear(code, parseKlines(code, response.body()));
        } catch (Exception ex) {
            return List.of();
        }
    }

    public StockQuote fetchQuote(String code) {
        StockQuote sinaQuote = fetchSinaQuote(code);
        return sinaQuote != null ? sinaQuote : fetchTencentQuote(code);
    }

    private StockQuote fetchSinaQuote(String code) {
        try {
            String symbol = toSinaSymbol(code);
            String url = "https://hq.sinajs.cn/list=" + symbol;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .header("Referer", "https://finance.sina.com.cn")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200 || response.body().length == 0) {
                return null;
            }
            return parseQuote(code, new String(response.body(), Charset.forName("GBK")));
        } catch (Exception ex) {
            return null;
        }
    }

    private StockQuote fetchTencentQuote(String code) {
        try {
            String symbol = toSinaSymbol(code);
            String url = "https://qt.gtimg.cn/q=" + symbol;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200 || response.body().length == 0) {
                return null;
            }
            return parseTencentQuote(code, new String(response.body(), Charset.forName("GBK")));
        } catch (Exception ex) {
            return null;
        }
    }

    private List<StockKline> parseKlines(String code, String body) throws Exception {
        List<Map<String, String>> rows = objectMapper.readValue(body, new TypeReference<>() {
        });
        List<StockKline> klines = new ArrayList<>();
        for (Map<String, String> row : rows) {
            StockKline kline = new StockKline();
            kline.setCode(code);
            kline.setDate(LocalDate.parse(row.get("day")));
            kline.setOpen(new BigDecimal(row.get("open")));
            kline.setHigh(new BigDecimal(row.get("high")));
            kline.setLow(new BigDecimal(row.get("low")));
            kline.setClose(new BigDecimal(row.get("close")));
            kline.setVolume(Long.valueOf(row.get("volume")));
            klines.add(kline);
        }
        return klines;
    }

    private List<StockKline> fillLastYear(String code, List<StockKline> source) {
        if (source.isEmpty()) {
            return source;
        }
        List<StockKline> sorted = source.stream()
                .sorted(Comparator.comparing(StockKline::getDate))
                .toList();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(364);
        StockKline previous = sorted.get(0);
        List<StockKline> result = new ArrayList<>();
        int index = 0;
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            while (index < sorted.size() && !sorted.get(index).getDate().isAfter(day)) {
                previous = sorted.get(index);
                index++;
            }
            result.add(copyForDate(code, previous, day));
        }
        return result;
    }

    private StockKline copyForDate(String code, StockKline source, LocalDate date) {
        StockKline kline = new StockKline();
        kline.setCode(code);
        kline.setDate(date);
        kline.setOpen(source.getOpen());
        kline.setHigh(source.getHigh());
        kline.setLow(source.getLow());
        kline.setClose(source.getClose());
        kline.setVolume(source.getVolume());
        return kline;
    }

    private StockQuote parseQuote(String code, String body) {
        int firstQuote = body.indexOf('"');
        int lastQuote = body.lastIndexOf('"');
        if (firstQuote < 0 || lastQuote <= firstQuote) {
            return null;
        }
        String[] fields = body.substring(firstQuote + 1, lastQuote).split(",");
        if (fields.length < 32 || fields[0].isBlank()) {
            return null;
        }
        BigDecimal open = decimal(fields[1]);
        BigDecimal previousClose = decimal(fields[2]);
        BigDecimal price = decimal(fields[3]);
        BigDecimal high = decimal(fields[4]);
        BigDecimal low = decimal(fields[5]);
        long volume = parseLong(fields[8]);
        BigDecimal change = price.subtract(previousClose);
        BigDecimal changePercent = previousClose.signum() == 0
                ? BigDecimal.ZERO
                : change.multiply(BigDecimal.valueOf(100)).divide(previousClose, 4, java.math.RoundingMode.HALF_UP);
        String updateTime = fields[30] + " " + fields[31];
        if (price.signum() == 0) {
            price = open.signum() == 0 ? previousClose : open;
        }
        return new StockQuote(code, fields[0], price, volume, change, changePercent, high, low, previousClose, updateTime);
    }

    private StockQuote parseTencentQuote(String code, String body) {
        int firstQuote = body.indexOf('"');
        int lastQuote = body.lastIndexOf('"');
        if (firstQuote < 0 || lastQuote <= firstQuote) {
            return null;
        }
        String[] fields = body.substring(firstQuote + 1, lastQuote).split("~");
        if (fields.length < 35 || fields[1].isBlank()) {
            return null;
        }
        BigDecimal price = decimal(fields[3]);
        BigDecimal previousClose = decimal(fields[4]);
        BigDecimal high = decimal(fields[33]);
        BigDecimal low = decimal(fields[34]);
        BigDecimal change = decimal(fields[31]);
        BigDecimal changePercent = decimal(fields[32]);
        long volume = parseLong(fields[6]) * 100;
        String updateTime = fields[30];
        return new StockQuote(code, fields[1], price, volume, change, changePercent, high, low, previousClose, updateTime);
    }

    private static BigDecimal decimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return 0L;
        }
    }

    private String toSinaSymbol(String code) {
        if (code.startsWith("sh") || code.startsWith("sz") || code.startsWith("bj")) {
            return code;
        }
        if (code.startsWith("6") || code.startsWith("9") || code.equals("000300")) {
            return "sh" + code;
        }
        if (code.startsWith("8")) {
            return "bj" + code;
        }
        return "sz" + code;
    }
}
