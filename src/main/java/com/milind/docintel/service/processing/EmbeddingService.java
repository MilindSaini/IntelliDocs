package com.milind.docintel.service.processing;

import com.milind.docintel.entity.DocumentChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EmbeddingService {

    private final TextEmbeddingClient embeddingClient;
    private final String modelName;

    public EmbeddingService(TextEmbeddingClient embeddingClient,
                            @Value("${docintel.embedding.model:fallback-hash-embedding}") String modelName) {
        this.embeddingClient = embeddingClient;
        this.modelName = modelName;
    }

    public List<Double> embed(String text) {
        return embeddingClient.embed(text);
    }

    public Map<UUID, List<Double>> embedEachChunk(List<DocumentChunk> chunks) {
        Map<UUID, List<Double>> output = new LinkedHashMap<>();
        for (DocumentChunk chunk : chunks) {
            output.put(chunk.getId(), embed(chunk.getContent()));
        }
        return output;
    }

    public String getModelName() {
        return modelName;
    }
}
