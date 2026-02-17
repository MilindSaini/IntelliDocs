package com.milind.docintel.controller;

import com.milind.docintel.dto.request.UploadDocumentRequest;
import com.milind.docintel.dto.response.DocumentResponse;
import com.milind.docintel.service.auth.AuthenticatedUser;
import com.milind.docintel.service.document.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @Valid @ModelAttribute UploadDocumentRequest request) {
        return ResponseEntity.ok(documentService.uploadDocument(request, user));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(documentService.listDocuments(user));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                       @PathVariable UUID documentId) {
        documentService.deleteDocument(documentId, user);
        return ResponseEntity.noContent().build();
    }
}
