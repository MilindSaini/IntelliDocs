package com.milind.docintel.service.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PromptBuilderService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String buildRagPrompt(String context, String question) {
        String template = loadTemplate("prompt/templates/rag-query.txt");
        return template
            .replace("{{context}}", context == null ? "" : context)
            .replace("{{question}}", question == null ? "" : question);
    }

    public String buildSummaryPrompt(String content) {
        String template = loadTemplate("prompt/templates/summarize.txt");
        return template.replace("{{content}}", content == null ? "" : content);
    }

    private String loadTemplate(String path) {
        return cache.computeIfAbsent(path, this::readTemplate);
    }

    private String readTemplate(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load prompt template: " + path, ex);
        }
    }
}
