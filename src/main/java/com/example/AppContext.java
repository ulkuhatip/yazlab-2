package com.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextArea;
import java.util.ArrayList;
import java.util.List;

public class AppContext {
    public final Graph graph = new Graph();
    public final InteractionState state = new InteractionState();
    public final List<Node> highlightedNodes = new ArrayList<>();
    
    public final Canvas canvas;
    public final TextArea infoArea;
    
    public final GraphRenderer renderer;
    public final InteractionController interactionController;
    public final AnimationManager animationManager;

    public double mouseX;
    public double mouseY;

    public AppContext(Canvas canvas, TextArea infoArea) {
        this.canvas = canvas;
        this.infoArea = infoArea;
        this.renderer = new GraphRenderer(canvas);
        this.interactionController = new InteractionController(graph, infoArea);
        this.animationManager = new AnimationManager();
    }
}