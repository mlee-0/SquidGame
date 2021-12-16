package com.example.squidgame;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
//import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

public class Game extends Application {
    static HelloController controller;

    private Random random = new Random();

    private final int MAX_PLAYERS = 456;
    private final int PRIZE_INCREMENT = 100000000;
//    private final int[] RESERVED_PLAYER_NUMBERS = {1, 67, 101, 199, 218, 240, 456};

    private int players_remaining;
    private int prize;

    private Player[] players = new Player[MAX_PLAYERS];

    private Pane rootMode1 = new Pane();
    private Scene sceneMode1 = new Scene(rootMode1, 960, 480);

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("squidgame-view.fxml"));
        VBox rootMain = (VBox) fxmlLoader.load();

        AnimationTimer game1 = new AnimationTimer() {
            enum State { RED, GREEN, TURNING }

            private State state = State.RED;
            long previous;
            long elapsed = 0;
            long timeLastLightSwitch = 0;
            long duration = (long) 3e9;

            @Override
            public void handle(long now) {
                if (previous == 0) {
                    previous = now;
                    return;
                }
                elapsed += now - previous;
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
                for (Player player: players) {
                    if (player.isAlive()) {
                        switch (state) {
                            case RED:
                                if (player.isMoving()) {
                                    player.kill();
//                                    rootMode1.getChildren().remove(player.getSprite());
                                    numberPlayersEliminated += 1;
                                }
                                break;
                            case GREEN:
                                if (!player.isMoving() && random.nextFloat() < 0.05) {
                                    player.toggleMove(
                                            now, 1, random.nextInt(3) - 1
                                    );
                                }
                                break;
                            case TURNING:
                                if (player.isMoving() && random.nextFloat() < 0.05 && player.isComputer()) {
                                    player.stopMove();
                                }
                                break;
                        }
                        player.move(now);
                    }
                }
                if (numberPlayersEliminated > 0) {
                    eliminatePlayers(numberPlayersEliminated);
                }
            }
        };

        controller = (HelloController) fxmlLoader.getController();
        controller.buttonMode1.setOnAction(event -> {
            resetGame();
            createPlayers();
            game1.start();
            stage.setScene(sceneMode1);
        });

        Scene sceneMain = new Scene(rootMain);//, 320, 240);

        sceneMode1.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    game1.stop();
                    stage.setScene(sceneMain);
                    System.out.println("Quitting scene2");
            }
        });

        stage.setTitle("Squid Game");
        stage.setScene(sceneMain);
        stage.show();
    }

    private void resetGame() {
        players_remaining = MAX_PLAYERS;
        prize = 0;
        for (Player player: players) {
            if (player != null) {
                rootMode1.getChildren().remove(player.getSprite());
            }
        }
    }

    private void createPlayers() {
        for (int i = 0; i < players.length; i++) {
            double speed = random.nextDouble() * 0.5 + 0.5;
            players[i] = new Player(i, 0, random.nextDouble() * 500, speed);
            rootMode1.getChildren().add(players[i].getSprite());
        }
    }

    private void eliminatePlayers(int count) {
        players_remaining -= count;
        prize += PRIZE_INCREMENT * count;
        System.out.println(String.valueOf(players_remaining) + " remaining");
    }

    public static void main(String[] args) {
        launch();
    }
}