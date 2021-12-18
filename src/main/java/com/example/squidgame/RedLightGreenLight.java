package com.example.squidgame;

import javafx.animation.AnimationTimer;

import java.util.Random;

public class RedLightGreenLight extends AnimationTimer {
    Game game;
    private Random random = new Random();

    enum State { RED, GREEN, TURNING }

    private State state = State.RED;
    // Time limit (seconds).
    private final double TIME_LIMIT = 5 * 60;
    long previous;
    long elapsed = 0;
    long timeLastLightSwitch = 0;
    long duration = (long) 3e9;

    private final double probabilityStartMoving = 0.025;
    private final double probabilityStopMoving = 0.05;

    RedLightGreenLight(Game game) {
        this.game = game;
    }

    @Override
    public void handle(long now) {
        if (previous == 0) {
            previous = now;
            return;
        }
        elapsed += (now - previous);
        previous = now;
        game.updateTimer(TIME_LIMIT - elapsed / 1e9);

        // Cycle the game state.
        if ((now - timeLastLightSwitch) > duration) {
            timeLastLightSwitch = now;
            switch (state) {
                case RED:
                    state = State.GREEN;
                    duration = (long) ((random.nextDouble() * 5 + 1) * 1e9);
                    break;
                case GREEN:
                    state = State.TURNING;
                    duration = (long) ((random.nextDouble() * 0.5 + 0.5) * 1e9);
                    break;
                case TURNING:
                    state = State.RED;
                    duration = (long) ((random.nextDouble() * 4 + 2) * 1e9);
                    break;
            }
            System.out.println(state);
        }

        // Process each player.
        int numberPlayersEliminated = 0;
        for (Player player: game.getPlayers()) {
            if (player.isAlive() && player.isPlaying()) {
                // Kill the player if enough time has elapsed.
                if (now >= player.getTimeKill()) {
                    player.kill();
                    numberPlayersEliminated += 1;
                }

                switch (state) {
                    case RED:
                        if (player.isMoving()) {
                            if (!player.isTargeted()) {
                                player.target(now + (long)(random.nextDouble() * duration / 3));
                            }
                            if (random.nextFloat() < probabilityStopMoving && player.isComputer()) {
                                player.stopMove();
                            }
                        }
                        break;
                    case GREEN:
                        if (!player.isMoving() && random.nextFloat() < probabilityStartMoving && player.isComputer()) {
                            player.setMoveX(1);
                            player.setMoveY(random.nextInt(3) - 1);
                        }
                        break;
                    case TURNING:
                        if (player.isMoving() && random.nextFloat() < probabilityStopMoving && player.isComputer()) {
                            player.stopMove();
                        }
                        break;
                }

                // Increment the player's position.
                player.move(now);
                double[] location = player.getLocation();

                // Stop playing if reached the end.
                if (location[0] >= Entity.X_MAX) {
                    player.stop();
                }
                // Reverse the direction if at the bounds.
                if (location[1] < Entity.Y_MIN || location[1] > Entity.Y_MAX && player.isComputer()) {
                    player.changeYDirection(-1);
                }
            }
        }
        if (numberPlayersEliminated > 0) {
            game.eliminatePlayers(numberPlayersEliminated);
        }
    }
}
