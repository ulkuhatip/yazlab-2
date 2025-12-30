package com.example;

import java.util.*;

public class GraphAlgorithms {

    // --- 1. DIJKSTRA ---
    public static List<Node> runDijkstra(Graph graph, Node start, Node end) {

        for (Node n : graph.nodes) {
            n.minDistance = Double.MAX_VALUE;
            n.previous = null;
        }
        start.minDistance = 0;

        PriorityQueue<Node> queue =
                new PriorityQueue<>(Comparator.comparingDouble(n -> n.minDistance));
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            if (u == end) break;

            for (Edge e : graph.edges) {
                if (e.source != u) continue;

                Node v = e.target;
                double dist = u.minDistance + e.cost;

                if (dist < v.minDistance) {
                    queue.remove(v);
                    v.minDistance = dist;
                    v.previous = u;
                    queue.add(v);
                }
            }
        }
        return reconstructPath(start, end);
    }

    // --- 2. A* ---
    public static List<Node> runAStar(Graph graph, Node start, Node end) {

        for (Node n : graph.nodes) {
            n.minDistance = Double.MAX_VALUE;
            n.previous = null;
        }
        start.minDistance = 0;

        PriorityQueue<Node> open =
                new PriorityQueue<>(Comparator.comparingDouble(
                        n -> n.minDistance + heuristic(n, end)));

        open.add(start);

        while (!open.isEmpty()) {
            Node u = open.poll();
            if (u == end) break;

            for (Edge e : graph.edges) {
                if (e.source != u) continue;

                Node v = e.target;
                double g = u.minDistance + e.cost;

                if (g < v.minDistance) {
                    open.remove(v);
                    v.minDistance = g;
                    v.previous = u;
                    open.add(v);
                }
            }
        }
        return reconstructPath(start, end);
    }

    private static double heuristic(Node a, Node b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    // --- 3. BFS ---
    public static List<Node> runBFS(Graph graph, Node start) {

        List<Node> visited = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();

            for (Edge e : graph.edges) {
                if (e.source == u && !visited.contains(e.target)) {
                    visited.add(e.target);
                    queue.add(e.target);
                }
            }
        }
        return visited;
    }

    // --- 4. DFS ---
    public static List<Node> runDFS(Graph graph, Node start) {

        List<Node> visited = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            Node u = stack.pop();

            if (!visited.contains(u)) {
                visited.add(u);

                for (Edge e : graph.edges) {
                    if (e.source == u && !visited.contains(e.target)) {
                        stack.push(e.target);
                    }
                }
            }
        }
        return visited;
    }

    // --- 5. CONNECTED COMPONENTS ---
    public static int countConnectedComponents(Graph graph) {
        List<Node> visited = new ArrayList<>();
        int count = 0;

        for (Node n : graph.nodes) {
            if (!visited.contains(n)) {
                count++;
                visited.addAll(runBFS(graph, n));
            }
        }
        return count;
    }

    // --- 6. WELSH–POWELL ---
    public static void runColoring(Graph graph) {

        List<Node> sorted = new ArrayList<>(graph.nodes);
        sorted.sort((a, b) -> graph.getDegree(b) - graph.getDegree(a));

        for (Node n : graph.nodes) n.colorIndex = 0;

        int color = 1;
        for (Node n : sorted) {
            if (n.colorIndex == 0) {
                n.colorIndex = color;

                for (Node other : sorted) {
                    if (other.colorIndex == 0 && !graph.areConnected(n, other)) {
                        boolean safe = true;
                        for (Node x : sorted) {
                            if (x.colorIndex == color && graph.areConnected(x, other)) {
                                safe = false;
                                break;
                            }
                        }
                        if (safe) other.colorIndex = color;
                    }
                }
                color++;
            }
        }
    }

    // --- 7. CENTRALITY ---
    public static List<Node> getTopCentrality(Graph graph) {
        List<Node> list = new ArrayList<>(graph.nodes);
        list.sort((a, b) -> graph.getDegree(b) - graph.getDegree(a));
        return list;
    }

    private static List<Node> reconstructPath(Node start, Node end) {
        List<Node> path = new ArrayList<>();
        for (Node n = end; n != null; n = n.previous) path.add(n);
        Collections.reverse(path);
        if (!path.isEmpty() && path.get(0) != start) return new ArrayList<>();
        return path;
    }

    // === COMPATIBILITY METHOD (Main.java için) ===
   public static int countAndColorComponents(Graph graph) {

    // Her şeyi önce sıfırla
    for (Node n : graph.nodes) {
        n.colorIndex = 0;
    }

    Set<Node> visited = new HashSet<>();
    int components = 0;
    int color = 1;

    for (Node start : graph.nodes) {
        if (visited.contains(start)) continue;

        // Yeni bileşen
        components++;

        // BFS ile bu bileşendeki tüm nodeları bul
        Queue<Node> q = new LinkedList<>();
        q.add(start);
        visited.add(start);

        while (!q.isEmpty()) {
            Node u = q.poll();

            // Bu node'u bileşen rengine boya
            u.colorIndex = color;

            // Komşuları bul (Graph'in çift edge yapısına uyumlu)
            for (Edge e : graph.edges) {
                Node v = null;

                if (e.source == u) v = e.target;
                else if (e.target == u) v = e.source;
                else continue;

                if (!visited.contains(v)) {
                    visited.add(v);
                    q.add(v);
                }
            }
        }

        // Sonraki bileşen farklı renk alsın
        color++;
        // Çok renk yoksa döndür (istersen kaldır)
        if (color > 12) color = 1;
    }

    return components;
}
}
