package com.example.ner.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testEntityCreationAndGetters() {
        Entity entity = new Entity("Google", "ORGANIZATION", 0, 1, 0, 6);
        
        assertEquals("Google", entity.getText());
        assertEquals("ORGANIZATION", entity.getType());
        assertEquals(0, entity.getSentenceIndex());
        assertEquals(1, entity.getTokenIndex());
        assertEquals(0, entity.getStartPosition());
        assertEquals(6, entity.getEndPosition());
    }
    
    @Test
    void testEntityEquality() {
        Entity e1 = new Entity("Sundar", "PERSON", 0, 5, 10, 16);
        Entity e2 = new Entity("Sundar", "PERSON", 0, 5, 10, 16);
        Entity e3 = new Entity("Sundar", "PERSON", 0, 5, 10, 17);
        Entity e4 = new Entity("Pichai", "PERSON", 0, 5, 10, 16);
        
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertNotEquals(e1, e4);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
    
    @Test
    void testNullValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Entity(null, "PERSON");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Entity("Sundar", null);
        });
    }
}
