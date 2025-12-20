package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random; 

public class Main extends Application {
    private Graph graph = new Graph();
    private Canvas canvas = new Canvas(900, 600);
    private TextArea infoArea = new TextArea();
    
    // Променливи за селекция и път
    private Node selected1 = null;
    private Node selected2 = null;
    private List<Node> highlightedPath = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // --- Load default data if exists ---
        File defaultFile = new File("data.json");
        if(defaultFile.exists()) {
            JsonLoader.load("data.json", graph);
        }

        BorderPane root = new BorderPane();

        // --- Menu ---
        MenuBar menuBar = new MenuBar();
        
        // Menu File
        Menu menuFile = new Menu("File");
        MenuItem itemOpen = new MenuItem("Open JSON...");
        MenuItem itemSave = new MenuItem("Save JSON...");
        MenuItem itemExit = new MenuItem("Exit");
        
        // Menu Tools
        Menu menuTools = new Menu("Tools");
        MenuItem itemGenerate = new MenuItem("Generate 100 Random People");

        // --- Menubar ---

        itemOpen.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose JSON File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                JsonLoader.load(file.getAbsolutePath(), graph);
                infoArea.setText("Load File: " + file.getName());
                resetSelection();
                draw();
            }
        });

        itemSave.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Safe JSON File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            fileChooser.setInitialFileName("data.json");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                JsonLoader.save(file.getAbsolutePath(), graph);
                infoArea.setText("File is load successfully!");
            }
        });

        // generating 100 new people
        itemGenerate.setOnAction(e -> {
            generateRandomData(100); // Create 100 random nodes and edges
            infoArea.setText("100 people ramdomly generated. Press save to keep the data.");
            resetSelection();
            draw();
        });

        itemExit.setOnAction(e -> System.exit(0));
        
        menuFile.getItems().addAll(itemOpen, itemSave, new SeparatorMenuItem(), itemExit);
        menuTools.getItems().add(itemGenerate);
        
        menuBar.getMenus().addAll(menuFile, menuTools);
        root.setTop(menuBar);

        // --- Center and Buttons ---
        root.setCenter(canvas);
        
        HBox controls = new HBox(10);
        Button btnDijkstra = new Button("Dijkstra (Path)");
        Button btnColor = new Button("Coloring");
        Button btnCentrality = new Button("Top Leaders");
        Button btnReset = new Button("Reset View");

        controls.getChildren().addAll(btnDijkstra, btnColor, btnCentrality, btnReset);
        
        BorderPane bottomPanel = new BorderPane();
        bottomPanel.setTop(controls); 
        infoArea.setPrefHeight(100);
        bottomPanel.setCenter(infoArea);
        root.setBottom(bottomPanel);

        draw();

        // --- Events ---
        canvas.setOnMouseClicked(e -> {
            handleClick(e.getX(), e.getY());
            draw();
        });

        btnDijkstra.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                highlightedPath = graph.runDijkstra(selected1, selected2);
                infoArea.setText(highlightedPath.isEmpty() ? "No connection between " + selected1.name + " and " + selected2.name + "." : "Route found: " + highlightedPath.size() + " steps.");
                draw();
            } else infoArea.setText("Please choose two people!");
        });

        btnColor.setOnAction(e -> {
            graph.runColoring();
            draw();
            infoArea.setText("Coloring done!");
        });

        btnCentrality.setOnAction(e -> {
            List<Node> top = graph.getTopCentrality();
            if (!top.isEmpty()) {
                infoArea.setText("Lider: " + top.get(0).name + " (" + graph.getDegree(top.get(0)) + " edges)");
            }
        });

        btnReset.setOnAction(e -> {
            resetSelection();
            draw();
            infoArea.setText("Reset done.");
        });

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("HR Network - Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- Generating Random Data ---
    private void generateRandomData(int count) {
        graph.nodes.clear();
        graph.edges.clear();
        Random rand = new Random();

        // 1. Creating people
        for (int i = 1; i <= count; i++) {
            String name = "Employee " + i;
            // For coordinates within canvas
            double x = rand.nextDouble() * 800 + 50; 
            double y = rand.nextDouble() * 500 + 50; 
            
            double activity = Math.round(rand.nextDouble() * 10.0 * 100.0) / 100.0; // Число 0-10
            int interaction = rand.nextInt(100);
            int projects = rand.nextInt(20) + 1;

            graph.addNode(new Node(i, name, x, y, activity, interaction, projects));
        }

        // 2. Creating edges (Everybody gets 1-3 random connections)
        for (Node n : graph.nodes) {
            int connectionsCount = rand.nextInt(3) + 1; // Everybody gets at leasy 1 connection
            for (int j = 0; j < connectionsCount; j++) {
                Node randomTarget = graph.nodes.get(rand.nextInt(count));
                
                // Avoid self-loops
                if (randomTarget != n) {
                    graph.addEdge(n, randomTarget);
                }
            }
        }
    }

    private void resetSelection() {
        selected1 = null; selected2 = null; highlightedPath.clear();
        for(Node n : graph.nodes) n.colorIndex = 0;
    }

    // --- Drawing ---
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // drawing edges
        gc.setLineWidth(1);
        for (Edge e : graph.edges) {
            if (isEdgeInPath(e)) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(3);
                gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
                gc.setLineWidth(1);
            } else {
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
            }
        }

        // Dranwing nodes
        for (Node n : graph.nodes) {
            boolean isSel = (n == selected1 || n == selected2);
            boolean isHigh = highlightedPath.contains(n);
            n.draw(gc, isSel, isHigh);
        }
    }

    private void handleClick(double x, double y) {
        for (Node n : graph.nodes) {
            if (Math.abs(x - n.x) < 20 && Math.abs(y - n.y) < 20) {
                if (selected1 == null) selected1 = n;
                else if (selected2 == null && n != selected1) selected2 = n;
                else { selected1 = n; selected2 = null; highlightedPath.clear(); }
                
                infoArea.setText("Choosed: " + n.name + " (ID: " + n.id + ")");
                return;
            }
        }
    }

    private boolean isEdgeInPath(Edge e) {
        if (highlightedPath.size() < 2) return false;
        for (int i=0; i<highlightedPath.size()-1; i++) {
            Node n1 = highlightedPath.get(i);
            Node n2 = highlightedPath.get(i+1);
            if ((e.source==n1 && e.target==n2) || (e.source==n2 && e.target==n1)) return true;
        }
        return false;
    }
}