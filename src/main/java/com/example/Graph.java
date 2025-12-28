package com.example;

import java.util.*;

public class Graph {
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    public void addNode(Node n) {
        nodes.add(n);
    }

    public void addEdge(Node s, Node t) {
        // 1. Предпазване от връзка със самия себе си
        if (s == t) return; 

        // 2. Проверка дали връзката вече съществува
        for(Edge e : edges) {
            if((e.source == s && e.target == t) || (e.source == t && e.target == s)) {
                return; // Вече я има, излизаме
            }
        }
        
        // 3. Добавяме връзката САМО ВЕДНЪЖ
        // Тъй като графът е неориентиран (undirected), един обект Edge е достатъчен.
        // Логиката ти в Main.java вече знае как да чете и source, и target.
        edges.add(new Edge(s, t));
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

    public int getDegree(Node n) {
        int count = 0;
        for (Edge e : edges) {
            // Броим връзката, независимо дали n е source или target
            if (e.source == n || e.target == n) count++;
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