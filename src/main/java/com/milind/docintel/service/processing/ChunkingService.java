package com.milind.docintel.service.processing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private final int chunkSize;
    private final int overlap;

    public ChunkingService(@Value("${docintel.processing.chunk-size:300}") int chunkSize,
                           @Value("${docintel.processing.chunk-overlap:50}") int overlap) {
        this.chunkSize = Math.max(20, chunkSize);
        this.overlap = Math.max(0, overlap);
    }

    public List<ChunkPart> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String[] words = text.trim().split("\\s+");
        List<ChunkPart> chunks = new ArrayList<>();

        int step = Math.max(1, chunkSize - overlap);
        int index = 0;

        for (int start = 0; start < words.length; start += step) {
            int end = Math.min(start + chunkSize, words.length);
            StringBuilder content = new StringBuilder();
            for (int i = start; i < end; i++) {
                if (content.length() > 0) {
                    content.append(' ');
                }
                content.append(words[i]);
            }

            chunks.add(new ChunkPart(index, content.toString(), end - start));
            index++;

            if (end == words.length) {
                break;
            }
        }

        return chunks;
    }

    public record ChunkPart(int chunkIndex, String content, int tokenCount) {
    }
}
