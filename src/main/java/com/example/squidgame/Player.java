package com.example.squidgame;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Player extends Entity {
    private final int playerNumber;
    private boolean playing = true;
    private boolean targeted = false;
    private final boolean computer;

    private String name = "";
    private String occupation = "";
    private int age;

    private long timeKill = Long.MAX_VALUE;

    private Circle sprite;

    Player(int playerNumber, double x, double y, double maxSpeed, boolean computer) {
        this.playerNumber = playerNumber;
        this.x = x;
        this.y = y;
        this.maxSpeed = maxSpeed;
        this.computer = computer;
        sprite = new Circle(x, y, 5);
        sprite.setFill(Paint.valueOf("#009FBF"));
        sprite.setStroke(Paint.valueOf("#006A7F"));
        sprite.setStrokeWidth(1);
    }

    public Shape getSprite() {
        return sprite;
    }

    public boolean isPlaying() { return playing; }

    public boolean isTargeted() { return targeted; }

    public boolean isComputer() {
        return computer;
    }

    public long getTimeKill() { return timeKill; }

    public void setMoveX(int direction) {
        xDirection = direction;
    }

    public void setMoveY(int direction) {
        yDirection = direction;
    }

    public void stopMove() {
        setMoveX(0);
        setMoveY(0);
    }

    public void move(long now) {
        // Keep in bounds.
        if (x < X_MIN) {
            x = X_MIN;
        }
        if (y < Y_MIN) {
            y = Y_MIN;
        }
        else if (y > Y_MAX) {
            y = Y_MAX;
        }

        xSpeed = xDirection != 0 ? maxSpeed : 0;
        ySpeed = yDirection != 0 ? 0.25 : 0;
        x += xDirection * xSpeed;
        y += yDirection * ySpeed;

        sprite.setCenterX(x);
        sprite.setCenterY(y);
    }

    public void changeXDirection(int multiplier) {
        xDirection *= multiplier;
    }

    public void changeYDirection(int multiplier) {
        yDirection *= multiplier;
    }

    public void stop() {
        playing = false;
        sprite.setFill(Paint.valueOf("#BFBFBF"));
        sprite.setStrokeWidth(0);
        System.out.println("Stopped " + String.valueOf(playerNumber));
    }

    public void target(long timeKill) {
        targeted = true;
        this.timeKill = timeKill;
        sprite.setFill(Paint.valueOf("#FF4040"));
        sprite.setStrokeWidth(0);
    }

    public void kill() {
        alive = false;
        playing = false;
        xSpeed = 0.0;
        ySpeed = 0.0;
        sprite.setFill(Paint.valueOf("#FFB6B6"));
        sprite.setStrokeWidth(0);
        System.out.println("Killed " + String.valueOf(playerNumber));
    }
}