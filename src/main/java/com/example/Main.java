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

public class Main extends Application {

    private final Graph graph = new Graph();
    private final Canvas canvas = new Canvas(900, 600);
    private final TextArea infoArea = new TextArea();
    
    // Таблица за резултатите (изискване за визуализация + таблица)
    private final TableView<Node> resultTable = new TableView<>();

    // Single source of truth for selection/connect state
    private final InteractionState state = new InteractionState();

    // Ghost line mouse coords
    private double mouseX, mouseY;

    private GraphRenderer renderer;
    private InteractionController interactionController;
    private AnimationManager animationManager;
    private GraphController graphController; // Нашият логически контролер

    // Highlighted nodes (path / search results)
    private final List<Node> highlightedNodes = new ArrayList<>();

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

        // --- 0. НАСТРОЙКА НА ТАБЛИЦАТА ---
        resultTable.setPrefHeight(150);
        
        TableColumn<Node, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().id));
        colId.setPrefWidth(50);

        TableColumn<Node, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().name));
        colName.setPrefWidth(150);

        TableColumn<Node, Number> colActivity = new TableColumn<>("Activity");
        colActivity.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().activity));
        colActivity.setPrefWidth(100);

        resultTable.getColumns().addAll(colId, colName, colActivity);

        // Core helpers
        renderer = new GraphRenderer(canvas);
        interactionController = new InteractionController(graph, infoArea);
        animationManager = new AnimationManager();
        
        // ВАЖНО: Подаваме и resultTable на контролера
        graphController = new GraphController(graph, infoArea, animationManager, resultTable, this::draw);

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
                
                // Обновяваме таблицата с всички заредени хора
                resultTable.getItems().clear();
                resultTable.getItems().addAll(graph.nodes);
                
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
            graphController.generateRandomData(canvas.getWidth(), canvas.getHeight(), 50);
            resetSelection();
        });

        itemExit.setOnAction(e -> System.exit(0));

        // --- 2. CENTER (Canvas) ---
        root.setCenter(canvas);
        canvas.widthProperty().bind(root.widthProperty().subtract(250));
        canvas.heightProperty().bind(root.heightProperty().subtract(250)); // Малко повече място заради таблицата
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

        // --- 4. BOTTOM PANEL (Buttons + Table) ---
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

        // Добавяме и таблицата най-отдолу
        bottomContainer.getChildren().addAll(row1, row2, row3, infoArea, resultTable);
        infoArea.setPrefHeight(50);
        
        root.setBottom(bottomContainer);

        // --- 5. EVENT HANDLERS ---
        // Делегираме всичко на GraphController

        btnBFS.setOnAction(e -> graphController.runBFS(state, highlightedNodes));
        btnDFS.setOnAction(e -> graphController.runDFS(state, highlightedNodes));
        btnDijkstra.setOnAction(e -> graphController.runDijkstra(state, highlightedNodes));
        btnAStar.setOnAction(e -> graphController.runAStar(state, highlightedNodes));
        btnCompare.setOnAction(e -> graphController.runComparison(state));
        btnComponents.setOnAction(e -> graphController.runComponents());
        btnColor.setOnAction(e -> graphController.runColoring());
        btnCentrality.setOnAction(e -> graphController.runCentrality());

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

                itemEditNode.setOnAction(ev -> {
                    NodeFormDialog.open(clickedNode, false, graph, infoArea, this::draw);
                    showNodeInfo(clickedNode);
                });
                
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
                    Node newNode = new Node(graphController.getNextId(), "New Employee", e.getX(), e.getY(), 5.0, 50, 5);
                    NodeFormDialog.open(newNode, true, graph, infoArea, this::draw);
                });
            }
            contextMenu.show(canvas, e.getScreenX(), e.getScreenY());
        });

        // --- 7. MOUSE EVENTS ---
        canvas.setOnMouseMoved(e -> {
            Node hovered = interactionController.findNodeAt(e.getX(), e.getY());
            canvas.setCursor(hovered != null ? Cursor.HAND : Cursor.DEFAULT);
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
                if (!state.isConnectMode && state.selected1 != null) {
                    showNodeInfo(state.selected1);
                }
                draw();
            }
        });

        Scene scene = new Scene(root, 1000, 800); // Малко по-висок прозорец заради таблицата
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

        draw();
    }

    // -------------------------------------------------------------------------
    // UI Helpers (Right Panel)
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

    private void resetSelection() {
        state.selected1 = null;
        state.selected2 = null;
        state.isConnectMode = false;
        highlightedNodes.clear();
        for (Node n : graph.nodes) n.colorIndex = 0;
        
        resultTable.getItems().clear(); // Чистим и таблицата при ресет
        clearNodeInfo();
    }

    private void draw() {
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