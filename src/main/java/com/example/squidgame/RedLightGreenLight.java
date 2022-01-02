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
    private static final float PROBABILITY_STOP = 0.6f;

    private final Doll doll = new Doll(Entity.X_MAX - 25, Entity.Y_MAX / 2);
    private final MediaPlayer music = new MediaPlayer(new Media(getClass().getResource("game_1.mp3").toExternalForm()));
    private final MediaPlayer[] sounds = new MediaPlayer[] {
            new MediaPlayer(new Media(getClass().getResource("game1_40.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_50.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_60.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_70.wav").toExternalForm())),
            new MediaPlayer(new Media(getClass().getResource("game1_80.wav").toExternalForm())),
    };

    private final Game1Controller controller;

    RedLightGreenLight() {
        NAME = "Red Light, Green Light";
        TIME_LIMIT = (long) (2 * 60 * 1e9);
        startingPosition = new double[] {Entity.X_MIN, -1};
        playerSpeedRange = new double[] {0.6, 1.0};

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game1.fxml"));
        setRoot(fxmlLoader);

        controller = fxmlLoader.getController();
        controller.finishLine.setFill(Paint.valueOf(Colors.PINK));
        controller.finishLine.setHeight(Entity.Y_MAX);
        controller.finishLine.setX(Entity.X_MAX - controller.finishLine.getWidth()/2);
        controller.pane.getChildren().add(doll.getSprite());

        scene = new Scene(root, Entity.X_MAX + 20, Entity.Y_MAX + 100);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    app.getHumanPlayer().setMoveX(-1);
                    break;
                case RIGHT:
                    app.getHumanPlayer().setMoveX(+1);
                    break;
                case UP:
                    app.getHumanPlayer().setMoveY(-1);
                    break;
                case DOWN:
                    app.getHumanPlayer().setMoveY(+1);
                    break;
            }
        });
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    stop();
                    System.out.printf("Quitting %s\n", NAME);
                    break;
                case LEFT:
                case RIGHT:
                    app.getHumanPlayer().setMoveX(0);
                    break;
                case UP:
                case DOWN:
                    app.getHumanPlayer().setMoveY(0);
                    break;
            }
        });

        music.setCycleCount(MediaPlayer.INDEFINITE);
        music.setVolume(Main.getApp().getVolumeMusic());

        for (MediaPlayer sound: sounds) {
            sound.setOnEndOfMedia(() -> {
                sound.stop();
//                sound.seek(sound.getStartTime());
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
                        player.scheduleKill(now + random.nextLong((long)((next - now)/2.0)));
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
        if (x >= Entity.X_MAX) {
            player.stop();
        }
        // Reverse the direction if at the bounds.
        if (y < Entity.Y_MIN || y > Entity.Y_MAX && player.isComputer()) {
            player.changeYDirection(-1);
        }
    }

    @Override
    public void start() {
        super.start();
        for (Player player: app.getPlayers()) {
            controller.pane.getChildren().add(player.getSprite());
        }
        app.addGuard(new Guard(Entity.X_MAX - 25, Entity.Y_MAX / 2 - 75, Guard.Rank.CIRCLE));
        app.addGuard(new Guard(Entity.X_MAX - 25, Entity.Y_MAX / 2 + 75, Guard.Rank.CIRCLE));
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