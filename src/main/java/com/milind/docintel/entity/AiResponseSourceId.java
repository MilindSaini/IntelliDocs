package com.milind.docintel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AiResponseSourceId implements Serializable {

    @Column(name = "response_id")
    private UUID responseId;

    @Column(name = "chunk_id")
    private UUID chunkId;

    public AiResponseSourceId() {
    }

    public AiResponseSourceId(UUID responseId, UUID chunkId) {
        this.responseId = responseId;
        this.chunkId = chunkId;
    }

    public UUID getResponseId() {
        return responseId;
    }

    public void setResponseId(UUID responseId) {
        this.responseId = responseId;
    }

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AiResponseSourceId that)) {
            return false;
        }
        return Objects.equals(responseId, that.responseId) && Objects.equals(chunkId, that.chunkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseId, chunkId);
    }
}
