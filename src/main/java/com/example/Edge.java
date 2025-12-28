package com.example;

public class Edge {

    // Връзка между два възела
    public Node source;
    public Node target;

    // Цената (тежестта) на връзката
    public double cost;

    public Edge(Node source, Node target) {
        this.source = source;
        this.target = target;
        // Изчисляваме цената автоматично при създаване
        this.cost = calculateCost(source, target);
    }

    /**
     * Weight = 1 / (1 + sqrt((Ai-Aj)^2 + (Ei-Ej)^2 + (Bi-Bj)^2))
     */
    private double calculateCost(Node a, Node b) {
        // 1. Разликите на квадрат
        double diffActivity = Math.pow(a.activity - b.activity, 2);
        double diffInteraction = Math.pow(a.interaction - b.interaction, 2);
        double diffProjects = Math.pow(a.projects - b.projects, 2);

        double euclideanDistance = Math.sqrt(diffActivity + diffInteraction + diffProjects);

        // 3. Формулата: 1 / (1 + разстоянието)
        return 1.0 / (1.0 + euclideanDistance);
    }
}