package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

public class AnimationManager {

    public void animatePath(List<Node> path,
                             List<Node> highlightedNodes,
                             Runnable redraw) {

        if (path == null || path.isEmpty()) return;

        highlightedNodes.clear();
        redraw.run();

        Timeline timeline = new Timeline();

        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);

            KeyFrame kf = new KeyFrame(
                Duration.millis(i * 150),
                e -> {
                    highlightedNodes.add(node);
                    redraw.run();
                }
            );

            timeline.getKeyFrames().add(kf);
        }

        timeline.play();
    }
}
