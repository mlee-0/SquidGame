package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TugOfWar extends Game {
    // Array of all teams.
    private List<List<Player>> teams;
    private final int TEAM_SIZE = 10;
    // Indices of the two teams currently playing.
    private final int[] ACTIVE_TEAMS = new int[2];
    // Index of team containing the human player.
    private int humanTeamIndex;
    // Current speed of all players.
    private double speed;
    // Speed of all players when all players on both teams are pulling.
    private double maxSpeed;
    // Bounds of middle region, as ratios of window width.
    private final double[] BOUNDS = {0.4, 0.6};
    // Height and width of flag.
    private final double[] FLAG_SIZE = {15.0, 30.0};
    // Time, in nanoseconds, when the human starts pulling.
    private long timePull;
    // Duration, in nanoseconds, of the initial pull by the human.
    private final double PULL_DURATION = 0.25e9;

    private final ControllerGame3 controller;

    TugOfWar() {
        NAME = "Tug-of-War";
        TIME_LIMIT = 0;
        X_MIN = 0;
        X_MAX = 1200;
        Y_MIN = 0;
        Y_MAX = 300;
        Z_MIN = -900;

        soundsKill = new AudioClip[] {
                new AudioClip(Player.class.getResource("fall_1.mp3").toExternalForm()),
                new AudioClip(Player.class.getResource("fall_2.mp3").toExternalForm()),
                new AudioClip(Player.class.getResource("fall_3.mp3").toExternalForm()),
        };

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game3.fxml"));
        FXMLLoader fxmlLoaderPlatform1 = new FXMLLoader(getClass().getResource("game3_platform.fxml"));
        FXMLLoader fxmlLoaderPlatform2 = new FXMLLoader(getClass().getResource("game3_platform.fxml"));
        setRoot(fxmlLoader);

        controller = fxmlLoader.getController();
        try {
            fxmlLoaderPlatform1.load();
            fxmlLoaderPlatform2.load();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        ControllerGame3Platform controllerPlatform1 = fxmlLoaderPlatform1.getController();
        ControllerGame3Platform controllerPlatform2 = fxmlLoaderPlatform2.getController();
        controller.pane.getChildren().add(0, controllerPlatform1.group);
        controller.pane.getChildren().add(1, controllerPlatform2.group);
        controllerPlatform1.group.setLayoutX(X_MAX * BOUNDS[0]/2);
        controllerPlatform1.group.setLayoutY((Y_MAX - controllerPlatform1.platform.getHeight())/2);
        controllerPlatform1.group.setScaleX(X_MAX * BOUNDS[0]);
        controllerPlatform2.group.setLayoutX(X_MAX * (1 - (1 - BOUNDS[1])/2));
        controllerPlatform2.group.setLayoutY((Y_MAX - controllerPlatform1.platform.getHeight())/2);
        controllerPlatform2.group.setScaleX(X_MAX * (1 - BOUNDS[1]));
        controller.pane.setMaxWidth(X_MAX);
        controller.pane.setMaxHeight(Y_MAX);
        controller.rope.setX((X_MAX - controller.rope.getWidth())/2);
        controller.rope.setY((Y_MAX - controller.rope.getHeight())/2);
        controller.flag.setFill(Paint.valueOf(Colors.PINK));
        controller.flag.getPoints().addAll(0.0, 0.0, FLAG_SIZE[1]/2, FLAG_SIZE[0], FLAG_SIZE[1], 0.0);
        controller.flag.setLayoutX((X_MAX - FLAG_SIZE[1])/2);
        controller.flag.setLayoutY((Y_MAX - controller.rope.getHeight())/2);

        scene = new Scene(root, X_MAX + 20, Y_MAX + 100);
        KeyEventHandler handler = new KeyEventHandler();
        scene.setOnKeyPressed(handler);
        scene.setOnKeyReleased(handler);
    }

    public Scene getScene() { return scene; }
    public Pane getPane() { return controller.pane; }

    @Override
    public void handle(long now) {
        speed = maxSpeed * TEAM_SIZE;
        for (int i = 0; i < ACTIVE_TEAMS.length; i++) {
            int teamIndex = ACTIVE_TEAMS[i];
            List<Player> team = teams.get(teamIndex);
            // Whether all players on this team have been eliminated or have started falling.
            boolean teamEliminated = true;
            // Calculate the collective speed using the strength of each player.
            for (Player player: team) {
                teamEliminated = teamEliminated && (!player.isAlive() || player.isFalling());

                int xDirection = player.getXDirection();
                if (!player.isAlive()) {
                    speed -= xDirection * player.getStrength();
                }
                else if (player.isFalling()) {
                    speed -= xDirection * player.getStrength() / 2;
                }

                // Increase the speed if the human player has just started pulling.
                if (!player.isComputer() && xDirection != 0 && !player.isFalling() && player.isAlive()) {
                    long elapsed = now - timePull;
                    if (elapsed <= PULL_DURATION) {
                        // Increase the speed more if the human player's team is at a higher disadvantage.
                        speed += xDirection * player.getStrength() * (1 - elapsed / PULL_DURATION)
                                * (5 + (xDirection * maxSpeed < 0 ? 5 * Math.abs(maxSpeed) : 0));
                    }
                }
            }
            if (teamEliminated) {
                speed = 0;
                stopPlayers(ACTIVE_TEAMS[(i+1) % ACTIVE_TEAMS.length]);
                break;
            }
        }
        speed /= TEAM_SIZE;

        // Position the rope and flag.
        controller.rope.setX(controller.rope.getX() + speed);
        controller.flag.setLayoutX(controller.flag.getLayoutX() + speed);

        super.handle(now);
    }

    @Override
    protected void handlePlayer(Player player) {
        if (!player.isPlaying()) {
            return;
        }
        player.move(speed, 0);
        double x = player.getX();
        double z = player.getZ();
        player.setFalling(
                x > BOUNDS[0] * X_MAX && x < BOUNDS[1] * X_MAX || z < 0
        );
        if (z <= Z_MIN) {
            player.kill();
        }
    }

    @Override
    public void start() {
        super.start();

        int remaining = Main.getApp().getRemaining();
        teams = new ArrayList<>(remaining);
        int numberTeams = (int) Math.ceil(remaining / (double)TEAM_SIZE);
        Player[] playingPlayers = Main.getApp().getPlayingPlayers();

        // Assign players to teams.
        for (int i = 0; i < remaining; i++) {
            int teamIndex = i % numberTeams;
            int playerIndex = i / numberTeams;
            if (playerIndex == 0) {
                teams.add(teamIndex, new ArrayList<>());
            }
            teams.get(teamIndex).add(playerIndex, playingPlayers[i]);
            if (!playingPlayers[i].isComputer()) {
                humanTeamIndex = teamIndex;
            }
            playingPlayers[i].setPlaying(false);
        }

        // Select two teams to play, one of which contains the human player.
        int sideIndex = random.nextFloat() < 0.5 ? 0 : 1;
        ACTIVE_TEAMS[sideIndex] = humanTeamIndex;
        ACTIVE_TEAMS[(sideIndex+1) % 2] = (humanTeamIndex+1) % numberTeams;

        // Position players in the two active teams and start pulling.
        int side = 0;
        maxSpeed = 0;
        final int SPACING = 10;
        for (int teamIndex: ACTIVE_TEAMS) {
            int i = 0;
            for (Player player: teams.get(teamIndex)) {
                player.setPlaying(true);
                int xDirection = side == 0 ? -1 : +1;
                maxSpeed += xDirection * player.getStrength();
                player.setX(
                        X_MAX/2.0 + xDirection * (X_MAX*0.2 + i * SPACING)
                );
                player.setY(Y_MAX/2.0 + 5 * (i % 2 == 0 ? +1 : -1));
                if (player.isComputer()) {
                    player.setMoveX(xDirection);
                }
                controller.pane.getChildren().add(player.getSprite());
                i += 1;
            }
            side += 1;
        }
        maxSpeed /= TEAM_SIZE;
        System.out.printf("Maximum speed: %.2f\n", maxSpeed);
    }

    @Override
    public void stop() {
        super.stop();

        // Process remaining teams.
        for (int teamIndex: ACTIVE_TEAMS) {
            teams.remove(teamIndex);
        }
        while (teams.size() >= 2) {
            // Select two teams and calculate their total strengths.
            double[] strengths = new double[2];
            for (int teamIndex = 0; teamIndex < 2; teamIndex++) {
                double strength = 0;
                for (Player player: teams.get(teamIndex)) {
                    strength += player.getStrength();
                }
                strengths[teamIndex] = strength;
            }

            // Eliminate teams based on their strengths.
            if (strengths[0] > strengths[1]) {
                killPlayers(1);
                stopPlayers(0);
            }
            else if (strengths[1] > strengths[0]) {
                killPlayers(0);
                stopPlayers(1);
            }
            else {
                int losingTeamIndex = random.nextInt(2);
                killPlayers(losingTeamIndex);
                stopPlayers((losingTeamIndex+1) % 2);
            }

            // Remove the two teams.
            teams.subList(0, 2).clear();
        }

        app.setScenePlayerboard();
    }

    // Stop the players on the specified team.
    private void stopPlayers(int teamIndex) {
        for (Player player: teams.get(teamIndex)) {
            if (player.isPlaying()) {
                player.stop();
            }
        }
    }

    // Kill the players on the specified team.
    private void killPlayers(int teamIndex) {
        for (Player player: teams.get(teamIndex)) {
            player.kill();
        }
    }

    protected void onLeftPress() {
        app.getHumanPlayer().setMoveX(-1);
        timePull = now;
    }
    protected void onRightPress() {
        app.getHumanPlayer().setMoveX(+1);
        timePull = now;
    }
    protected void onUpPress() {}
    protected void onUpRelease() {}
    protected void onDownPress() {}
    protected void onDownRelease() {}
}