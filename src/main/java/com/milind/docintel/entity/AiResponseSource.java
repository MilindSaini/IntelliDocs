package com.milind.docintel.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_response_sources")
public class AiResponseSource {

    @EmbeddedId
    private AiResponseSourceId id;

    @ManyToOne(optional = false)
    @MapsId("responseId")
    @JoinColumn(name = "response_id", nullable = false)
    private AiResponseLog response;

    @ManyToOne(optional = false)
    @MapsId("chunkId")
    @JoinColumn(name = "chunk_id", nullable = false)
    private DocumentChunk chunk;

    private double similarityScore;

    public AiResponseSourceId getId() {
        return id;
    }

    public void setId(AiResponseSourceId id) {
        this.id = id;
    }

    public AiResponseLog getResponse() {
        return response;
    }

    public void setResponse(AiResponseLog response) {
        this.response = response;
    }

    public DocumentChunk getChunk() {
        return chunk;
    }

    public void setChunk(DocumentChunk chunk) {
        this.chunk = chunk;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
