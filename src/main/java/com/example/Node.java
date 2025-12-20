package com.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Node implements Comparable<Node> {

    public int id;
    public String name;
    public double x, y;

    public double activity;
    public int interaction;
    public int projects;

    public double minDistance = Double.POSITIVE_INFINITY;
    public Node previous = null;
    public int colorIndex = 0;

    private double radius = 15; // default radius

    // ✅ CONSTRUCTOR
    public Node(int id, String name, double x, double y,
                double activity, int interaction, int projects) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.activity = activity;
        this.interaction = interaction;
        this.projects = projects;
    }

    // ✅ GET / SET
    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    // ✅ DRAW METHOD 
    public void draw(GraphicsContext gc, boolean isSelected, boolean isHighlighted) {
        if (isHighlighted) gc.setFill(Color.RED);
        else if (isSelected) gc.setFill(Color.LIGHTGREEN);
        else gc.setFill(getColorFromIndex(colorIndex));

        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.setFill(Color.BLACK);
        gc.fillText(name, x - radius, y - radius - 5);
    }

    // ✅ DIJKSTRA SUPPORT
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.minDistance, other.minDistance);
    }

    // ✅ COLORING SUPPORT (Welsh–Powell)
    private Color getColorFromIndex(int idx) {
        switch (idx) {
            case 1: return Color.SKYBLUE;
            case 2: return Color.PINK;
            case 3: return Color.YELLOW;
            case 4: return Color.VIOLET;
            case 5: return Color.ORANGE;
            default: return Color.LIGHTGRAY;
        }
    }
}
