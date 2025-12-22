package com.example;

import java.util.*;

public class GraphAlgorithms {

    // --- 1. DIJKSTRA (Shortest Path) ---
    public static List<Node> runDijkstra(Graph graph, Node start, Node end) {
        for (Node n : graph.nodes) {
            n.minDistance = Double.MAX_VALUE;
            n.previous = null;
        }
        start.minDistance = 0;

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.minDistance));
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            if (u == end) break;

            for (Edge e : graph.edges) {
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
        return reconstructPath(start, end);
    }

    // --- 2. A* ALGORITHM (Heuristic Shortest Path) ---
    // По-бърз от Dijkstra, защото ползва координатите X,Y за ориентир
    public static List<Node> runAStar(Graph graph, Node start, Node end) {
        for (Node n : graph.nodes) {
            n.minDistance = Double.MAX_VALUE; // gScore
            n.previous = null;
        }
        start.minDistance = 0;

        // PriorityQueue uses fScore = gScore + heuristic
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.minDistance + heuristic(n, end)));
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            if (u == end) break;

            for (Edge e : graph.edges) {
                if (e.source == u) {
                    Node v = e.target;
                    double tentativeG = u.minDistance + e.cost;

                    if (tentativeG < v.minDistance) {
                        queue.remove(v);
                        v.minDistance = tentativeG;
                        v.previous = u;
                        queue.add(v);
                    }
                }
            }
        }
        return reconstructPath(start, end);
    }

    private static double heuristic(Node a, Node b) {
        // Euclidean distance (distance between two points)
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    // --- 3. BFS (Breadth-First Search) - Търсене в ширина ---
    // Връща списък с всички достижими възли
    public static List<Node> runBFS(Graph graph, Node start) {
        List<Node> visited = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            
            // Намери съседите
            for (Edge e : graph.edges) {
                if (e.source == u && !visited.contains(e.target)) {
                    visited.add(e.target);
                    queue.add(e.target);
                }
            }
        }
        return visited;
    }

    // --- 4. DFS (Depth-First Search) - Търсене в дълбочина ---
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

    // --- 5. CONNECTED COMPONENTS (Острови) ---
    // Връща колко отделни групи има
    public static int countConnectedComponents(Graph graph) {
        List<Node> visitedAll = new ArrayList<>();
        int count = 0;

        for (Node n : graph.nodes) {
            if (!visitedAll.contains(n)) {
                count++;
                // Пускаме BFS, за да маркираме целия "остров"
                List<Node> island = runBFS(graph, n);
                visitedAll.addAll(island);
            }
        }
        return count;
    }

    // --- 6. WELSH-POWELL (Coloring) ---
    public static void runColoring(Graph graph) {
        List<Node> sortedNodes = new ArrayList<>(graph.nodes);
        // Сортиране по степен (най-много връзки първи)
        sortedNodes.sort((a, b) -> Integer.compare(graph.getDegree(b), graph.getDegree(a)));

        for(Node n : graph.nodes) n.colorIndex = 0;

        int colorIndex = 1;
        for (Node n : sortedNodes) {
            if (n.colorIndex == 0) {
                n.colorIndex = colorIndex;
                for (Node other : sortedNodes) {
                    if (other.colorIndex == 0 && !graph.areConnected(n, other)) {
                        boolean safe = true;
                        for (Node existing : sortedNodes) {
                            if (existing.colorIndex == colorIndex && graph.areConnected(existing, other)) {
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

    // --- 7. CENTRALITY (Top Leaders) ---
    public static List<Node> getTopCentrality(Graph graph) {
        List<Node> top = new ArrayList<>(graph.nodes);
        top.sort((a, b) -> Integer.compare(graph.getDegree(b), graph.getDegree(a)));
        return top;
    }

    // Helper for path reconstruction
    private static List<Node> reconstructPath(Node start, Node end) {
        List<Node> path = new ArrayList<>();
        for (Node target = end; target != null; target = target.previous) {
            path.add(target);
        }
        Collections.reverse(path);
        if (!path.isEmpty() && path.get(0) != start) return new ArrayList<>();
        return path;
    }
}