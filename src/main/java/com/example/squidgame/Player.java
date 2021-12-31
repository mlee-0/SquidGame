package com.example.squidgame;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Player extends Entity {
    private final int playerNumber;
    private final boolean computer;

    private String name = "";
    private int age;
    private String occupation = "";
    private double strength;

    private boolean playing;
    private boolean cutting;
    private boolean licking;

    private long timeKill = Long.MAX_VALUE;
    private long timeStartMove = Long.MAX_VALUE;
    private long timeStopMove = Long.MAX_VALUE;
    private boolean scheduledKill = false;
    private boolean scheduledStartMove = false;
    private boolean scheduledStopMove = false;

    private Circle sprite;
    private FillTransition humanAnimation;
    private Button playerboardButton;

    Player(int playerNumber, double x, double y, double maxSpeed, boolean computer, int age, String occupation, double strength) {
        this.playerNumber = playerNumber;
        this.x = x;
        this.y = y;
        this.maxSpeed = maxSpeed;
        this.computer = computer;
        this.age = age;
        this.occupation = occupation;
        this.strength = strength;

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

    public Circle getSprite() { return sprite; }
    public Button getPlayerboardButton() { return playerboardButton; }
    public boolean isComputer() { return computer; }
    public boolean isPlaying() { return playing; }
    public boolean isCutting() { return cutting; }
    public boolean isLicking() { return licking; }
    public void setCutting(boolean cutting) { this.cutting = cutting; }
    public void setLicking(boolean licking) { this.licking = licking; }
    public String getPlayerNumber() { return String.format("%03d", playerNumber); }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getOccupation() { return occupation; }

    public long getTimeKill() { return timeKill; }
    public long getTimeStartMove() { return timeStartMove; }
    public long getTimeStopMove() { return timeStopMove; }
    public boolean isScheduledKill() { return scheduledKill; }
    public boolean isScheduledStartMove() { return scheduledStartMove; }
    public boolean isScheduledStopMove() { return scheduledStopMove; }
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

        System.out.printf("Killed %d\n", playerNumber);
    }

    @Override
    public String toString() {
        return String.format("Player %d, %s", playerNumber, occupation);
    }
}