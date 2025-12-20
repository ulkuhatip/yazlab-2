package com.example;

public class Edge {

    // Kenarın bağladığı iki düğüm
    public Node source;
    public Node target;

    // Kenar maliyeti (dinamik ağırlık)
    public double cost;

    /**
     * Edge constructor
     * İki düğüm arasındaki maliyeti, düğümlerin sayısal
     * özelliklerine göre dinamik olarak hesaplar.
     */
    public Edge(Node source, Node target) {
        this.source = source;
        this.target = target;

        // Dinamik ağırlık hesaplama (İsterlerde verilen formül)
        this.cost = calculateCost(source, target);
    }

    /**
     * İki düğüm arasındaki maliyeti hesaplar
     * A(i,j) = 1 / (1 + (Ai - Aj)^2 + (Ei - Ej)^2 + (Bi - Bj)^2)
     */
    private double calculateCost(Node a, Node b) {
        double activityDiff = Math.pow(a.activity - b.activity, 2);
        double interactionDiff = Math.pow(a.interaction - b.interaction, 2);
        double projectDiff = Math.pow(a.projects - b.projects, 2);

        return 1.0 / (1 + activityDiff + interactionDiff + projectDiff);
    }
}