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
    protected long elapsed;
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

    @Override
    public void handle(long now) {
        this.now = now;
        if (previous == 0) {
            previous = now;
            return;
        }
        elapsed += (now - previous);
        previous = now;
        app.updateLabelTimer((TIME_LIMIT - elapsed) / 1e9);

        // Process each player.
        for (Player player: app.getPlayers()) {
            if (player.isAlive() && player.isPlaying()) {
                handlePlayer(player);
            }
        }

        // Stop the game if no more players playing.
        if (app.getPlaying() <= 0) {
            stop();
        }
    }

    protected void handlePlayer(Player player) {
        // Perform any scheduled actions if enough time has elapsed.
        if (player.isScheduledStartMove() && now >= player.getTimeStartMove()) {
            player.setMoveX(1);
        }
        if (player.isScheduledKill() && now >= player.getTimeKill()) {
            player.kill();
            app.getControllerPlayerboard().board.getChildren().remove(player.getPlayerboardButton());
            app.eliminatePlayers(1);
        }

        // Update the player's position.
        player.move();
    }

    @Override
    public void start() {
        super.start();
        elapsed = 0;
        previous = 0;
        app.resetPlayers();
    }

    @Override
    public void stop() {
        super.stop();
        app.incrementGame();
    }

    @Override
    public String toString() { return NAME; }
}