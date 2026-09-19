package com.example.ner.model;

import java.util.Objects;

/**
 * Represents a single named entity detected by the NLP pipeline.
 * This object is immutable.
 */
public final class Entity {
    private final String text;
    private final String type;
    private final int sentenceIndex; // 0-based index
    private final int tokenIndex;    // 0-based index within the sentence
    private final int startPosition; // character offset in original text
    private final int endPosition;   // character offset in original text

    /**
     * Constructs a full Entity with positional metadata.
     */
    public Entity(String text, String type, int sentenceIndex, int tokenIndex, int startPosition, int endPosition) {
        if (text == null || type == null) {
            throw new IllegalArgumentException("Text and type cannot be null");
        }
        this.text = text;
        this.type = type;
        this.sentenceIndex = sentenceIndex;
        this.tokenIndex = tokenIndex;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    /**
     * Constructs a minimal Entity (useful for testing or fallback).
     */
    public Entity(String text, String type) {
        this(text, type, -1, -1, -1, -1);
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public int getSentenceIndex() {
        return sentenceIndex;
    }

    public int getTokenIndex() {
        return tokenIndex;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return sentenceIndex == entity.sentenceIndex && 
               tokenIndex == entity.tokenIndex &&
               startPosition == entity.startPosition && 
               endPosition == entity.endPosition &&
               Objects.equals(text, entity.text) && 
               Objects.equals(type, entity.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, type, sentenceIndex, tokenIndex, startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "text='" + text + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
