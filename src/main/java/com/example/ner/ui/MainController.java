package com.example.ner.ui;

import com.example.ner.document.BatchDocumentAnalysisService;
import com.example.ner.document.DocumentAnalysisService;
import com.example.ner.export.CsvExporter;
import com.example.ner.export.JsonExporter;
import com.example.ner.export.TxtExporter;
import com.example.ner.model.BatchResult;
import com.example.ner.model.DocumentResult;
import com.example.ner.model.Entity;
import com.example.ner.ner.StanfordNERService;
import com.example.ner.search.EntityFilterService;
import com.example.ner.search.EntitySearchResult;
import com.example.ner.search.EntitySearchService;
import com.example.ner.statistics.SummaryService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    @FXML private Button analyzeBtn;
    @FXML private Button analyzeMultipleBtn;
    @FXML private Button clearBtn;
    
    @FXML private ListView<String> documentList;
    @FXML private TextArea documentText;
    
    @FXML private TableView<Entity> entityTable;
    @FXML private TableColumn<Entity, String> entityCol;
    @FXML private TableColumn<Entity, String> typeCol;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    
    @FXML private Label summaryDocsLabel;
    @FXML private Label summaryEntitiesLabel;
    @FXML private Label summaryProcessingLabel;
    
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    // Services
    private StanfordNERService nerService;
    private DocumentAnalysisService analysisService;
    private BatchDocumentAnalysisService batchService;
    private SummaryService summaryService;
    private EntitySearchService searchService;
    private EntityFilterService filterService;
    private CsvExporter csvExporter;
    private JsonExporter jsonExporter;
    private TxtExporter txtExporter;

    // State
    private List<DocumentResult> globalResults = new ArrayList<>();
    private List<DocumentResult> visibleResults = new ArrayList<>();
    private Map<String, String> documentTexts = new HashMap<>();

    @FXML
    public void initialize() {
        entityCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        filterCombo.getItems().add("ALL");
        filterCombo.getSelectionModel().selectFirst();
        
        documentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayDocument(newVal);
            }
        });
        
        filterCombo.setOnAction(e -> applyFilterAndSearch());

        setLoadingState("Initializing Stanford CoreNLP...", true);
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                nerService = new StanfordNERService();
                analysisService = new DocumentAnalysisService(nerService);
                batchService = new BatchDocumentAnalysisService(analysisService);
                summaryService = new SummaryService();
                searchService = new EntitySearchService();
                filterService = new EntityFilterService();
                csvExporter = new CsvExporter();
                jsonExporter = new JsonExporter();
                txtExporter = new TxtExporter();
                return null;
            }
        };
        initTask.setOnSucceeded(e -> {
            setLoadingState("Ready", false);
            updateEmptyState();
        });
        initTask.setOnFailed(e -> {
            setLoadingState("Failed to initialize NLP pipeline.", false);
            showError("Initialization Error", "Failed to load Stanford CoreNLP. Check memory and dependencies.");
        });
        new Thread(initTask).start();
    }
    
    private void setLoadingState(String status, boolean isLoading) {
        Platform.runLater(() -> {
            statusLabel.setText("Status: " + status);
            progressBar.setVisible(isLoading);
            analyzeBtn.setDisable(isLoading);
            analyzeMultipleBtn.setDisable(isLoading);
        });
    }
    
    @FXML
    public void onAnalyzeDocument() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            processDocuments(Collections.singletonList(file.toPath()));
        }
    }
    
    @FXML
    public void onAnalyzeMultiple() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        List<File> files = chooser.showOpenMultipleDialog(getStage());
        if (files != null && !files.isEmpty()) {
            List<Path> paths = files.stream().map(File::toPath).collect(Collectors.toList());
            processDocuments(paths);
        }
    }
    
    private void processDocuments(List<Path> paths) {
        setLoadingState("Analyzing " + paths.size() + " document(s)...", true);
        
        Task<BatchResult> task = new Task<>() {
            @Override
            protected BatchResult call() throws Exception {
                // Read texts into memory for UI display
                for (Path p : paths) {
                    try {
                        String content = Files.readString(p);
                        documentTexts.put(p.getFileName().toString(), content);
                    } catch (Exception ex) {
                        // ignore here, DocumentAnalysisService will catch it and log in BatchResult
                    }
                }
                return batchService.analyzeDocuments(paths);
            }
        };
        
        task.setOnSucceeded(e -> {
            BatchResult result = task.getValue();
            globalResults = new ArrayList<>(result.getSuccessfulResults());
            visibleResults = new ArrayList<>(globalResults);
            
            updateFilterOptions();
            documentList.getItems().clear();
            for (DocumentResult dr : globalResults) {
                documentList.getItems().add(dr.getDocumentName());
            }
            if (!globalResults.isEmpty()) {
                documentList.getSelectionModel().selectFirst();
            } else {
                updateEmptyState();
            }
            
            if (!result.getFailedDocuments().isEmpty()) {
                showError("Analysis Errors", "Some documents failed to process:\n" + result.getFailedDocuments().keySet());
            }
            setLoadingState("Ready", false);
        });
        
        task.setOnFailed(e -> {
            setLoadingState("Analysis Failed", false);
            showError("Analysis Error", "An error occurred during processing.");
        });
        
        new Thread(task).start();
    }
    
    @FXML
    public void onSearch() {
        applyFilterAndSearch();
    }
    
    private void applyFilterAndSearch() {
        if (globalResults.isEmpty()) return;
        
        String selectedType = filterCombo.getValue();
        List<DocumentResult> filtered = filterService.filterByType(globalResults, selectedType);
        
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            List<EntitySearchResult> searchRes = searchService.search(filtered, query);
            
            List<DocumentResult> searchFiltered = new ArrayList<>();
            for (DocumentResult doc : filtered) {
                List<Entity> matchedEntities = searchRes.stream()
                        .filter(r -> r.getDocumentName().equals(doc.getDocumentName()))
                        .map(EntitySearchResult::getEntity)
                        .collect(Collectors.toList());
                searchFiltered.add(new DocumentResult(doc.getDocumentName(), matchedEntities, doc.getProcessingTimeMillis()));
            }
            visibleResults = searchFiltered;
        } else {
            visibleResults = filtered;
        }
        
        String selectedDocName = documentList.getSelectionModel().getSelectedItem();
        if (selectedDocName != null) {
            displayDocument(selectedDocName);
        } else if (!documentList.getItems().isEmpty()) {
            documentList.getSelectionModel().selectFirst();
        }
    }
    
    private void updateFilterOptions() {
        String currentSelection = filterCombo.getValue();
        filterCombo.getItems().clear();
        filterCombo.getItems().add("ALL");
        
        List<String> types = globalResults.stream()
                .flatMap(d -> d.getEntities().stream())
                .map(Entity::getType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
                
        filterCombo.getItems().addAll(types);
        if (filterCombo.getItems().contains(currentSelection)) {
            filterCombo.getSelectionModel().select(currentSelection);
        } else {
            filterCombo.getSelectionModel().selectFirst();
        }
    }

    private void displayDocument(String docName) {
        DocumentResult selected = visibleResults.stream()
                .filter(d -> d.getDocumentName().equals(docName))
                .findFirst()
                .orElse(null);
                
        if (selected != null) {
            ObservableList<Entity> data = FXCollections.observableArrayList(selected.getEntities());
            entityTable.setItems(data);
            summaryEntitiesLabel.setText("Entities: " + selected.getEntities().size());
            summaryProcessingLabel.setText("Processing: " + selected.getProcessingTimeMillis() + " ms");
        } else {
            entityTable.setItems(FXCollections.observableArrayList());
            summaryEntitiesLabel.setText("Entities: 0");
            summaryProcessingLabel.setText("Processing: 0 ms");
        }
        
        summaryDocsLabel.setText("Documents: " + visibleResults.size());
        
        String text = documentTexts.getOrDefault(docName, "Unable to read document text.");
        documentText.setText(text);
    }
    
    @FXML
    public void onClear() {
        globalResults.clear();
        visibleResults.clear();
        documentTexts.clear();
        documentList.getItems().clear();
        entityTable.setItems(FXCollections.observableArrayList());
        documentText.setText("");
        searchField.setText("");
        filterCombo.getItems().clear();
        filterCombo.getItems().add("ALL");
        filterCombo.getSelectionModel().selectFirst();
        updateEmptyState();
        setLoadingState("Ready", false);
    }
    
    @FXML
    public void onExportCsv() { exportData("CSV"); }
    @FXML
    public void onExportJson() { exportData("JSON"); }
    @FXML
    public void onExportTxt() { exportData("TXT"); }
    
    private void exportData(String format) {
        if (globalResults.isEmpty()) {
            showError("Export Error", "No data to export. Please analyze a document first.");
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export " + format);
        chooser.setInitialFileName("ner-results." + format.toLowerCase());
        File file = chooser.showSaveDialog(getStage());
        
        if (file != null) {
            try {
                if (format.equals("CSV")) {
                    csvExporter.export(visibleResults, file.toPath());
                } else if (format.equals("JSON")) {
                    jsonExporter.export(visibleResults, file.toPath());
                } else if (format.equals("TXT")) {
                    txtExporter.export(visibleResults, file.toPath());
                }
                setLoadingState("Exported to " + file.getName(), false);
            } catch (Exception e) {
                showError("Export Error", "Failed to export data: " + e.getMessage());
            }
        }
    }
    
    private void updateEmptyState() {
        summaryDocsLabel.setText("Documents: 0");
        summaryEntitiesLabel.setText("Entities: 0");
        summaryProcessingLabel.setText("Processing: 0 ms");
        documentText.setText("No document analyzed yet.\nChoose a .txt document to begin.");
    }
    
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private Stage getStage() {
        return (Stage) analyzeBtn.getScene().getWindow();
    }
}
