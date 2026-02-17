package com.milind.docintel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milind.docintel.service.ai.ChatClient;
import com.milind.docintel.service.ai.GeminiChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class GeminiAiConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public ChatClient geminiChatClient(ObjectMapper objectMapper,
                                       @Value("${docintel.gemini.api-key}") String apiKey,
                                       @Value("${docintel.gemini.model:gemini-1.5-flash}") String model,
                                       @Value("${docintel.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
                                       @Value("${docintel.gemini.timeout-seconds:30}") int timeoutSeconds,
                                       @Value("${docintel.gemini.temperature:0.2}") double temperature,
                                       @Value("${docintel.gemini.max-output-tokens:1024}") int maxOutputTokens) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("docintel.gemini.api-key must be set to use Gemini");
        }
        return new GeminiChatClient(objectMapper, apiKey, baseUrl, model, timeoutSeconds, temperature, maxOutputTokens);
    }
}
