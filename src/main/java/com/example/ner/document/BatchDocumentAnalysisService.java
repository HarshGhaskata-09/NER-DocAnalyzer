package com.example.ner.document;

import com.example.ner.model.BatchResult;
import com.example.ner.model.DocumentResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analyzing multiple documents sequentially while reusing a single pipeline.
 */
public class BatchDocumentAnalysisService {
    private final DocumentAnalysisService analysisService;

    public BatchDocumentAnalysisService(DocumentAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Processes a list of document paths sequentially.
     * Failed documents are isolated and do not crash the batch.
     * 
     * @param paths The list of document paths to process.
     * @return BatchResult containing successful results and failed paths with errors.
     */
    public BatchResult analyzeDocuments(List<Path> paths) {
        List<DocumentResult> successful = new ArrayList<>();
        Map<String, String> failed = new LinkedHashMap<>();

        if (paths == null || paths.isEmpty()) {
            return new BatchResult(successful, failed);
        }

        for (Path path : paths) {
            try {
                DocumentResult result = analysisService.analyzeDocument(path);
                successful.add(result);
            } catch (Exception e) {
                String pathStr = path != null ? path.toString() : "null";
                failed.put(pathStr, e.getMessage() != null ? e.getMessage() : "Unknown error");
            }
        }

        return new BatchResult(successful, failed);
    }
}
