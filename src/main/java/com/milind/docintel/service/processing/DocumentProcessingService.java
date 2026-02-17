package com.milind.docintel.service.processing;

import com.milind.docintel.entity.Document;
import com.milind.docintel.entity.DocumentChunk;
import com.milind.docintel.entity.DocumentStatus;
import com.milind.docintel.repository.ChunkRepository;
import com.milind.docintel.service.document.DocumentMetadataService;
import com.milind.docintel.service.document.DocumentStorageService;
import com.milind.docintel.vector.VectorStoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    private final DocumentMetadataService documentMetadataService;
    private final DocumentStorageService documentStorageService;
    private final TextExtractionService textExtractionService;
    private final ChunkingService chunkingService;
    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final VectorStoreClient vectorStoreClient;

    public DocumentProcessingService(DocumentMetadataService documentMetadataService,
                                     DocumentStorageService documentStorageService,
                                     TextExtractionService textExtractionService,
                                     ChunkingService chunkingService,
                                     ChunkRepository chunkRepository,
                                     EmbeddingService embeddingService,
                                     VectorStoreClient vectorStoreClient) {
        this.documentMetadataService = documentMetadataService;
        this.documentStorageService = documentStorageService;
        this.textExtractionService = textExtractionService;
        this.chunkingService = chunkingService;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.vectorStoreClient = vectorStoreClient;
    }

    @Transactional
    public void processDocument(UUID documentId) {
        Document document = documentMetadataService.getById(documentId);

        try {
            documentMetadataService.updateStatus(document, DocumentStatus.PROCESSING);

            byte[] payload = documentStorageService.readFile(document.getStorageUrl());
            String extractedText = textExtractionService.extract(document.getFileName(), payload);

            List<ChunkingService.ChunkPart> parts = chunkingService.chunk(extractedText);

            List<DocumentChunk> existingChunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
            if (!existingChunks.isEmpty()) {
                chunkRepository.deleteAll(existingChunks);
            }

            List<DocumentChunk> chunksToPersist = new ArrayList<>();
            for (ChunkingService.ChunkPart part : parts) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocument(document);
                chunk.setChunkIndex(part.chunkIndex());
                chunk.setContent(part.content());
                chunk.setTokenCount(part.tokenCount());
                chunksToPersist.add(chunk);
            }

            List<DocumentChunk> savedChunks = chunkRepository.saveAll(chunksToPersist);
            chunkRepository.flush();
            Map<UUID, List<Double>> vectors = embeddingService.embedEachChunk(savedChunks);
            vectorStoreClient.saveVectors(vectors, embeddingService.getModelName());

            documentMetadataService.updateStatus(document, DocumentStatus.READY);
        } catch (Exception ex) {
            documentMetadataService.updateStatus(document, DocumentStatus.FAILED);
            log.error("Failed document processing for {}", documentId, ex);
        }
    }
}
