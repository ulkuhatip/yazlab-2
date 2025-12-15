package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private Graph graph = new Graph();
    private Canvas canvas = new Canvas(900, 600);
    private TextArea infoArea = new TextArea();
    
    // Състояние на селекцията
    private Node selected1 = null;
    private Node selected2 = null;
    private List<Node> highlightedPath = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Зареждане
        JsonLoader.load("data.json", graph);

        BorderPane root = new BorderPane();
        
        // 1. Canvas (Център)
        root.setCenter(canvas);
        draw();

        // 2. Бутони (Горе)
        HBox controls = new HBox(10);
        Button btnDijkstra = new Button("Намери Път (Dijkstra)");
        Button btnColor = new Button("Оцвети (Welsh-Powell)");
        Button btnCentrality = new Button("Топ Лидери");
        Button btnReset = new Button("Изчисти");

        controls.getChildren().addAll(btnDijkstra, btnColor, btnCentrality, btnReset);
        
        // 3. Инфо Панел (Долу)
        BorderPane bottomPanel = new BorderPane();
        bottomPanel.setTop(controls);
        infoArea.setPrefHeight(100);
        infoArea.setEditable(false);
        bottomPanel.setCenter(infoArea);
        
        root.setBottom(bottomPanel);

        // --- EVENTS (Събития) ---

        // Клик с мишката
        canvas.setOnMouseClicked(e -> {
            handleClick(e.getX(), e.getY());
            draw();
        });

        btnDijkstra.setOnAction(e -> {
            if (selected1 != null && selected2 != null) {
                highlightedPath = graph.runDijkstra(selected1, selected2);
                if (highlightedPath.isEmpty()) {
                    infoArea.setText("Няма намерен път между тези служители.");
                } else {
                    infoArea.setText("Най-къс път намерен! Дължина: " + highlightedPath.size() + " човека.");
                }
                draw();
            } else {
                infoArea.setText("Грешка: Трябва да изберете ДВАМА служители с мишката!");
            }
        });

        btnColor.setOnAction(e -> {
            graph.runColoring();
            infoArea.setText("Графът е оцветен по групи (няма свързани хора с еднакъв цвят).");
            draw();
        });

        btnCentrality.setOnAction(e -> {
            List<Node> top = graph.getTopCentrality();
            StringBuilder sb = new StringBuilder("ТОП 5 НАЙ-ВЛИЯТЕЛНИ СЛУЖИТЕЛИ:\n");
            for (int i = 0; i < Math.min(5, top.size()); i++) {
                Node n = top.get(i);
                sb.append((i+1) + ". " + n.name + " (Връзки: " + graph.getDegree(n) + ")\n");
            }
            infoArea.setText(sb.toString());
        });
        
        btnReset.setOnAction(e -> {
            selected1 = null;
            selected2 = null;
            highlightedPath.clear();
            for(Node n : graph.nodes) n.colorIndex = 0; // Махаме цветовете
            infoArea.setText("Изчистено.");
            draw();
        });

        Scene scene = new Scene(root, 900, 750);
        primaryStage.setTitle("HR Social Network Analysis");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineWidth(2);

        // 1. Рисуване на връзките
        for (Edge e : graph.edges) {
            if (isEdgeInPath(e)) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(4);
                gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
                gc.setLineWidth(2); // Връщаме за другите
            } else {
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
            }
        }

        // 2. Рисуване на възлите
        for (Node n : graph.nodes) {
            boolean isSel = (n == selected1 || n == selected2);
            boolean isHigh = highlightedPath.contains(n);
            n.draw(gc, isSel, isHigh);
        }
    }

    private void handleClick(double x, double y) {
        for (Node n : graph.nodes) {
            // Проверка дали сме кликнали върху кръгчето (радиус 20)
            if (Math.abs(x - n.x) < 20 && Math.abs(y - n.y) < 20) {
                if (selected1 == null) {
                    selected1 = n;
                } else if (selected2 == null && n != selected1) {
                    selected2 = n;
                } else {
                    // Ресет на избора
                    selected1 = n;
                    selected2 = null;
                    highlightedPath.clear();
                }
                
                // Показване на инфо
                String info = "Служител: " + n.name + 
                              "\nPerformance: " + n.activity + 
                              "\nMessages: " + n.interaction + 
                              "\nProjects: " + n.projects;
                infoArea.setText(info);
                return;
            }
        }
    }

    private boolean isEdgeInPath(Edge e) {
        if (highlightedPath.size() < 2) return false;
        for (int i = 0; i < highlightedPath.size() - 1; i++) {
            Node n1 = highlightedPath.get(i);
            Node n2 = highlightedPath.get(i+1);
            if ((e.source == n1 && e.target == n2) || (e.source == n2 && e.target == n1)) {
                return true;
            }
        }
        return false;
    }
}