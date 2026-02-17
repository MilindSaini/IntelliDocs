package com.milind.docintel.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueryResponse {

    private String answer;
    private List<SourceRef> sources = new ArrayList<>();

    public QueryResponse() {
    }

    public QueryResponse(String answer, List<SourceRef> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<SourceRef> getSources() {
        return sources;
    }

    public void setSources(List<SourceRef> sources) {
        this.sources = sources;
    }

    public static class SourceRef {
        private UUID chunkId;
        private double similarityScore;
        private String excerpt;

        public SourceRef() {
        }

        public SourceRef(UUID chunkId, double similarityScore, String excerpt) {
            this.chunkId = chunkId;
            this.similarityScore = similarityScore;
            this.excerpt = excerpt;
        }

        public UUID getChunkId() {
            return chunkId;
        }

        public void setChunkId(UUID chunkId) {
            this.chunkId = chunkId;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        public void setSimilarityScore(double similarityScore) {
            this.similarityScore = similarityScore;
        }

        public String getExcerpt() {
            return excerpt;
        }

        public void setExcerpt(String excerpt) {
            this.excerpt = excerpt;
        }
    }
}
