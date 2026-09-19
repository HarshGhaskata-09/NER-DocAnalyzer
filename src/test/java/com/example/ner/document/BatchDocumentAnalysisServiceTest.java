package com.example.ner.document;

import com.example.ner.model.BatchResult;
import com.example.ner.model.DocumentResult;
import com.example.ner.ner.StanfordNERService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchDocumentAnalysisServiceTest {

    private static StanfordNERService nerService;
    private BatchDocumentAnalysisService batchService;
    private Path tempFile1;
    private Path tempFile2;
    private Path tempFile3;

    @BeforeAll
    static void initModel() {
        nerService = new StanfordNERService();
    }

    @BeforeEach
    void setUp() throws IOException {
        DocumentAnalysisService analysisService = new DocumentAnalysisService(nerService);
        batchService = new BatchDocumentAnalysisService(analysisService);
        
        tempFile1 = Files.createTempFile("doc1", ".txt");
        tempFile2 = Files.createTempFile("doc2", ".txt");
        tempFile3 = Files.createTempFile("doc3", ".txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile1);
        Files.deleteIfExists(tempFile2);
        Files.deleteIfExists(tempFile3);
    }

    @Test
    void testAnalyzeDocuments_ThreeValidDocuments() throws IOException {
        Files.writeString(tempFile1, "Google is great.");
        Files.writeString(tempFile2, "Elon Musk leads Tesla.");
        Files.writeString(tempFile3, "Microsoft CEO Satya Nadella.");
        
        List<Path> paths = Arrays.asList(tempFile1, tempFile2, tempFile3);
        
        BatchResult result = batchService.analyzeDocuments(paths);
        
        assertNotNull(result);
        assertEquals(3, result.getSuccessfulResults().size());
        assertTrue(result.getFailedDocuments().isEmpty());
        
        assertEquals(tempFile1.getFileName().toString(), result.getSuccessfulResults().get(0).getDocumentName());
        assertEquals(tempFile2.getFileName().toString(), result.getSuccessfulResults().get(1).getDocumentName());
        assertEquals(tempFile3.getFileName().toString(), result.getSuccessfulResults().get(2).getDocumentName());
    }

    @Test
    void testAnalyzeDocuments_OneInvalidDocument() throws IOException {
        Files.writeString(tempFile1, "Valid text here.");
        Files.writeString(tempFile3, "More valid text.");
        Path invalidPath = tempFile2.resolveSibling("does_not_exist.txt");
        
        List<Path> paths = Arrays.asList(tempFile1, invalidPath, tempFile3);
        
        BatchResult result = batchService.analyzeDocuments(paths);
        
        assertEquals(2, result.getSuccessfulResults().size());
        assertEquals(1, result.getFailedDocuments().size());
        
        assertTrue(result.getFailedDocuments().containsKey(invalidPath.toString()));
        assertEquals(tempFile1.getFileName().toString(), result.getSuccessfulResults().get(0).getDocumentName());
        assertEquals(tempFile3.getFileName().toString(), result.getSuccessfulResults().get(1).getDocumentName());
    }

    @Test
    void testAnalyzeDocuments_EmptyDocument() throws IOException {
        Files.writeString(tempFile1, "   ");
        List<Path> paths = Collections.singletonList(tempFile1);
        
        BatchResult result = batchService.analyzeDocuments(paths);
        
        assertEquals(1, result.getSuccessfulResults().size());
        assertTrue(result.getFailedDocuments().isEmpty());
        assertTrue(result.getSuccessfulResults().get(0).getEntities().isEmpty());
    }

    @Test
    void testAnalyzeDocuments_EmptyInputList() {
        BatchResult result = batchService.analyzeDocuments(Collections.emptyList());
        
        assertNotNull(result);
        assertTrue(result.getSuccessfulResults().isEmpty());
        assertTrue(result.getFailedDocuments().isEmpty());
    }
}
