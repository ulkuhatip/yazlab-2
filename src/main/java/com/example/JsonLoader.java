package com.example;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonLoader implements IDataLoader {

    @Override
    public void load(String filePath, Graph graph) {
        // 1. Önceki verileri temizle
        graph.nodes.clear();
        graph.edges.clear();
        Set<Integer> seenIds = new HashSet<>();

        try {
            File f = new File(filePath);
            if (!f.exists()) {
                System.out.println("ERROR: File not found: " + filePath);
                return;
            }

            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            // --- 2. DÜĞÜMLERİ AYRIŞTIR (Arkadaşının hatasız Regex yapısı) ---
            // Bu regex x ve y koordinatlarını da yakalar, böylece düğümler kaymaz.
            Pattern nodePattern = Pattern.compile("\\{\\s*\"id\":\\s*(\\d+),\\s*\"name\":\\s*\"(.*?)\",\\s*\"x\":\\s*([\\d\\.]+),\\s*\"y\":\\s*([\\d\\.]+),\\s*\"activity\":\\s*([\\d\\.]+),\\s*\"interaction\":\\s*(\\d+),\\s*\"projects\":\\s*(\\d+)");
            Matcher nodeMatcher = nodePattern.matcher(content);

            while (nodeMatcher.find()) {
                try {
                    int id = Integer.parseInt(nodeMatcher.group(1));
                    
                    // ID Çakışma kontrolü (Senin eklediğin güvenlik)
                    if (!seenIds.add(id)) {
                        showError("Veri Hatası", "Tekrar eden ID: " + id);
                        continue;
                    }

                    String name = nodeMatcher.group(2);
                    double x = Double.parseDouble(nodeMatcher.group(3)); // JSON'daki X
                    double y = Double.parseDouble(nodeMatcher.group(4)); // JSON'daki Y
                    double act = Double.parseDouble(nodeMatcher.group(5));
                    int inter = Integer.parseInt(nodeMatcher.group(6));
                    int proj = Integer.parseInt(nodeMatcher.group(7));

                    // Artık random X/Y değil, gerçek koordinatlar ekleniyor
                    graph.addNode(new Node(id, name, x, y, act, inter, proj));
                    
                } catch (Exception e) {
                    System.out.println("Düğüm ayrıştırma hatası: " + e.getMessage());
                }
            }
            System.out.println("Yüklenen Düğüm Sayısı: " + graph.nodes.size());

            // --- 3. KENARLARI AYRIŞTIR ---
            String[] parts = content.split("\"edges\":\\s*\\[");
            if (parts.length > 1) {
                String edgesContent = parts[1];
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
                    }
                }
                System.out.println("Yüklenen Kenar Sayısı: " + edgesCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hata mesajı gösterme fonksiyonu
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void save(String filePath, Graph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"nodes\": [\n");
        for (int i = 0; i < graph.nodes.size(); i++) {
            Node n = graph.nodes.get(i);
            // x ve y'yi de kaydediyoruz ki bir sonraki load işleminde yerleri sabit kalsın
            sb.append(String.format(Locale.US, "    { \"id\": %d, \"name\": \"%s\", \"x\": %.2f, \"y\": %.2f, \"activity\": %.2f, \"interaction\": %d, \"projects\": %d }",
                    n.id, n.name, n.x, n.y, n.activity, n.interaction, n.projects));
            
            if (i < graph.nodes.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n  \"edges\": [\n");
        
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
        sb.append("  ]\n}");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sb.toString());
            System.out.println("Dosya başarıyla kaydedildi: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}