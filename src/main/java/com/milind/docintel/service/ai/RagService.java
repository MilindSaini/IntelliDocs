package com.milind.docintel.service.ai;

import com.milind.docintel.service.search.VectorSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RagService {

    private final VectorSearchService vectorSearchService;
    private final PromptBuilderService promptBuilderService;
    private final int defaultTopK;

    public RagService(VectorSearchService vectorSearchService,
                      PromptBuilderService promptBuilderService,
                      @Value("${docintel.ai.max-context-chunks:5}") int defaultTopK) {
        this.vectorSearchService = vectorSearchService;
        this.promptBuilderService = promptBuilderService;
        this.defaultTopK = Math.max(1, defaultTopK);
    }

    public RagContext buildContext(UUID documentId,
                                   String question,
                                   List<Double> questionEmbedding,
                                   Integer requestedTopK) {
        int topK = requestedTopK == null ? defaultTopK : Math.max(1, requestedTopK);
        List<VectorSearchService.RetrievedChunk> chunks = vectorSearchService.topK(documentId, questionEmbedding, topK);

        StringBuilder contextBuilder = new StringBuilder();
        for (VectorSearchService.RetrievedChunk chunk : chunks) {
            contextBuilder
                .append("[chunk=")
                .append(chunk.chunkIndex())
                .append(", score=")
                .append(String.format("%.4f", chunk.similarityScore()))
                .append("] ")
                .append(chunk.content())
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        }

        String prompt = promptBuilderService.buildRagPrompt(contextBuilder.toString(), question);
        return new RagContext(prompt, chunks);
    }

    public record RagContext(String prompt, List<VectorSearchService.RetrievedChunk> retrievedChunks) {
    }
}
