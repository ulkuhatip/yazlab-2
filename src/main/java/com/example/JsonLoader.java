package com.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonLoader {
    
    // --- reading (LOAD) ---
    public static void load(String filePath, Graph graph) {
        // 1. Изчистваме старите данни, за да не се смесват с новите
        graph.nodes.clear();
        graph.edges.clear();
        
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                System.out.println("ГРЕШКА: Файлът не е намерен: " + filePath);
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            // --- ПАРСВАНЕ НА NODES ---
            String[] nodesSplit = content.split("\"nodes\": \\[");
            if (nodesSplit.length > 1) {
                String nodesData = nodesSplit[1].split("\\]")[0]; // Взимаме всичко до затварящата скоба ]
                // Разделяме на отделни обекти по затваряща фигурна скоба },
                String[] entries = nodesData.split("\\},");

                for (String entry : entries) {
                    int id = parseInt(entry, "id");
                    // Ако id-то е валидно, вадим и другите данни
                    if (id != 0) {
                        String name = parseString(entry, "name");
                        double x = parseDouble(entry, "x");
                        double y = parseDouble(entry, "y");
                        double act = parseDouble(entry, "activity");
                        int inter = parseInt(entry, "interaction");
                        int proj = parseInt(entry, "projects");

                        graph.addNode(new Node(id, name, x, y, act, inter, proj));
                    }
                }
            }

            // --- ПАРСВАНЕ НА EDGES ---
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
                        graph.addEdge(s, t); 
                    }
                }
            }
            System.out.println("Данните са заредени успешно от: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- ЗАПИСВАНЕ (SAVE) - НОВО! ---
    public static void save(String filePath, Graph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        // 1. Записваме Nodes
        sb.append("  \"nodes\": [\n");
        for (int i = 0; i < graph.nodes.size(); i++) {
            Node n = graph.nodes.get(i);
            // Форматираме данните като текст
            sb.append(String.format("    { \"id\": %d, \"name\": \"%s\", \"x\": %.1f, \"y\": %.1f, \"activity\": %.2f, \"interaction\": %d, \"projects\": %d }",
                    n.id, n.name, n.x, n.y, n.activity, n.interaction, n.projects));
            
            // Слагаме запетая след всеки ред, освен последния
            if (i < graph.nodes.size() - 1) sb.append(","); 
            sb.append("\n");
        }
        sb.append("  ],\n");

        // 2. Записваме Edges
        sb.append("  \"edges\": [\n");
        // Използваме хитрина: записваме само връзките, където source ID < target ID.
        // Така избягваме дублирането (защото в паметта пазим и 1->2, и 2->1), а в файла ни трябва само веднъж.
        List<String> edgesLines = new ArrayList<>();
        for (Edge e : graph.edges) {
            if (e.source.id < e.target.id) {
                edgesLines.add(String.format("    { \"source\": %d, \"target\": %d }", e.source.id, e.target.id));
            }
        }
        
        for (int i = 0; i < edgesLines.size(); i++) {
            sb.append(edgesLines.get(i));
            if (i < edgesLines.size() - 1) sb.append(",");
            sb.append("\n");
        }
        
        sb.append("  ]\n");
        sb.append("}");

        // Записваме готовия текст във файла
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sb.toString());
            System.out.println("Файлът е запазен успешно: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Помощни методи (без промяна) ---
    private static String parseString(String src, String key) {
        try {
            int start = src.indexOf("\"" + key + "\":") + key.length() + 4; // +4 заради ": " и кавичките
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
        int start = src.indexOf("\"" + key + "\":") + key.length() + 3; // по-малко отместване за числа
        int end = src.indexOf(",", start);
        if (end == -1) end = src.indexOf("}", start); // Ако е последен елемент
        
        String val = src.substring(start, end).trim();
        // Чистене на остатъчни скоби, ако парсването е грубо
        if (val.endsWith("}")) val = val.substring(0, val.length()-1).trim();
        if (val.endsWith("]")) val = val.substring(0, val.length()-1).trim();
        return val;
    }
}