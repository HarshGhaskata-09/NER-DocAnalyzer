package com.example.ner.model;

import java.util.Collections;
import java.util.List;

/**
 * Represents the structured result of analyzing a single document.
 */
public final class DocumentResult {
    private final String documentName;
    private final List<Entity> entities;
    private final long processingTimeMillis;

    public DocumentResult(String documentName, List<Entity> entities) {
        this(documentName, entities, 0);
    }

    /**
     * Constructs a DocumentResult containing the identified entities and processing time.
     * 
     * @param documentName The name or identifier of the document.
     * @param entities The list of entities found.
     * @param processingTimeMillis The time taken to analyze the document.
     */
    public DocumentResult(String documentName, List<Entity> entities, long processingTimeMillis) {
        if (documentName == null) {
            throw new IllegalArgumentException("documentName cannot be null");
        }
        this.documentName = documentName;
        // Make defensive copy and ensure immutability
        this.entities = entities != null ? List.copyOf(entities) : Collections.emptyList();
        this.processingTimeMillis = processingTimeMillis;
    }

    public String getDocumentName() {
        return documentName;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public int getTotalEntities() {
        return entities.size();
    }
    
    public long getProcessingTimeMillis() {
        return processingTimeMillis;
    }
}
