package com.example.squidgame;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;

import java.io.IOException;
import java.util.Random;

abstract public class Game extends AnimationTimer {
    protected String NAME;
    protected static final Random random = new Random();
    protected static Main app = Main.getApp();

    protected long TIME_LIMIT;
    protected long elapsed;
    protected long now;
    protected long previous;

    protected double X_MIN;
    protected double X_MAX;
    protected double Y_MIN;
    protected double Y_MAX;
    protected double Z_MIN;
    protected double Z_MAX = 100;

    // The initial X and Y positions for players. Use a negative number to randomize the position within the possible range of values.
    protected double[] startingPosition = {X_MIN, Y_MIN};
    // The minimum and maximum possible speeds for players.
    protected double[] playerSpeedRange = {1, 1};
    // Sound files played when players die.
    protected AudioClip[] soundsKill = new AudioClip[] {
            new AudioClip(Player.class.getResource("kill_1.mp3").toExternalForm()),
            new AudioClip(Player.class.getResource("kill_2.mp3").toExternalForm()),
            new AudioClip(Player.class.getResource("kill_3.mp3").toExternalForm()),
    };

    protected VBox root;
    protected Scene scene;

    public double getXMin() { return X_MIN; }
    public double getXMax() { return X_MAX; }
    public double getYMin() { return Y_MIN; }
    public double getYMax() { return Y_MAX; }
    public double getZMin() { return Z_MIN; }
    public double getZMax() { return Z_MAX; }

    // Return a random kill sound file.
    public AudioClip getSoundKill() {
        return soundsKill[random.nextInt(soundsKill.length)];
    }

    abstract protected Scene getScene();
    public VBox getRoot() { return root; }
    protected void setRoot(FXMLLoader fxmlLoader) {
        try {
            root = fxmlLoader.load();
        }
        catch (IOException e) {
            root = new VBox();
        }
    }
    abstract protected Pane getPane();

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
        app.updateLabelStatus();
    }

    @Override
    public void stop() {
        super.stop();
        app.incrementGame();
    }

    @Override
    public String toString() { return NAME; }

    protected void onEscapePress() {}
    protected void onEscapeRelease() { stop(); System.out.printf("Quitting %s\n", NAME); }
    protected void onLeftPress() { app.getHumanPlayer().setMoveX(-1); }
    protected void onLeftRelease() { app.getHumanPlayer().setMoveX(0); }
    protected void onRightPress() { app.getHumanPlayer().setMoveX(+1); }
    protected void onRightRelease() { app.getHumanPlayer().setMoveX(0); }
    protected void onUpPress() { app.getHumanPlayer().setMoveY(-1); }
    protected void onUpRelease() { app.getHumanPlayer().setMoveY(0); }
    protected void onDownPress() { app.getHumanPlayer().setMoveY(+1); }
    protected void onDownRelease() { app.getHumanPlayer().setMoveY(0); }
    protected void onSpacePress() {}
    protected void onSpaceRelease() {}
    protected void onCPress() {}
    protected void onCRelease() {}
    protected void onLPress() {}
    protected void onLRelease() {}
}