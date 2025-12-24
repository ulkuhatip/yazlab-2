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
    public void startConnectMode(InteractionState state, List<Node> highlightedNodes) {
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
    public void handleClick(double x, double y, InteractionState state, List<Node> highlightedNodes) {
        Node clicked = findNodeAt(x, y);

        // Boş alana tıklandıysa
        if (clicked == null) {
            if (state.isConnectMode) {
                resetConnectMode(state);
                infoArea.setText("Connect mode cancelled.");
            }
            return;
        }

        // CONNECT MODE MANTIĞI
        if (state.isConnectMode) {
            handleConnectSelection(clicked, state);
            return;
        }

        // NORMAL MODE MANTIĞI (Dijkstra/A* için çift seçim sağlar)
        if (state.selected1 == null || (state.selected1 != null && state.selected2 != null)) {
            // Hiçbir şey seçili değilse VEYA zaten iki tane seçiliyse: Yeni bir seçim başlat
            state.selected1 = clicked;
            state.selected2 = null;
            highlightedNodes.clear();
            infoArea.setText("Start: " + clicked.name + " (Select another for path)");
        } else if (state.selected1 != clicked) {
            // Birinci zaten seçiliyse ve farklı birine tıklandıysa: İkinciyi ata
            state.selected2 = clicked;
            infoArea.setText("Target: " + clicked.name + " (Ready for Algorithms)");
        }
    }

    // Helper metod: Connect mode seçimlerini yönetir
    private void handleConnectSelection(Node clicked, InteractionState state) {
        if (state.selected1 == null) {
            state.selected1 = clicked;
            infoArea.setText("Connect: Select second person");
        } else if (state.selected1 != clicked) {
            graph.addEdge(state.selected1, clicked);
            infoArea.setText("Connected: " + state.selected1.name + " & " + clicked.name);
            resetConnectMode(state);
        }
    }

    // --- NODE BULUCU (Eksik olan kısım buydu) ---
    public Node findNodeAt(double x, double y) {
        for (Node n : graph.nodes) {
            // Tıklanan koordinat ile düğüm merkezi arasındaki mesafe kontrolü
            if (Math.abs(x - n.x) < 20 && Math.abs(y - n.y) < 20)
                return n;
        }
        return null;
    }
}