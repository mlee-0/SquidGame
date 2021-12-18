package com.example.squidgame;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

public class Game extends Application {
    private HelloController controllerMain;
    private Game1Controller controllerGame1;

    private final Random random = new Random();

    private final int MAX_PLAYERS = 456;
    private final long PRIZE_INCREMENT = 100000000;
//    private final int[] RESERVED_PLAYER_NUMBERS = {1, 67, 101, 199, 218, 240, 456};
    private int humanPlayerNumber;

    private int players_remaining;
    private long prize;

    private Player[] players = new Player[MAX_PLAYERS];

    private BorderPane rootGame1;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("squidgame-view.fxml"));
        FXMLLoader fxmlLoaderGame1 = new FXMLLoader(getClass().getResource("game1.fxml"));

        VBox rootMain = (VBox) fxmlLoader.load();
        rootGame1 = (BorderPane) fxmlLoaderGame1.load();
        rootGame1.setStyle("-fx-background-color: #FFEABF");

        Scene sceneMain = new Scene(rootMain, Entity.X_MAX, Entity.Y_MAX);
//        private Pane rootGame1 = new Pane();
        Scene sceneGame1 = new Scene(rootGame1, Entity.X_MAX, Entity.Y_MAX);

        AnimationTimer game1 = new RedLightGreenLight(this);

        controllerMain = (HelloController) fxmlLoader.getController();
        controllerMain.buttonMode1.setOnAction(event -> {
            resetGame();
            createPlayers();
            game1.start();
            stage.setScene(sceneGame1);
        });

        controllerGame1 = (Game1Controller) fxmlLoaderGame1.getController();
        controllerGame1.finishLine.setHeight(Entity.Y_MAX);
        controllerGame1.finishLine.setX(Entity.X_MAX - controllerGame1.finishLine.getWidth()/2);

        sceneGame1.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    game1.stop();
                    stage.setScene(sceneMain);
                    System.out.println("Quitting game 1");
                    break;
                case LEFT:
                case RIGHT:
                    players[humanPlayerNumber].setMoveX(0);
                case UP:
                case DOWN:
                    players[humanPlayerNumber].setMoveY(0);
            }
        });
        sceneGame1.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    players[humanPlayerNumber].setMoveX(-1);
                    break;
                case RIGHT:
                    players[humanPlayerNumber].setMoveX(+1);
                    break;
                case UP:
                    players[humanPlayerNumber].setMoveY(-1);
                    break;
                case DOWN:
                    players[humanPlayerNumber].setMoveY(+1);
                    break;
            }
        });

        stage.setTitle("Squid Game");
        stage.setScene(sceneMain);
        stage.show();
    }

    public Player[] getPlayers() { return players; }

    public void eliminatePlayers(int count) {
        players_remaining -= count;
        prize += PRIZE_INCREMENT * (long)count;
        updatePrize();
        System.out.println(String.valueOf(players_remaining) + " remaining");
    }

    private void createPlayers() {
        humanPlayerNumber = random.nextInt(MAX_PLAYERS);
        updatePlayerNumber(humanPlayerNumber);
        for (int i = 0; i < players.length; i++) {
            double speed = random.nextDouble() * 0.25 + 0.5;
            players[i] = new Player(i+1, 0, random.nextDouble() * Entity.Y_MAX, speed, i != humanPlayerNumber);
            ((Pane) rootGame1.getCenter()).getChildren().add(players[i].getSprite());
        }
    }

    private void resetGame() {
        players_remaining = MAX_PLAYERS;
        prize = 0;
        updatePrize();
        for (Player player: players) {
            if (player != null) {
                ((Pane) rootGame1.getCenter()).getChildren().remove(player.getSprite());
            }
        }
    }

    // Display the remaining time (seconds).
    public void updateTimer(double remaining) {
        if (remaining >= 0) {
            controllerGame1.labelTimer.setText(String.format("%02d:%02d", (int) Math.floor(remaining / 60), (int) Math.floor(remaining % 60)));
        }
    }

    public void updatePlayerNumber(int number) {
        controllerGame1.labelPlayerNumber.setText(String.format("%03d", number));
    }

    public void updatePrize() {
        controllerGame1.labelPrize.setText(String.format("%,d", prize));
    }

    public static void main(String[] args) {
        launch();
    }
}