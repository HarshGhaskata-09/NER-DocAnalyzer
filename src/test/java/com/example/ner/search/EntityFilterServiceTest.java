package com.example.ner.search;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityFilterServiceTest {

    private EntityFilterService filterService;
    private List<DocumentResult> documents;

    @BeforeEach
    void setUp() {
        filterService = new EntityFilterService();
        
        DocumentResult doc1 = new DocumentResult("doc1.txt", Arrays.asList(
                new Entity("Google", "ORGANIZATION", 0, 0, 0, 6),
                new Entity("Sundar Pichai", "PERSON", 0, 1, 7, 20)
        ));
        
        DocumentResult doc2 = new DocumentResult("doc2.txt", Arrays.asList(
                new Entity("Seattle", "LOCATION", 0, 0, 0, 7),
                new Entity("Microsoft", "ORGANIZATION", 0, 1, 8, 17)
        ));
        
        documents = Arrays.asList(doc1, doc2);
    }

    @Test
    void testFilterByType_MatchOne() {
        List<DocumentResult> results = filterService.filterByType(documents, "person");
        
        assertEquals(2, results.size()); // Both documents preserved
        assertEquals(1, results.get(0).getEntities().size());
        assertEquals("Sundar Pichai", results.get(0).getEntities().get(0).getText());
        assertTrue(results.get(1).getEntities().isEmpty()); // doc2 has no PERSON
    }

    @Test
    void testFilterByType_MatchMultiple() {
        List<DocumentResult> results = filterService.filterByType(documents, "ORGANIZATION");
        
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getEntities().size());
        assertEquals("Google", results.get(0).getEntities().get(0).getText());
        
        assertEquals(1, results.get(1).getEntities().size());
        assertEquals("Microsoft", results.get(1).getEntities().get(0).getText());
    }

    @Test
    void testFilterByType_All() {
        List<DocumentResult> results = filterService.filterByType(documents, "ALL");
        
        assertEquals(2, results.size());
        assertEquals(2, results.get(0).getEntities().size());
        assertEquals(2, results.get(1).getEntities().size());
    }

    @Test
    void testFilterByType_NoMatch() {
        List<DocumentResult> results = filterService.filterByType(documents, "DATE");
        
        assertEquals(2, results.size());
        assertTrue(results.get(0).getEntities().isEmpty());
        assertTrue(results.get(1).getEntities().isEmpty());
    }
}
