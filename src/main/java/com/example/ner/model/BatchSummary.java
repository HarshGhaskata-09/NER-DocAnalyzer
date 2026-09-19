package com.example.ner.model;

import java.util.Collections;
import java.util.Map;

/**
 * Represents the summary metrics of a batch analysis.
 */
public class BatchSummary {
    private final int totalSubmitted;
    private final int successful;
    private final int failed;
    private final int totalEntities;
    private final Map<String, Integer> typeCounts;
    private final Map<String, Integer> documentCounts;

    public BatchSummary(int totalSubmitted, int successful, int failed, int totalEntities, 
                        Map<String, Integer> typeCounts, Map<String, Integer> documentCounts) {
        this.totalSubmitted = totalSubmitted;
        this.successful = successful;
        this.failed = failed;
        this.totalEntities = totalEntities;
        this.typeCounts = typeCounts != null ? Map.copyOf(typeCounts) : Collections.emptyMap();
        this.documentCounts = documentCounts != null ? Map.copyOf(documentCounts) : Collections.emptyMap();
    }

    public int getTotalSubmitted() { return totalSubmitted; }
    public int getSuccessful() { return successful; }
    public int getFailed() { return failed; }
    public int getTotalEntities() { return totalEntities; }
    public Map<String, Integer> getTypeCounts() { return typeCounts; }
    public Map<String, Integer> getDocumentCounts() { return documentCounts; }
}
