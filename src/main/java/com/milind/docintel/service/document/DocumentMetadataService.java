package com.milind.docintel.service.document;

import com.milind.docintel.entity.Document;
import com.milind.docintel.entity.DocumentStatus;
import com.milind.docintel.entity.User;
import com.milind.docintel.repository.DocumentRepository;
import com.milind.docintel.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentMetadataService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentMetadataService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Document createDraft(UUID userId, String fileName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Document document = new Document();
        document.setUser(user);
        document.setFileName(fileName);
        document.setStorageUrl("pending");
        document.setStatus(DocumentStatus.UPLOADED);
        return documentRepository.save(document);
    }

    @Transactional
    public Document save(Document document) {
        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<Document> listForUser(UUID userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Document getOwnedDocument(UUID documentId, UUID userId) {
        return documentRepository.findByIdAndUserId(documentId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    @Transactional(readOnly = true)
    public Document getById(UUID documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    @Transactional
    public Document updateStatus(Document document, DocumentStatus status) {
        document.setStatus(status);
        return documentRepository.save(document);
    }

    @Transactional
    public void delete(Document document) {
        documentRepository.delete(document);
    }
}
