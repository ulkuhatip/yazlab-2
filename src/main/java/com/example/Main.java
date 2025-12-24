package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Main extends Application {

    private final Graph graph = new Graph();

    private final Canvas canvas = new Canvas(900, 600);
    private final TextArea infoArea = new TextArea();

    // ✅ Single source of truth for selection/connect state
    private final InteractionState state = new InteractionState();

    // Ghost line mouse coords
    private double mouseX, mouseY;

    private GraphRenderer renderer;
    private InteractionController interactionController;
    private AnimationManager animationManager;

    // Highlighted nodes (path / search results)
    private List<Node> highlightedNodes = new ArrayList<>();

    // --- Employee Info Panel labels ---
    private final Label lblId = new Label("-");
    private final Label lblName = new Label("-");
    private final Label lblActivity = new Label("-");
    private final Label lblInteraction = new Label("-");
    private final Label lblProjects = new Label("-");
    private final Label lblTitle = new Label("Select a person");

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

        // Core helpers
        renderer = new GraphRenderer(canvas);
        interactionController = new InteractionController(graph, infoArea);
        animationManager = new AnimationManager();

        BorderPane root = new BorderPane();

        // --- 1. MENU BAR ---
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem itemOpen = new MenuItem("Open JSON...");
        MenuItem itemSave = new MenuItem("Save JSON...");
        MenuItem itemExit = new MenuItem("Exit");

        Menu menuTools = new Menu("Tools");
        MenuItem itemGenerate = new MenuItem("Generate Random Data...");

        menuFile.getItems().addAll(itemOpen, itemSave, new SeparatorMenuItem(), itemExit);
        menuTools.getItems().add(itemGenerate);
        menuBar.getMenus().addAll(menuFile, menuTools);
        root.setTop(menuBar);

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
            generateRandomData(50);
            infoArea.setText("Generated 50 random employees. Try 'Compare Algorithms' now!");
            resetSelection();
            draw();
        });

        itemExit.setOnAction(e -> System.exit(0));

        // --- 2. CENTER (Canvas) ---
        root.setCenter(canvas);

        // Make canvas responsive
        canvas.widthProperty().bind(root.widthProperty().subtract(250));
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

        HBox row1 = new HBox(10);
        Button btnBFS = new Button("BFS");
        Button btnDFS = new Button("DFS");
        Button btnDijkstra = new Button("Dijkstra (Anim)");
        Button btnAStar = new Button("A* (Anim)");
        Button btnCompare = new Button("⚡ Compare All");
        btnCompare.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: black; -fx-font-weight: bold;");
        row1.getChildren().addAll(new Label("Search:"), btnBFS, btnDFS, btnDijkstra, btnAStar, btnCompare);

        HBox row2 = new HBox(10);
        Button btnComponents = new Button("Islands (Color)");
        Button btnColor = new Button("Coloring (WP)");
        Button btnCentrality = new Button("Top Leaders");
        Button btnReset = new Button("Reset View");
        row2.getChildren().addAll(new Label("Analyze:"), btnComponents, btnColor, btnCentrality, btnReset);

        HBox row3 = new HBox(10);
        Button btnAddEdge = new Button("➕ Connect");
        Button btnRemoveEdge = new Button("❌ Disconnect");
        Label lblHint = new Label("(Left-click 2 nodes to select)");
        row3.getChildren().addAll(new Label("Edit:"), btnAddEdge, btnRemoveEdge, lblHint);

        bottomContainer.getChildren().addAll(row1, row2, row3, infoArea);
        infoArea.setPrefHeight(60);
        root.setBottom(bottomContainer);

        // --- 5. EVENT HANDLERS ---

        btnBFS.setOnAction(e -> {
            if (state.selected1 != null) {
                highlightedNodes = GraphAlgorithms.runBFS(graph, state.selected1);
                infoArea.setText("BFS: Found " + highlightedNodes.size() + " reachable people from " + state.selected1.name);
                draw();
            } else {
                infoArea.setText("Select 1 person to start BFS.");
            }
        });

        btnDFS.setOnAction(e -> {
            if (state.selected1 != null) {
                highlightedNodes = GraphAlgorithms.runDFS(graph, state.selected1);
                infoArea.setText("DFS: Found " + highlightedNodes.size() + " reachable people from " + state.selected1.name);
                draw();
            } else {
                infoArea.setText("Select 1 person to start DFS.");
            }
        });

        btnDijkstra.setOnAction(e -> {
            if (state.selected1 != null && state.selected2 != null) {
                List<Node> path = GraphAlgorithms.runDijkstra(graph, state.selected1, state.selected2);
                if (path.isEmpty()) {
                    infoArea.setText("No path found.");
                    highlightedNodes.clear();
                    draw();
                } else {
                    infoArea.setText("Dijkstra Path: " + path.size() + " steps. Animating...");
                    animationManager.animatePath(path, highlightedNodes, this::draw);
                }
            } else {
                infoArea.setText("Select 2 people for Dijkstra.");
            }
        });

        btnAStar.setOnAction(e -> {
            if (state.selected1 != null && state.selected2 != null) {
                List<Node> path = GraphAlgorithms.runAStar(graph, state.selected1, state.selected2);
                if (path.isEmpty()) {
                    infoArea.setText("No path found.");
                    highlightedNodes.clear();
                    draw();
                } else {
                    infoArea.setText("A* Path: " + path.size() + " steps. Animating...");
                    animationManager.animatePath(path, highlightedNodes, this::draw);
                }
            } else {
                infoArea.setText("Select 2 people for A*.");
            }
        });

        btnCompare.setOnAction(e -> {
            if (state.selected1 != null && state.selected2 != null) {
                StringBuilder result = new StringBuilder("🏆 Algorithm Performance Race 🏆\n");
                result.append("Route: ").append(state.selected1.name).append(" ➔ ").append(state.selected2.name).append("\n\n");

                long startD = System.nanoTime();
                List<Node> pathD = GraphAlgorithms.runDijkstra(graph, state.selected1, state.selected2);
                long endD = System.nanoTime();
                double timeD = (endD - startD) / 1_000_000.0;
                result.append(String.format("🔹 Dijkstra:\n   Time: %.4f ms\n   Steps: %d\n\n", timeD, pathD.size()));

                long startA = System.nanoTime();
                List<Node> pathA = GraphAlgorithms.runAStar(graph, state.selected1, state.selected2);
                long endA = System.nanoTime();
                double timeA = (endA - startA) / 1_000_000.0;
                result.append(String.format("🔸 A* (A-Star):\n   Time: %.4f ms\n   Steps: %d\n\n", timeA, pathA.size()));

                long startBFS = System.nanoTime();
                List<Node> reachBFS = GraphAlgorithms.runBFS(graph, state.selected1);
                boolean canReach = reachBFS.contains(state.selected2);
                long endBFS = System.nanoTime();
                double timeBFS = (endBFS - startBFS) / 1_000_000.0;
                result.append(String.format("🔹 BFS (Scan):\n   Time: %.4f ms\n   Reachable: %s\n", timeBFS, canReach ? "YES" : "NO"));

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Performance Benchmark");
                alert.setHeaderText("Race Results");
                alert.setContentText(result.toString());
                alert.showAndWait();
            } else {
                infoArea.setText("Select 2 people to compare algorithms!");
            }
        });

        btnComponents.setOnAction(e -> {
            int count = GraphAlgorithms.countAndColorComponents(graph);
            infoArea.setText("Found " + count + " disconnected communities (Islands). They are now colored.");
            draw();
        });

        btnColor.setOnAction(e -> {
            GraphAlgorithms.runColoring(graph);
            draw();
            infoArea.setText("Graph colored using Welsh-Powell algorithm.");
        });

        btnCentrality.setOnAction(e -> {
            List<Node> top = GraphAlgorithms.getTopCentrality(graph);

            for (Node n : graph.nodes) n.colorIndex = 0;

            if (!top.isEmpty()) {
                StringBuilder sb = new StringBuilder("Top 5 Influencers:\n");
                int count = Math.min(5, top.size());
                for (int i = 0; i < count; i++) {
                    Node n = top.get(i);
                    n.colorIndex = i + 1;
                    sb.append(String.format("%d. %s (%d connections)\n", i + 1, n.name, graph.getDegree(n)));
                }
                infoArea.setText(sb.toString());
                draw();
            }
        });

        btnReset.setOnAction(e -> {
            resetSelection();
            draw();
            infoArea.setText("View cleared.");
        });

        btnAddEdge.setOnAction(e -> {
            interactionController.startConnectMode(state, highlightedNodes);
            draw();
        });

        btnRemoveEdge.setOnAction(e -> {
            if (state.selected1 != null && state.selected2 != null) {
                graph.removeEdge(state.selected1, state.selected2);
                draw();
                infoArea.setText("Disconnected: " + state.selected1.name + " & " + state.selected2.name);
            } else {
                infoArea.setText("Select 2 people first.");
            }
        });

        // --- 6. CONTEXT MENU ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemAddNode = new MenuItem("Add Person Here");
        MenuItem itemEditNode = new MenuItem("Edit Person");
        MenuItem itemDeleteNode = new MenuItem("Delete Person");
        contextMenu.getItems().addAll(itemAddNode, itemEditNode, itemDeleteNode);

        canvas.setOnContextMenuRequested(e -> {
            Node clickedNode = interactionController.findNodeAt(e.getX(), e.getY());

            if (clickedNode != null) {
                itemAddNode.setVisible(false);
                itemEditNode.setVisible(true);
                itemDeleteNode.setVisible(true);

                itemEditNode.setOnAction(ev -> openNodeDialog(clickedNode, false));
                itemDeleteNode.setOnAction(ev -> {
                    graph.removeNode(clickedNode);

                    if (state.selected1 == clickedNode) state.selected1 = null;
                    if (state.selected2 == clickedNode) state.selected2 = null;

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

        // --- 7. MOUSE EVENTS (hover + ghost + click) ---
        canvas.setOnMouseMoved(e -> {
            // Hover cursor
            Node hovered = interactionController.findNodeAt(e.getX(), e.getY());
            canvas.setCursor(hovered != null ? Cursor.HAND : Cursor.DEFAULT);

            // Ghost line tracking
            if (state.isConnectMode && state.selected1 != null) {
                mouseX = e.getX();
                mouseY = e.getY();
                draw();
            }
        });

        canvas.setOnMouseClicked(e -> {
            contextMenu.hide();

            if (e.getButton() == MouseButton.PRIMARY) {
                interactionController.handleClick(e.getX(), e.getY(), state, highlightedNodes);

                // Optional: show right panel info when selecting in NORMAL mode
                if (!state.isConnectMode && state.selected1 != null) {
                    showNodeInfo(state.selected1);
                }

                draw();
            }
        });

        Scene scene = new Scene(root, 1000, 750);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                interactionController.resetConnectMode(state);
                draw();
            }
        });

        primaryStage.setTitle("HR Social Network Analysis");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        draw(); // Initial draw
    }

    // -------------------------------------------------------------------------
    // Helper UI
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Data helpers
    // -------------------------------------------------------------------------

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
            graph.addNode(new Node(
                i,
                "Emp " + i,
                rand.nextDouble() * (w - 50) + 25,
                rand.nextDouble() * (h - 50) + 25,
                rand.nextDouble() * 10,
                rand.nextInt(100),
                rand.nextInt(20)
            ));
        }

        for (Node n : graph.nodes) {
            int connections = rand.nextInt(3) + 1;
            for (int k = 0; k < connections; k++) {
                Node t = graph.nodes.get(rand.nextInt(count));
                if (t != n) graph.addEdge(n, t);
            }
        }
    }

    private void resetSelection() {
        state.selected1 = null;
        state.selected2 = null;
        state.isConnectMode = false;

        highlightedNodes.clear();
        for (Node n : graph.nodes) n.colorIndex = 0;

        clearNodeInfo();
    }

    // -------------------------------------------------------------------------
    // Drawing entry points
    // -------------------------------------------------------------------------

    private void draw() {
        redraw();
    }

    private void redraw() {
        renderer.draw(
            graph,
            highlightedNodes,
            state.selected1,
            state.selected2,
            state.isConnectMode,
            mouseX,
            mouseY
        );
    }
}

