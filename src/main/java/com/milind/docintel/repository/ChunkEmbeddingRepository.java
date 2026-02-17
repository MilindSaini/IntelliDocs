package com.milind.docintel.repository;

import com.milind.docintel.entity.ChunkEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChunkEmbeddingRepository extends JpaRepository<ChunkEmbedding, UUID> {
}
