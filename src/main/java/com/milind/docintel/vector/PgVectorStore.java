package com.milind.docintel.vector;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class PgVectorStore implements VectorStoreClient {

    private static final String UPSERT_SQL = """
        INSERT INTO chunk_embeddings (chunk_id, embedding, model)
        VALUES (?, CAST(? AS vector), ?)
        ON CONFLICT (chunk_id)
        DO UPDATE SET embedding = EXCLUDED.embedding, model = EXCLUDED.model
        """;

    private static final String SEARCH_SQL = """
        SELECT ce.chunk_id,
               1 - (ce.embedding <=> CAST(? AS vector)) AS similarity_score
        FROM chunk_embeddings ce
        JOIN document_chunks dc ON dc.id = ce.chunk_id
        WHERE dc.document_id = ?
        ORDER BY ce.embedding <=> CAST(? AS vector)
        LIMIT ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public PgVectorStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveVectors(Map<UUID, List<Double>> vectorsByChunkId, String modelName) {
        if (vectorsByChunkId == null || vectorsByChunkId.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = new ArrayList<>();
        for (Map.Entry<UUID, List<Double>> entry : vectorsByChunkId.entrySet()) {
            batchArgs.add(new Object[]{entry.getKey(), toVectorLiteral(entry.getValue()), modelName});
        }
        jdbcTemplate.batchUpdate(UPSERT_SQL, batchArgs);
    }

    @Override
    public List<SearchMatch> topK(UUID documentId, List<Double> queryEmbedding, int topK) {
        if (queryEmbedding == null || queryEmbedding.isEmpty() || topK <= 0) {
            return List.of();
        }

        String vectorLiteral = toVectorLiteral(queryEmbedding);
        return jdbcTemplate.query(
            SEARCH_SQL,
            ps -> {
                ps.setString(1, vectorLiteral);
                ps.setObject(2, documentId);
                ps.setString(3, vectorLiteral);
                ps.setInt(4, topK);
            },
            (rs, rowNum) -> new SearchMatch(
                UUID.fromString(rs.getString("chunk_id")),
                rs.getDouble("similarity_score")
            )
        );
    }

    private String toVectorLiteral(List<Double> vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.ROOT, "%.8f", vector.get(i)));
        }
        builder.append(']');
        return builder.toString();
    }
}
