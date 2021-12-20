package com.example.squidgame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main extends Application {
    private MainController controllerMain;
    private Stage stage;
    private Scene sceneMain;
    private RedLightGreenLight game1;

    private GridPane dashboard;
    private DashboardController controllerDashboard;

    private final Random random = new Random();

    private static final int MAX_PLAYERS = 456;
    private static final long PRIZE_INCREMENT = 100000000;
    private int humanPlayerNumber;

    private int remaining;
    private long prize;

    private final Player[] players = new Player[MAX_PLAYERS];
    private final ArrayList<Guard> guards = new ArrayList<>();

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        VBox rootMain = fxmlLoader.load();
        sceneMain = new Scene(rootMain, Entity.X_MAX, Entity.Y_MAX);
        controllerMain = fxmlLoader.getController();
        controllerMain.buttonMode1.setText(RedLightGreenLight.NAME);
        controllerMain.buttonMode1.setOnAction(event -> {
            game1 = new RedLightGreenLight(this);
            game1.getRoot().getChildren().add(0, dashboard);
            resetGame();
            createPlayers();
            game1.start();
            stage.setScene(game1.getScene());
        });

        FXMLLoader fxmlLoaderDashboard = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        dashboard = fxmlLoaderDashboard.load();
        dashboard.setStyle("-fx-background-color: " + Colors.BLACK);
        controllerDashboard = fxmlLoaderDashboard.getController();

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
        remaining -= count;
        prize += PRIZE_INCREMENT * (long)count;
        updateRemaining();
        updatePrize();
    }

    private void createPlayers() {
        ArrayList<String> occupations = new ArrayList<>();
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("occupations.txt")
            ))) {
                String line;
                do {
                    line = reader.readLine();
                    if (line != null && line.length() > 0) {
                        occupations.add(line);
                    }
                } while (line != null);
            }
        }
        catch (IOException e) {
            System.out.println(e);
            occupations = null;
        }

        humanPlayerNumber = random.nextInt(MAX_PLAYERS);
        for (int i = 0; i < players.length; i++) {
            double speed = random.nextDouble() * 0.25 + 0.5;
            double x = Entity.X_MIN;
            double y = random.nextDouble() * (Entity.Y_MAX - Entity.Y_MIN) + Entity.Y_MIN;
            int age = random.nextInt(83) + 18;
            String occupation = (occupations != null) ? occupations.get(random.nextInt(occupations.size())) : "";
            players[i] = new Player(i+1, x, y, speed, i != humanPlayerNumber, age, occupation);
            game1.getPane().getChildren().add(players[i].getSprite());
        }
        // Make human player appear on top.
        players[humanPlayerNumber].getSprite().toFront();
    }

    public void addGuard(Guard guard) {
        guards.add(guard);
        game1.getPane().getChildren().add(guard.getSprite());
    }

    private void resetGame() {
        remaining = MAX_PLAYERS;
        prize = 0;
        updateRemaining();
        updatePrize();
        for (Player player: players) {
            if (player != null) {
                game1.getPane().getChildren().remove(player.getSprite());
            }
        }
        for (Guard guard: guards) {
            if (guard != null) {
                game1.getPane().getChildren().remove(guard.getSprite());
            }
        }
        guards.clear();
    }

    // Display the remaining time (seconds).
    public void updateTimer(double remaining) {
        if (remaining >= 0) {
            controllerDashboard.labelTimer.setText(String.format("%02d:%02d", (int) Math.floor(remaining / 60), (int) Math.floor(remaining % 60)));
        }
    }

    public void updateRemaining() {
        controllerDashboard.labelRemaining.setText(String.format("%,d", remaining));
    }

    public void updatePrize() {
        controllerDashboard.labelPrize.setText(String.format("₩%,d", prize));
    }

    public static void main(String[] args) {
        launch();
    }
}