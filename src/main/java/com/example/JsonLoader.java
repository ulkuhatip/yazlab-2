package com.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;

public class JsonLoader {
    public static void load(String filePath, Graph graph) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                System.out.println("ГРЕШКА: Файлът data.json не е намерен!");
                return;
            }
            // Четем целия файл като един текст
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            // 1. ПАРСВАНЕ НА NODES
            String[] nodesSplit = content.split("\"nodes\": \\[");
            if (nodesSplit.length > 1) {
                String nodesData = nodesSplit[1].split("\\]")[0];
                String[] entries = nodesData.split("\\},");

                for (String entry : entries) {
                    // Извличане на данните ръчно
                    int id = parseInt(entry, "id");
                    String name = parseString(entry, "name");
                    double x = parseDouble(entry, "x");
                    double y = parseDouble(entry, "y");
                    double act = parseDouble(entry, "activity");
                    int inter = parseInt(entry, "interaction");
                    int proj = parseInt(entry, "projects");

                    // Добавяне в графа
                    if (id != 0) {
                        graph.addNode(new Node(id, name, x, y, act, inter, proj));
                    }
                }
            }

            // 2. ПАРСВАНЕ НА EDGES
            String[] edgesSplit = content.split("\"edges\": \\[");
            if (edgesSplit.length > 1) {
                String edgesData = edgesSplit[1].split("\\]")[0];
                String[] entries = edgesData.split("\\},");

                for (String entry : entries) {
                    int sId = parseInt(entry, "source");
                    int tId = parseInt(entry, "target");
                    
                    Node s = graph.getNodeById(sId);
                    Node t = graph.getNodeById(tId);
                    
                    if (s != null && t != null) {
                        graph.addEdge(s, t); // Тук автоматично се смята формулата в Edge
                    }
                }
            }
            System.out.println("Данните са заредени успешно.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Помощни методи за "изрязване" на стойности от текста
    private static String parseString(String src, String key) {
        try {
            int start = src.indexOf("\"" + key + "\":") + key.length() + 4;
            int end = src.indexOf("\"", start);
            return src.substring(start, end);
        } catch (Exception e) { return ""; }
    }
    private static int parseInt(String src, String key) {
        try { return Integer.parseInt(extractVal(src, key)); } catch(Exception e) { return 0; }
    }
    private static double parseDouble(String src, String key) {
        try { return Double.parseDouble(extractVal(src, key)); } catch(Exception e) { return 0.0; }
    }
    private static String extractVal(String src, String key) {
        int start = src.indexOf("\"" + key + "\":") + key.length() + 3;
        int end = src.indexOf(",", start);
        if (end == -1) end = src.indexOf("}", start);
        return src.substring(start, end).trim();
    }
}