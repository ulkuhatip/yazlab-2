package com.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class GraphRenderer {

    private final Canvas canvas;

    public GraphRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void draw(Graph graph,
                     List<Node> highlightedNodes,
                     Node selected1,
                     Node selected2,
                     boolean isConnectMode,
                     double mouseX,
                     double mouseY) {

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // 1. Изчистване на екрана
        gc.clearRect(0, 0, w, h);
        
        // Лек фон
        gc.setFill(Color.web("#fcfcfc"));
        gc.fillRect(0, 0, w, h);

        // ============================
        // 2. РИСУВАНЕ НА ВРЪЗКИ (EDGES)
        // ============================
        for (Edge e : graph.edges) {
            boolean isPath = isEdgeInPath(e, highlightedNodes);
            
            boolean isConnectedToSelected = (selected1 != null) && (e.source == selected1 || e.target == selected1);

            if (isPath) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(3);
            } else if (isConnectedToSelected) {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(3);
            } else {
                // ОБИКНОВЕНА ВРЪЗКА: Сиво и тънко
                gc.setStroke(Color.LIGHTGRAY);
                gc.setLineWidth(1);
            }

            // Рисуваме самата линия
            gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);

            // ПОКАЗВАНЕ НА ТЕГЛОТО (Ако е свързана с избрания)
            if (isConnectedToSelected) {
                double midX = (e.source.x + e.target.x) / 2;
                double midY = (e.source.y + e.target.y) / 2;

                gc.setFill(Color.WHITE);
                gc.fillOval(midX - 12, midY - 12, 24, 24);
                
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                String weightText = String.format("%.2f", e.cost); // Без запетаи
                gc.fillText(weightText, midX - 4 * weightText.length(), midY + 4);
            }
        }

        // ============================
        // 3. РИСУВАНЕ НА ВЪЗЛИ (NODES)
        // ============================
        for (Node n : graph.nodes) {
            double radius = 15;
            
            if (n == selected1) {
                gc.setFill(Color.ORANGE);      
            } else if (n == selected2) {
                gc.setFill(Color.RED);          
            } else if (highlightedNodes.contains(n)) {
                gc.setFill(Color.LIGHTGREEN);   
            } else if (n.colorIndex > 0) {
                gc.setFill(getColorByIndex(n.colorIndex)); 
            } else {
                gc.setFill(Color.LIGHTBLUE);    
            }

            gc.fillOval(n.x - radius, n.y - radius, radius * 2, radius * 2);
            
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1);
            gc.strokeOval(n.x - radius, n.y - radius, radius * 2, radius * 2);

            // Име на човека (над кръгчето)
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 11));
            gc.fillText(n.name, n.x - 15, n.y - 18);
        }

        // ============================
        // 4. GHOST LINE (when connecting)
        // ============================
        if (isConnectMode && selected1 != null) {
            gc.save();
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1.5);
            gc.setLineDashes(10); // Пунктирана линия
            gc.strokeLine(selected1.x, selected1.y, mouseX, mouseY);
            gc.restore();
        }
    }

    private boolean isEdgeInPath(Edge e, List<Node> highlightedNodes) {
        if (highlightedNodes == null || highlightedNodes.size() < 2) return false;
        
        for (int i = 0; i < highlightedNodes.size() - 1; i++) {
            Node n1 = highlightedNodes.get(i);
            Node n2 = highlightedNodes.get(i + 1);
            
            if ((e.source == n1 && e.target == n2) ||
                (e.source == n2 && e.target == n1)) {
                return true;
            }
        }
        return false;
    }

    private Color getColorByIndex(int index) {
        switch (index % 6) {
            case 1: return Color.TOMATO;
            case 2: return Color.VIOLET;
            case 3: return Color.LIMEGREEN;
            case 4: return Color.GOLD;
            case 5: return Color.CYAN;
            default: return Color.GRAY;
        }
    }
}