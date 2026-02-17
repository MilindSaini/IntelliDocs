package com.milind.docintel.listener;

import com.milind.docintel.events.DocumentUploadedEvent;
import com.milind.docintel.service.processing.DocumentProcessingService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DocumentProcessingListener {

    private final DocumentProcessingService documentProcessingService;

    public DocumentProcessingListener(DocumentProcessingService documentProcessingService) {
        this.documentProcessingService = documentProcessingService;
    }

    @Async("processingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        documentProcessingService.processDocument(event.documentId());
    }
}
