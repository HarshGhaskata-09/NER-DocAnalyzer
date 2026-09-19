package com.example.ner.statistics;

import com.example.ner.model.BatchResult;
import com.example.ner.model.BatchSummary;
import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SummaryServiceTest {

    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        summaryService = new SummaryService();
    }

    @Test
    void testGenerateSummary_Calculation() {
        // Document A: 3 entities (2 PERSON, 1 LOCATION)
        DocumentResult docA = new DocumentResult("docA.txt", Arrays.asList(
                new Entity("A", "PERSON", 0, 0, 0, 1),
                new Entity("B", "PERSON", 0, 1, 2, 3),
                new Entity("C", "LOCATION", 0, 2, 4, 5)
        ));

        // Document B: 2 entities (1 PERSON, 1 ORGANIZATION)
        DocumentResult docB = new DocumentResult("docB.txt", Arrays.asList(
                new Entity("X", "PERSON", 0, 0, 0, 1),
                new Entity("Y", "ORGANIZATION", 0, 1, 2, 3)
        ));

        BatchResult batchResult = new BatchResult(
                Arrays.asList(docA, docB),
                Collections.singletonMap("missing.txt", "Document not found.")
        );

        BatchSummary summary = summaryService.generateSummary(batchResult);

        assertEquals(3, summary.getTotalSubmitted());
        assertEquals(2, summary.getSuccessful());
        assertEquals(1, summary.getFailed());
        assertEquals(5, summary.getTotalEntities());
        
        assertEquals(3, summary.getTypeCounts().get("PERSON"));
        assertEquals(1, summary.getTypeCounts().get("LOCATION"));
        assertEquals(1, summary.getTypeCounts().get("ORGANIZATION"));
        
        assertEquals(3, summary.getDocumentCounts().get("docA.txt"));
        assertEquals(2, summary.getDocumentCounts().get("docB.txt"));
    }
}
