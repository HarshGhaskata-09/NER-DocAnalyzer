package com.example.ner.search;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to search entities across multiple analysis results without invoking CoreNLP.
 */
public class EntitySearchService {

    /**
     * Searches for entities containing the query (case-insensitive partial match).
     *
     * @param documents The existing analysis results.
     * @param query The search query.
     * @return A list of matching EntitySearchResult objects.
     */
    public List<EntitySearchResult> search(List<DocumentResult> documents, String query) {
        List<EntitySearchResult> results = new ArrayList<>();
        if (documents == null || query == null || query.trim().isEmpty()) {
            return results;
        }

        String lowerQuery = query.toLowerCase();

        for (DocumentResult doc : documents) {
            if (doc.getEntities() != null) {
                for (Entity entity : doc.getEntities()) {
                    if (entity.getText().toLowerCase().contains(lowerQuery)) {
                        results.add(new EntitySearchResult(doc.getDocumentName(), entity));
                    }
                }
            }
        }
        return results;
    }
}
