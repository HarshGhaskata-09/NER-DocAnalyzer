package com.example.ner.search;

import com.example.ner.model.Entity;

/**
 * Represents a single match from the EntitySearchService.
 */
public class EntitySearchResult {
    private final String documentName;
    private final Entity entity;

    public EntitySearchResult(String documentName, Entity entity) {
        this.documentName = documentName;
        this.entity = entity;
    }

    public String getDocumentName() {
        return documentName;
    }

    public Entity getEntity() {
        return entity;
    }
}
