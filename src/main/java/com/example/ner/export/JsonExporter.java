package com.example.ner.export;

import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonExporter {

    public void export(List<DocumentResult> documents, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        List<Map<String, Object>> docsList = new ArrayList<>();
        if (documents != null) {
            for (DocumentResult doc : documents) {
                Map<String, Object> docMap = new HashMap<>();
                docMap.put("documentName", doc.getDocumentName());

                List<Map<String, Object>> entityList = new ArrayList<>();
                for (Entity e : doc.getEntities()) {
                    Map<String, Object> entityMap = new HashMap<>();
                    entityMap.put("text", e.getText());
                    entityMap.put("type", e.getType());
                    entityList.add(entityMap);
                }
                docMap.put("entities", entityList);
                docsList.add(docMap);
            }
        }

        Map<String, Object> root = new HashMap<>();
        root.put("documents", docsList);

        mapper.writeValue(outputPath.toFile(), root);
    }
}
