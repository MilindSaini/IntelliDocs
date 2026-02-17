package com.milind.docintel.config;

import com.milind.docintel.vector.PgVectorStore;
import com.milind.docintel.vector.VectorStoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorDbConfig {

    @Bean
    public VectorStoreClient vectorStoreClient(PgVectorStore pgVectorStore) {
        return pgVectorStore;
    }
}
