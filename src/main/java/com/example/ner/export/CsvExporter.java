package com.example.ner.export;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvExporter {

    public void export(List<DocumentResult> documents, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("Document,Entity,Type");
            writer.newLine();

            if (documents != null) {
                for (DocumentResult doc : documents) {
                    for (Entity e : doc.getEntities()) {
                        writer.write(String.format("\"%s\",\"%s\",\"%s\"",
                                escapeCsv(doc.getDocumentName()),
                                escapeCsv(e.getText()),
                                escapeCsv(e.getType())));
                        writer.newLine();
                    }
                }
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
