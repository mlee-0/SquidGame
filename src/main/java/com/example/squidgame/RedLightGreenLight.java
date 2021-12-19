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

    public enum State { RED, GREEN, TURNING;}
    private State state = State.RED;
    private static final double TIME_LIMIT = 2 * 60;  // Seconds
    private long elapsed = 0;
    private long now;
    private long previous;
    private long next = Long.MAX_VALUE;

    private final Doll doll = new Doll(Entity.X_MAX - 25, Entity.Y_MAX / 2);
    private MediaPlayer sound = new MediaPlayer(new Media(getClass().getResource("game1.wav").toExternalForm()));

    private static final double probabilityStartMoving = 0.025;
    private static final double probabilityStopMoving = 0.75;

    private final Game app;
    private final VBox root;
    private final Scene scene;
    private final Game1Controller controller;

    RedLightGreenLight(Game app) throws IOException {
        this.app = app;

        FXMLLoader fxmlLoaderGame1 = new FXMLLoader(getClass().getResource("game1.fxml"));
        root = fxmlLoaderGame1.load();
        root.setStyle("-fx-background-color: #FFEABF");
        controller = fxmlLoaderGame1.getController();
        controller.finishLine.setFill(Paint.valueOf(Colors.PINK));
        controller.finishLine.setHeight(Entity.Y_MAX);
        controller.finishLine.setX(Entity.X_MAX - controller.finishLine.getWidth()/2);

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
        app.updateTimer(TIME_LIMIT - elapsed / 1e9);

        // Cycle the game state.
        if (now >= next) {
            cycleState();
        }

        // Process each player.
        int numberPlayersEliminated = 0;
        for (Player player: app.getPlayers()) {
            if (player.isAlive() && player.isPlaying()) {
                // Kill the player if enough time has elapsed.
                if (now >= player.getTimeKill()) {
                    player.kill();
                    numberPlayersEliminated += 1;
                }

                switch (state) {
                    case RED:
                        if (player.isMoving()) {
                            if (!player.isTargeted()) {
                                player.target(now + (long)(random.nextDouble() * (next-now) / 3));
                            }
                            if (random.nextFloat() < probabilityStopMoving && player.isComputer()) {
                                player.stopMove();
                            }
                        }
                        break;
                    case GREEN:
                        if (!player.isMoving() && random.nextFloat() < probabilityStartMoving && player.isComputer()) {
                            player.setMoveX(1);
                            player.setMoveY(random.nextInt(3) - 1);
                        }
                        break;
                    case TURNING:
                        if (player.isMoving() && random.nextFloat() < probabilityStopMoving && player.isComputer()) {
                            player.stopMove();
                        }
                        break;
                }

                // Increment the player's position.
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

    public void start() {
        super.start();
        app.addGuard(new Guard(Entity.X_MAX - 25, Entity.Y_MAX / 2 - 75, Guard.Rank.CIRCLE));
        app.addGuard(new Guard(Entity.X_MAX - 25, Entity.Y_MAX / 2 + 75, Guard.Rank.CIRCLE));
        app.addEntity(doll);
    }

    private void cycleState() {
        switch (state) {
            case RED:
                state = State.GREEN;
                next = Long.MAX_VALUE;  //now + (long) ((random.nextDouble() * 5 + 1) * 1e9);
                // Play sound.
                sound.setRate(random.nextDouble() * 2 + 1);
                sound.play();
                break;
            case GREEN:
                state = State.TURNING;
                next = now + (long)((random.nextDouble() * 0.05 + 0.025) * 1e9);
                doll.setState(state);
                break;
            case TURNING:
                state = State.RED;
                next = now + (long) ((random.nextDouble() * 4 + 2) * 1e9);
                break;
        }
        doll.setState(state);
        System.out.println(state);
    }
}