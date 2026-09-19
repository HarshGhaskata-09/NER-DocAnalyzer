package com.example.ner.document;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import com.example.ner.ner.StanfordNERService;

import java.nio.file.Path;
import java.util.List;

/**
 * Coordinates the reading of documents and their NER analysis.
 */
public class DocumentAnalysisService {
    private final DocumentReader reader;
    private final StanfordNERService nerService;

    public DocumentAnalysisService(StanfordNERService nerService) {
        this.reader = new DocumentReader();
        this.nerService = nerService;
    }

    /**
     * Analyzes a document from the given path end-to-end.
     * 
     * @param path The path to the .txt document.
     * @return A DocumentResult containing extracted entities and processing metrics.
     */
    public DocumentResult analyzeDocument(Path path) {
        long startTime = System.nanoTime();
        
        String text;
        try {
            text = reader.readDocument(path);
        } catch (IllegalArgumentException e) {
            throw e; // Pass explicit validation messages cleanly
        } catch (Exception e) {
            throw new RuntimeException("Unable to read the document.", e);
        }

        List<Entity> groupedEntities;
        try {
            groupedEntities = nerService.analyzeEntities(text);
        } catch (Exception e) {
            throw new RuntimeException("Unable to analyze the document using Stanford NER.", e);
        }

        long endTime = System.nanoTime();
        long durationMillis = (endTime - startTime) / 1_000_000;
        
        return new DocumentResult(path.getFileName().toString(), groupedEntities, durationMillis);
    }
}
