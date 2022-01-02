package com.example.squidgame;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Player extends Entity {
    private final int playerNumber;
    private final boolean computer;

    private final String name;
    private final int age;
    private final String occupation;
    private final double strength;

    private boolean playing;
    private boolean cutting;
    private boolean licking;

    private long timeKill = Long.MAX_VALUE;
    private long timeStartMove = Long.MAX_VALUE;
    private long timeStopMove = Long.MAX_VALUE;
    private boolean scheduledKill = false;
    private boolean scheduledStartMove = false;
    private boolean scheduledStopMove = false;

    private final Circle sprite;
    private FillTransition humanAnimation;
    private Button playerboardButton;

    private static Random random = new Random();

    private static String CONSONANTS = "bcdfghjklmnpqrstvwxyz";
    private static String VOWELS = "aeiou";
    private static ArrayList<String> occupations;

    Player(int playerNumber, boolean computer) {
        this.playerNumber = playerNumber;
        this.computer = computer;

        name = generateName();
        age = random.nextInt(18, 101);
        occupation = (occupations != null) ? occupations.get(random.nextInt(occupations.size())) : "";
        strength = random.nextDouble(0.5, 1);

        relativeSpeed = new Random().nextDouble(0, 1);

        sprite = new Circle(x, y, 5);
        // Create an animation if a human player.
        if (!computer) {
            humanAnimation = new FillTransition(
                    Duration.seconds(0.5), sprite, Color.web(Colors.PLAYER_LIGHT), Color.web(Colors.PLAYER)
                    );
            humanAnimation.setCycleCount(Animation.INDEFINITE);
            humanAnimation.setAutoReverse(true);
        }

        reset();
    }

    public String getPlayerNumber() { return String.format("%03d", playerNumber); }
    public boolean isComputer() { return computer; }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getOccupation() { return occupation; }
    public double getStrength() { return strength; }

    public boolean isPlaying() { return playing; }
    public boolean isCutting() { return cutting; }
    public boolean isLicking() { return licking; }
    public void setCutting(boolean cutting) { this.cutting = cutting; }

    public void setLicking(boolean licking) { this.licking = licking; }
    public long getTimeKill() { return timeKill; }
    public long getTimeStartMove() { return timeStartMove; }
    public long getTimeStopMove() { return timeStopMove; }
    public boolean isScheduledKill() { return scheduledKill; }
    public boolean isScheduledStartMove() { return scheduledStartMove; }

    public boolean isScheduledStopMove() { return scheduledStopMove; }
    public Circle getSprite() { return sprite; }
    public Button getPlayerboardButton() { return playerboardButton; }
    public void setPlayerboardButton(Button button) { playerboardButton = button; }

    public void setMoveX(int direction) {
        xDirection = direction;
        scheduledStartMove = false;
    }

    public void setMoveY(int direction) {
        yDirection = direction;
        scheduledStartMove = false;
    }

    public void stopMove() {
        setMoveX(0);
        setMoveY(0);
        scheduledStopMove = false;
    }

    public void move() {
        super.move();
        sprite.setCenterX(x);
        sprite.setCenterY(y);
    }

    public void changeXDirection(int multiplier) { xDirection *= multiplier; }

    public void changeYDirection(int multiplier) { yDirection *= multiplier; }

    public void scheduleStartMove(long time) {
        if (!scheduledStartMove) {
            timeStartMove = time;
            scheduledStartMove = true;
        }
    }
    public void scheduleStopMove(long time) {
        if (!scheduledStopMove) {
            timeStopMove = time;
            scheduledStopMove = true;
        }
    }

    public void reset() {
        playing = true;
        cutting = false;
        licking = false;

        timeKill = Long.MAX_VALUE;
        timeStartMove = Long.MAX_VALUE;
        timeStopMove = Long.MAX_VALUE;
        scheduledKill = false;
        scheduledStartMove = false;
        scheduledStopMove = false;

        double[] startingPosition = Main.getGame().startingPosition;
        if (startingPosition[0] >= 0) {
            x = startingPosition[0];
        }
        else {
            x = new Random().nextDouble(Entity.X_MIN, Entity.X_MAX);
        }
        if (startingPosition[1] >= 0) {
            y = startingPosition[1];
        }
        else {
            y = new Random().nextDouble(Entity.Y_MIN, Entity.Y_MAX);
        }
        xDirection = 0;
        yDirection = 0;
        zDirection = 0;

        sprite.setFill(Paint.valueOf(Colors.PLAYER));
        sprite.setStroke(Paint.valueOf(computer ? Colors.PLAYER_DARK : Colors.BLACK));
        sprite.setStrokeWidth(computer ? 1 : 2);
        sprite.setOpacity(0.9);
        if (!computer) {
            humanAnimation.play();
        }
    }

    public void stop() {
        playing = false;
        if (!computer) {
            humanAnimation.stop();
        }
        sprite.setFill(Paint.valueOf(Colors.GRAY));
        sprite.setStrokeWidth(0);
        System.out.printf("Stopped %d\n", playerNumber);
    }

    public void scheduleKill(long time) {
        if (!scheduledKill) {
            timeKill = time;
            scheduledKill = true;
            if (!computer) {
                humanAnimation.stop();
            }
            sprite.setFill(Paint.valueOf(Colors.RED));
            sprite.setStrokeWidth(0);
        }
    }

    public void kill() {
        alive = false;
        playing = false;
        scheduledKill = false;
        xSpeed = 0.0;
        ySpeed = 0.0;

        if (!computer) {
            humanAnimation.stop();
        }
        PauseTransition killAnimation = new PauseTransition(Duration.millis(1));
        killAnimation.setOnFinished(event -> {
            sprite.setFill(Paint.valueOf(Colors.RED_LIGHT));
            sprite.setStrokeWidth(0);
        });
        killAnimation.play();

        Main app = Main.getApp();
        app.eliminatePlayers(1);
        app.getControllerPlayerboard().board.getChildren().remove(playerboardButton);

        System.out.printf("Killed %d\n", playerNumber);
    }

    public static String generateName() {
        String name = "";
        boolean isVowel = false;
        double probabilityVowel = 0.1;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < random.nextInt(3, 9); j++) {
                String letters;
                if (random.nextDouble() < probabilityVowel) {
                    letters = VOWELS;
                }
                else {
                    letters = CONSONANTS;
                }
                char character = letters.charAt(random.nextInt(letters.length()));
                if (j == 0) {
                    character = Character.toUpperCase(character);
                }
                name += character;
                isVowel = letters == VOWELS;
                probabilityVowel = isVowel ? 0.1 : 0.9;
            }
            name += ' ';
        }
        return name;
    }

    public static void loadOccupations() {
        occupations = new ArrayList<>();
        InputStream input = Player.class.getResourceAsStream("occupations.txt");
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
    }

    @Override
    public String toString() {
        return String.format("Player %d, %s", playerNumber, occupation);
    }
}