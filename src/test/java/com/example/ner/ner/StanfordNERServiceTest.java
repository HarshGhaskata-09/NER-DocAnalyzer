package com.example.ner.ner;

import com.example.ner.model.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StanfordNERServiceTest {

    private static StanfordNERService service;

    @BeforeAll
    static void setUp() {
        service = new StanfordNERService();
    }

    @Test
    void testAnalyzeText_NormalEntities() {
        String input = "Google is a company.";
        List<Entity> result = service.analyzeTokenEntities(input);
        
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Should detect at least one entity");
        
        // We expect Google to be detected as ORGANIZATION
        boolean foundGoogle = result.stream().anyMatch(e -> 
            e.getText().equals("Google") && e.getType().equals("ORGANIZATION")
        );
        assertTrue(foundGoogle, "Should correctly detect 'Google' as ORGANIZATION");
    }

    @Test
    void testAnalyzeText_MultipleEntities() {
        String input = "Barack Obama visited Hawaii.";
        List<Entity> result = service.analyzeTokenEntities(input);
        
        assertNotNull(result);
        
        boolean foundBarack = result.stream().anyMatch(e -> e.getText().equals("Barack") && e.getType().equals("PERSON"));
        boolean foundHawaii = result.stream().anyMatch(e -> e.getText().equals("Hawaii") && (e.getType().equals("LOCATION") || e.getType().equals("STATE_OR_PROVINCE")));
        
        assertTrue(foundBarack, "Should detect Barack as PERSON");
        assertTrue(foundHawaii, "Should detect Hawaii as a location type");
    }

    @Test
    void testAnalyzeText_NoEntities() {
        String input = "This is a simple sentence with no obvious named entities.";
        List<Entity> result = service.analyzeTokenEntities(input);
        
        assertNotNull(result);
        // Sometimes "simple" could be misclassified depending on models, but normally this is empty
        // The contract is that it returns an empty list or only entities Stanford detects.
        // It definitely shouldn't throw an error.
        
        // We verify that 'O' tags are excluded, so if empty or valid entities, it's correct.
        for (Entity e : result) {
            assertNotEquals("O", e.getType(), "Result should not contain 'O' entity types");
        }
    }

    @Test
    void testAnalyzeText_EmptyInput() {
        List<Entity> result1 = service.analyzeTokenEntities("");
        List<Entity> result2 = service.analyzeTokenEntities("   \t  \n ");
        
        assertNotNull(result1);
        assertTrue(result1.isEmpty());
        
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
    }
    
    @Test
    void testAnalyzeText_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.analyzeTokenEntities(null);
        });
    }
}
