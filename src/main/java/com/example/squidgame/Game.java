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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

    private Pane rootGame1 = new Pane();
    private Scene sceneGame1 = new Scene(rootGame1, Entity.X_MAX, Entity.Y_MAX);

    @Override
    public void start(Stage stage) throws IOException {
        rootGame1.setStyle("-fx-background-color: #FFEABF");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("squidgame-view.fxml"));
        VBox rootMain = (VBox) fxmlLoader.load();

        AnimationTimer game1 = new RedLightGreenLight(this);

        controller = (HelloController) fxmlLoader.getController();
        controller.buttonMode1.setOnAction(event -> {
            resetGame();
            createPlayers();
            game1.start();
            stage.setScene(sceneGame1);
        });

        Scene sceneMain = new Scene(rootMain, Entity.X_MAX, Entity.Y_MAX);

        sceneGame1.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    game1.stop();
                    stage.setScene(sceneMain);
                    System.out.println("Quitting game 1");
            }
        });

        stage.setTitle("Squid Game");
        stage.setScene(sceneMain);
        stage.show();
    }

    public Player[] getPlayers() { return players; }

    public void eliminatePlayers(int count) {
        players_remaining -= count;
        prize += PRIZE_INCREMENT * count;
        System.out.println(String.valueOf(players_remaining) + " remaining");
    }

    private void createPlayers() {
        for (int i = 0; i < players.length; i++) {
            double speed = random.nextDouble() * 0.25 + 0.5;
            players[i] = new Player(i+1, 0, random.nextDouble() * Entity.Y_MAX, speed);
            rootGame1.getChildren().add(players[i].getSprite());
        }
    }

    private void resetGame() {
        players_remaining = MAX_PLAYERS;
        prize = 0;
        for (Player player: players) {
            if (player != null) {
                rootGame1.getChildren().remove(player.getSprite());
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}