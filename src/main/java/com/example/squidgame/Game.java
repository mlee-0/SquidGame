package com.example.squidgame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Game extends Application {
    private HelloController controllerMain;
    private Stage stage;
    private Scene sceneMain;
    private RedLightGreenLight game1;

    private final Random random = new Random();

    private static final int MAX_PLAYERS = 456;
    private static final long PRIZE_INCREMENT = 100000000;
    private int humanPlayerNumber;

    private int players_remaining;
    private long prize;

    private final Player[] players = new Player[MAX_PLAYERS];
    private final ArrayList<Guard> guards = new ArrayList<>();

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("squidgame-view.fxml"));
        VBox rootMain = fxmlLoader.load();
        sceneMain = new Scene(rootMain, Entity.X_MAX, Entity.Y_MAX);

        game1 = new RedLightGreenLight(this);

        controllerMain = fxmlLoader.getController();
        controllerMain.buttonMode1.setText(RedLightGreenLight.NAME);
        controllerMain.buttonMode1.setOnAction(event -> {
            resetGame();
            createPlayers();
            game1.start();
            stage.setScene(game1.getScene());
        });

        stage.setTitle("Squid Game");
        stage.setScene(sceneMain);
        stage.show();
    }

    public void setSceneMain() {
        stage.setScene(sceneMain);
    }

    public Player[] getPlayers() { return players; }
    public Player getHumanPlayer() { return players[humanPlayerNumber]; }

    public void eliminatePlayers(int count) {
        players_remaining -= count;
        prize += PRIZE_INCREMENT * (long)count;
        updatePrize();
        System.out.println(players_remaining + " remaining");
    }

    private void createPlayers() {
        humanPlayerNumber = random.nextInt(MAX_PLAYERS);
        updatePlayerNumber(humanPlayerNumber);
        for (int i = 0; i < players.length; i++) {
            double speed = random.nextDouble() * 0.25 + 0.5;
            players[i] = new Player(i+1, 0, random.nextDouble() * Entity.Y_MAX, speed, i != humanPlayerNumber);
            game1.getPane().getChildren().add(players[i].getSprite());
        }
        // Make human player appear on top.
        players[humanPlayerNumber].getSprite().toFront();
    }

    public void createGuard(double x, double y) {
        guards.add(new Guard(x, y));
        game1.getPane().getChildren().add(guards.get(guards.size()-1).getSprite());
    }

    private void resetGame() {
        players_remaining = MAX_PLAYERS;
        prize = 0;
        updatePrize();
        for (Player player: players) {
            if (player != null) {
                game1.getPane().getChildren().remove(player.getSprite());
            }
        }
        guards.clear();
    }

    // Display the remaining time (seconds).
    public void updateTimer(double remaining) {
        if (remaining >= 0) {
            game1.getController().labelTimer.setText(String.format("%02d:%02d", (int) Math.floor(remaining / 60), (int) Math.floor(remaining % 60)));
        }
    }

    public void updatePlayerNumber(int number) {
        game1.getController().labelPlayerNumber.setText(String.format("%03d", number));
    }

    public void updatePrize() {
        game1.getController().labelPrize.setText(String.format("%,d", prize));
    }

    public static void main(String[] args) {
        launch();
    }
}