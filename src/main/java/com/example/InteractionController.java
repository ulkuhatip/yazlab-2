package com.example;

import javafx.scene.control.TextArea;
import java.util.List;

public class InteractionController {

    private final Graph graph;
    private final TextArea infoArea;

    public InteractionController(Graph graph, TextArea infoArea) {
        this.graph = graph;
        this.infoArea = infoArea;
    }

    // --- CONNECT MODE BAŞLAT ---
    public void startConnectMode(InteractionState state,
                                 List<Node> highlightedNodes) {

        state.selected1 = null;
        state.selected2 = null;
        highlightedNodes.clear();

        state.isConnectMode = true;
        infoArea.setText("Connect mode: select first person");
    }

    // --- CONNECT MODE İPTAL ---
    public void resetConnectMode(InteractionState state) {
        state.isConnectMode = false;
        state.selected1 = null;
        state.selected2 = null;
    }

    // --- MOUSE CLICK HANDLER ---
    public void handleClick(double x,
                            double y,
                            InteractionState state,
                            List<Node> highlightedNodes) {

        Node clicked = findNodeAt(x, y);

        // Boş alana tıklandı
        if (clicked == null) {
            if (state.isConnectMode) {
                resetConnectMode(state);
                infoArea.setText("Connect mode cancelled.");
            }
            return;
        }

        // NORMAL MODE
        if (!state.isConnectMode) {
            state.selected1 = clicked;
            state.selected2 = null;
            highlightedNodes.clear();
            infoArea.setText("Selected: " + clicked.name);
            return;
        }

        // CONNECT MODE – ilk seçim
        if (state.selected1 == null) {
            state.selected1 = clicked;
            infoArea.setText("First selected: " + clicked.name);
            return;
        }

        // Aynı node seçilirse
        if (clicked == state.selected1) {
            infoArea.setText("Select a different person.");
            return;
        }

        // Gerçek bağlantı
        graph.addEdge(state.selected1, clicked);
        infoArea.setText(
            "Connected: " + state.selected1.name + " & " + clicked.name
        );

        resetConnectMode(state);
    }

    // --- NODE HIT TEST ---
    public Node findNodeAt(double x, double y) {
        for (Node n : graph.nodes) {
            if (Math.abs(x - n.x) < 20 && Math.abs(y - n.y) < 20)
                return n;
        }
        return null;
    }
}
