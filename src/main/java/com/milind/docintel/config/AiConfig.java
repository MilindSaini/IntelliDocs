package com.milind.docintel.config;

import com.milind.docintel.service.processing.TextEmbeddingClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnMissingBean(TextEmbeddingClient.class)
    public TextEmbeddingClient textEmbeddingClient(@Value("${docintel.embedding.dimension:1536}") int dimension) {
        return text -> {
            int size = Math.max(8, dimension);
            List<Double> vector = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                vector.add(0.0d);
            }
            if (text == null || text.isBlank()) {
                return vector;
            }
            String normalized = text.toLowerCase();
            for (int i = 0; i < normalized.length(); i++) {
                int bucket = Math.floorMod(normalized.charAt(i) * 31 + i, size);
                double updated = vector.get(bucket) + 1.0;
                vector.set(bucket, updated);
            }
            double norm = Math.sqrt(vector.stream().mapToDouble(v -> v * v).sum());
            if (norm > 0) {
                for (int i = 0; i < vector.size(); i++) {
                    vector.set(i, vector.get(i) / norm);
                }
            }
            return vector;
        };
    }
}
