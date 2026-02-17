package com.milind.docintel.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class GeminiChatClient implements ChatClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final Duration timeout;
    private final double temperature;
    private final int maxOutputTokens;

    public GeminiChatClient(ObjectMapper objectMapper,
                            String apiKey,
                            String baseUrl,
                            String model,
                            int timeoutSeconds,
                            double temperature,
                            int maxOutputTokens) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = model;
        this.timeout = Duration.ofSeconds(Math.max(5, timeoutSeconds));
        this.temperature = temperature;
        this.maxOutputTokens = maxOutputTokens;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(this.timeout)
            .build();
    }

    @Override
    public String complete(String prompt) {
        String body = buildRequestBody(prompt);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/models/" + model + ":generateContent"))
            .timeout(timeout)
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gemini API error: HTTP " + response.statusCode() + " - " + response.body());
            }
            return extractText(response.body());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini API request failed", ex);
        }
    }

    private String buildRequestBody(String prompt) {
        String safePrompt = prompt == null ? "" : prompt;
        Map<String, Object> generationConfig = Map.of(
            "temperature", temperature,
            "maxOutputTokens", maxOutputTokens
        );
        Map<String, Object> content = Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", safePrompt))
        );
        Map<String, Object> request = Map.of(
            "contents", List.of(content),
            "generationConfig", generationConfig
        );
        try {
            return objectMapper.writeValueAsString(request);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize Gemini request", ex);
        }
    }

    private String extractText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return "No response generated.";
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray()) {
                return "No response generated.";
            }
            StringBuilder builder = new StringBuilder();
            for (JsonNode part : parts) {
                if (part.hasNonNull("text")) {
                    builder.append(part.get("text").asText());
                }
            }
            String text = builder.toString().trim();
            return text.isEmpty() ? "No response generated." : text;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse Gemini response", ex);
        }
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://generativelanguage.googleapis.com/v1beta";
        }
        String trimmed = value.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
