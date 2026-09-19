package com.example.ner.search;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntitySearchServiceTest {

    private EntitySearchService searchService;
    private List<DocumentResult> documents;

    @BeforeEach
    void setUp() {
        searchService = new EntitySearchService();
        
        DocumentResult doc1 = new DocumentResult("doc1.txt", Arrays.asList(
                new Entity("Google", "ORGANIZATION", 0, 0, 0, 6),
                new Entity("Sundar Pichai", "PERSON", 0, 1, 7, 20)
        ));
        
        DocumentResult doc2 = new DocumentResult("doc2.txt", Arrays.asList(
                new Entity("Sundar", "PERSON", 0, 0, 0, 6),
                new Entity("Microsoft", "ORGANIZATION", 0, 1, 7, 16)
        ));
        
        documents = Arrays.asList(doc1, doc2);
    }

    @Test
    void testSearch_ExactMatchCaseInsensitive() {
        List<EntitySearchResult> results = searchService.search(documents, "gOOgle");
        
        assertEquals(1, results.size());
        assertEquals("doc1.txt", results.get(0).getDocumentName());
        assertEquals("Google", results.get(0).getEntity().getText());
    }

    @Test
    void testSearch_PartialMatchMultipleDocuments() {
        List<EntitySearchResult> results = searchService.search(documents, "sundar");
        
        assertEquals(2, results.size());
        assertEquals("doc1.txt", results.get(0).getDocumentName());
        assertEquals("Sundar Pichai", results.get(0).getEntity().getText());
        
        assertEquals("doc2.txt", results.get(1).getDocumentName());
        assertEquals("Sundar", results.get(1).getEntity().getText());
    }

    @Test
    void testSearch_NoMatch() {
        List<EntitySearchResult> results = searchService.search(documents, "Apple");
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearch_BlankQuery() {
        List<EntitySearchResult> results = searchService.search(documents, "   ");
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testSearch_NullInput() {
        assertTrue(searchService.search(null, "Google").isEmpty());
    }
}
