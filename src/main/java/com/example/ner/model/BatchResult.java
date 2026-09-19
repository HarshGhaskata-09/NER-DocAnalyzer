package com.example.ner.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents the overall result of analyzing multiple documents.
 */
public class BatchResult {
    private final List<DocumentResult> successfulResults;
    private final Map<String, String> failedDocuments; // Path -> Error message

    public BatchResult(List<DocumentResult> successfulResults, Map<String, String> failedDocuments) {
        this.successfulResults = successfulResults != null ? List.copyOf(successfulResults) : Collections.emptyList();
        this.failedDocuments = failedDocuments != null ? Map.copyOf(failedDocuments) : Collections.emptyMap();
    }

    public List<DocumentResult> getSuccessfulResults() {
        return successfulResults;
    }

    public Map<String, String> getFailedDocuments() {
        return failedDocuments;
    }
}
