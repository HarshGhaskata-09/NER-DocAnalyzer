package com.example.ner;

import com.example.ner.document.BatchDocumentAnalysisService;
import com.example.ner.document.DocumentAnalysisService;
import com.example.ner.export.CsvExporter;
import com.example.ner.export.JsonExporter;
import com.example.ner.export.TxtExporter;
import com.example.ner.model.BatchResult;
import com.example.ner.model.BatchSummary;
import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import com.example.ner.ner.StanfordNERService;
import com.example.ner.search.EntityFilterService;
import com.example.ner.search.EntitySearchResult;
import com.example.ner.search.EntitySearchService;
import com.example.ner.statistics.SummaryService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static List<DocumentResult> currentResults = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Initializing Stanford CoreNLP pipeline. This may take a moment...");
        StanfordNERService nerService = new StanfordNERService();
        DocumentAnalysisService analysisService = new DocumentAnalysisService(nerService);
        BatchDocumentAnalysisService batchService = new BatchDocumentAnalysisService(analysisService);
        SummaryService summaryService = new SummaryService();
        
        EntitySearchService searchService = new EntitySearchService();
        EntityFilterService filterService = new EntityFilterService();
        CsvExporter csvExporter = new CsvExporter();
        JsonExporter jsonExporter = new JsonExporter();
        TxtExporter txtExporter = new TxtExporter();
        
        System.out.println("Pipeline initialized successfully.\n");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n========================================");
            System.out.println("        NER DOCUMENT ANALYZER");
            System.out.println("========================================");
            System.out.println("1. Analyze single document");
            System.out.println("2. Analyze multiple documents");
            System.out.println("3. Search entity");
            System.out.println("4. Filter by entity type");
            System.out.println("5. Export results");
            System.out.println("6. Exit");
            System.out.print("\nChoose option: ");
            
            String option = scanner.nextLine().trim();
            
            if (option.equals("1")) {
                handleSingleDocument(scanner, analysisService);
            } else if (option.equals("2")) {
                handleMultipleDocuments(scanner, batchService, summaryService);
            } else if (option.equals("3")) {
                handleSearch(scanner, searchService);
            } else if (option.equals("4")) {
                handleFilter(scanner, filterService);
            } else if (option.equals("5")) {
                handleExport(scanner, csvExporter, jsonExporter, txtExporter);
            } else if (option.equals("6")) {
                System.out.println("Exiting application.");
                break;
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void handleSingleDocument(Scanner scanner, DocumentAnalysisService analysisService) {
        System.out.print("Enter path of .txt document: ");
        String inputPath = scanner.nextLine();
        
        if (inputPath == null || inputPath.trim().isEmpty()) {
            System.out.println("Please enter a valid document path.");
            return;
        }
        
        Path path = Paths.get(inputPath.trim());
        System.out.println("\nDocument: " + path.getFileName());
        
        try {
            long fileSize = Files.exists(path) && !Files.isDirectory(path) ? Files.size(path) : 0;
            if (fileSize > 0) {
                System.out.println("File Size: " + fileSize + " bytes");
            }
            
            System.out.println("Analyzing...\n");
            DocumentResult result = analysisService.analyzeDocument(path);
            
            currentResults = Collections.singletonList(result);
            
            printDocumentResult(result);
            
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleMultipleDocuments(Scanner scanner, BatchDocumentAnalysisService batchService, SummaryService summaryService) {
        System.out.println("Enter document paths. Press Enter on a blank line when finished.");
        List<Path> paths = new ArrayList<>();
        
        while (true) {
            System.out.print("Enter document path: ");
            String inputPath = scanner.nextLine().trim();
            if (inputPath.isEmpty()) {
                break;
            }
            paths.add(Paths.get(inputPath));
        }
        
        if (paths.isEmpty()) {
            System.out.println("No documents entered.");
            return;
        }
        
        System.out.println("\nAnalyzing " + paths.size() + " documents...\n");
        BatchResult batchResult = batchService.analyzeDocuments(paths);
        
        currentResults = new ArrayList<>(batchResult.getSuccessfulResults());
        
        for (DocumentResult docResult : batchResult.getSuccessfulResults()) {
            System.out.println("----------------------------------------");
            System.out.println("DOCUMENT: " + docResult.getDocumentName());
            System.out.println("----------------------------------------");
            printDocumentResult(docResult);
            System.out.println();
        }
        
        for (Map.Entry<String, String> failed : batchResult.getFailedDocuments().entrySet()) {
            System.out.println("----------------------------------------");
            System.out.println("DOCUMENT: " + failed.getKey());
            System.out.println("----------------------------------------");
            System.out.println("Status: FAILED");
            System.out.println("Reason: " + failed.getValue());
            System.out.println();
        }
        
        BatchSummary summary = summaryService.generateSummary(batchResult);
        printSummary(summary);
    }
    
    private static void handleSearch(Scanner scanner, EntitySearchService searchService) {
        if (currentResults.isEmpty()) {
            System.out.println("Please analyze a document first.");
            return;
        }
        
        System.out.print("Search entity: ");
        String query = scanner.nextLine().trim();
        
        if (query.isEmpty()) {
            System.out.println("Please enter an entity to search.");
            return;
        }
        
        List<EntitySearchResult> searchResults = searchService.search(currentResults, query);
        
        System.out.println("\nSearch Results:\n");
        if (searchResults.isEmpty()) {
            System.out.println("No matching entities found.");
        } else {
            for (EntitySearchResult r : searchResults) {
                System.out.println("Document: " + r.getDocumentName());
                System.out.println("Entity: " + r.getEntity().getText());
                System.out.println("Type: " + r.getEntity().getType() + "\n");
            }
        }
    }
    
    private static void handleFilter(Scanner scanner, EntityFilterService filterService) {
        if (currentResults.isEmpty()) {
            System.out.println("Please analyze a document first.");
            return;
        }
        
        System.out.print("Enter entity type to filter (e.g. PERSON, LOCATION, or ALL): ");
        String type = scanner.nextLine().trim();
        
        if (type.isEmpty()) {
            type = "ALL";
        }
        
        List<DocumentResult> filteredResults = filterService.filterByType(currentResults, type);
        System.out.println("\nFiltered Results (" + type.toUpperCase() + "):\n");
        
        for (DocumentResult docResult : filteredResults) {
            System.out.println("DOCUMENT: " + docResult.getDocumentName());
            if (docResult.getEntities().isEmpty()) {
                System.out.println("No matching entities found in this document.");
            } else {
                for (Entity e : docResult.getEntities()) {
                    System.out.println(e.getText() + " -> " + e.getType());
                }
            }
            System.out.println();
        }
    }
    
    private static void handleExport(Scanner scanner, CsvExporter csv, JsonExporter json, TxtExporter txt) {
        if (currentResults.isEmpty()) {
            System.out.println("Please analyze a document first.");
            return;
        }
        
        System.out.println("Choose format:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. TXT");
        System.out.print("Option: ");
        
        String formatOpt = scanner.nextLine().trim();
        
        System.out.print("Enter output directory (default: output/): ");
        String dirInput = scanner.nextLine().trim();
        if (dirInput.isEmpty()) {
            dirInput = "output";
        }
        Path dirPath = Paths.get(dirInput);
        
        try {
            if (formatOpt.equals("1")) {
                Path file = dirPath.resolve("ner-results.csv");
                csv.export(currentResults, file);
                System.out.println("Exported successfully to: " + file.toAbsolutePath());
            } else if (formatOpt.equals("2")) {
                Path file = dirPath.resolve("ner-results.json");
                json.export(currentResults, file);
                System.out.println("Exported successfully to: " + file.toAbsolutePath());
            } else if (formatOpt.equals("3")) {
                Path file = dirPath.resolve("ner-results.txt");
                txt.export(currentResults, file);
                System.out.println("Exported successfully to: " + file.toAbsolutePath());
            } else {
                System.out.println("Invalid format selected.");
            }
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }
    
    private static void printDocumentResult(DocumentResult result) {
        if (result.getEntities().isEmpty()) {
            System.out.println("No named entities were found.");
        } else {
            for (Entity e : result.getEntities()) {
                System.out.println("Entity: " + e.getText());
                System.out.println("Type: " + e.getType() + "\n");
            }
        }
        System.out.println("----------------------------------------");
        System.out.println("Total Entities: " + result.getTotalEntities());
        System.out.println("Processing Time: " + result.getProcessingTimeMillis() + " ms");
    }

    private static void printSummary(BatchSummary summary) {
        System.out.println("========================================");
        System.out.println("OVERALL SUMMARY");
        System.out.println("========================================");
        System.out.println("\nDocuments Submitted    : " + summary.getTotalSubmitted());
        System.out.println("Successfully Processed : " + summary.getSuccessful());
        System.out.println("Failed                 : " + summary.getFailed());
        System.out.println("\nTotal Entities         : " + summary.getTotalEntities());
        
        if (summary.getSuccessful() > 0) {
            System.out.println("\nDocument-wise count:");
            for (Map.Entry<String, Integer> entry : summary.getDocumentCounts().entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue() + " entities");
            }
        }
        
        if (summary.getTotalEntities() > 0) {
            System.out.println("\nEntity Type Summary:");
            for (Map.Entry<String, Integer> entry : summary.getTypeCounts().entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        }
        System.out.println();
    }
}
