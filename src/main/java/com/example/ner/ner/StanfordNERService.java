package com.example.ner.ner;

import com.example.ner.model.Entity;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Service to encapsulate Stanford CoreNLP NER integration.
 */
public class StanfordNERService {

    // One reusable pipeline instance to prevent performance overhead.
    private final StanfordCoreNLP pipeline;

    public StanfordNERService() {
        try {
            Properties props = new Properties();
            // We need tokenize and ssplit to break text into words and sentences.
            // pos (Part of Speech) and lemma (Lemmatization) are typically required by Stanford NER models.
            // ner is the actual Named Entity Recognition annotator.
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
            
            // Initialize the pipeline once
            this.pipeline = new StanfordCoreNLP(props);
        } catch (Exception e) {
            throw new RuntimeException("Pipeline initialization failed", e);
        }
    }

    /**
     * Analyzes text and returns a list of detected named entities at the raw token level.
     * Tokens identified as "O" (non-entities) are ignored.
     * 
     * @param text The input text to process.
     * @return List of structured Entity objects. Empty list if input is empty or contains no entities.
     * @throws IllegalArgumentException if input text is null.
     */
    public List<Entity> analyzeTokenEntities(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Input text cannot be null.");
        }
        
        List<Entity> results = new ArrayList<>();
        if (text.trim().isEmpty()) {
            return results;
        }

        try {
            CoreDocument document = new CoreDocument(text);
            pipeline.annotate(document);
            
            int sentIndex = 0;
            for (CoreSentence sentence : document.sentences()) {
                int tokIndex = 0;
                for (CoreLabel token : sentence.tokens()) {
                    String nerLabel = token.ner();
                    
                    if (nerLabel != null && !nerLabel.equals("O")) {
                        Entity entity = new Entity(
                                token.word(), 
                                nerLabel, 
                                sentIndex, 
                                tokIndex, 
                                token.beginPosition(), 
                                token.endPosition()
                        );
                        results.add(entity);
                    }
                    tokIndex++;
                }
                sentIndex++;
            }
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during NER analysis", e);
        }

        return results;
    }

    /**
     * Analyzes text and returns a list of grouped multi-token named entities.
     * 
     * @param text The input text to process.
     * @return List of grouped Entity objects.
     */
    public List<Entity> analyzeEntities(String text) {
        List<Entity> rawTokens = analyzeTokenEntities(text);
        com.example.ner.processing.EntityGrouper grouper = new com.example.ner.processing.EntityGrouper();
        return grouper.groupEntities(rawTokens);
    }
}
