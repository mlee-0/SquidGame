package com.example.squidgame;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.util.Random;

public class RedLightGreenLight extends AnimationTimer {
    public static final String NAME = "Red Light, Green Light";
    private final Random random = new Random();

    public enum State { RED, GREEN, TURNING }
    private State state = State.RED;
    private static final long TIME_LIMIT = (long) (2 * 60 * 1e9);  // Seconds
    private long elapsed = 0;
    private long now;
    private long previous;
    private long next = Long.MAX_VALUE;

    private final Doll doll = new Doll(Entity.X_MAX - 25, Entity.Y_MAX / 2);
    private final MediaPlayer sound = new MediaPlayer(new Media(getClass().getResource("game1.wav").toExternalForm()));

    private final Main app;
    private VBox root;
    private final Scene scene;
    private final Game1Controller controller;

    RedLightGreenLight(Main app) {
        this.app = app;

        FXMLLoader fxmlLoaderGame1 = new FXMLLoader(getClass().getResource("game1.fxml"));
        try {
            root = fxmlLoaderGame1.load();
        }
        catch (IOException e) {
            root = new VBox();
        }
        root.setStyle("-fx-background-color: #FFEABF");
        controller = fxmlLoaderGame1.getController();
        controller.finishLine.setFill(Paint.valueOf(Colors.PINK));
        controller.finishLine.setHeight(Entity.Y_MAX);
        controller.finishLine.setX(Entity.X_MAX - controller.finishLine.getWidth()/2);
        controller.pane.getChildren().add(doll.getSprite());

        scene = new Scene(root, Entity.X_MAX + 20, Entity.Y_MAX + 100);
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    stop();
                    app.setSceneMain();
                    System.out.println("Quitting " + NAME);
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

        sound.setOnEndOfMedia(() -> {
            sound.stop();
            sound.seek(sound.getStartTime());
            cycleState();
        });
    }

    public Scene getScene() { return scene; }
    public VBox getRoot() { return root; }
    public Pane getPane() { return controller.pane; }
    public Game1Controller getController() { return controller; }

    @Override
    public void handle(long now) {
        this.now = now;
        if (previous == 0) {
            previous = now;
            next = now + (long) 2e9;
            return;
        }
        elapsed += (now - previous);
        previous = now;
        app.updateTimer((TIME_LIMIT - elapsed) / 1e9);

        // Cycle the game state.
        if (now >= next && elapsed < TIME_LIMIT) {
            cycleState();
        }

        // Process each player.
        int numberPlayersEliminated = 0;
        for (Player player: app.getPlayers()) {
            if (player.isAlive() && player.isPlaying()) {
                // Perform any scheduled actions if enough time has elapsed.
                if (player.isScheduledStartMove() && now >= player.getTimeStartMove()) {
                    player.setMoveX(1);
                    player.setMoveY(random.nextInt(-1, 2));
                }
                if (player.isScheduledStopMove() && now >= player.getTimeStopMove()) {
                    player.stopMove();
                }
                if (player.isScheduledKill() && now >= player.getTimeKill()) {
                    player.kill();
                    numberPlayersEliminated += 1;
                }
                // Schedule killing for remaining players after game ends.
                if (elapsed >= TIME_LIMIT) {
                    player.scheduleKill(now + random.nextLong((long)5e9));
                }

                switch (state) {
                    case RED:
                        if (player.isMoving() && !player.isScheduledKill()) {
                            player.scheduleKill(now + random.nextLong((long)((next - now)/2.0)));
                        }
                        break;
                    case GREEN:
                        if (player.isComputer() && !player.isScheduledStartMove()) {
                            player.scheduleStartMove(now + random.nextLong((long)1e9));
                        }
                        break;
                    case TURNING:
                        if (player.isComputer() && !player.isScheduledStopMove()) {
                            player.scheduleStopMove(now + random.nextLong((long)((1.0/60.0) * 1e9)));
                        }
                        break;
                }

                // Update the player's position.
                player.move();
                double[] location = player.getLocation();

                // Stop playing if reached the end.
                if (location[0] >= Entity.X_MAX) {
                    player.stop();
                }
                // Reverse the direction if at the bounds.
                if (location[1] < Entity.Y_MIN || location[1] > Entity.Y_MAX && player.isComputer()) {
                    player.changeYDirection(-1);
                }
            }
        }
        if (numberPlayersEliminated > 0) {
            app.eliminatePlayers(numberPlayersEliminated);
        }
    }

    @Override
    public void start() {
        super.start();
        app.addGuard(new Guard(Entity.X_MAX - 25, Entity.Y_MAX / 2 - 75, Guard.Rank.CIRCLE));
        app.addGuard(new Guard(Entity.X_MAX - 25, Entity.Y_MAX / 2 + 75, Guard.Rank.CIRCLE));
    }

    @Override
    public void stop() {
        super.stop();
        sound.stop();
    }

    private void cycleState() {
        switch (state) {
            case RED:
                state = State.GREEN;
                next = Long.MAX_VALUE;
                // Play sound.
                sound.setRate(random.nextDouble() * 2.0 + 1.0);
                sound.play();
                break;
            case GREEN:
                state = State.TURNING;
                next = now + (long)((3.0/60.0) * 1e9);
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