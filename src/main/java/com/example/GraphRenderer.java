package com.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

public class GraphRenderer {

    private Canvas canvas;
    private GraphicsContext gc;

    public GraphRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void draw(Graph graph,
                 List<Node> highlightedNodes,
                 Node selected1,
                 Node selected2,
                 boolean isConnectMode,
                 double mouseX,
                 double mouseY) {

    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

    // Draw Edges
    gc.setLineWidth(1);
    for (Edge e : graph.edges) {
        if (isEdgeInPath(e, highlightedNodes)) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
            gc.setLineWidth(1);
        } else {
            gc.setStroke(Color.LIGHTGRAY);
            gc.strokeLine(e.source.x, e.source.y, e.target.x, e.target.y);
        }
    }

    // Draw Nodes
    for (Node n : graph.nodes) {
        boolean isSel = (n == selected1 || n == selected2);
        boolean isHigh = highlightedNodes.contains(n);
        n.draw(gc, isSel, isHigh);
    }

    // 👻 Ghost line
    if (isConnectMode && selected1 != null) {
        gc.save();
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1.5);
        gc.setLineDashes(10);
        gc.strokeLine(selected1.x, selected1.y, mouseX, mouseY);
        gc.restore();
    }
}
    private boolean isEdgeInPath(Edge e, List<Node> highlightedNodes) {
        if (highlightedNodes.size() < 2) return false;
        for (int i = 0; i < highlightedNodes.size() - 1; i++) {
            Node n1 = highlightedNodes.get(i);
            Node n2 = highlightedNodes.get(i + 1);
            if ((e.source == n1 && e.target == n2) ||
                (e.source == n2 && e.target == n1))
                return true;
        }
        return false;
    }




}
