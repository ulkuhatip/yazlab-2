package com.example;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.Optional;

public class NodeFormDialog {

    /**
     *
     * @param node      node for editing (or new node).
     * @param isNew     is it new data.
     * @param graph     Reference to the graph (for adding).
     * @param infoArea  Text area for logs.
     * @param onUpdate  Method to run after successful save (usually draw).
     */
    public static void open(Node node, boolean isNew, Graph graph, TextArea infoArea, Runnable onUpdate) {
        Dialog<Node> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add Person" : "Edit Person");
        dialog.setHeaderText("Enter details:");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        TextField nameField = new TextField(node.name);
        TextField actField = new TextField(String.valueOf(node.activity));
        TextField interField = new TextField(String.valueOf(node.interaction));
        TextField projField = new TextField(String.valueOf(node.projects));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Activity:"), 0, 1);
        grid.add(actField, 1, 1);
        grid.add(new Label("Interactions:"), 0, 2);
        grid.add(interField, 1, 2);
        grid.add(new Label("Projects:"), 0, 3);
        grid.add(projField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                try {
                    node.name = nameField.getText();
                    node.activity = Double.parseDouble(actField.getText());
                    node.interaction = Integer.parseInt(interField.getText());
                    node.projects = Integer.parseInt(projField.getText());
                    return node;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Node> result = dialog.showAndWait();
        result.ifPresent(n -> {
            if (isNew) {
                graph.addNode(n);
                infoArea.setText("Added: " + n.name);
            } else {
                infoArea.setText("Updated: " + n.name);
            }
            
            // run callback-а for redraw
            if (onUpdate != null) {
                onUpdate.run();
            }
        });
    }
}