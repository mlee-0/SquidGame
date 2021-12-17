package com.example.squidgame;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Player extends Entity {
    private final int playerNumber;
    private boolean playing = true;
    private final boolean computer;

    private String name = "";
    private String occupation = "";
    private int age;

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

    public boolean isComputer() {
        return computer;
    }

    public void startMove(long now, int xDirection, int yDirection) {
        this.xDirection = xDirection;
        this.yDirection = yDirection;
        if (xDirection != 0) {
            timeStartMoveX = now;
        }
        if (yDirection != 0) {
            timeStartMoveY = now;
        }
    }

    public void stopMove() {
        xDirection = 0;
        yDirection = 0;
        xSpeed = 0.0;
        ySpeed = 0.0;
    }

    public void move(long now) {
        if (x < X_MIN) {
            xDirection = 0;
        }
        if (y < Y_MIN || y > Y_MAX) {
            yDirection = 0;
        }

        xSpeed = xDirection != 0 ? maxSpeed : 0;
        ySpeed = yDirection != 0 ? maxSpeed : 0;
        x += xDirection * xSpeed;
        y += yDirection * ySpeed;

        sprite.setCenterX(x);
        sprite.setCenterY(y);
    }

    public void stop() {
        playing = false;
        sprite.setFill(Paint.valueOf("#BFBFBF"));
        sprite.setStrokeWidth(0);
        System.out.println("Stopped " + String.valueOf(playerNumber));
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