package com.milind.docintel.dto.response;

import com.milind.docintel.entity.Document;

import java.time.Instant;
import java.util.UUID;

public class DocumentResponse {

    private UUID id;
    private String fileName;
    private String storageUrl;
    private String status;
    private Instant createdAt;

    public static DocumentResponse from(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFileName(document.getFileName());
        response.setStorageUrl(document.getStorageUrl());
        response.setStatus(document.getStatus().name());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
