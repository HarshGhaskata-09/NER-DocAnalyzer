package com.example.ner.document;

import com.example.ner.model.DocumentResult;
import com.example.ner.ner.StanfordNERService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentAnalysisServiceTest {

    private static StanfordNERService nerService;
    private DocumentAnalysisService analysisService;
    private Path tempFile;

    @BeforeAll
    static void initModel() {
        nerService = new StanfordNERService();
    }

    @BeforeEach
    void setUp() throws IOException {
        analysisService = new DocumentAnalysisService(nerService);
        tempFile = Files.createTempFile("test_analysis", ".txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testAnalyzeDocument_Integration() throws IOException {
        Files.writeString(tempFile, "Barack Obama visited New York.");
        
        DocumentResult result = analysisService.analyzeDocument(tempFile);
        
        assertNotNull(result);
        assertEquals(tempFile.getFileName().toString(), result.getDocumentName());
        assertNotNull(result.getEntities());
        assertFalse(result.getEntities().isEmpty());
        assertTrue(result.getProcessingTimeMillis() >= 0);
        
        boolean foundObama = result.getEntities().stream().anyMatch(e -> 
            e.getText().equals("Barack Obama") && e.getType().equals("PERSON")
        );
        assertTrue(foundObama, "Should detect grouped 'Barack Obama'");
    }

    @Test
    void testAnalyzeDocument_EmptyDocument() throws IOException {
        Files.writeString(tempFile, "    ");
        
        DocumentResult result = analysisService.analyzeDocument(tempFile);
        
        assertNotNull(result);
        assertTrue(result.getEntities().isEmpty());
    }
}
