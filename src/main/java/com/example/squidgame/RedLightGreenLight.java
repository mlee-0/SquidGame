package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;

public class RedLightGreenLight extends Game {
    public enum State { RED, GREEN, TURNING }
    private State state = State.RED;
    private long next = Long.MAX_VALUE;
    private static final float PROBABILITY_STOP = 0.5f;

    private final Doll doll;

    private final MediaPlayer music = new MediaPlayer(new Media(getClass().getResource("game_1.mp3").toExternalForm()));
    private final MediaPlayer[] sounds = new MediaPlayer[] {
            new MediaPlayer(new Media(getClass().getResource("game1_40.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_50.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_60.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_70.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_80.wav").toExternalForm())),
    };

    private final ControllerGame1 controller;

    RedLightGreenLight() {
        NAME = "Red Light, Green Light";
        TIME_LIMIT = (long) (2 * 60 * 1e9);
        X_MIN = 10;
        X_MAX = 1190;
        Y_MIN = 10;
        Y_MAX = 590;
        startingPosition = new double[] {X_MIN, -1};
        playerSpeedRange = new double[] {0.5, 1.0};

        doll = new Doll(X_MAX - 25, Y_MAX / 2);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game1.fxml"));
        setRoot(fxmlLoader);

        controller = fxmlLoader.getController();
        controller.finishLine.setFill(Paint.valueOf(Colors.PINK));
        controller.finishLine.setHeight(Y_MAX);
        controller.finishLine.setX(X_MAX - controller.finishLine.getWidth()/2);
        controller.pane.setMaxWidth(X_MAX);
        controller.pane.setMaxHeight(Y_MAX);
        controller.pane.getChildren().add(doll.getSprite());

        scene = new Scene(root, X_MAX + 20, Y_MAX + 100);
        KeyEventHandler handler = new KeyEventHandler();
        scene.setOnKeyPressed(handler);
        scene.setOnKeyReleased(handler);

        music.setCycleCount(MediaPlayer.INDEFINITE);
        music.setVolume(Main.getApp().getVolumeMusic());

        for (MediaPlayer sound: sounds) {
            sound.setOnEndOfMedia(() -> {
                sound.stop();
                cycleState();
            });
        }
    }

    public Scene getScene() { return scene; }
    public Pane getPane() { return controller.pane; }

    @Override
    public void handle(long now) {
        // Initial duration.
        if (previous == 0) {
            next = now + (long) 2e9;
        }
        super.handle(now);

        // Cycle the game state.
        if (elapsed < TIME_LIMIT) {
            if (now >= next) {
                cycleState();
            }
        }
        else {
            state = State.RED;
            doll.setState(state);
        }
    }

    @Override
    protected void handlePlayer(Player player) {
        super.handlePlayer(player);

        // Schedule killing for remaining players after game ends.
        if (elapsed >= TIME_LIMIT) {
            player.scheduleKill(now + random.nextLong((long)5e9));
        }

        switch (state) {
            case RED:
                if (player.isMoving()) {
                    if (!player.isScheduledKill()) {
                        player.scheduleKill(now +
                                random.nextLong((long)(0.2 * (next - now)), (long)(0.6 * (next - now)))
                        );
                    }
                    if (random.nextFloat() < PROBABILITY_STOP/5) {
                        player.stopMove();
                    }
                }
                break;
            case GREEN:
                if (player.isComputer() && !player.isScheduledStartMove()) {
                    player.scheduleStartMove(now + random.nextLong((long)1e9));
                }
                break;
            case TURNING:
                if (player.isComputer() && random.nextFloat() < PROBABILITY_STOP) {
                    player.stopMove();
                }
                break;
        }

        double x = player.getX();
        double y = player.getY();
        // Stop playing if reached the end.
        if (x >= X_MAX) {
            player.stop();
        }
        // Reverse the direction if at the bounds.
        if (y < Y_MIN || y > Y_MAX && player.isComputer()) {
            player.changeYDirection(-1);
        }
    }

    @Override
    public void start() {
        super.start();
        for (Player player: app.getPlayers()) {
            controller.pane.getChildren().add(player.getSprite());
        }
        app.addGuard(new Guard(X_MAX - 25, Y_MAX / 2 - 75, Guard.Rank.CIRCLE));
        app.addGuard(new Guard(X_MAX - 25, Y_MAX / 2 + 75, Guard.Rank.CIRCLE));
        // Make doll and human player appear in front.
        doll.getSprite().toFront();
        app.getHumanPlayer().getSprite().toFront();
        music.play();
    }

    @Override
    public void stop() {
        super.stop();
        for (MediaPlayer sound: sounds) {
            sound.stop();
        }
        music.stop();
        app.setScenePlayerboard();
    }

    private void cycleState() {
        switch (state) {
            case RED:
                state = State.GREEN;
                next = Long.MAX_VALUE;
                // Play sound.
                sounds[random.nextInt(sounds.length)].play();
                break;
            case GREEN:
                state = State.TURNING;
                next = now + (long)((5.0/60.0) * 1e9);
                doll.setState(state);
                break;
            case TURNING:
                state = State.RED;
                next = now + random.nextLong((long)2e9, (long)6e9);
                break;
        }
        doll.setState(state);
        System.out.println(state);
    }
}