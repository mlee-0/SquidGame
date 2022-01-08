package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class TugOfWar extends Game {
    // Array of all teams.
    private Player[][] teams;
    private final int TEAM_SIZE = 10;
    // Array of the two teams currently playing.
    private final Player[][] activeTeams = new Player[2][];
    // Index of team containing the human player.
    private int humanTeamIndex;
    // Collective speed of all players on both teams.
    private double speed;
    // Bounds of middle region, as ratios of window width.
    private final double[] BOUNDS = {0.4, 0.6};

    private final ControllerGame3 controller;

    TugOfWar() {
        NAME = "Tug-of-War";
        TIME_LIMIT = 0;
        X_MIN = 0;
        X_MAX = 1200;
        Y_MIN = 0;
        Y_MAX = 300;
        Z_MIN = -900;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game3.fxml"));
        FXMLLoader fxmlLoaderPlatform = new FXMLLoader(getClass().getResource("platform.fxml"));
        setRoot(fxmlLoader);

        controller = fxmlLoader.getController();
        controller.platform1.setWidth(X_MAX * BOUNDS[0]);
        controller.platform1.setY((Y_MAX - controller.platform1.getHeight())/2);
        controller.lineCenter1.setWidth(X_MAX * BOUNDS[0]);
        controller.lineCenter1.setY((Y_MAX - controller.lineCenter1.getHeight())/2);
        controller.platform2.setWidth(X_MAX * BOUNDS[0]);
        controller.platform2.setX(X_MAX * BOUNDS[1]);
        controller.platform2.setY((Y_MAX - controller.platform2.getHeight())/2);
        controller.lineCenter2.setWidth(X_MAX * BOUNDS[0]);
        controller.lineCenter2.setX(X_MAX * BOUNDS[1]);
        controller.lineCenter2.setY((Y_MAX - controller.lineCenter2.getHeight())/2);
        controller.pane.setMaxWidth(X_MAX);
        controller.pane.setMaxHeight(Y_MAX);

        scene = new Scene(root, X_MAX + 20, Y_MAX + 100);
        scene.setOnKeyPressed(event -> {
            Player human = Main.getApp().getHumanPlayer();
            switch (event.getCode()) {
                case LEFT:
                    human.setMoveX(-1);
                    break;
                case RIGHT:
                    human.setMoveX(+1);
                    break;
            }
        });
        scene.setOnKeyReleased(event -> {
            Player human = app.getHumanPlayer();
            switch (event.getCode()) {
                case ESCAPE:
                    stop();
                    System.out.printf("Quitting %s\n", NAME);
                    break;
                case LEFT:
                case RIGHT:
                    human.setMoveX(0);
                    break;
            }
        });
    }

    public Scene getScene() { return scene; }
    public Pane getPane() { return controller.pane; }

    @Override
    public void handle(long now) {
        double speed = 0;
        for (Player[] team: activeTeams) {
            for (Player player: team) {
                if (player.isAlive()) {
                    speed += player.getXDirection() *
                            player.getStrength() * (player.isFalling() ? 0.5 : 1.0);
                }
            }
        }
        this.speed = speed / (TEAM_SIZE * 1.0);

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
        teams = new Player[remaining][TEAM_SIZE];
        int numberTeams = (int) Math.ceil(remaining / (double)TEAM_SIZE);
        Player[] playingPlayers = Main.getApp().getPlayingPlayers();

        // Assign players to teams.
        for (int i = 0; i < remaining; i++) {
            int teamIndex = i % numberTeams;
            teams[teamIndex][i / numberTeams] = playingPlayers[i];
            if (!playingPlayers[i].isComputer()) {
                humanTeamIndex = teamIndex;
            }
            playingPlayers[i].setPlaying(false);
        }

        // Select two teams to play, one of which contains the human player.
        int sideIndex = random.nextFloat() < 0.5 ? 0 : 1;
        activeTeams[sideIndex] = teams[humanTeamIndex];
        activeTeams[(sideIndex+1) % 2] = teams[(humanTeamIndex+1) % numberTeams];

        int side = 0;
        final int SPACING = 10;
        for (Player[] team: activeTeams) {
            int i = 0;
            for (Player player: team) {
                player.setPlaying(true);
                int xDirection = side == 0 ? -1 : +1;
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
    }

    @Override
    public void stop() {
        super.stop();
        app.setScenePlayerboard();
    }
}