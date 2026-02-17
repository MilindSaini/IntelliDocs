package com.milind.docintel.service.document;

import com.milind.docintel.dto.request.UploadDocumentRequest;
import com.milind.docintel.dto.response.DocumentResponse;
import com.milind.docintel.entity.Document;
import com.milind.docintel.entity.DocumentStatus;
import com.milind.docintel.events.DocumentUploadedEvent;
import com.milind.docintel.service.auth.AuthenticatedUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentMetadataService documentMetadataService;
    private final DocumentStorageService documentStorageService;
    private final ApplicationEventPublisher eventPublisher;

    public DocumentService(DocumentMetadataService documentMetadataService,
                           DocumentStorageService documentStorageService,
                           ApplicationEventPublisher eventPublisher) {
        this.documentMetadataService = documentMetadataService;
        this.documentStorageService = documentStorageService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DocumentResponse uploadDocument(UploadDocumentRequest request, AuthenticatedUser user) {
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String fileName = request.getFile().getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = "uploaded-file";
        }

        Document document = documentMetadataService.createDraft(user.getId(), fileName);

        String storageUrl = documentStorageService.storeFile(request.getFile(), user.getId(), document.getId());
        document.setStorageUrl(storageUrl);
        document.setStatus(DocumentStatus.UPLOADED);
        Document saved = documentMetadataService.save(document);

        eventPublisher.publishEvent(new DocumentUploadedEvent(saved.getId()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments(AuthenticatedUser user) {
        return documentMetadataService.listForUser(user.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void deleteDocument(UUID documentId, AuthenticatedUser user) {
        Document document = documentMetadataService.getOwnedDocument(documentId, user.getId());
        documentStorageService.deleteFile(document.getStorageUrl());
        documentMetadataService.delete(document);
    }

    @Transactional(readOnly = true)
    public Document getOwnedDocumentEntity(UUID documentId, UUID userId) {
        return documentMetadataService.getOwnedDocument(documentId, userId);
    }

    private DocumentResponse toResponse(Document document) {
        DocumentResponse response = DocumentResponse.from(document);
        response.setStorageUrl(documentStorageService.toPublicUrl(document.getStorageUrl()));
        return response;
    }
}
