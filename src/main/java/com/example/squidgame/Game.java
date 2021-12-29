package com.example.squidgame;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
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

    protected VBox root;
    protected Scene scene;

    abstract protected Scene getScene();
    public VBox getRoot() { return root; }
    abstract protected Pane getPane();

    public static void setApp(Main app) { Game.app = app; }
    protected void setRoot(FXMLLoader fxmlLoader) {
        try {
            root = fxmlLoader.load();
        }
        catch (IOException e) {
            root = new VBox();
        }
    }

    public void handle(long now) {
        this.now = now;
        if (previous == 0) {
            previous = now;
            return;
        }
        elapsed += (now - previous);
        previous = now;
        app.updateLabelTimer((TIME_LIMIT - elapsed) / 1e9);

        // Stop the game if no more players playing.
        if (app.getPlaying() <= 0) {
            stop();
        }
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        app.incrementGame();
    }

    @Override
    public String toString() { return NAME; }
}