package com.example.ner.ner;

import com.example.ner.model.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StanfordNERServiceIntegrationTest {

    private static StanfordNERService service;

    @BeforeAll
    static void setUp() {
        service = new StanfordNERService();
    }

    @Test
    void testAnalyzeEntities_MultiToken() {
        String input = "Barack Obama visited New York.";
        List<Entity> result = service.analyzeEntities(input);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        boolean foundObama = result.stream().anyMatch(e -> 
            e.getText().equals("Barack Obama") && e.getType().equals("PERSON")
        );
        
        boolean foundNewYork = result.stream().anyMatch(e -> 
            e.getText().equals("New York") && (e.getType().equals("LOCATION") || e.getType().equals("CITY") || e.getType().equals("STATE_OR_PROVINCE"))
        );
        
        assertTrue(foundObama, "Should correctly group and detect 'Barack Obama' as PERSON");
        assertTrue(foundNewYork, "Should correctly group and detect 'New York' as a location entity");
    }
}
