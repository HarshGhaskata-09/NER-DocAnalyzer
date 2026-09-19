package com.example.ner.statistics;

import com.example.ner.model.BatchResult;
import com.example.ner.model.BatchSummary;
import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple service for generating aggregated summary statistics from multiple document results.
 */
public class SummaryService {

    public BatchSummary generateSummary(BatchResult batchResult) {
        if (batchResult == null) {
            throw new IllegalArgumentException("BatchResult cannot be null");
        }

        int successful = batchResult.getSuccessfulResults().size();
        int failed = batchResult.getFailedDocuments().size();
        int totalSubmitted = successful + failed;
        
        int totalEntities = 0;
        Map<String, Integer> typeCounts = new LinkedHashMap<>();
        Map<String, Integer> documentCounts = new LinkedHashMap<>();
        
        for (DocumentResult result : batchResult.getSuccessfulResults()) {
            totalEntities += result.getTotalEntities();
            documentCounts.put(result.getDocumentName(), result.getTotalEntities());
            
            for (Entity entity : result.getEntities()) {
                String type = entity.getType();
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
            }
        }
        
        return new BatchSummary(totalSubmitted, successful, failed, totalEntities, typeCounts, documentCounts);
    }
}
