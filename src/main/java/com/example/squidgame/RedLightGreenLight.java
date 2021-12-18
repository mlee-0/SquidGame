package com.example.squidgame;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.util.Random;

public class RedLightGreenLight extends AnimationTimer {
    public static final String NAME = "Red Light, Green Light";

    private final Game app;
    private final VBox root;
    private final Scene scene;
    private final Game1Controller controller;

    private final Random random = new Random();

    enum State { RED, GREEN, TURNING }

    private State state = State.RED;
    // Time limit (seconds).
    private static final double TIME_LIMIT = 5 * 60;
    long previous;
    long elapsed = 0;
    long timeLastLightSwitch = 0;
    long duration = (long) 3e9;

    private static final double probabilityStartMoving = 0.025;
    private static final double probabilityStopMoving = 0.05;

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
    }

    public Scene getScene() { return scene; }
    public VBox getRoot() { return root; }
    public Pane getPane() { return controller.pane; }
    public Game1Controller getController() { return controller; }

    @Override
    public void handle(long now) {
        if (previous == 0) {
            previous = now;
            return;
        }
        elapsed += (now - previous);
        previous = now;
        app.updateTimer(TIME_LIMIT - elapsed / 1e9);

        // Cycle the game state.
        if ((now - timeLastLightSwitch) > duration) {
            timeLastLightSwitch = now;
            switch (state) {
                case RED:
                    state = State.GREEN;
                    duration = (long) ((random.nextDouble() * 5 + 1) * 1e9);
                    break;
                case GREEN:
                    state = State.TURNING;
                    duration = (long) ((random.nextDouble() * 0.5 + 0.5) * 1e9);
                    break;
                case TURNING:
                    state = State.RED;
                    duration = (long) ((random.nextDouble() * 4 + 2) * 1e9);
                    break;
            }
            System.out.println(state);
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
                                player.target(now + (long)(random.nextDouble() * duration / 3));
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
        app.createGuard(Entity.X_MAX - 25, Entity.Y_MAX / 2 - 50);
        app.createGuard(Entity.X_MAX - 25, Entity.Y_MAX / 2 + 50);
    }
}