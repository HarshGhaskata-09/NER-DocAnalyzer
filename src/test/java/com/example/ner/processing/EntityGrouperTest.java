package com.example.ner.processing;

import com.example.ner.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityGrouperTest {

    private EntityGrouper grouper;

    @BeforeEach
    void setUp() {
        grouper = new EntityGrouper();
    }

    @Test
    void testGroupEntities_TwoTokenPerson() {
        List<Entity> tokens = Arrays.asList(
                new Entity("Sundar", "PERSON", 0, 0, 0, 6),
                new Entity("Pichai", "PERSON", 0, 1, 7, 13)
        );
        
        List<Entity> result = grouper.groupEntities(tokens);
        
        assertEquals(1, result.size());
        assertEquals("Sundar Pichai", result.get(0).getText());
        assertEquals("PERSON", result.get(0).getType());
        assertEquals(0, result.get(0).getStartPosition());
        assertEquals(13, result.get(0).getEndPosition());
        assertEquals(0, result.get(0).getTokenIndex());
    }

    @Test
    void testGroupEntities_ThreeTokenOrganization() {
        List<Entity> tokens = Arrays.asList(
                new Entity("United", "ORGANIZATION", 0, 0, 0, 6),
                new Entity("Nations", "ORGANIZATION", 0, 1, 7, 14),
                new Entity("Organization", "ORGANIZATION", 0, 2, 15, 27)
        );

        List<Entity> result = grouper.groupEntities(tokens);

        assertEquals(1, result.size());
        assertEquals("United Nations Organization", result.get(0).getText());
    }

    @Test
    void testGroupEntities_DifferentTypes() {
        List<Entity> tokens = Arrays.asList(
                new Entity("Sundar", "PERSON", 0, 0, 0, 6),
                new Entity("Google", "ORGANIZATION", 0, 1, 7, 13)
        );

        List<Entity> result = grouper.groupEntities(tokens);

        assertEquals(2, result.size());
        assertEquals("Sundar", result.get(0).getText());
        assertEquals("Google", result.get(1).getText());
    }

    @Test
    void testGroupEntities_SentenceBoundary() {
        List<Entity> tokens = Arrays.asList(
                new Entity("Barack", "PERSON", 0, 0, 0, 6),
                new Entity("Obama", "PERSON", 0, 1, 7, 12),
                new Entity("Michelle", "PERSON", 1, 0, 20, 28) // Different sentence
        );

        List<Entity> result = grouper.groupEntities(tokens);

        assertEquals(2, result.size());
        assertEquals("Barack Obama", result.get(0).getText());
        assertEquals("Michelle", result.get(1).getText());
    }

    @Test
    void testGroupEntities_TokenGap() {
        List<Entity> tokens = Arrays.asList(
                new Entity("Bank", "ORGANIZATION", 0, 1, 0, 4),
                new Entity("America", "ORGANIZATION", 0, 3, 8, 15) // Missed tokenIndex 2
        );

        List<Entity> result = grouper.groupEntities(tokens);

        assertEquals(2, result.size(), "Should remain separate due to gap in token index");
        assertEquals("Bank", result.get(0).getText());
        assertEquals("America", result.get(1).getText());
    }

    @Test
    void testGroupEntities_SingleEntity() {
        List<Entity> tokens = Collections.singletonList(
                new Entity("Google", "ORGANIZATION", 0, 0, 0, 6)
        );

        List<Entity> result = grouper.groupEntities(tokens);

        assertEquals(1, result.size());
        assertEquals("Google", result.get(0).getText());
    }

    @Test
    void testGroupEntities_EmptyInput() {
        List<Entity> result = grouper.groupEntities(new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGroupEntities_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> grouper.groupEntities(null));
    }

    @Test
    void testGroupEntities_RepeatedEntityWithGap() {
        List<Entity> tokens = Arrays.asList(
                new Entity("Google", "ORGANIZATION", 0, 0, 0, 6),
                new Entity("Google", "ORGANIZATION", 0, 5, 20, 26) // non-consecutive
        );

        List<Entity> result = grouper.groupEntities(tokens);

        assertEquals(2, result.size());
    }
}
