package com.milind.docintel.vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VectorStoreClient {

    void saveVectors(Map<UUID, List<Double>> vectorsByChunkId, String modelName);

    List<SearchMatch> topK(UUID documentId, List<Double> queryEmbedding, int topK);

    record SearchMatch(UUID chunkId, double similarityScore) {
    }
}
