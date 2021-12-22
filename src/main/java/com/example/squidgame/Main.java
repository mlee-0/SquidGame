package com.example.squidgame;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main extends Application {
    private Stage stage;
    private Scene sceneMain;
    private RedLightGreenLight game1;

    private DashboardController controllerDashboard;
    private PlayerboardController controllerPlayerboard;

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

        FXMLLoader fxmlLoaderDashboard = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        GridPane dashboard = fxmlLoaderDashboard.load();
        dashboard.setStyle("-fx-background-color: " + Colors.BLACK);
        controllerDashboard = fxmlLoaderDashboard.getController();

        FXMLLoader fxmlLoaderPlayerboard = new FXMLLoader(getClass().getResource("playerboard.fxml"));
        HBox playerboard = fxmlLoaderPlayerboard.load();
        Scene scenePlayerboard = new Scene(playerboard, 400, 600);
        controllerPlayerboard = fxmlLoaderPlayerboard.getController();
        controllerPlayerboard.buttonContinue.setOnAction(event -> {
            game1 = new RedLightGreenLight(this);
            game1.getRoot().getChildren().add(0, dashboard);
            for (Player player: players) {
                game1.getPane().getChildren().add(player.getSprite());
            }
            game1.start();
            stage.setScene(game1.getScene());
        });

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        VBox rootMain = fxmlLoader.load();
        sceneMain = new Scene(rootMain, Entity.X_MAX, Entity.Y_MAX);
        MainController controllerMain = fxmlLoader.getController();
        controllerMain.buttonPlay.setText("Play");
        controllerMain.buttonPlay.setOnAction(event -> {
            stage.setScene(scenePlayerboard);
            resetGame();
            createPlayers();
        });

        stage.setTitle("Squid Game");
        stage.setScene(sceneMain);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public DashboardController getControllerDashboard() { return controllerDashboard; }
    public PlayerboardController getControllerPlayerboard() { return controllerPlayerboard; }

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
        InputStream input = getClass().getResourceAsStream("occupations.txt");
        if (input != null) {
            try {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
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
                occupations = null;
            }
        }

        ObservableList<Node> children = controllerPlayerboard.board.getChildren();
        children.remove(0, children.size());
        humanPlayerNumber = random.nextInt(MAX_PLAYERS);
        for (int i = 0; i < players.length; i++) {
            double speed = random.nextDouble(0.5, 1.0);
            double x = Entity.X_MIN;
            double y = random.nextDouble(Entity.Y_MIN, Entity.Y_MIN + (Entity.Y_MAX - Entity.Y_MIN));
            int age = random.nextInt(18, 101);
            String occupation = (occupations != null) ? occupations.get(random.nextInt(occupations.size())) : "";
            Player player = new Player(i+1, x, y, speed, i != humanPlayerNumber, age, occupation);
            players[i] = player;

            // Add players to player board.
            int row = i / 24;
            int column = i % 24;
            Button button = new Button(player.getPlayerNumber());
            button.setStyle("-fx-padding: 1.0"); // -fx-background-color: #000; -fx-text-fill: #808080");
            button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            button.setOnAction(event -> {
                controllerPlayerboard.labelNumber.setText(
                        String.format("Player %s", player.getPlayerNumber())
                );
                controllerPlayerboard.labelName.setText(player.getName());
                controllerPlayerboard.labelAge.setText(String.valueOf(player.getAge()));
                controllerPlayerboard.labelOccupation.setText(player.getOccupation());
            });
            controllerPlayerboard.board.add(button, column, row);
            player.setPlayerboardButton(button);
            // Display the human player initially.
            if (i == humanPlayerNumber) {
                button.fire();
            }
        }
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
}