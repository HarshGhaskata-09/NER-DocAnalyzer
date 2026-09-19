package com.example.ner.export;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportersTest {

    private List<DocumentResult> documents;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("export_tests");
        
        DocumentResult doc1 = new DocumentResult("doc1.txt", Arrays.asList(
                new Entity("Google, Inc.", "ORGANIZATION", 0, 0, 0, 12),
                new Entity("Sundar", "PERSON", 0, 1, 13, 19)
        ), 150);
        
        documents = Arrays.asList(doc1);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(p -> {
                try { Files.delete(p); } catch (Exception ignored) {}
            });
    }

    @Test
    void testCsvExport() throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        CsvExporter exporter = new CsvExporter();
        exporter.export(documents, csvFile);
        
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertTrue(content.contains("Document,Entity,Type"));
        assertTrue(content.contains("\"doc1.txt\",\"Google, Inc.\",\"ORGANIZATION\""));
    }

    @Test
    void testJsonExport() throws IOException {
        Path jsonFile = tempDir.resolve("test.json");
        JsonExporter exporter = new JsonExporter();
        exporter.export(documents, jsonFile);
        
        assertTrue(Files.exists(jsonFile));
        String content = Files.readString(jsonFile);
        assertTrue(content.contains("\"documents\""));
        assertTrue(content.contains("\"documentName\" : \"doc1.txt\""));
        assertTrue(content.contains("\"text\" : \"Google, Inc.\""));
        assertTrue(content.contains("\"type\" : \"ORGANIZATION\""));
    }

    @Test
    void testTxtExport() throws IOException {
        Path txtFile = tempDir.resolve("test.txt");
        TxtExporter exporter = new TxtExporter();
        exporter.export(documents, txtFile);
        
        assertTrue(Files.exists(txtFile));
        String content = Files.readString(txtFile);
        assertTrue(content.contains("NER DOCUMENT ANALYZER"));
        assertTrue(content.contains("Google, Inc. -> ORGANIZATION"));
        assertTrue(content.contains("Total Entities: 2"));
    }
}
