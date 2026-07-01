package com.example.quant.util;

import com.example.quant.entity.AiAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public DeepSeekClient(@Value("${deepseek.api-key}") String apiKey,
                          @Value("${deepseek.base-url}") String baseUrl,
                          @Value("${deepseek.model}") String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public AiAnalysisResult analyze(String code, double totalReturn, double correlation) {
        String prompt = "请用课程设计答辩风格分析股票 " + code
                + "。区间收益率=" + formatPercent(totalReturn)
                + "，与沪深300 Pearson相关系数=" + String.format("%.4f", correlation)
                + "。请输出风险解释和投资解读，控制在120字以内。";
        if (apiKey == null || apiKey.isBlank()) {
            return new AiAnalysisResult(code, "local", localAnalysis(totalReturn, correlation));
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.3
            );
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").path(0).path("message").path("content").asText();
                if (!content.isBlank()) {
                    return new AiAnalysisResult(code, "deepseek", content);
                }
            }
        } catch (Exception ignored) {
        }
        return new AiAnalysisResult(code, "local", localAnalysis(totalReturn, correlation));
    }

    public String chat(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "DeepSeek API Key 未配置，当前只能展示本地参考建议。";
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是量化投资绩效分析系统中的通用AI助手。可以回答金融、技术、项目部署、课程设计和日常问题；如果用户提供股票行情或指标，可结合这些信息分析。不要承诺收益。"),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.4
            );
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").path(0).path("message").path("content").asText();
                if (!content.isBlank()) {
                    return content;
                }
            }
        } catch (Exception ignored) {
        }
        return "DeepSeek 暂时没有返回有效内容，请稍后重试，或结合K线、MACD和成交量先做人工判断。";
    }

    private String localAnalysis(double totalReturn, double correlation) {
        String direction = totalReturn >= 0 ? "区间收益为正，趋势表现偏强" : "区间收益为负，需关注回撤压力";
        String risk = Math.abs(correlation) > 0.7
                ? "与沪深300联动较强，系统性风险暴露较高"
                : "与沪深300联动有限，个股因素影响更明显";
        return direction + "；" + risk + "。建议结合成交量、均线和MACD确认趋势，不宜只依赖单一指标。";
    }

    private String formatPercent(double value) {
        return String.format("%.2f%%", value * 100);
    }
}
