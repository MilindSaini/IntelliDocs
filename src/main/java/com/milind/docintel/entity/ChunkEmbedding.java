package com.milind.docintel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "chunk_embeddings")
public class ChunkEmbedding {

    @Id
    @Column(name = "chunk_id")
    private UUID chunkId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "chunk_id", nullable = false)
    private DocumentChunk chunk;

    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(nullable = false, columnDefinition = "vector(1536)")
    private Object embedding;

    @Column(nullable = false)
    private String model;

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public DocumentChunk getChunk() {
        return chunk;
    }

    public void setChunk(DocumentChunk chunk) {
        this.chunk = chunk;
    }

    public Object getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Object embedding) {
        this.embedding = embedding;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
