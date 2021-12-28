package com.example.squidgame;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Random;

abstract public class Game extends AnimationTimer {
    protected String NAME;
    protected static final Random random = new Random();
    protected static Main app;

    protected long TIME_LIMIT;
    protected long elapsed = 0;
    protected long now;
    protected long previous;

    abstract protected Scene getScene();
    protected VBox root;
    protected Scene scene;

    public static void setApp(Main app) { Game.app = app; }
    public void setRoot(FXMLLoader fxmlLoader) {
        try {
            root = fxmlLoader.load();
        }
        catch (IOException e) {
            root = new VBox();
        }
        root.getChildren().add(0, app.getDashboard());
    }

    public void handle(long now) {
        this.now = now;
        if (previous == 0) {
            previous = now;
            return;
        }
        elapsed += (now - previous);
        previous = now;
        app.updateTimer((TIME_LIMIT - elapsed) / 1e9);
    }
}
