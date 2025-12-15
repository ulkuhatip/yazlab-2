package main.java.com.example; 

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

    public Node(int id, String name, double x, double y, double activity, int interaction, int projects) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.activity = activity;
        this.interaction = interaction;
        this.projects = projects;
    }

    public void draw(GraphicsContext gc, boolean isSelected, boolean isHighlighted) {
        if (isHighlighted) gc.setFill(Color.RED);          
        else if (isSelected) gc.setFill(Color.LIGHTGREEN); 
        else gc.setFill(getColorFromIndex(colorIndex));   

        gc.fillOval(x - 15, y - 15, 30, 30);
        
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - 15, y - 15, 30, 30);
        
        gc.setFill(Color.BLACK);
        gc.fillText(name, x - 10, y - 20);
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.minDistance, other.minDistance);
    }

    // Помощен метод за цветове (за Welsh-Powell)
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