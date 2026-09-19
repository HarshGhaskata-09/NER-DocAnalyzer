package com.example.ner.document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles reading text documents from disk for NER analysis.
 */
public class DocumentReader {

    /**
     * Reads a UTF-8 text file from the given path.
     * 
     * @param path The path to the document.
     * @return The complete text content of the document.
     * @throws IllegalArgumentException if the path is invalid or file is not supported.
     * @throws IOException if the file cannot be read.
     */
    public String readDocument(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Please enter a valid document path.");
        }
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Document not found.");
        }
        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("The selected path is a directory, not a text document.");
        }
        
        String fileName = path.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".txt")) {
            throw new IllegalArgumentException("Only .txt files are currently supported.");
        }
        
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
