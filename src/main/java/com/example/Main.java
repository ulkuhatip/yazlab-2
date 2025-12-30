package com.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main extends Application {

    private final Graph graph = new Graph();
    private final Canvas canvas = new Canvas(800, 600); // Малко по-малък, за да се събере всичко
    private final TextArea infoArea = new TextArea();
    
    // Таблица за резултатите (по снимката)
    private final TableView<Node> resultTable = new TableView<>();

    // Състояние на интеракцията
    private final InteractionState state = new InteractionState();

    // Координати на мишката за ghost line
    private double mouseX, mouseY;

    private GraphRenderer renderer;
    private InteractionController interactionController;
    private AnimationManager animationManager;
    private GraphController graphController;

    private final List<Node> highlightedNodes = new ArrayList<>();

    // --- Инфо панел етикети ---
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
        // Зареждане на данни по подразбиране
        File defaultFile = new File("data.json");
        if (defaultFile.exists()) {
            JsonLoader.load("data.json", graph);
        }

        // Helpers
        renderer = new GraphRenderer(canvas);
        interactionController = new InteractionController(graph, infoArea);
        animationManager = new AnimationManager();
        graphController = new GraphController(graph, infoArea, animationManager, this::draw, this::updateTableContent);

        BorderPane root = new BorderPane();

        // ---------------------------------------------------------
        // 1. MENU BAR (Най-горе)
        // ---------------------------------------------------------
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);

        // ---------------------------------------------------------
        // 2. LEFT PANEL - БУТОНИ (В ляво)
        // ---------------------------------------------------------
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);

        // ---------------------------------------------------------
        // 3. RIGHT PANEL - ТАБЛИЦА + ИНФО (В дясно)
        // ---------------------------------------------------------
        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);

        // ---------------------------------------------------------
        // 4. CENTER - CANVAS (По средата)
        // ---------------------------------------------------------
        Pane canvasContainer = new Pane(canvas);
        canvasContainer.setStyle("-fx-background-color: #fcfcfc; -fx-border-color: #ddd;");
        // Canvas-а да се преоразмерява с прозореца
        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());
        canvas.widthProperty().addListener(evt -> draw());
        canvas.heightProperty().addListener(evt -> draw());
        
        root.setCenter(canvasContainer);

        // ---------------------------------------------------------
        // 5. EVENT HANDLERS & SETUP
        // ---------------------------------------------------------
        setupEvents(primaryStage);
        
        // Първоначално зареждане на таблицата
        resultTable.getItems().addAll(graph.nodes);

        Scene scene = new Scene(root, 1200, 750); // По-широк екран
        primaryStage.setTitle("HR Social Network Analysis");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        draw();
    }

    // --- СЪЗДАВАНЕ НА ЛЯВ ПАНЕЛ (БУТОНИ) ---
    private VBox createLeftPanel() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        box.setPrefWidth(220);
        box.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #aaa; -fx-border-width: 0 1 0 0;");

        // Група 1: Търсене
        Label lblSearch = new Label("Search Algorithms");
        lblSearch.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        
        Button btnBFS = new Button("BFS Search");
        Button btnDFS = new Button("DFS Search");
        Button btnDijkstra = new Button("Dijkstra (Shortest)");
        Button btnAStar = new Button("A* Pathfinding");
        Button btnCompare = new Button("⚡ Compare All");
        btnCompare.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: black; -fx-font-weight: bold;");

        setFullWidth(btnBFS, btnDFS, btnDijkstra, btnAStar, btnCompare);

        // Група 2: Анализ
        Label lblAnalyze = new Label("Network Analysis");
        lblAnalyze.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Button btnComponents = new Button("Find Islands");
        Button btnColor = new Button("Color Graph (WP)");
        Button btnCentrality = new Button("Top Leaders");
        
        setFullWidth(btnComponents, btnColor, btnCentrality);

        Label lblEdit = new Label("Edit Graph");
        lblEdit.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Button btnAddEdge = new Button("➕ Connect Mode");
        Button btnRemoveEdge = new Button("❌ Disconnect Selected");
        Button btnReset = new Button("↺ Reset View");
        
        setFullWidth(btnAddEdge, btnRemoveEdge, btnReset);

      btnBFS.setOnAction(e -> {
            renderer.showPathLines = false; 
            graphController.runBFS(state, highlightedNodes);
        });
        
        btnDFS.setOnAction(e -> {
            renderer.showPathLines = false;
            graphController.runDFS(state, highlightedNodes);
        });

        btnDijkstra.setOnAction(e -> {
            renderer.showPathLines = true;
            graphController.runDijkstra(state, highlightedNodes);
        });
        
        btnAStar.setOnAction(e -> {
            renderer.showPathLines = true;
            graphController.runAStar(state, highlightedNodes);
        });
        btnCompare.setOnAction(e -> graphController.runComparison(state));
        
        btnComponents.setOnAction(e -> graphController.runComponents());
        btnColor.setOnAction(e -> graphController.runColoring());
       // Подаваме списъка, за да може контролерът да го напълни с топ лидерите
btnCentrality.setOnAction(e -> graphController.runCentrality(highlightedNodes));

       btnReset.setOnAction(e -> {
    resetSelection();
    draw();
    updateTableContent(); // Ще върне пълния списък
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
                // Обновяваме и таблицата, защото съседите се променят
                resultTable.refresh();
                infoArea.setText("Disconnected.");
            } else {
                infoArea.setText("Select 2 people first.");
            }
        });

        box.getChildren().addAll(
            lblSearch, btnBFS, btnDFS, btnDijkstra, btnAStar, btnCompare, new Separator(),
            lblAnalyze, btnComponents, btnColor, btnCentrality, new Separator(),
            lblEdit, btnAddEdge, btnRemoveEdge, btnReset
        );
        return box;
    }

    // --- СЪЗДАВАНЕ НА ДЕСЕН ПАНЕЛ (ТАБЛИЦА) ---
    private VBox createRightPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setPrefWidth(420); // По-широк за таблицата
        box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #aaa; -fx-border-width: 0 0 0 1;");

        // 1. Info Panel (горе)
        GridPane infoPanel = new GridPane();
        infoPanel.setHgap(10);
        infoPanel.setVgap(5);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        infoPanel.add(lblTitle, 0, 0, 2, 1);
        infoPanel.add(new Separator(), 0, 1, 2, 1);
        infoPanel.add(new Label("ID:"), 0, 2); infoPanel.add(lblId, 1, 2);
        infoPanel.add(new Label("Name:"), 0, 3); infoPanel.add(lblName, 1, 3);
        infoPanel.add(new Label("Activity:"), 0, 4); infoPanel.add(lblActivity, 1, 4);
        infoPanel.add(new Label("Interactions:"), 0, 5); infoPanel.add(lblInteraction, 1, 5);
        infoPanel.add(new Label("Projects:"), 0, 6); infoPanel.add(lblProjects, 1, 6);

        // 2. Log Area
        infoArea.setPrefHeight(60);
        infoArea.setWrapText(true);
        infoArea.setEditable(false);

        // 3. TABLE VIEW (Настроена по снимката)
        Label lblTable = new Label("Node Data (CSV Format)");
        lblTable.setStyle("-fx-font-weight: bold;");
        
        setupTableColumns(); // Метод за настройка на колоните

        VBox.setVgrow(resultTable, Priority.ALWAYS); // Таблицата да заема останалото място

        box.getChildren().addAll(infoPanel, infoArea, new Separator(), lblTable, resultTable);
        return box;
    }

    private void setupTableColumns() {
        // 1. DugumId
        TableColumn<Node, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().id));
        colId.setPrefWidth(40);

        TableColumn<Node, Number> colActivity = new TableColumn<>("Aktiflik");
        colActivity.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().activity));
        colActivity.setPrefWidth(60);

        TableColumn<Node, Number> colInteraction = new TableColumn<>("Etkilesim");
        colInteraction.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().interaction));
        colInteraction.setPrefWidth(70);

        TableColumn<Node, Number> colProjects = new TableColumn<>("Projeler");
        colProjects.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().projects));
        colProjects.setPrefWidth(60);

        TableColumn<Node, String> colNeighbors = new TableColumn<>("Komsular");
colNeighbors.setCellValueFactory(cell -> {
    Node currentNode = cell.getValue();
    
    // Използваме LinkedHashSet, за да няма дубликати и да пази реда на добавяне
    Set<String> uniqueNeighbors = new LinkedHashSet<>();
    
    for (Edge e : graph.edges) {

        if (e.source == currentNode) {
            uniqueNeighbors.add(String.valueOf(e.target.id));
        }
        
    }
    
    String res = String.join(", ", uniqueNeighbors);
    return new SimpleStringProperty(res.isEmpty() ? "-" : res);
});
colNeighbors.setPrefWidth(120);

        resultTable.getColumns().clear();
        resultTable.getColumns().addAll(colId, colActivity, colInteraction, colProjects, colNeighbors);
    }

    private void setFullWidth(Button... buttons) {
        for (Button b : buttons) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem itemOpen = new MenuItem("Open JSON...");
        MenuItem itemSave = new MenuItem("Save JSON...");
        MenuItem itemExit = new MenuItem("Exit");
        
        Menu menuTools = new Menu("Tools");
        MenuItem itemGenerate = new MenuItem("Generate Random Data...");

        itemOpen.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    JsonLoader.load(file.getAbsolutePath(), graph);
                    graphController.repositionNodesIfNeeded(
                    canvas.getWidth(),
                    canvas.getHeight()
                    );

                    // ✅ başarılı yükleme
                    infoArea.setText("Loaded: " + file.getName());
                    resetSelection();
                    draw(); 

                } catch (IllegalArgumentException ex) {

                    // 🔴 HATALI JSON → HER ŞEYİ TEMİZLE
                    graph.nodes.clear();
                    graph.edges.clear();

                    resetSelection();

                    // canvas temizle
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                    infoArea.setText("JSON yüklenemedi: " + file.getName());

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Veri Yükleme Hatası");
                    alert.setHeaderText("Geçersiz JSON Dosyası");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });


        itemSave.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("data.json");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) JsonLoader.save(file.getAbsolutePath(), graph);
        });

       itemGenerate.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog("20"); 
    dialog.setTitle("Generate Random Graph");
    dialog.setHeaderText("Generation Settings");
    dialog.setContentText("Please enter the number of employees:");

    dialog.showAndWait().ifPresent(input -> {
        try {
            int count = Integer.parseInt(input);
            
            if (count > 0 && count <= 2000) {
                graphController.generateRandomData(canvas.getWidth(), canvas.getHeight(), count);
                resetSelection(); 
                infoArea.setText("Generated " + count + " random nodes.");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a number between 1 and 2000.");
                alert.showAndWait();
            }
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Number");
            alert.setContentText("Please enter a valid integer number.");
            alert.showAndWait();
        }
    });
});
        itemExit.setOnAction(e -> System.exit(0));

        menuFile.getItems().addAll(itemOpen, itemSave, new SeparatorMenuItem(), itemExit);
        menuTools.getItems().add(itemGenerate);
        menuBar.getMenus().addAll(menuFile, menuTools);
        return menuBar;
    }

    private void setupEvents(Stage stage) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemAddNode = new MenuItem("Add Person");
        MenuItem itemEditNode = new MenuItem("Edit Properties");
        MenuItem itemDeleteNode = new MenuItem("Delete");

        contextMenu.getItems().addAll(itemAddNode, itemEditNode, itemDeleteNode);

        canvas.setOnContextMenuRequested(e -> {
            Node clickedNode = interactionController.findNodeAt(e.getX(), e.getY());
            if (clickedNode != null) {
                itemAddNode.setVisible(false);
                itemEditNode.setVisible(true);
                itemDeleteNode.setVisible(true);

               itemEditNode.setOnAction(ev -> {
    // 1. Отваряме диалога (той блокира докато не се затвори)
    NodeFormDialog.open(clickedNode, false, graph, infoArea, this::draw);
    
    // 2. Обновяваме инфо панела
    showNodeInfo(clickedNode);
    
    // 3. ВАЖНО: Принудително обновяване на таблицата
    // Дори данните в списъка да са същите, refresh() кара клетките да си прочетат новите имена/числа
    resultTable.refresh(); 
    
    // 4. Ако сме във Focus Mode, може да се наложи пренареждане на данните
    updateTableContent(); 
});
                itemDeleteNode.setOnAction(ev -> {
                    graph.removeNode(clickedNode);
                    if (state.selected1 == clickedNode) state.selected1 = null;
                    if (state.selected2 == clickedNode) state.selected2 = null;
                    clearNodeInfo();
                    draw();
                    refreshTable();
                });
            } else {
                itemAddNode.setVisible(true);
                itemEditNode.setVisible(false);
                itemDeleteNode.setVisible(false);
                itemAddNode.setOnAction(ev -> {
                    Node newNode = new Node(graphController.getNextId(), "New", e.getX(), e.getY(), 0.5, 10, 2);
                    NodeFormDialog.open(newNode, true, graph, infoArea, this::draw);
                    refreshTable();
                });
            }
            contextMenu.show(canvas, e.getScreenX(), e.getScreenY());
        });

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
            // НЕ селектираме реда в таблицата автоматично, за да не счупим филтъра,
            // но обновяваме съдържанието:
        }
        
        // ТУК Е КЛЮЧЪТ: Всеки клик преизчислява какво да се вижда в таблицата
        updateTableContent(); 
        
        draw();
    }
});
    
    }

    private void showNodeInfo(Node n) {
        lblId.setText(String.valueOf(n.id));
        lblName.setText(n.name);
        lblActivity.setText(String.valueOf(n.activity));
        lblInteraction.setText(String.valueOf(n.interaction));
        lblProjects.setText(String.valueOf(n.projects));
    }

    private void clearNodeInfo() {
        lblId.setText("-"); lblName.setText("-");
        lblActivity.setText("-"); lblInteraction.setText("-"); lblProjects.setText("-");
    }

    private void resetSelection() {
        state.selected1 = null;
        state.selected2 = null;
        state.isConnectMode = false;
        highlightedNodes.clear();
        for (Node n : graph.nodes) n.colorIndex = 0;
        
        refreshTable();
        clearNodeInfo();
    }
    
    private void refreshTable() {
        resultTable.getItems().clear();
        resultTable.getItems().addAll(graph.nodes);
        resultTable.refresh();
    }


    // Този метод е "мозъкът" на таблицата
private void updateTableContent() {
    resultTable.getItems().clear();

    // 1. СЛУЧАЙ: Имаме резултат от алгоритъм (highlightedNodes не е празен)
    // Това е най-висок приоритет - ако сме пуснали търсене, искаме да видим резултата.
    if (!highlightedNodes.isEmpty()) {
        resultTable.getItems().addAll(highlightedNodes);
        return;
    }

    // 2. СЛУЧАЙ: Селектиран е само 1 човек (Focus Mode)
    // Искаш да виждаш него и съседите му
    if (state.selected1 != null && state.selected2 == null) {
        // Използваме Set, за да избегнем дубликати, ако има двупосочни връзки
        Set<Node> viewData = new LinkedHashSet<>();
        
        viewData.add(state.selected1); // Добавяме главния герой
        
        // Намираме съседите
        for (Edge e : graph.edges) {
            // Тъй като пазим и двете посоки, достатъчно е да видим source
            if (e.source == state.selected1) {
                viewData.add(e.target);
            }
        }
        
        resultTable.getItems().addAll(viewData);
        return;
    }

    // 3. СЛУЧАЙ: По подразбиране (Default)
    // Няма активен алгоритъм и няма специална селекция -> покажи всичко
    resultTable.getItems().addAll(graph.nodes);
}
    private void draw() {
        renderer.draw(graph, highlightedNodes, state.selected1, state.selected2, 
                      state.isConnectMode, mouseX, mouseY);
    }
}