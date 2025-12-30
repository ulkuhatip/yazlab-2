package com.example;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import java.util.List;
import java.util.Random;

public class GraphController {

    private final Graph graph;
    private final TextArea infoArea;
    private final AnimationManager animationManager;
    
    // ВЕЧЕ НЕ пазим таблицата тук (Separation of Concerns)
    // private final TableView<Node> resultTable; 
    
    private final Runnable redrawCallback;      // За прерисуване на Canvas
    private final Runnable tableUpdateCallback; // НОВО: За обновяване на Таблицата

    // Конструкторът приема Runnable вместо TableView
    public GraphController(Graph graph, TextArea infoArea, AnimationManager animationManager, 
                           Runnable redrawCallback, Runnable tableUpdateCallback) {
        this.graph = graph;
        this.infoArea = infoArea;
        this.animationManager = animationManager;
        this.redrawCallback = redrawCallback;
        this.tableUpdateCallback = tableUpdateCallback;
    }

    public void runBFS(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null) {
            long startTime = System.nanoTime();
            List<Node> results = GraphAlgorithms.runBFS(graph, state.selected1);
            long endTime = System.nanoTime();

            // Обновяваме списъка, който Main гледа, за да попълни таблицата
            highlightedNodes.clear();
            highlightedNodes.addAll(results);
            
            infoArea.setText("BFS: Found " + highlightedNodes.size() + " reachable people from " + state.selected1.name);
            double duration = (endTime - startTime) / 1_000_000.0;
            infoArea.appendText("\nTime: " + String.format("%.4f", duration) + " ms");
            
            // КЛЮЧОВ МОМЕНТ: Казваме на Main да се обнови
            tableUpdateCallback.run(); 
            redrawCallback.run();
        } else {
            infoArea.setText("Select 1 person to start BFS.");
        }
    }

    public void runDFS(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null) {
            long startTime = System.nanoTime();
            List<Node> results = GraphAlgorithms.runDFS(graph, state.selected1);
            long endTime = System.nanoTime();

            highlightedNodes.clear();
            highlightedNodes.addAll(results);
            
            infoArea.setText("DFS: Found " + highlightedNodes.size() + " reachable people from " + state.selected1.name);
            double duration = (endTime - startTime) / 1_000_000.0;
            infoArea.appendText("\nTime: " + String.format("%.4f", duration) + " ms");
            
            tableUpdateCallback.run();
            redrawCallback.run();
        } else {
            infoArea.setText("Select 1 person to start DFS.");
        }
    }

    public void runDijkstra(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null && state.selected2 != null) {
            long startTime = System.nanoTime();
            List<Node> path = GraphAlgorithms.runDijkstra(graph, state.selected1, state.selected2);
            long endTime = System.nanoTime();

            if (path.isEmpty()) {
                infoArea.setText("No path found.");
                highlightedNodes.clear();
                
                tableUpdateCallback.run();
                redrawCallback.run();
            } else {
                infoArea.setText("Dijkstra Path: " + path.size() + " steps. Animating...");
                double duration = (endTime - startTime) / 1_000_000.0;
                infoArea.appendText("\nTime: " + String.format("%.4f", duration) + " ms");
                
                // Тук не пълним highlightedNodes веднага, защото анимацията ще го прави стъпка по стъпка.
                // Но викаме update, за да се изчисти таблицата или да покаже начално състояние.
                tableUpdateCallback.run();
                
                // Стартираме анимацията
                animationManager.animatePath(path, highlightedNodes, redrawCallback);
            }
        } else {
            infoArea.setText("Select 2 people for Dijkstra.");
        }
    }

    public void runAStar(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null && state.selected2 != null) {
            long startTime = System.nanoTime();
            List<Node> path = GraphAlgorithms.runAStar(graph, state.selected1, state.selected2);
            long endTime = System.nanoTime();
            
            if (path.isEmpty()) {
                infoArea.setText("No path found.");
                highlightedNodes.clear();
                
                tableUpdateCallback.run();
                redrawCallback.run();
            } else {
                infoArea.setText("A* Path: " + path.size() + " steps. Animating...");
                double duration = (endTime - startTime) / 1_000_000.0;
                infoArea.appendText("\nTime: " + String.format("%.4f", duration) + " ms");
                
                tableUpdateCallback.run();
                animationManager.animatePath(path, highlightedNodes, redrawCallback);
            }
        } else {
            infoArea.setText("Select 2 people for A*.");
        }
    }

    public void runComparison(InteractionState state) {
        if (state.selected1 != null && state.selected2 != null) {
            StringBuilder result = new StringBuilder("🏆 Algorithm Performance Race 🏆\n");
            result.append("Route: ").append(state.selected1.name).append(" ➔ ").append(state.selected2.name).append("\n\n");

            // 1. Dijkstra
            long startD = System.nanoTime();
            List<Node> pathD = GraphAlgorithms.runDijkstra(graph, state.selected1, state.selected2);
            long endD = System.nanoTime();
            double timeD = (endD - startD) / 1_000_000.0;
            result.append(String.format("🔹 Dijkstra:\n   Time: %.4f ms\n   Steps: %d\n\n", timeD, pathD.size()));

            // 2. A*
            long startA = System.nanoTime();
            List<Node> pathA = GraphAlgorithms.runAStar(graph, state.selected1, state.selected2);
            long endA = System.nanoTime();
            double timeA = (endA - startA) / 1_000_000.0;
            result.append(String.format("🔸 A* (A-Star):\n   Time: %.4f ms\n   Steps: %d\n\n", timeA, pathA.size()));

            // 3. BFS
            long startBFS = System.nanoTime();
            List<Node> reachBFS = GraphAlgorithms.runBFS(graph, state.selected1);
            boolean canReach = reachBFS.contains(state.selected2);
            long endBFS = System.nanoTime();
            double timeBFS = (endBFS - startBFS) / 1_000_000.0;
            result.append(String.format("🔹 BFS (Scan):\n   Time: %.4f ms\n   Reachable: %s\n", timeBFS, canReach ? "YES" : "NO"));

            // 4. DFS
            long startDFS = System.nanoTime();
            List<Node> reachDFS = GraphAlgorithms.runDFS(graph, state.selected1);
            boolean canReachDFS = reachDFS.contains(state.selected2);
            long endDFS = System.nanoTime();
            double timeDFS = (endDFS - startDFS) / 1_000_000.0;
            result.append(String.format("🔸 DFS (Scan):\n   Time: %.4f ms\n   Reachable: %s\n", timeDFS, canReachDFS ? "YES" : "NO"));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Performance Benchmark");
            alert.setHeaderText("Race Results");
            alert.setContentText(result.toString());
            alert.showAndWait();
        } else {
            infoArea.setText("Select 2 people to compare algorithms!");
        }
    }

    public void runComponents() {
        int count = GraphAlgorithms.countAndColorComponents(graph);
        infoArea.setText("Found " + count + " disconnected communities (Islands). They are now colored.");
        
        // Тук highlightedNodes не се променя, което значи, че Main 
        // ще покаже всички нодове в таблицата (както е редно за този изглед).
        tableUpdateCallback.run();
        redrawCallback.run();
    }

    public void runColoring() {
        GraphAlgorithms.runColoring(graph);
        infoArea.setText("Graph colored using Welsh-Powell algorithm.");
        
        tableUpdateCallback.run();
        redrawCallback.run();
    }

    // ВНИМАНИЕ: Промених метода да приема highlightedNodes, 
    // за да може таблицата да покаже само топ лидерите.
    public void runCentrality(List<Node> highlightedNodes) {
        List<Node> top = GraphAlgorithms.getTopCentrality(graph);
        
        // Нулираме цветовете
        for (Node n : graph.nodes) n.colorIndex = 0;
        highlightedNodes.clear();

        if (!top.isEmpty()) {
            StringBuilder sb = new StringBuilder("Top 5 Influencers:\n");
            int count = Math.min(5, top.size());
            
            for (int i = 0; i < count; i++) {
                Node n = top.get(i);
                n.colorIndex = i + 1; // Оцветяваме топ лидерите
                sb.append(String.format("%d. %s (%d connections)\n", i + 1, n.name, graph.getDegree(n)));
                
                // Добавяме ги в списъка за показване, за да ги видиш в таблицата
                highlightedNodes.add(n);
            }
            infoArea.setText(sb.toString());
            
            tableUpdateCallback.run();
            redrawCallback.run();
        }
    }

    public void generateRandomData(double width, double height, int count) {
        graph.nodes.clear();
        graph.edges.clear();

        Random rand = new Random();
        for (int i = 1; i <= count; i++) {
            graph.addNode(new Node(
                i,
                "Emp " + i,
                rand.nextDouble() * (width - 50) + 25,
                rand.nextDouble() * (height - 50) + 25,
                rand.nextDouble() * 10,  // Activity
                rand.nextInt(100),       // Interactions
                rand.nextInt(20)         // Projects
            ));
        }

        // Случайно свързване
        for (Node n : graph.nodes) {
            int connections = rand.nextInt(3) + 1;
            for (int k = 0; k < connections; k++) {
                Node t = graph.nodes.get(rand.nextInt(count));
                if (t != n) graph.addEdge(n, t);
            }
        }
        
        infoArea.setText("Generated " + count + " random employees.");
        tableUpdateCallback.run();
        redrawCallback.run();
    }

    public int getNextId() {
        int max = 0;
        for (Node n : graph.nodes) if (n.id > max) max = n.id;
        return max + 1;
    }

    public void repositionNodesIfNeeded(double width, double height) {
    double margin = 40;

    for (Node n : graph.nodes) {
        // Eğer node canvas dışındaysa veya köşeye yapışmışsa
        if (n.x < margin || n.y < margin || n.x > width - margin || n.y > height - margin) {
            n.x = margin + Math.random() * (width - 2 * margin);
            n.y = margin + Math.random() * (height - 2 * margin);
        }
    }
}

}