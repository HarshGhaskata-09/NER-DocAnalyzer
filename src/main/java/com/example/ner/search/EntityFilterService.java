package com.example.ner.search;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to filter analysis results by Entity type without invoking CoreNLP.
 */
public class EntityFilterService {

    /**
     * Filters documents to only include entities of the specified type.
     * Does not mutate original DocumentResult objects.
     *
     * @param documents The existing analysis results.
     * @param entityType The entity type to filter by (e.g. "PERSON"). "ALL" returns everything.
     * @return A list of new DocumentResult objects containing the filtered entities.
     */
    public List<DocumentResult> filterByType(List<DocumentResult> documents, String entityType) {
        if (documents == null) {
            return new ArrayList<>();
        }
        
        if (entityType == null || entityType.trim().equalsIgnoreCase("ALL")) {
            return new ArrayList<>(documents);
        }

        String lowerType = entityType.trim().toLowerCase();
        List<DocumentResult> filteredList = new ArrayList<>();

        for (DocumentResult doc : documents) {
            List<Entity> filteredEntities = doc.getEntities().stream()
                    .filter(e -> e.getType().toLowerCase().equals(lowerType))
                    .collect(Collectors.toList());

            // Add the document even if empty to maintain the batch structure
            filteredList.add(new DocumentResult(
                    doc.getDocumentName(), 
                    filteredEntities, 
                    doc.getProcessingTimeMillis()
            ));
        }
        return filteredList;
    }
}
