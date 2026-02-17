package com.milind.docintel.service.processing;

import java.util.List;

public interface TextEmbeddingClient {
    List<Double> embed(String text);
}
