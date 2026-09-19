package com.example.ner.processing;

import com.example.ner.model.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for grouping consecutive token-level Entity objects 
 * into multi-word Entity spans based on NER types and positions.
 */
public class EntityGrouper {

    /**
     * Groups consecutive tokens that belong to the same entity.
     * 
     * @param tokenEntities A list of raw token-level Entity objects.
     * @return A new list of grouped Entity objects.
     */
    public List<Entity> groupEntities(List<Entity> tokenEntities) {
        if (tokenEntities == null) {
            throw new IllegalArgumentException("Input list cannot be null");
        }
        
        List<Entity> grouped = new ArrayList<>();
        if (tokenEntities.isEmpty()) {
            return grouped;
        }

        Entity currentSpan = tokenEntities.get(0);
        int lastTokenIndex = currentSpan.getTokenIndex();

        for (int i = 1; i < tokenEntities.size(); i++) {
            Entity currentEntity = tokenEntities.get(i);
            
            boolean sameType = currentSpan.getType().equals(currentEntity.getType());
            boolean sameSentence = currentSpan.getSentenceIndex() == currentEntity.getSentenceIndex();
            boolean consecutiveToken = (lastTokenIndex + 1 == currentEntity.getTokenIndex());
            
            boolean indicesAvailable = currentSpan.getSentenceIndex() != -1 && 
                                       lastTokenIndex != -1 && 
                                       currentEntity.getTokenIndex() != -1;
                                       
            if (sameType && sameSentence && consecutiveToken && indicesAvailable) {
                // Combine into a single span
                currentSpan = new Entity(
                    currentSpan.getText() + " " + currentEntity.getText(),
                    currentSpan.getType(),
                    currentSpan.getSentenceIndex(),
                    currentSpan.getTokenIndex(), // Keep the first token's index
                    currentSpan.getStartPosition(),
                    currentEntity.getEndPosition()
                );
                // Update the last token index to the current one in the span
                lastTokenIndex = currentEntity.getTokenIndex();
            } else {
                // Save the completed span and start a new one
                grouped.add(currentSpan);
                currentSpan = currentEntity;
                lastTokenIndex = currentEntity.getTokenIndex();
            }
        }
        
        // Add the final span
        grouped.add(currentSpan);
        
        return grouped;
    }
}
