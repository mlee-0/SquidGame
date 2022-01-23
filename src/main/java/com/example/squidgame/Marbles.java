package com.example.squidgame;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;

public class Marbles extends Game {
    private Player teammate;
    // Whether the human player is making the guess.
    private boolean isTurnHuman = true;

    private final MediaPlayer music = new MediaPlayer(new Media(getClass().getResource("game_4.mp3").toExternalForm()));

    private final ControllerGame4 controller;

    Marbles() {
        NAME = "Marbles";
        TIME_LIMIT = (long) (5 * 60 * 1e9);
        X_MIN = 10;
        X_MAX = 1190;
        Y_MIN = 10;
        Y_MAX = 590;
        startingPosition = new double[] {X_MIN, Y_MIN/2};
        playerSpeedRange = new double[] {1.0, 1.0};

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game4.fxml"));
        setRoot(fxmlLoader);
        root.requestFocus();

        controller = fxmlLoader.getController();
        controller.pane.setMaxWidth(X_MAX);
        controller.pane.setMaxHeight(Y_MAX);
        controller.buttonBetLeft.setOnAction(event -> {
            human.setMarblesBet(controller.spinnerLeft.getValue());
            setDisableButtonsBet(true);
        });
        controller.buttonBetRight.setOnAction(event -> {
            teammate.setMarblesBet(controller.spinnerRight.getValue());
        });
        controller.buttonBetRight.setDisable(true);
        controller.spinnerRight.setPromptText("?");
        controller.spinnerRight.setDisable(true);
        controller.buttonEven.setOnAction(event -> {
            controller.labelGuess.setText(controller.buttonEven.getText());
            guess(true);
        });
        controller.buttonOdd.setOnAction(event -> {
            controller.labelGuess.setText(controller.buttonOdd.getText());
            guess(false);
        });

        scene = new Scene(root, X_MAX + 20, Y_MAX + 100);
        KeyEventHandler handler = new KeyEventHandler();
        scene.setOnKeyPressed(handler);
        scene.setOnKeyReleased(handler);

        music.setCycleCount(MediaPlayer.INDEFINITE);
        music.setVolume(Main.getApp().getVolumeMusic());
    }

    public Scene getScene() { return scene; }
    public Pane getPane() { return controller.pane; }

    @Override
    public void handle(long now) {
        super.handle(now);

        controller.labelMarblesLeft.setText(String.valueOf(app.getHumanPlayer().getMarbles()));
        controller.labelMarblesRight.setText(String.valueOf(teammate.getMarbles()));
    }

    @Override
    protected void handlePlayer(Player player) {
        super.handlePlayer(player);
    }

    @Override
    public void start() {
        super.start();

        // Select the teammate.
        Player[] playingPlayers = app.getPlayingPlayers(true);
        teammate = playingPlayers[random.nextInt(playingPlayers.length)];

        // Initialize marbles.
        app.getHumanPlayer().setMarbles(10);
        teammate.setMarbles(10);

        controller.paneLeft.getChildren().add(app.getHumanPlayer().getSprite());
        controller.paneRight.getChildren().add(teammate.getSprite());
        // Make human player appear in front.
        app.getHumanPlayer().getSprite().toFront();
        music.play();
    }

    @Override
    public void stop() {
        super.stop();
        music.stop();
        app.setScenePlayerboard();
    }

    @Override
    protected void onLeftPress() {}
    protected void onLeftRelease() {}
    protected void onRightPress() {}
    protected void onRightRelease() {}
    protected void onUpPress() {}
    protected void onUpRelease() {}
    protected void onDownPress() {}
    protected void onDownRelease() {}

    private void guess(boolean even) {
        Player guessingPlayer = isTurnHuman ? human : teammate;
        Player otherPlayer = isTurnHuman ? teammate : human;
        Player winningPlayer;
        Player losingPlayer;

        int marblesBet = guessingPlayer.getMarblesBet();
        if (even && marblesBet % 2 == 0) {
            winningPlayer = guessingPlayer;
            losingPlayer = otherPlayer;
        }
        else {
            winningPlayer = otherPlayer;
            losingPlayer = guessingPlayer;
        }
        winningPlayer.changeMarbles(marblesBet);
        losingPlayer.changeMarbles(-marblesBet);
        takeMarbles(losingPlayer == human, marblesBet);
        updateLabelsMarbles();

        setDisableButtonsGuess(isTurnHuman);
        isTurnHuman = !isTurnHuman;
    }

    private void updateLabelsMarbles() {
        controller.labelMarblesLeft.setText(String.valueOf(human.getMarbles()));
        controller.labelMarblesRight.setText(String.valueOf(teammate.getMarbles()));
    }

    private void setDisableButtonsGuess(boolean disable) {
        controller.buttonEven.setDisable(disable);
        controller.buttonOdd.setDisable(disable);
    }

    private void setDisableButtonsBet(boolean disable) {
        controller.spinnerLeft.setDisable(disable);
        controller.buttonBetLeft.setDisable(disable);
    }

    // Remove a specific number of marbles from one player and add them to the center.
    private void takeMarbles(boolean fromHuman, int count) {
         ObservableList<Node> marbles = (fromHuman ? controller.paneLeft : controller.paneRight).getChildren();
         if (count <= marbles.size()) {
             for (int i = 0; i < count; i++) {
                 Node marble = marbles.get(i);
                 marbles.remove(marble);
                 controller.paneCenter.getChildren().add(marble);
             }
         }
    }

    // Remove all marbles from the center and add them to a player.
    private void putMarbles(boolean toHuman) {
        (toHuman ? controller.paneLeft : controller.paneRight).getChildren().addAll(controller.paneCenter.getChildren());
        controller.paneCenter.getChildren().clear();

        setDisableButtonsBet(false);
    }

    // Reset the bet amount for the spinners.
    private void resetSpinners() {
        controller.spinnerLeft.getValueFactory().setValue(1);
        controller.spinnerRight.getValueFactory().setValue(1);
    }
}