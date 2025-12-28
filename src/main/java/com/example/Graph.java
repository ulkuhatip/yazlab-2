package com.example;

import java.util.*;

public class Graph {
    public List<Node> nodes = new ArrayList<>();
 public List<Edge> edges = new ArrayList<>();

    public void addNode(Node n) {
        nodes.add(n);
    }

    public void addEdge(Node s, Node t) {
        for(Edge e : edges) {
            if((e.source == s && e.target == t) || (e.source == t && e.target == s)) {
                return; 
            }
        }
        edges.add(new Edge(s, t));
        edges.add(new Edge(t, s));
    }

    public void removeNode(Node n) {
        edges.removeIf(e -> e.source == n || e.target == n);
        nodes.remove(n);
    }

    public void removeEdge(Node s, Node t) {
        edges.removeIf(e -> (e.source == s && e.target == t) || (e.source == t && e.target == s));
    }

    public Node getNodeById(int id) {
        for (Node n : nodes) {
            if (n.id == id) return n;
        }
        return null;
    }

    // Помощни методи, които са структурни (остават тук)
    public int getDegree(Node n) {
        int count = 0;
        for (Edge e : edges) {
            if (e.source == n) count++;
        }
        return count;
    }

    public boolean areConnected(Node a, Node b) {
        for (Edge e : edges) {
            if ((e.source == a && e.target == b) || (e.source == b && e.target == a)) return true;
        }
        return false;
    }
} 