package com.example;

import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import java.util.List;
import java.util.Random;

public class GraphController {

    private final Graph graph;
    private final TextArea infoArea;
    private final AnimationManager animationManager;
    private final TableView<Node> resultTable; // Таблицата за визуализация на резултати
    private final Runnable redrawCallback;     // Метод за прерисуване (Main::draw)

    public GraphController(Graph graph, TextArea infoArea, AnimationManager animationManager, 
                           TableView<Node> resultTable, Runnable redrawCallback) {
        this.graph = graph;
        this.infoArea = infoArea;
        this.animationManager = animationManager;
        this.resultTable = resultTable;
        this.redrawCallback = redrawCallback;
    }

    // Помощен метод за обновяване на данните в таблицата
    private void updateTable(List<Node> nodes) {
        resultTable.getItems().clear();
        if (nodes != null) {
            resultTable.getItems().addAll(nodes);
        }
    }

    public void runBFS(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null) {
            // Изпълняваме алгоритъма
            List<Node> results = GraphAlgorithms.runBFS(graph, state.selected1);
            
            // Обновяваме списъка за оцветяване
            highlightedNodes.clear();
            highlightedNodes.addAll(results);
            
            // Лог в текстовото поле
            infoArea.setText("BFS: Found " + highlightedNodes.size() + " reachable people from " + state.selected1.name);
            
            // Показваме резултата в таблицата
            updateTable(highlightedNodes);
            
            // Прерисуваме екрана
            redrawCallback.run();
        } else {
            infoArea.setText("Select 1 person to start BFS.");
        }
    }

    public void runDFS(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null) {
            List<Node> results = GraphAlgorithms.runDFS(graph, state.selected1);
            
            highlightedNodes.clear();
            highlightedNodes.addAll(results);
            
            infoArea.setText("DFS: Found " + highlightedNodes.size() + " reachable people from " + state.selected1.name);
            
            updateTable(highlightedNodes);
            redrawCallback.run();
        } else {
            infoArea.setText("Select 1 person to start DFS.");
        }
    }

    public void runDijkstra(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null && state.selected2 != null) {
            List<Node> path = GraphAlgorithms.runDijkstra(graph, state.selected1, state.selected2);
            
            if (path.isEmpty()) {
                infoArea.setText("No path found.");
                highlightedNodes.clear();
                updateTable(null); // Чистим таблицата
                redrawCallback.run();
            } else {
                infoArea.setText("Dijkstra Path: " + path.size() + " steps. Animating...");
                
                // Показваме пътя в таблицата веднага
                updateTable(path);
                
                // Стартираме анимацията
                animationManager.animatePath(path, highlightedNodes, redrawCallback);
            }
        } else {
            infoArea.setText("Select 2 people for Dijkstra.");
        }
    }

    public void runAStar(InteractionState state, List<Node> highlightedNodes) {
        if (state.selected1 != null && state.selected2 != null) {
            List<Node> path = GraphAlgorithms.runAStar(graph, state.selected1, state.selected2);
            
            if (path.isEmpty()) {
                infoArea.setText("No path found.");
                highlightedNodes.clear();
                updateTable(null);
                redrawCallback.run();
            } else {
                infoArea.setText("A* Path: " + path.size() + " steps. Animating...");
                updateTable(path);
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

            // 3. BFS (за достъпност)
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
    }

    public void runComponents() {
        int count = GraphAlgorithms.countAndColorComponents(graph);
        infoArea.setText("Found " + count + " disconnected communities (Islands). They are now colored.");
        
        // Показваме всички, за да се види оцветяването
        updateTable(graph.nodes);
        redrawCallback.run();
    }

    public void runColoring() {
        GraphAlgorithms.runColoring(graph);
        infoArea.setText("Graph colored using Welsh-Powell algorithm.");
        updateTable(graph.nodes);
        redrawCallback.run();
    }

    public void runCentrality() {
        List<Node> top = GraphAlgorithms.getTopCentrality(graph);
        
        // Нулираме цветовете
        for (Node n : graph.nodes) n.colorIndex = 0;

        if (!top.isEmpty()) {
            StringBuilder sb = new StringBuilder("Top 5 Influencers:\n");
            int count = Math.min(5, top.size());
            for (int i = 0; i < count; i++) {
                Node n = top.get(i);
                n.colorIndex = i + 1; // Оцветяваме топ лидерите
                sb.append(String.format("%d. %s (%d connections)\n", i + 1, n.name, graph.getDegree(n)));
            }
            infoArea.setText(sb.toString());
            
            // В таблицата показваме само топ лидерите
            updateTable(top.subList(0, count));
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
        updateTable(graph.nodes); // Показваме всички нови в таблицата
        redrawCallback.run();
    }

    // Helper за намиране на следващо ID при ръчно добавяне
    public int getNextId() {
        int max = 0;
        for (Node n : graph.nodes) if (n.id > max) max = n.id;
        return max + 1;
    }
}