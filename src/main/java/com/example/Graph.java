package com.example;

import java.util.*;

public class Graph {
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    public void addNode(Node n) {
        nodes.add(n);
    }

    public void addEdge(Node s, Node t) {
        // 1. Check if connection exists
        for(Edge e : edges) {
            if((e.source == s && e.target == t) || (e.source == t && e.target == s)) {
                return; 
            }
        }
        // 2. Add bidirectional connection
        edges.add(new Edge(s, t));
        edges.add(new Edge(t, s));
    }

    // --- НОВО: МЕТОДИ ЗА ИЗТРИВАНЕ ---
    
    public void removeNode(Node n) {
        // First, remove all edges connected to this node
        edges.removeIf(e -> e.source == n || e.target == n);
        // Then remove the node itself
        nodes.remove(n);
    }

    public void removeEdge(Node s, Node t) {
        // Remove both directions
        edges.removeIf(e -> (e.source == s && e.target == t) || (e.source == t && e.target == s));
    }
    // ---------------------------------

    public Node getNodeById(int id) {
        for (Node n : nodes) {
            if (n.id == id) return n;
        }
        return null;
    }

    // --- ALGORITHM: Dijkstra ---
    public List<Node> runDijkstra(Node start, Node end) {
        for (Node n : nodes) {
            n.minDistance = Double.MAX_VALUE;
            n.previous = null;
        }
        start.minDistance = 0;

        PriorityQueue<Node> queue = new PriorityQueue<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            if (u == end) break; 

            for (Edge e : edges) {
                if (e.source == u) {
                    Node v = e.target;
                    double weight = e.cost; 
                    double distanceThroughU = u.minDistance + weight;

                    if (distanceThroughU < v.minDistance) {
                        queue.remove(v);
                        v.minDistance = distanceThroughU;
                        v.previous = u;
                        queue.add(v);
                    }
                }
            }
        }

        List<Node> path = new ArrayList<>();
        for (Node target = end; target != null; target = target.previous) {
            path.add(target);
        }
        Collections.reverse(path);
        
        if (!path.isEmpty() && path.get(0) != start) return new ArrayList<>(); 
        return path;
    }
    
    // --- ALGORITHM: Welsh-Powell ---
    public void runColoring() {
        List<Node> sortedNodes = new ArrayList<>(nodes);
        sortedNodes.sort((a, b) -> Integer.compare(getDegree(b), getDegree(a))); 

        // Reset colors first
        for(Node n : nodes) n.colorIndex = 0;

        int colorIndex = 1;
        for (Node n : sortedNodes) {
            if (n.colorIndex == 0) { 
                n.colorIndex = colorIndex;
                for (Node other : sortedNodes) {
                    if (other.colorIndex == 0 && !areConnected(n, other)) {
                        boolean safe = true;
                        for (Node existing : sortedNodes) {
                            if (existing.colorIndex == colorIndex && areConnected(existing, other)) {
                                safe = false; 
                                break;
                            }
                        }
                        if (safe) other.colorIndex = colorIndex;
                    }
                }
                colorIndex++; 
            }
        }
    }
    
    public int getDegree(Node n) {
        int count = 0;
        for (Edge e : edges) {
            if (e.source == n) count++;
        }
        return count;
    }

    private boolean areConnected(Node a, Node b) {
        for (Edge e : edges) {
            if ((e.source == a && e.target == b) || (e.source == b && e.target == a)) return true;
        }
        return false;
    }

    public List<Node> getTopCentrality() {
        List<Node> top = new ArrayList<>(nodes);
        top.sort((a, b) -> Integer.compare(getDegree(b), getDegree(a)));
        return top;
    }
}