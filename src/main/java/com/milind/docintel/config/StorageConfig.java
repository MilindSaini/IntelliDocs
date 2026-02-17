package com.milind.docintel.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration(proxyBeanMethods = false)
public class StorageConfig {

    @Value("${docintel.storage.local-root:storage/documents}")
    private String localRoot;

    @PostConstruct
    void ensureStorageRoot() throws IOException {
        Files.createDirectories(Path.of(localRoot));
    }
}
