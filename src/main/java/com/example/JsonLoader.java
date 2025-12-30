package com.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;


public class JsonLoader {

    // --- LOAD JSON (Using Regex for stability) ---
    public static void load(String filePath, Graph graph) {
        // 1. Clear previous data
        graph.nodes.clear();
        graph.edges.clear();
        
        Set<Integer> seenIds = new HashSet<>(); 

        try {
            File f = new File(filePath);
            if (!f.exists()) {
                // TRANSLATED: Error message
                System.out.println("ERROR: File not found: " + filePath);
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            // --- 2. PARSE NODES ---
            
            // [REGEX USED HERE] 
            // We use Regex to extract node properties. 
            // \s* allows for any amount of whitespace, making it robust against formatting changes.
           // 1. Regex'i koordinatları beklemeyecek şekilde sadeleştiriyoruz:
            // Sadece id, name, activity, interaction ve projects alanlarını arar.
            Pattern nodePattern = Pattern.compile("\\{\\s*\"id\":\\s*(\\d+),\\s*\"name\":\\s*\"(.*?)\",\\s*\"activity\":\\s*([\\d\\.]+),\\s*\"interaction\":\\s*(\\d+),\\s*\"projects\":\\s*(\\d+)");
            Matcher nodeMatcher = nodePattern.matcher(content);

            while (nodeMatcher.find()) {
                try {
                    int id = Integer.parseInt(nodeMatcher.group(1));

                    // JSON yükleme döngüsü içinde
                    if (!seenIds.add(id)) {
                        // Sadece konsola yazma, kullanıcıya göster
                        javafx.application.Platform.runLater(() -> {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                            alert.setTitle("Veri Hatası");
                            alert.setHeaderText("Tekrar Eden ID Tespit Edildi");
                            alert.setContentText("ID: " + id + " zaten mevcut. Lütfen JSON dosyasını kontrol edin.");
                            alert.showAndWait();
                        });
                        continue; // Bu hatalı düğümü atla
}

                    String name = nodeMatcher.group(2);
                    double act = Double.parseDouble(nodeMatcher.group(3));
                    int inter = Integer.parseInt(nodeMatcher.group(4));
                    int proj = Integer.parseInt(nodeMatcher.group(5));

                    // Koordinatlar JSON'da olmadığı için burada rastgele oluşturuyoruz
                    // 50 ile 750 arası X, 50 ile 550 arası Y (Canvas boyutuna göre)
                    double randomX = 50 + (Math.random() * 700); 
                    double randomY = 50 + (Math.random() * 500); 

                    graph.addNode(new Node(id, name, randomX, randomY, act, inter, proj));
                } catch (Exception e) {
                    System.out.println("Düğüm ayrıştırma hatası: " + e.getMessage());
                }
            }
            // TRANSLATED: Success message
            System.out.println("Nodes loaded: " + graph.nodes.size());

            // --- 3. PARSE EDGES ---
            // Split content to find the "edges" section
            String[] parts = content.split("\"edges\":\\s*\\[");
            if (parts.length > 1) {
                String edgesContent = parts[1];
                
                // [REGEX USED HERE]
                // We use Regex to find "source" and "target" pairs specifically.
                // This ignores newlines or spaces between the numbers.
                Pattern edgePattern = Pattern.compile("\\{\\s*\"source\":\\s*(\\d+),\\s*\"target\":\\s*(\\d+)\\s*\\}");
                Matcher edgeMatcher = edgePattern.matcher(edgesContent);

                int edgesCount = 0;
                while (edgeMatcher.find()) {
                    int sId = Integer.parseInt(edgeMatcher.group(1));
                    int tId = Integer.parseInt(edgeMatcher.group(2));
                    
                    Node s = graph.getNodeById(sId);
                    Node t = graph.getNodeById(tId);
                    
                    if (s != null && t != null) {
                        graph.addEdge(s, t);
                        edgesCount++;
                    } else {
                        // TRANSLATED: Warning message
                        System.out.println("Warning: Attempting to connect non-existent ID: " + sId + " -> " + tId);
                    }
                }
                // TRANSLATED: Success message
                System.out.println("Edges loaded: " + edgesCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- SAVE JSON ---
    public static void save(String filePath, Graph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        // 1. Write Nodes
        sb.append("  \"nodes\": [\n");
        for (int i = 0; i < graph.nodes.size(); i++) {
            Node n = graph.nodes.get(i);
            // Format string to match JSON structure
            sb.append(String.format("    { \"id\": %d, \"name\": \"%s\", \"x\": %.2f, \"y\": %.2f, \"activity\": %.2f, \"interaction\": %d, \"projects\": %d }",
                    n.id, n.name, n.x, n.y, n.activity, n.interaction, n.projects));
            
            if (i < graph.nodes.size() - 1) sb.append(","); 
            sb.append("\n");
        }
        sb.append("  ],\n");

        // 2. Write Edges
        sb.append("  \"edges\": [\n");
        List<String> edgesLines = new ArrayList<>();
        // Prevent duplicates (save only if source ID < target ID)
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

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sb.toString());
            // TRANSLATED: Success message
            System.out.println("File saved successfully: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}