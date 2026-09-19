package com.example.ner.export;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TxtExporter {

    public void export(List<DocumentResult> documents, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("NER DOCUMENT ANALYZER");
            writer.newLine();
            writer.write("=====================");
            writer.newLine();
            writer.newLine();

            int totalDocs = 0;
            int totalEntities = 0;

            if (documents != null) {
                totalDocs = documents.size();
                for (DocumentResult doc : documents) {
                    writer.write("DOCUMENT: " + doc.getDocumentName());
                    writer.newLine();
                    writer.newLine();
                    writer.write("Detected Entities");
                    writer.newLine();
                    writer.write("-----------------");
                    writer.newLine();
                    writer.newLine();

                    if (doc.getEntities().isEmpty()) {
                        writer.write("No named entities were found.");
                        writer.newLine();
                    } else {
                        for (Entity e : doc.getEntities()) {
                            writer.write(e.getText() + " -> " + e.getType());
                            writer.newLine();
                        }
                    }

                    writer.newLine();
                    writer.newLine();
                    writer.write("Total Entities: " + doc.getTotalEntities());
                    writer.newLine();
                    writer.newLine();
                    writer.newLine();

                    totalEntities += doc.getTotalEntities();
                }
            }

            writer.write("OVERALL SUMMARY");
            writer.newLine();
            writer.write("---------------");
            writer.newLine();
            writer.newLine();
            writer.write("Documents: " + totalDocs);
            writer.newLine();
            writer.write("Total Entities: " + totalEntities);
            writer.newLine();
        }
    }
}
