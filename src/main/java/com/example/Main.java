package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
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
import javafx.util.Duration;

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
        if (defaultFile.exists()) {
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
            generateRandomData(50); // Generating more people to make comparison interesting
            infoArea.setText("Generated 50 random employees. Try 'Compare Algorithms' now!");
            resetSelection();
            draw();
        });

        itemExit.setOnAction(e -> System.exit(0));

        menuFile.getItems().addAll(itemOpen, itemSave, new SeparatorMenuItem(), itemExit);
        menuTools.getItems().add(itemGenerate);
        menuBar.getMenus().addAll(menuFile, menuTools);
        root.setTop(menuBar);

        // --- 2. CENTER (Canvas) ---
        root.setCenter(canvas);
        // Make canvas responsive
        canvas.widthProperty().bind(root.widthProperty().subtract(250)); // subtract right panel width
        canvas.heightProperty().bind(root.heightProperty().subtract(200)); // subtract bottom panel height

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

        infoPanel.add(new Label("ID:"), 0, 2);
        infoPanel.add(lblId, 1, 2);
        infoPanel.add(new Label("Name:"), 0, 3);
        infoPanel.add(lblName, 1, 3);
        infoPanel.add(new Label("Activity:"), 0, 4);
        infoPanel.add(lblActivity, 1, 4);
        infoPanel.add(new Label("Interactions:"), 0, 5);
        infoPanel.add(lblInteraction, 1, 5);
        infoPanel.add(new Label("Projects:"), 0, 6);
        infoPanel.add(lblProjects, 1, 6);

        root.setRight(infoPanel);

        // --- 4. BOTTOM PANEL (Buttons) ---
        VBox bottomContainer = new VBox(8);
        bottomContainer.setPadding(new Insets(10));
        bottomContainer.setStyle("-fx-background-color: #e0e0e0;");

        // Row 1: Search & Pathfinding
        HBox row1 = new HBox(10);
        Button btnBFS = new Button("BFS");
        Button btnDFS = new Button("DFS");
        Button btnDijkstra = new Button("Dijkstra (Anim)");
        Button btnAStar = new Button("A* (Anim)");
        // 🔥 NEW FEATURE 1: Comparison Button
        Button btnCompare = new Button("⚡ Compare All"); 
        btnCompare.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: black; -fx-font-weight: bold;");
        
        row1.getChildren().addAll(new Label("Search:"), btnBFS, btnDFS, btnDijkstra, btnAStar, btnCompare);

        // Row 2: Analysis
        HBox row2 = new HBox(10);
        Button btnComponents = new Button("Islands (Color)");
        Button btnColor = new Button("Coloring (WP)");
        Button btnCentrality = new Button("Top Leaders");
        Button btnReset = new Button("Reset View");
        row2.getChildren().addAll(new Label("Analyze:"), btnComponents, btnColor, btnCentrality, btnReset);

        // Row 3: Editing
        HBox row3 = new HBox(10);
        Button btnAddEdge = new Button("➕ Connect");
        Button btnRemoveEdge = new Button("❌ Disconnect");
        Label lblHint = new Label("(Left-click 2 nodes to select)");
        row3.getChildren().addAll(new Label("Edit:"), btnAddEdge, btnRemoveEdge, lblHint);

        bottomContainer.getChildren().addAll(row1, row2, row3, infoArea);
        infoArea.setPrefHeight(60);
        root.setBottom(bottomContainer);

        // --- 5. EVENT HANDLERS ---

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

        // Dijkstra (with Animation)
        btnDijkstra.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                List<Node> path = GraphAlgorithms.runDijkstra(graph, selected1, selected2);
                if (path.isEmpty()) {
                    infoArea.setText("No path found.");
                    highlightedNodes.clear();
                    draw();
                } else {
                    infoArea.setText("Dijkstra Path: " + path.size() + " steps. Animating...");
                    // 🔥 NEW FEATURE 2: Animation call
                    animatePath(path); 
                }
            } else infoArea.setText("Select 2 people for Dijkstra.");
        });

        // A* (with Animation)
        btnAStar.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                List<Node> path = GraphAlgorithms.runAStar(graph, selected1, selected2);
                if (path.isEmpty()) {
                    infoArea.setText("No path found.");
                    highlightedNodes.clear();
                    draw();
                } else {
                    infoArea.setText("A* Path: " + path.size() + " steps. Animating...");
                    // 🔥 NEW FEATURE 2: Animation call
                    animatePath(path); 
                }
            } else infoArea.setText("Select 2 people for A*.");
        });

        // ⚡ NEW FEATURE 1: Algorithm Comparison Race
        btnCompare.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                StringBuilder result = new StringBuilder("🏆 Algorithm Performance Race 🏆\n");
                result.append("Route: ").append(selected1.name).append(" ➔ ").append(selected2.name).append("\n\n");

                // 1. Measure Dijkstra
                long startD = System.nanoTime();
                List<Node> pathD = GraphAlgorithms.runDijkstra(graph, selected1, selected2);
                long endD = System.nanoTime();
                double timeD = (endD - startD) / 1_000_000.0; // Convert to ms
                result.append(String.format("🔹 Dijkstra:\n   Time: %.4f ms\n   Steps: %d\n\n", timeD, pathD.size()));

                // 2. Measure A*
                long startA = System.nanoTime();
                List<Node> pathA = GraphAlgorithms.runAStar(graph, selected1, selected2);
                long endA = System.nanoTime();
                double timeA = (endA - startA) / 1_000_000.0;
                result.append(String.format("🔸 A* (A-Star):\n   Time: %.4f ms\n   Steps: %d\n\n", timeA, pathA.size()));

                // 3. Measure BFS (Reachability check)
                long startBFS = System.nanoTime();
                List<Node> reachBFS = GraphAlgorithms.runBFS(graph, selected1);
                boolean canReach = reachBFS.contains(selected2);
                long endBFS = System.nanoTime();
                double timeBFS = (endBFS - startBFS) / 1_000_000.0;
                result.append(String.format("🔹 BFS (Scan):\n   Time: %.4f ms\n   Reachable: %s\n", timeBFS, canReach ? "YES" : "NO"));

                // Show Alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Performance Benchmark");
                alert.setHeaderText("Race Results");
                alert.setContentText(result.toString());
                alert.showAndWait();

            } else {
                infoArea.setText("Select 2 people to compare algorithms!");
            }
        });

        // Components (Islands with Coloring)
        btnComponents.setOnAction(e -> {
            // Uses the new countAndColor method
            int count = GraphAlgorithms.countAndColorComponents(graph);
            infoArea.setText("Found " + count + " disconnected communities (Islands). They are now colored.");
            draw();
        });

        // Coloring
        btnColor.setOnAction(e -> {
            GraphAlgorithms.runColoring(graph);
            draw();
            infoArea.setText("Graph colored using Welsh-Powell algorithm.");
        });

        // Centrality (Top 5 Colored)
        btnCentrality.setOnAction(e -> {
            List<Node> top = GraphAlgorithms.getTopCentrality(graph);
            // Clear old colors
            for (Node n : graph.nodes) n.colorIndex = 0;

            if (!top.isEmpty()) {
                StringBuilder sb = new StringBuilder("Top 5 Influencers:\n");
                int count = Math.min(5, top.size());
                for (int i = 0; i < count; i++) {
                    Node n = top.get(i);
                    n.colorIndex = i + 1; // Assign color based on rank
                    sb.append(String.format("%d. %s (%d connections)\n", i + 1, n.name, graph.getDegree(n)));
                }
                infoArea.setText(sb.toString());
                draw(); // Redraw to show colors
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
            if (selected1 != null && selected2 != null) {
                graph.addEdge(selected1, selected2);
                draw();
                infoArea.setText("Connected: " + selected1.name + " & " + selected2.name);
            } else infoArea.setText("Select 2 people first.");
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
                itemAddNode.setVisible(false);
                itemEditNode.setVisible(true);
                itemDeleteNode.setVisible(true);

                itemEditNode.setOnAction(ev -> openNodeDialog(clickedNode, false));
                itemDeleteNode.setOnAction(ev -> {
                    graph.removeNode(clickedNode);
                    if (selected1 == clickedNode) selected1 = null;
                    if (selected2 == clickedNode) selected2 = null;
                    clearNodeInfo();
                    draw();
                    infoArea.setText("Deleted: " + clickedNode.name);
                });
            } else {
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

        // 🔥 NEW FEATURE 3: Mouse Hover Effect (Tooltip/Cursor)
        canvas.setOnMouseMoved(e -> {
            Node hovered = findNodeAt(e.getX(), e.getY());
            if (hovered != null) {
                canvas.setCursor(Cursor.HAND);
                // Optional: Show quick info in bottom area or label
                // infoArea.setText("Hovering: " + hovered.name); 
            } else {
                canvas.setCursor(Cursor.DEFAULT);
            }
        });

        draw(); // Initial draw

        Scene scene = new Scene(root, 1000, 750);
        primaryStage.setTitle("HR Social Network Analysis");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- NEW: PATH ANIMATION METHOD ---
    private void animatePath(List<Node> path) {
        if (path.isEmpty()) return;

        // Clear previous
        highlightedNodes.clear();
        draw();

        Timeline timeline = new Timeline();

        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            // Add a frame for each node with delay
            KeyFrame kf = new KeyFrame(Duration.millis(i * 150), e -> {
                highlightedNodes.add(node);
                draw();
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }

    // --- HELPERS ---

    private void handleClick(double x, double y) {
        Node clicked = findNodeAt(x, y);
        if (clicked != null) {
            if (selected1 == null) {
                selected1 = clicked;
            } else if (selected2 == null && clicked != selected1) {
                selected2 = clicked;
            } else {
                selected1 = clicked;
                selected2 = null;
                highlightedNodes.clear();
            }
            showNodeInfo(clicked);
            infoArea.setText("Selected: " + clicked.name);
        } else {
            clearNodeInfo();
        }
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
        lblId.setText("-");
        lblName.setText("-");
        lblActivity.setText("-");
        lblInteraction.setText("-");
        lblProjects.setText("-");
    }

    private void openNodeDialog(Node node, boolean isNew) {
        Dialog<Node> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add Person" : "Edit Person");
        dialog.setHeaderText("Enter details:");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        TextField nameField = new TextField(node.name);
        TextField actField = new TextField(String.valueOf(node.activity));
        TextField interField = new TextField(String.valueOf(node.interaction));
        TextField projField = new TextField(String.valueOf(node.projects));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Activity:"), 0, 1);
        grid.add(actField, 1, 1);
        grid.add(new Label("Interactions:"), 0, 2);
        grid.add(interField, 1, 2);
        grid.add(new Label("Projects:"), 0, 3);
        grid.add(projField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                try {
                    node.name = nameField.getText();
                    node.activity = Double.parseDouble(actField.getText());
                    node.interaction = Integer.parseInt(interField.getText());
                    node.projects = Integer.parseInt(projField.getText());
                    return node;
                } catch (Exception e) {
                    return null;
                }
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
        for (Node n : graph.nodes) if (n.id > max) max = n.id;
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
                    rand.nextDouble() * (w - 50) + 25,
                    rand.nextDouble() * (h - 50) + 25,
                    rand.nextDouble() * 10, rand.nextInt(100), rand.nextInt(20)));
        }
        // Connect random nodes
        for (Node n : graph.nodes) {
            int connections = rand.nextInt(3) + 1;
            for (int k = 0; k < connections; k++) {
                Node t = graph.nodes.get(rand.nextInt(count));
                if (t != n) graph.addEdge(n, t);
            }
        }
    }

    private void resetSelection() {
        selected1 = null;
        selected2 = null;
        highlightedNodes.clear();
        for (Node n : graph.nodes) n.colorIndex = 0;
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
    }

    private boolean isEdgeInPath(Edge e) {
        if (highlightedNodes.size() < 2) return false;
        // Check if edge connects two nodes currently in the highlighted list
        // Important for animation: we only highlight edges if both ends are revealed
        for (int i = 0; i < highlightedNodes.size() - 1; i++) {
            Node n1 = highlightedNodes.get(i);
            Node n2 = highlightedNodes.get(i + 1);
            if ((e.source == n1 && e.target == n2) || (e.source == n2 && e.target == n1)) return true;
        }
        return false;
    }
}