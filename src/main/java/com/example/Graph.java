package com.example;

import java.util.*;

public class Graph {
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    public void addNode(Node n) {
        nodes.add(n);
    }

    public void addEdge(Node s, Node t) {
        edges.add(new Edge(s, t));
        edges.add(new Edge(t, s));
    }

    //we can change the searching algo id to have better performance (plus for PRESENTATION )
    public Node getNodeById(int id) {
        for (Node n : nodes) {
            if (n.id == id) return n;
        }
        return null;
    }

    // --- ALGORITHM: DIJKSTRA (Най-къс път) ---
    public List<Node> runDijkstra(Node start, Node end) {
        // Ресет на стойностите
        for (Node n : nodes) {
            n.minDistance = Double.POSITIVE_INFINITY;
            n.previous = null;
        }
        start.minDistance = 0;

        PriorityQueue<Node> queue = new PriorityQueue<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();

            for (Edge e : edges) {
                if (e.source == u) {
                    Node v = e.target;
                    double newDist = u.minDistance + e.cost;
                    
                    if (newDist < v.minDistance) {
                        queue.remove(v);
                        v.minDistance = newDist;
                        v.previous = u;
                        queue.add(v);
                    }
                }
            }
        }

        // Възстановяване на пътя
        List<Node> path = new ArrayList<>();
        for (Node curr = end; curr != null; curr = curr.previous) {
            path.add(curr);
        }
        Collections.reverse(path);
        
        // Връщаме пътя само ако започва от нашия старт
        if (!path.isEmpty() && path.get(0) == start) return path;
        return new ArrayList<>();
    }

    // --- ALGORITHM: WELSH-POWELL (Оцветяване) ---
    public void runColoring() {
        // Сортираме възлите по брой връзки (най-свързаните първи)
        nodes.sort((n1, n2) -> Integer.compare(getDegree(n2), getDegree(n1)));

        int color = 1;
        for (Node n : nodes) {
            if (n.colorIndex == 0) {
                n.colorIndex = color;
                // Оцветяваме всички несвързани с него със същия цвят
                for (Node other : nodes) {
                    if (other.colorIndex == 0 && !areConnected(n, other)) {
                        other.colorIndex = color;
                    }
                }
                color++;
            }
        }
    }

    // --- ALGORITHM: CENTRALITY (Лидери) ---
    public List<Node> getTopCentrality() {
        List<Node> sorted = new ArrayList<>(nodes);
        sorted.sort((n1, n2) -> Integer.compare(getDegree(n2), getDegree(n1)));
        return sorted;
    }

    public int getDegree(Node n) {
        int count = 0;
        for (Edge e : edges) {
            if (e.source == n) count++;
        }
        return count;
    }

    private boolean areConnected(Node n1, Node n2) {
        for (Edge e : edges) {
            if ((e.source == n1 && e.target == n2) || (e.source == n2 && e.target == n1)) return true;
        }
        return false;
    }
}