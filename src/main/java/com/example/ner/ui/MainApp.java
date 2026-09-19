package com.example.ner.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setTitle("NER Document Analyzer - Stanford Named Entity Recognition Tool");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
