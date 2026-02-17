package com.milind.docintel.service.search;

import com.milind.docintel.entity.DocumentChunk;
import com.milind.docintel.repository.ChunkRepository;
import com.milind.docintel.vector.VectorStoreClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VectorSearchService {

    private final VectorStoreClient vectorStoreClient;
    private final ChunkRepository chunkRepository;

    public VectorSearchService(VectorStoreClient vectorStoreClient, ChunkRepository chunkRepository) {
        this.vectorStoreClient = vectorStoreClient;
        this.chunkRepository = chunkRepository;
    }

    public List<RetrievedChunk> topK(UUID documentId, List<Double> queryEmbedding, int topK) {
        List<VectorStoreClient.SearchMatch> matches = vectorStoreClient.topK(documentId, queryEmbedding, topK);
        if (matches.isEmpty()) {
            return List.of();
        }

        List<UUID> chunkIds = matches.stream().map(VectorStoreClient.SearchMatch::chunkId).toList();
        Map<UUID, DocumentChunk> chunksById = new LinkedHashMap<>();
        for (DocumentChunk chunk : chunkRepository.findByIdIn(chunkIds)) {
            chunksById.put(chunk.getId(), chunk);
        }

        return matches.stream()
            .map(match -> {
                DocumentChunk chunk = chunksById.get(match.chunkId());
                if (chunk == null) {
                    return null;
                }
                return new RetrievedChunk(chunk.getId(), chunk.getChunkIndex(), chunk.getContent(), match.similarityScore());
            })
            .filter(item -> item != null)
            .toList();
    }

    public record RetrievedChunk(UUID chunkId, int chunkIndex, String content, double similarityScore) {
    }
}
