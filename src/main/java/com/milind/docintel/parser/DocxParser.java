package com.milind.docintel.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class DocxParser {

    public String parse(byte[] payload) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(payload))) {
            StringBuilder builder = new StringBuilder();
            document.getParagraphs().forEach(p -> builder.append(p.getText()).append(System.lineSeparator()));
            return builder.toString();
        }
    }
}
