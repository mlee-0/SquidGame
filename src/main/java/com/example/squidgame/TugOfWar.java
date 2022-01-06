package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class TugOfWar extends Game {
    private Player[][] teams;
    private final int TEAM_SIZE = 10;

    private final Game3Controller controller;

    TugOfWar() {
        NAME = "Tug-of-War";
        TIME_LIMIT = 0;
        X_MIN = 0;
        X_MAX = 1200;
        Y_MIN = 0;
        Y_MAX = 300;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game3.fxml"));
        FXMLLoader fxmlLoaderPlatform = new FXMLLoader(getClass().getResource("platform.fxml"));
        setRoot(fxmlLoader);

        controller = fxmlLoader.getController();
        controller.platform1.setWidth(X_MAX * 0.4);
        controller.platform1.setY((Y_MAX - controller.platform1.getHeight())/2);
        controller.lineCenter1.setWidth(X_MAX * 0.4);
        controller.lineCenter1.setY((Y_MAX - controller.lineCenter1.getHeight())/2);
        controller.platform2.setWidth(X_MAX * 0.4);
        controller.platform2.setX(X_MAX * 0.6);
        controller.platform2.setY((Y_MAX - controller.platform2.getHeight())/2);
        controller.lineCenter2.setWidth(X_MAX * 0.4);
        controller.lineCenter2.setX(X_MAX * 0.6);
        controller.lineCenter2.setY((Y_MAX - controller.lineCenter2.getHeight())/2);
        controller.pane.setMaxWidth(X_MAX);
        controller.pane.setMaxHeight(Y_MAX);

        scene = new Scene(root, X_MAX + 20, Y_MAX + 100);
    }

    public Scene getScene() { return scene; }
    public Pane getPane() { return controller.pane; }

    @Override
    public void handle(long now) {

    }

    @Override
    protected void handlePlayer(Player player) {

    }

    @Override
    public void start() {
        int remaining = Main.getApp().getRemaining();
        teams = new Player[remaining][TEAM_SIZE];
        int numberTeams = (int) Math.ceil(remaining / (double)TEAM_SIZE);
        Player[] playingPlayers = Main.getApp().getPlayingPlayers();
        for (int i = 0; i < remaining; i++) {
            teams[i % numberTeams][i / numberTeams] = playingPlayers[i];
        }
    }

    @Override
    public void stop() {

    }
}