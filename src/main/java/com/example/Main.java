package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser; // <-- ВАЖНО: За прозореца с файловете
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        // Опитваме да заредим файл по подразбиране, ако има такъв
        File defaultFile = new File("data.json");
        if(defaultFile.exists()) {
            JsonLoader.load("data.json", graph);
        }

        BorderPane root = new BorderPane();

        // --- МЕНЮ (File -> Open / Save) ---
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File"); // Главното меню
        
        MenuItem itemOpen = new MenuItem("Open JSON...");
        MenuItem itemSave = new MenuItem("Save JSON...");
        MenuItem itemExit = new MenuItem("Exit");

        // Действие за БУТОНА OPEN
        itemOpen.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Избери файл от приятел");
            // Филтър да показва само .json файлове
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            
            File file = fileChooser.showOpenDialog(primaryStage); // Отваря прозореца
            if (file != null) {
                JsonLoader.load(file.getAbsolutePath(), graph);
                infoArea.setText("Успешно зареден файл: " + file.getName());
                // Ресет на селекциите, защото зареждаме нов граф
                selected1 = null; selected2 = null; highlightedPath.clear();
                draw(); // Прерисуваме всичко
            }
        });

        // Действие за БУТОНА SAVE
        itemSave.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Запази своята работа");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            fileChooser.setInitialFileName("my_graph_new.json"); // Предлага име
            
            File file = fileChooser.showSaveDialog(primaryStage); // Отваря прозореца за запис
            if (file != null) {
                JsonLoader.save(file.getAbsolutePath(), graph);
                infoArea.setText("Файлът е запазен! Можеш да го пратиш.");
            }
        });

        itemExit.setOnAction(e -> System.exit(0));
        
        // Сглобяване на менюто
        menuFile.getItems().addAll(itemOpen, itemSave, new SeparatorMenuItem(), itemExit);
        menuBar.getMenus().add(menuFile);
        
        // Слагаме менюто най-горе
        root.setTop(menuBar);

        // --- ОСТАНАЛАТА ЧАСТ (Платно и Бутони) ---
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

        draw(); // Първоначално рисуване

        // --- ЕВЕНТИ (Кликане и бутони) ---
        canvas.setOnMouseClicked(e -> {
            handleClick(e.getX(), e.getY());
            draw();
        });

        btnDijkstra.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                highlightedPath = graph.runDijkstra(selected1, selected2);
                infoArea.setText(highlightedPath.isEmpty() ? "Няма път." : "Път намерен: " + highlightedPath.size() + " стъпки.");
                draw();
            } else infoArea.setText("Моля, избери двама души (кликни върху тях)!");
        });

        btnColor.setOnAction(e -> {
            graph.runColoring();
            draw();
            infoArea.setText("Графът е оцветен по Welsh-Powell!");
        });

        btnCentrality.setOnAction(e -> {
            List<Node> top = graph.getTopCentrality();
            if (!top.isEmpty()) {
                infoArea.setText("Лидер с най-много връзки: " + top.get(0).name + " (" + graph.getDegree(top.get(0)) + " връзки)");
            }
        });

        btnReset.setOnAction(e -> {
            selected1 = null; selected2 = null; highlightedPath.clear();
            for(Node n : graph.nodes) n.colorIndex = 0;
            draw();
            infoArea.setText("Изгледът е изчистен.");
        });

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("HR Network - Student Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- МЕТОДИ ЗА РИСУВАНЕ (Draw) ---
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 1. Рисуване на връзките
        gc.setLineWidth(2);
        for (Edge e : graph.edges) {
            if (isEdgeInPath(e)) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(4);
                gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
                gc.setLineWidth(2);
            } else {
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
            }
        }

        // 2. Рисуване на хората
        for (Node n : graph.nodes) {
            boolean isSel = (n == selected1 || n == selected2);
            boolean isHigh = highlightedPath.contains(n);
            n.draw(gc, isSel, isHigh);
        }
    }

    private void handleClick(double x, double y) {
        for (Node n : graph.nodes) {
            // Проверка дали кликът е върху кръгчето (с радиус 20px)
            if (Math.abs(x - n.x) < 20 && Math.abs(y - n.y) < 20) {
                if (selected1 == null) selected1 = n;
                else if (selected2 == null && n != selected1) selected2 = n;
                else { selected1 = n; selected2 = null; highlightedPath.clear(); } // Ресет ако кликнеш трети път
                
                infoArea.setText("Избран: " + n.name + " (ID: " + n.id + ")");
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