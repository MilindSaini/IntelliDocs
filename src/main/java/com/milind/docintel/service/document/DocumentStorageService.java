package com.milind.docintel.service.document;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private final Path storageRoot;
    private final String publicBaseUrl;

    public DocumentStorageService(@Value("${docintel.storage.local-root:storage/documents}") String localRoot,
                                  @Value("${docintel.storage.public-base-url:/files}") String publicBaseUrl) {
        this.storageRoot = Path.of(localRoot).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
    }

    public String storeFile(MultipartFile file, UUID userId, UUID documentId) {
        String originalName = StringUtils.hasText(file.getOriginalFilename())
            ? file.getOriginalFilename()
            : "document.bin";

        String safeName = sanitizeFileName(originalName);
        String storageKey = buildStorageKey(userId, documentId, safeName);

        try {
            Path targetPath = resolveRelativePath(storageKey);
            Files.createDirectories(targetPath.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return storageKey;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file", ex);
        }
    }

    public byte[] readFile(String storageUrl) {
        Path path = resolveRelativePath(storageUrl);
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read file from storage", ex);
        }
    }

    public void deleteFile(String storageUrl) {
        Path path = resolveRelativePath(storageUrl);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete file from storage", ex);
        }
    }

    public String toPublicUrl(String storageUrl) {
        String normalized = storageUrl.replace('\\', '/');
        if (StringUtils.hasText(publicBaseUrl)) {
            if (publicBaseUrl.endsWith("/")) {
                return publicBaseUrl + normalized;
            }
            return publicBaseUrl + "/" + normalized;
        }
        return normalized;
    }

    private String sanitizeFileName(String fileName) {
        return fileName
            .replace("..", "")
            .replace("/", "_")
            .replace("\\", "_");
    }

    private String buildStorageKey(UUID userId, UUID documentId, String safeName) {
        return userId + "/" + documentId + "/" + safeName;
    }

    private Path resolveRelativePath(String storageUrl) {
        Path resolved = storageRoot.resolve(storageUrl).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        return resolved;
    }
}
