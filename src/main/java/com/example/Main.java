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
    // --- Connect Mode & Ghost Line ---
    private boolean isConnectMode = false;
    private double mouseX, mouseY;

    
    // List to store nodes that should be highlighted (Path or Search Result)
    private List<Node> highlightedNodes = new ArrayList<>();

    // --- Employee Info Panel labels ---
    private Label lblId = new Label("-");
    private Label lblName = new Label("-");
    private Label lblActivity = new Label("-");
    private Label lblInteraction = new Label("-");
    private Label lblProjects = new Label("-");
    private Label lblTitle = new Label("Select a person");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Load default file if exists
        File defaultFile = new File("data.json");
        if(defaultFile.exists()) {
            JsonLoader.load("data.json", graph);
        }

        BorderPane root = new BorderPane();

        // --- 1. MENU BAR ---
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem itemOpen = new MenuItem("Open JSON...");
        MenuItem itemSave = new MenuItem("Save JSON...");
        MenuItem itemExit = new MenuItem("Exit");
        Menu menuTools = new Menu("Tools");
        MenuItem itemGenerate = new MenuItem("Generate Random Data...");

        // Menu Actions
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
                infoArea.setText("File saved successfully!");
            }
        });

        itemGenerate.setOnAction(e -> {
            generateRandomData(20);
            infoArea.setText("Generated 20 random employees.");
            resetSelection();
            draw();
        });

        itemExit.setOnAction(e -> System.exit(0));
        
        menuFile.getItems().addAll(itemOpen, itemSave, new SeparatorMenuItem(), itemExit);
        menuTools.getItems().add(itemGenerate);
        menuBar.getMenus().addAll(menuFile, menuTools);
        root.setTop(menuBar);

        canvas.setOnMouseMoved(e -> {
            if (isConnectMode && selected1 != null) {
                mouseX = e.getX();
                mouseY = e.getY();
                draw();
            }
        });


        // --- 2. CENTER (Canvas) ---
        root.setCenter(canvas);
        // Make canvas responsive
        canvas.widthProperty().bind(root.widthProperty().subtract(250)); // subtract right panel width
        canvas.heightProperty().bind(root.heightProperty().subtract(200));
        
        // Redraw when resized
        canvas.widthProperty().addListener(evt -> draw());
        canvas.heightProperty().addListener(evt -> draw());

        // --- 3. RIGHT PANEL (Info) ---
        GridPane infoPanel = new GridPane();
        infoPanel.setPadding(new Insets(10));
        infoPanel.setHgap(10);
        infoPanel.setVgap(8);
        infoPanel.setPrefWidth(240);
        infoPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");

        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        infoPanel.add(lblTitle, 0, 0, 2, 1);
        infoPanel.add(new Separator(), 0, 1, 2, 1);

        infoPanel.add(new Label("ID:"), 0, 2);          infoPanel.add(lblId, 1, 2);
        infoPanel.add(new Label("Name:"), 0, 3);        infoPanel.add(lblName, 1, 3);
        infoPanel.add(new Label("Activity:"), 0, 4);    infoPanel.add(lblActivity, 1, 4);
        infoPanel.add(new Label("Interactions:"), 0, 5);infoPanel.add(lblInteraction, 1, 5);
        infoPanel.add(new Label("Projects:"), 0, 6);    infoPanel.add(lblProjects, 1, 6);

        root.setRight(infoPanel);

        // --- 4. BOTTOM PANEL (Buttons) ---
        VBox bottomContainer = new VBox(8);
        bottomContainer.setPadding(new Insets(10));
        bottomContainer.setStyle("-fx-background-color: #e0e0e0;");

        // Row 1: Search & Pathfinding (BFS, DFS, Dijkstra, A*)
        HBox row1 = new HBox(10);
        Button btnBFS = new Button("BFS (Reach)");
        Button btnDFS = new Button("DFS (Reach)");
        Button btnDijkstra = new Button("Dijkstra (Path)");
        Button btnAStar = new Button("A* (Path)");
        row1.getChildren().addAll(new Label("Search:"), btnBFS, btnDFS, btnDijkstra, btnAStar);

        // Row 2: Analysis (Components, Color, Centrality)
        HBox row2 = new HBox(10);
        Button btnComponents = new Button("Count Components");
        Button btnColor = new Button("Coloring (Welsh-Powell)");
        Button btnCentrality = new Button("Top Leaders");
        Button btnReset = new Button("Reset View");
        row2.getChildren().addAll(new Label("Analyze:"), btnComponents, btnColor, btnCentrality, btnReset);

        // Row 3: Editing (Add/Remove Edge)
        HBox row3 = new HBox(10);
        Button btnAddEdge = new Button("➕ Connect Selected");
        Button btnRemoveEdge = new Button("❌ Disconnect");
        Label lblHint = new Label("(Left-click 2 nodes to select)");
        row3.getChildren().addAll(new Label("Edit:"), btnAddEdge, btnRemoveEdge, lblHint);

        bottomContainer.getChildren().addAll(row1, row2, row3, infoArea);
        infoArea.setPrefHeight(60);
        root.setBottom(bottomContainer);

        // --- 5. EVENT HANDLERS (Buttons calling GraphAlgorithms) ---

        // BFS
        btnBFS.setOnAction(e -> {
            if (selected1 != null) {
                highlightedNodes = GraphAlgorithms.runBFS(graph, selected1);
                infoArea.setText("BFS: Found " + highlightedNodes.size() + " reachable people from " + selected1.name);
                draw();
            } else infoArea.setText("Select 1 person to start BFS.");
        });

        // DFS
        btnDFS.setOnAction(e -> {
            if (selected1 != null) {
                highlightedNodes = GraphAlgorithms.runDFS(graph, selected1);
                infoArea.setText("DFS: Found " + highlightedNodes.size() + " reachable people from " + selected1.name);
                draw();
            } else infoArea.setText("Select 1 person to start DFS.");
        });

        // Dijkstra
        btnDijkstra.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                highlightedNodes = GraphAlgorithms.runDijkstra(graph, selected1, selected2);
                infoArea.setText(highlightedNodes.isEmpty() ? "No path found." : "Dijkstra Path: " + highlightedNodes.size() + " steps.");
                draw();
            } else infoArea.setText("Select 2 people for Dijkstra.");
        });

        // A* (A-Star)
        btnAStar.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                highlightedNodes = GraphAlgorithms.runAStar(graph, selected1, selected2);
                infoArea.setText(highlightedNodes.isEmpty() ? "No path found." : "A* Path: " + highlightedNodes.size() + " steps.");
                draw();
            } else infoArea.setText("Select 2 people for A*.");
        });

        // Components
        btnComponents.setOnAction(e -> {
            int count = GraphAlgorithms.countConnectedComponents(graph);
            infoArea.setText("Number of disconnected communities (Islands): " + count);
        });

        // Coloring
        btnColor.setOnAction(e -> {
            GraphAlgorithms.runColoring(graph);
            draw();
            infoArea.setText("Graph colored using Welsh-Powell algorithm.");
        });

        // Centrality
      // Centrality (Top 5 Leaders with Colors)
        btnCentrality.setOnAction(e -> {
            // 1. Взимаме списъка, сортиран по важност
            List<Node> top = GraphAlgorithms.getTopCentrality(graph);
            
            // 2. Изчистваме старите цветове на всички
            for(Node n : graph.nodes) n.colorIndex = 0;

            if (!top.isEmpty()) {
                StringBuilder sb = new StringBuilder("Top 5 Influencers:\n");
                
                // 3. Взимаме само първите 5 (или по-малко, ако нямаме 5 човека)
                int count = Math.min(5, top.size());
                
                for(int i = 0; i < count; i++) {
                    Node n = top.get(i);
                    
                    // 4. Задаваме цвят според ранга (1=Orange, 2=Cyan, 3=Magenta...)
                    // Това използва логиката, която вече имаш в Node.java
                    n.colorIndex = i + 1; 
                    
                    sb.append(String.format("%d. %s (%d connections)\n", i+1, n.name, graph.getDegree(n)));
                }
                infoArea.setText(sb.toString());
                
                // 5. Прерисуваме екрана, за да се видят цветовете
                draw();
            } else {
                infoArea.setText("No data to analyze.");
            }
        });

        // Reset
        btnReset.setOnAction(e -> {
            resetSelection();
            draw();
            infoArea.setText("View cleared.");
        });

        // Edit Edges
        btnAddEdge.setOnAction(e -> {
            resetSelection();
            isConnectMode = true;
            infoArea.setText("Connect mode: select first person");
        });

        btnRemoveEdge.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                graph.removeEdge(selected1, selected2);
                draw();
                infoArea.setText("Disconnected: " + selected1.name + " & " + selected2.name);
            } else infoArea.setText("Select 2 people first.");
        });


        // --- 6. MOUSE & CONTEXT MENU ---
        
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemAddNode = new MenuItem("Add Person Here");
        MenuItem itemEditNode = new MenuItem("Edit Person");
        MenuItem itemDeleteNode = new MenuItem("Delete Person");
        contextMenu.getItems().addAll(itemAddNode, itemEditNode, itemDeleteNode);

        // Right Click Logic
        canvas.setOnContextMenuRequested(e -> {
            Node clickedNode = findNodeAt(e.getX(), e.getY());
            
            if (clickedNode != null) {
                // Clicked on Node
                itemAddNode.setVisible(false);
                itemEditNode.setVisible(true);
                itemDeleteNode.setVisible(true);

                itemEditNode.setOnAction(ev -> openNodeDialog(clickedNode, false));
                itemDeleteNode.setOnAction(ev -> {
                    graph.removeNode(clickedNode);
                    if(selected1 == clickedNode) selected1 = null;
                    if(selected2 == clickedNode) selected2 = null;
                    clearNodeInfo();
                    draw();
                    infoArea.setText("Deleted: " + clickedNode.name);
                });
            } else {
                // Clicked on Empty Space
                itemAddNode.setVisible(true);
                itemEditNode.setVisible(false);
                itemDeleteNode.setVisible(false);

                itemAddNode.setOnAction(ev -> {
                    Node newNode = new Node(getNextId(), "New Employee", e.getX(), e.getY(), 5.0, 50, 5);
                    openNodeDialog(newNode, true);
                });
            }
            contextMenu.show(canvas, e.getScreenX(), e.getScreenY());
        });

        // Left Click Logic
        canvas.setOnMouseClicked(e -> {
            contextMenu.hide();
            if (e.getButton() == MouseButton.PRIMARY) {
                handleClick(e.getX(), e.getY());
                draw();
            }
        });

        draw(); // Initial draw

        Scene scene = new Scene(root, 1000, 750);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                resetConnectMode();
                draw();
            }
        });
        primaryStage.setTitle("HR Social Network Analysis");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

// --- HELPERS ---

    private void handleClick(double x, double y) {
        Node clicked = findNodeAt(x, y);

        // Boş alana tıklandıysa → iptal
        if (clicked == null) {
            if (isConnectMode) {
                resetConnectMode();
                infoArea.setText("Connect mode cancelled.");
                draw();
            }
            clearNodeInfo();
            return;
        }

        // NORMAL MODE → sadece seçim
        if (!isConnectMode) {
            selected1 = clicked;
            selected2 = null;
            highlightedNodes.clear();
            showNodeInfo(clicked);
            infoArea.setText("Selected: " + clicked.name);
            return;
        }

        // CONNECT MODE
        if (selected1 == null) {
            selected1 = clicked;
            showNodeInfo(clicked);
            infoArea.setText("First selected: " + clicked.name);
            return;
        }

        // Aynı node'a tıklanırsa → yok say
        if (clicked == selected1) {
            infoArea.setText("Select a different person to connect.");
            return;
        }

        // Gerçek bağlantı
        graph.addEdge(selected1, clicked);
        infoArea.setText("Connected: " + selected1.name + " & " + clicked.name);
        resetConnectMode();
        draw();
    }

    private void resetConnectMode() {
        isConnectMode = false;
        selected1 = null;
        selected2 = null;
    }


    private void showNodeInfo(Node n) {
        lblTitle.setText("Employee Info");
        lblId.setText(String.valueOf(n.id));
        lblName.setText(n.name);
        lblActivity.setText(String.valueOf(n.activity));
        lblInteraction.setText(String.valueOf(n.interaction));
        lblProjects.setText(String.valueOf(n.projects));
    }

    private void clearNodeInfo() {
        lblTitle.setText("Select a person");
        lblId.setText("-"); lblName.setText("-");
        lblActivity.setText("-"); lblInteraction.setText("-"); lblProjects.setText("-");
    }

    private void openNodeDialog(Node node, boolean isNew) {
        Dialog<Node> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add Person" : "Edit Person");
        dialog.setHeaderText("Enter details:");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        TextField nameField = new TextField(node.name);
        TextField actField = new TextField(String.valueOf(node.activity));
        TextField interField = new TextField(String.valueOf(node.interaction));
        TextField projField = new TextField(String.valueOf(node.projects));

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Activity:"), 0, 1); grid.add(actField, 1, 1);
        grid.add(new Label("Interactions:"), 0, 2); grid.add(interField, 1, 2);
        grid.add(new Label("Projects:"), 0, 3); grid.add(projField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                try {
                    node.name = nameField.getText();
                    node.activity = Double.parseDouble(actField.getText());
                    node.interaction = Integer.parseInt(interField.getText());
                    node.projects = Integer.parseInt(projField.getText());
                    return node;
                } catch (Exception e) { return null; }
            }
            return null;
        });

        Optional<Node> result = dialog.showAndWait();
        result.ifPresent(n -> {
            if (isNew) {
                graph.addNode(n);
                infoArea.setText("Added: " + n.name);
            } else {
                infoArea.setText("Updated: " + n.name);
            }
            showNodeInfo(n);
            draw();
        });
    }

    private Node findNodeAt(double x, double y) {
        for (Node n : graph.nodes) {
            if (Math.abs(x - n.x) < 20 && Math.abs(y - n.y) < 20) return n;
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
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        
        for (int i = 1; i <= count; i++) {
            graph.addNode(new Node(i, "Emp " + i, 
                rand.nextDouble() * (w-50) + 25, 
                rand.nextDouble() * (h-50) + 25, 
                rand.nextDouble()*10, rand.nextInt(100), rand.nextInt(20)));
        }
        // Connect random nodes
        for (Node n : graph.nodes) {
            int connections = rand.nextInt(3) + 1;
            for (int k=0; k<connections; k++) {
                Node t = graph.nodes.get(rand.nextInt(count));
                if (t != n) graph.addEdge(n, t);
            }
        }
    }

    private void resetSelection() {
        selected1 = null; selected2 = null; 
        highlightedNodes.clear();
        for(Node n : graph.nodes) n.colorIndex = 0;
    }

    // --- DRAWING ---
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw Edges
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

        // Draw Nodes
        for (Node n : graph.nodes) {
            boolean isSel = (n == selected1 || n == selected2);
            boolean isHigh = highlightedNodes.contains(n);
            n.draw(gc, isSel, isHigh);
        }

        // 👻 GHOST LINE 
        if (isConnectMode && selected1 != null) {
            gc.save();
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1.5);
            gc.setLineDashes(10);
            gc.strokeLine(
                selected1.x,
                selected1.y,
                mouseX,
                mouseY
            );
            gc.restore();
        }
    }


    private boolean isEdgeInPath(Edge e) {
        if (highlightedNodes.size() < 2) return false;
        // Check if edge connects two consecutive nodes in the highlighted list
        // Note: For BFS/DFS which return a set of nodes, we might not highlight edges, 
        // but for Dijkstra/A* (Paths) we do.
        // This logic assumes highlightedNodes is an ordered path.
        for (int i=0; i<highlightedNodes.size()-1; i++) {
            Node n1 = highlightedNodes.get(i);
            Node n2 = highlightedNodes.get(i+1);
            if ((e.source==n1 && e.target==n2) || (e.source==n2 && e.target==n1)) return true;
        }
        return false;
    }
}
