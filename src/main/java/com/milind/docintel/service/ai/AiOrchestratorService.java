package com.milind.docintel.service.ai;

import com.milind.docintel.dto.request.AskQueryRequest;
import com.milind.docintel.dto.response.QueryResponse;
import com.milind.docintel.entity.AiQueryLog;
import com.milind.docintel.entity.AiResponseLog;
import com.milind.docintel.entity.AiResponseSource;
import com.milind.docintel.entity.AiResponseSourceId;
import com.milind.docintel.entity.Document;
import com.milind.docintel.entity.DocumentChunk;
import com.milind.docintel.entity.DocumentStatus;
import com.milind.docintel.entity.User;
import com.milind.docintel.repository.AiQueryLogRepository;
import com.milind.docintel.repository.AiResponseLogRepository;
import com.milind.docintel.repository.AiResponseSourceRepository;
import com.milind.docintel.service.auth.AuthenticatedUser;
import com.milind.docintel.service.document.DocumentService;
import com.milind.docintel.service.processing.EmbeddingService;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiOrchestratorService {

    private final DocumentService documentService;
    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final ChatClient chatClient;
    private final AiQueryLogRepository aiQueryLogRepository;
    private final AiResponseLogRepository aiResponseLogRepository;
    private final AiResponseSourceRepository aiResponseSourceRepository;
    private final EntityManager entityManager;

    public AiOrchestratorService(DocumentService documentService,
                                 EmbeddingService embeddingService,
                                 RagService ragService,
                                 ChatClient chatClient,
                                 AiQueryLogRepository aiQueryLogRepository,
                                 AiResponseLogRepository aiResponseLogRepository,
                                 AiResponseSourceRepository aiResponseSourceRepository,
                                 EntityManager entityManager) {
        this.documentService = documentService;
        this.embeddingService = embeddingService;
        this.ragService = ragService;
        this.chatClient = chatClient;
        this.aiQueryLogRepository = aiQueryLogRepository;
        this.aiResponseLogRepository = aiResponseLogRepository;
        this.aiResponseSourceRepository = aiResponseSourceRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public QueryResponse answer(AuthenticatedUser user, AskQueryRequest request) {
        Document document = documentService.getOwnedDocumentEntity(request.getDocumentId(), user.getId());
        if (document.getStatus() != DocumentStatus.READY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document is not ready for querying");
        }

        AiQueryLog queryLog = new AiQueryLog();
        queryLog.setUser(entityManager.getReference(User.class, user.getId()));
        queryLog.setDocument(document);
        queryLog.setQueryText(request.getQuestion());
        queryLog = aiQueryLogRepository.save(queryLog);

        List<Double> questionEmbedding = embeddingService.embed(request.getQuestion());
        RagService.RagContext ragContext = ragService.buildContext(
            document.getId(),
            request.getQuestion(),
            questionEmbedding,
            request.getTopK()
        );

        String answerText = chatClient.complete(ragContext.prompt());

        AiResponseLog responseLog = new AiResponseLog();
        responseLog.setQuery(queryLog);
        responseLog.setAnswerText(answerText);
        responseLog.setTokensUsed(estimateTokenCount(answerText));
        responseLog = aiResponseLogRepository.save(responseLog);

        List<AiResponseSource> sourceEntities = new ArrayList<>();
        List<QueryResponse.SourceRef> sourceRefs = new ArrayList<>();

        for (var chunk : ragContext.retrievedChunks()) {
            AiResponseSource source = new AiResponseSource();
            source.setId(new AiResponseSourceId(responseLog.getId(), chunk.chunkId()));
            source.setResponse(responseLog);
            source.setChunk(entityManager.getReference(DocumentChunk.class, chunk.chunkId()));
            source.setSimilarityScore(chunk.similarityScore());
            sourceEntities.add(source);

            sourceRefs.add(new QueryResponse.SourceRef(
                chunk.chunkId(),
                chunk.similarityScore(),
                excerpt(chunk.content(), 220)
            ));
        }

        if (!sourceEntities.isEmpty()) {
            aiResponseSourceRepository.saveAll(sourceEntities);
        }

        return new QueryResponse(answerText, sourceRefs);
    }

    private int estimateTokenCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private String excerpt(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
