package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random; 

public class Main extends Application {
    private Graph graph = new Graph();
    private Canvas canvas = new Canvas(900, 600);
    private TextArea infoArea = new TextArea();
    
    // Selection variables
    private Node selected1 = null;
    private Node selected2 = null;
    private List<Node> highlightedPath = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        File defaultFile = new File("data.json");
        if(defaultFile.exists()) {
            JsonLoader.load("data.json", graph);
        }

        BorderPane root = new BorderPane();

        // --- MENU BAR ---
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem itemOpen = new MenuItem("Open JSON...");
        MenuItem itemSave = new MenuItem("Save JSON...");
        MenuItem itemExit = new MenuItem("Exit");
        Menu menuTools = new Menu("Tools");
        MenuItem itemGenerate = new MenuItem("Generate Random Data...");

        // Actions
        itemOpen.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                JsonLoader.load(file.getAbsolutePath(), graph);
                infoArea.setText("Loaded file: " + file.getName());
                resetSelection();
                draw();
            }
        });

        itemSave.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("data.json");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                JsonLoader.save(file.getAbsolutePath(), graph);
                infoArea.setText("File is successfully saved!");
            }
        });

        itemGenerate.setOnAction(e -> {
            generateRandomData(100);
            infoArea.setText("100 new employees have been generated.");
            resetSelection();
            draw();
        });

        itemExit.setOnAction(e -> System.exit(0));
        
        menuFile.getItems().addAll(itemOpen, itemSave, new SeparatorMenuItem(), itemExit);
        menuTools.getItems().add(itemGenerate);
        menuBar.getMenus().addAll(menuFile, menuTools);
        root.setTop(menuBar);

        // --- CENTER ---
        root.setCenter(canvas);
        
        // --- BUTTONS (Bottom) ---
        VBox bottomContainer = new VBox(10);
        bottomContainer.setPadding(new Insets(10));
        
        // Row 1: Algorithms
        HBox algoControls = new HBox(10);
        Button btnDijkstra = new Button("Dijkstra (shortest path)");
        Button btnColor = new Button("Coloring");
        Button btnCentrality = new Button("Liders");
        Button btnReset = new Button("Clear");
        algoControls.getChildren().addAll(btnDijkstra, btnColor, btnCentrality, btnReset);

        // Row 2: Editing Edges (НОВО!)
        HBox editControls = new HBox(10);
        Button btnAddEdge = new Button("➕ Connect the selected people");
        Button btnRemoveEdge = new Button("❌ Remove the selected connection");
        Label lblHint = new Label("(Choose two people with left click and then use the buttons!)");
        editControls.getChildren().addAll(btnAddEdge, btnRemoveEdge, lblHint);

        bottomContainer.getChildren().addAll(algoControls, editControls, infoArea);
        infoArea.setPrefHeight(80);
        root.setBottom(bottomContainer);

        draw();

        // --- MOUSE EVENTS & CONTEXT MENU (НОВО!) ---
        
        // Context Menu (Right Click)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemAddNode = new MenuItem("Add new person");
        MenuItem itemEditNode = new MenuItem("Edit data");
        MenuItem itemDeleteNode = new MenuItem("Delete data");
        
        contextMenu.getItems().addAll(itemAddNode, itemEditNode, itemDeleteNode);

        // Logic for Right Click
        canvas.setOnContextMenuRequested(e -> {
            Node clickedNode = findNodeAt(e.getX(), e.getY());
            
            if (clickedNode != null) {
                // Clicked on a Node -> Show Edit/Delete, Hide Add
                itemAddNode.setVisible(false);
                itemEditNode.setVisible(true);
                itemDeleteNode.setVisible(true);
                
                itemEditNode.setOnAction(ev -> openNodeDialog(clickedNode, false));
                itemDeleteNode.setOnAction(ev -> {
                    graph.removeNode(clickedNode);
                    if(selected1 == clickedNode) selected1 = null;
                    if(selected2 == clickedNode) selected2 = null;
                    draw();
                    infoArea.setText("Deleted: " + clickedNode.name);
                });
            } else {
                // Clicked on Empty Space -> Show Add, Hide Edit/Delete
                itemAddNode.setVisible(true);
                itemEditNode.setVisible(false);
                itemDeleteNode.setVisible(false);
                
                itemAddNode.setOnAction(ev -> {
                    Node newNode = new Node(getNextId(), "New User", e.getX(), e.getY(), 5.0, 50, 5);
                    openNodeDialog(newNode, true); // True means "isNew"
                });
            }
            contextMenu.show(canvas, e.getScreenX(), e.getScreenY());
        });

        // Left Click Logic
        canvas.setOnMouseClicked(e -> {
            contextMenu.hide(); // Hide menu if visible
            if (e.getButton() == MouseButton.PRIMARY) {
                handleClick(e.getX(), e.getY());
                draw();
            }
        });

        // --- BUTTON ACTIONS ---
        
        // Add Edge
        btnAddEdge.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                graph.addEdge(selected1, selected2);
                draw();
                infoArea.setText("Connected: " + selected1.name + " и " + selected2.name);
            } else {
                infoArea.setText("Error: Please select two people to connect!");
            }
        });

        // Remove Edge
        btnRemoveEdge.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                graph.removeEdge(selected1, selected2);
                draw();
                infoArea.setText("Edge removed between: " + selected1.name + " and " + selected2.name);
            } else {
                infoArea.setText("select two people to remove an edge!");
            }
        });

        btnDijkstra.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                highlightedPath = graph.runDijkstra(selected1, selected2);
                infoArea.setText(highlightedPath.isEmpty() ? "No road found." : "Found road: " + highlightedPath.size() + " steps.");
                draw();
            } else infoArea.setText("Please select two nodes!");
        });

        btnColor.setOnAction(e -> {
            graph.runColoring();
            draw();
            infoArea.setText("Graph has been colored!");
        });

        btnCentrality.setOnAction(e -> {
            List<Node> top = graph.getTopCentrality();
            if (!top.isEmpty()) {
                infoArea.setText("Lider: " + top.get(0).name + " (" + graph.getDegree(top.get(0)) + " connections)");
            }
        });

        btnReset.setOnAction(e -> {
            resetSelection();
            draw();
            infoArea.setText("The view has been reset.");
        });

        Scene scene = new Scene(root, 900, 750); // Increased height for new buttons
        primaryStage.setTitle("HR Network - Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- DIALOG FOR ADD/EDIT (НОВО!) ---
    private void openNodeDialog(Node node, boolean isNew) {
        Dialog<Node> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add new person" : "Edit data");
        dialog.setHeaderText("Enter person data:");

        // Set the button types
        ButtonType loginButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the labels and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(node.name);
        TextField actField = new TextField(String.valueOf(node.activity));
        TextField interField = new TextField(String.valueOf(node.interaction));
        TextField projField = new TextField(String.valueOf(node.projects));

        grid.add(new Label("Име:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Activity (0-10):"), 0, 1);
        grid.add(actField, 1, 1);
        grid.add(new Label("Interactions:"), 0, 2);
        grid.add(interField, 1, 2);
        grid.add(new Label("Projects:"), 0, 3);
        grid.add(projField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    node.name = nameField.getText();
                    node.activity = Double.parseDouble(actField.getText());
                    node.interaction = Integer.parseInt(interField.getText());
                    node.projects = Integer.parseInt(projField.getText());
                    return node;
                } catch (Exception ex) {
                    return null; // Error in parsing
                }
            }
            return null;
        });

        Optional<Node> result = dialog.showAndWait();

        result.ifPresent(updatedNode -> {
            if (isNew) {
                graph.addNode(updatedNode);
                infoArea.setText("New added: " + updatedNode.name);
            } else {
                infoArea.setText("Edited: " + updatedNode.name);
            }
            draw();
        });
    }

    // --- HELPERS ---
    private Node findNodeAt(double x, double y) {
        for (Node n : graph.nodes) {
            if (Math.abs(x - n.x) < 20 && Math.abs(y - n.y) < 20) {
                return n;
            }
        }
        return null;
    }

    private int getNextId() {
        int max = 0;
        for (Node n : graph.nodes) if(n.id > max) max = n.id;
        return max + 1;
    }

    private void generateRandomData(int count) {
        graph.nodes.clear();
        graph.edges.clear();
        Random rand = new Random();
        for (int i = 1; i <= count; i++) {
            String name = "Employee " + i;
            double x = rand.nextDouble() * 800 + 50; 
            double y = rand.nextDouble() * 500 + 50; 
            graph.addNode(new Node(i, name, x, y, rand.nextDouble()*10, rand.nextInt(100), rand.nextInt(20)));
        }
        for (Node n : graph.nodes) {
            int connections = rand.nextInt(3) + 1; 
            for (int j = 0; j < connections; j++) {
                Node target = graph.nodes.get(rand.nextInt(count));
                if (target != n) graph.addEdge(n, target);
            }
        }
    }

    private void resetSelection() {
        selected1 = null; selected2 = null; highlightedPath.clear();
        for(Node n : graph.nodes) n.colorIndex = 0;
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

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

        for (Node n : graph.nodes) {
            boolean isSel = (n == selected1 || n == selected2);
            boolean isHigh = highlightedPath.contains(n);
            n.draw(gc, isSel, isHigh);
        }
    }

    private void handleClick(double x, double y) {
        Node clicked = findNodeAt(x, y);
        if (clicked != null) {
            if (selected1 == null) selected1 = clicked;
            else if (selected2 == null && clicked != selected1) selected2 = clicked;
            else { selected1 = clicked; selected2 = null; highlightedPath.clear(); }
            infoArea.setText("Selected: " + clicked.name);
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