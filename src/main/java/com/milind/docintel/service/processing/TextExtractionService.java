package com.milind.docintel.service.processing;

import com.milind.docintel.parser.DocxParser;
import com.milind.docintel.parser.PdfParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class TextExtractionService {

    private final PdfParser pdfParser;
    private final DocxParser docxParser;

    public TextExtractionService(PdfParser pdfParser, DocxParser docxParser) {
        this.pdfParser = pdfParser;
        this.docxParser = docxParser;
    }

    public String extract(String fileName, byte[] payload) {
        String extension = fileExtension(fileName);
        try {
            return switch (extension) {
                case "pdf" -> pdfParser.parse(payload);
                case "docx" -> docxParser.parse(payload);
                default -> new String(payload, StandardCharsets.UTF_8);
            };
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to extract text from document", ex);
        }
    }

    private String fileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
