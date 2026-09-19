package com.example.ner.document;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentReaderTest {

    private DocumentReader reader;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        reader = new DocumentReader();
        tempFile = Files.createTempFile("test_doc", ".txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testReadDocument_ValidFile() throws IOException {
        String content = "Hello World! Valid UTF-8 \u00A9";
        Files.writeString(tempFile, content);
        
        String result = reader.readDocument(tempFile);
        assertEquals(content, result);
    }

    @Test
    void testReadDocument_EmptyFile() throws IOException {
        Files.writeString(tempFile, "");
        String result = reader.readDocument(tempFile);
        assertEquals("", result);
    }

    @Test
    void testReadDocument_MissingFile() {
        Path missing = tempFile.resolveSibling("does_not_exist.txt");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            reader.readDocument(missing);
        });
        assertEquals("Document not found.", e.getMessage());
    }

    @Test
    void testReadDocument_Directory() throws IOException {
        Path tempDir = Files.createTempDirectory("test_dir");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            reader.readDocument(tempDir);
        });
        assertEquals("The selected path is a directory, not a text document.", e.getMessage());
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testReadDocument_InvalidExtension() throws IOException {
        Path pdfFile = Files.createTempFile("test_doc", ".pdf");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            reader.readDocument(pdfFile);
        });
        assertEquals("Only .txt files are currently supported.", e.getMessage());
        Files.deleteIfExists(pdfFile);
    }

    @Test
    void testReadDocument_NullPath() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            reader.readDocument(null);
        });
        assertEquals("Please enter a valid document path.", e.getMessage());
    }
}
