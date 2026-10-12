
package com.itheima.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class AiServiceImpl implements AiService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String chat(String message) {

        try {
            // 1. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 2. 构造请求参数
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "你是一个专业、友好的AI助手。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", message
                            )
                    ),
                    "stream", false
            );

            // 3. 封装 HTTP 请求
            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(requestBody, headers);

            // 4. 调用 DeepSeek API
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            baseUrl + "/chat/completions",
                            requestEntity,
                            String.class
                    );

            // 5. 解析响应 JSON
            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new RuntimeException("DeepSeek 返回内容为空");
            }

            JsonNode root = objectMapper.readTree(responseBody);

            // 6. 获取 AI 回答
            JsonNode content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (content.isMissingNode() || content.isNull()) {
                throw new RuntimeException("DeepSeek 未返回有效回答");
            }

            return content.asText();

        } catch (RestClientResponseException e) {
            // HTTP 401、402、429、500 等错误
            throw new RuntimeException(
                    "DeepSeek API 调用失败，HTTP 状态码："
                            + e.getStatusCode().value(),
                    e
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "调用 DeepSeek 失败：" + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public void streamChat(String message, Consumer<String> onChunk) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "你是一个专业、友好的AI助手。"
                        ),
                        Map.of(
                                "role", "user",
                                "content", message
                        )
                ),
                // 关键：开启 DeepSeek 流式输出
                "stream", true
        );

        restTemplate.execute(
                baseUrl + "/chat/completions",
                HttpMethod.POST,
                request -> {
                    request.getHeaders().putAll(headers);
                    objectMapper.writeValue(request.getBody(), requestBody);
                },
                (ClientHttpResponse response) -> {

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new RuntimeException(
                                "DeepSeek 请求失败，HTTP 状态码："
                                        + response.getStatusCode().value()
                        );
                    }

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    response.getBody(),
                                    StandardCharsets.UTF_8
                            ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {

                            // SSE 数据行格式：data: {...}
                            if (!line.startsWith("data:")) {
                                continue;
                            }

                            String data = line.substring(5).trim();

                            // 流结束标记
                            if ("[DONE]".equals(data)) {
                                break;
                            }

                            if (data.isEmpty()) {
                                continue;
                            }

                            JsonNode root = objectMapper.readTree(data);

                            // 流式内容在 delta.content，而非 message.content
                            JsonNode content = root.path("choices")
                                    .path(0)
                                    .path("delta")
                                    .path("content");

                            if (!content.isMissingNode() && !content.isNull()) {
                                String chunk = content.asText();

                                if (!chunk.isEmpty()) {
                                    onChunk.accept(chunk);
                                }
                            }
                        }
                    }

                    return null;
                }
        );
    }
}
