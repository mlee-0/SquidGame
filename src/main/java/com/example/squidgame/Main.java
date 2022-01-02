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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main extends Application {
    private static Main app;

    private Stage stage;
    private Scene sceneMain;

    private static Game[] games;
    private static int gameIndex;

    private GridPane dashboard;
    private DashboardController controllerDashboard;
    private PlayerboardController controllerPlayerboard;
    Scene scenePlayerboard;

    private final Random random = new Random();

    private static final int MAX_PLAYERS = 456;
    private static final long PRIZE_INCREMENT = 100000000;
    private int humanPlayerNumber;

    private int remaining;
    private long prize;

    private final Player[] players = new Player[MAX_PLAYERS];
    private final ArrayList<Guard> guards = new ArrayList<>();

    private double volumeMusic = 0.75;
    private MediaPlayer musicMenu = new MediaPlayer(new Media(
            getClass().getResource("menu.mp3").toExternalForm()
    ));
    private MediaPlayer musicPlayerboard = new MediaPlayer(new Media(
            getClass().getResource("pregame.mp3").toExternalForm()
    ));

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        Main.app = this;

        FXMLLoader fxmlLoaderDashboard = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        dashboard = fxmlLoaderDashboard.load();
        controllerDashboard = fxmlLoaderDashboard.getController();

        FXMLLoader fxmlLoaderPlayerboard = new FXMLLoader(getClass().getResource("playerboard.fxml"));
        HBox playerboard = fxmlLoaderPlayerboard.load();
        scenePlayerboard = new Scene(playerboard, 600, 400);
        controllerPlayerboard = fxmlLoaderPlayerboard.getController();
        controllerPlayerboard.buttonNext.setOnAction(event -> {
            Game game = games[gameIndex];
            musicPlayerboard.stop();
            setSceneGame(game.getScene());
            game.getRoot().getChildren().add(0, dashboard);
            game.start();
        });

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        VBox rootMain = fxmlLoader.load();
        sceneMain = new Scene(rootMain, Entity.X_MAX, Entity.Y_MAX);
        MainController controllerMain = fxmlLoader.getController();
        controllerMain.buttonPlay.setOnAction(event -> {
            musicMenu.stop();
            setScenePlayerboard();
            resetGame();
            createPlayers();
        });

        // Load occupations file.
        Player.loadOccupations();

        musicMenu.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayerboard.setCycleCount(MediaPlayer.INDEFINITE);
        musicMenu.setVolume(volumeMusic);
        musicPlayerboard.setVolume(volumeMusic);

        stage.setTitle("Squid Game Game");
        setSceneMain();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static Main getApp() { return app; }
    public GridPane getDashboard() { return dashboard; }
    public DashboardController getControllerDashboard() { return controllerDashboard; }
    public PlayerboardController getControllerPlayerboard() { return controllerPlayerboard; }

    public void setSceneMain() {
        stage.setScene(sceneMain);
        musicMenu.play();
    }

    public void setScenePlayerboard() {
        stage.setScene(scenePlayerboard);
        musicPlayerboard.play();
    }
    public void setSceneGame(Scene scene) { stage.setScene(scene); }
    public Player[] getPlayers() { return players; }
    public Player getHumanPlayer() { return players[humanPlayerNumber]; }
    public int getRemaining() { return remaining; }
    public int getPlaying() {
        int playing = 0;
        for (Player player: players) {
            if (player.isPlaying()) {
                playing += 1;
            }
        }
        return playing;
    }
    public Player[] getPlayingPlayers() {
        Player[] playingPlayers = new Player[getRemaining()];
        if (playingPlayers.length > 0) {
            int i = 0;
            for (Player player: players) {
                if (player.isPlaying()) {
                    playingPlayers[i] = player;
                    i += 1;
                }
            }
        }
        return playingPlayers;
    }

    private void createPlayers() {
        ObservableList<Node> children = controllerPlayerboard.board.getChildren();
        children.remove(0, children.size());
        humanPlayerNumber = random.nextInt(MAX_PLAYERS);
        for (int i = 0; i < players.length; i++) {
            int playerNumber = i + 1;
            Player player = new Player(playerNumber, i != humanPlayerNumber);
            players[i] = player;

            // Add players to player board.
            int row = i / 24;
            int column = i % 24;
            Button button = new Button(player.getPlayerNumber());
            button.setStyle(button.getStyle() +
                    String.format(".button {; -fx-background-color: %s; -fx-text-fill: %s; -fx-padding: 3}",
                            i == humanPlayerNumber ? Colors.WHITE : Colors.PINK_LIGHT, Colors.PLAYER_DARK)
            );
            button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            button.setOnAction(event -> {
                String labelNumber = String.format("Player %s", player.getPlayerNumber());
                if (!player.isComputer()) {
                    labelNumber += " (You)";
                }
                controllerPlayerboard.labelNumber.setText(labelNumber);
                controllerPlayerboard.labelName.setText(player.getName());
                controllerPlayerboard.labelAge.setText(String.valueOf(player.getAge()));
                controllerPlayerboard.labelOccupation.setText(player.getOccupation());
                controllerPlayerboard.labelStrength.setText(String.format("%.1f", player.getStrength()));
            });
            controllerPlayerboard.board.add(button, column, row);
            player.setPlayerboardButton(button);
            // Display the human player initially.
            if (i == humanPlayerNumber) {
                button.fire();
            }
        }
    }

    public void eliminatePlayers(int count) {
        remaining -= count;
        prize += PRIZE_INCREMENT * (long)count;
        updateLabelRemaining();
        updateLabelPrize();
    }

    public void addGuard(Guard guard) {
        guards.add(guard);
        games[gameIndex].getPane().getChildren().add(guard.getSprite());
    }

    public void updateButtonNextGame() {
        controllerPlayerboard.buttonNext.setText(games[gameIndex].NAME);
    }

    public static Game getGame() { return games[gameIndex]; }

    public void incrementGame() {
        if (gameIndex < games.length - 1) {
            gameIndex += 1;
        }
        updateButtonNextGame();
    }

    public void resetPlayers() {
        for (Player player: getPlayers()) {
            if (player.isAlive()) {
                player.reset();
            }
        }
    }

    private void resetGame() {
        games = new Game[] {
                new RedLightGreenLight(),
                new Dalgona(),
        };
        gameIndex = 0;
        remaining = MAX_PLAYERS;
        prize = 0;
        updateLabelRemaining();
        updateLabelPrize();
        updateButtonNextGame();
        for (Player player: players) {
            if (player != null) {
                games[gameIndex].getPane().getChildren().remove(player.getSprite());
            }
        }
        for (Guard guard: guards) {
            if (guard != null) {
                games[gameIndex].getPane().getChildren().remove(guard.getSprite());
            }
        }
        guards.clear();
    }

    // Display the remaining time (seconds).
    public void updateLabelTimer(double remaining) {
        if (remaining >= 0) {
            controllerDashboard.labelTimer.setText(String.format("%02d:%02d", (int) Math.floor(remaining / 60), (int) Math.floor(remaining % 60)));
        }
    }

    public void updateLabelRemaining() {
        controllerDashboard.labelRemaining.setText(String.format("%,d", remaining));
    }

    public void updateLabelPrize() {
        controllerDashboard.labelPrize.setText(String.format("$%,d", prize));
    }

    public double getVolumeMusic() { return volumeMusic; }
}