package com.example.squidgame;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Player {
    private final int playerNumber;
    private boolean alive = true;
    private boolean finished = false;
    private boolean computer = true;

    private double x;
    private double y;
    private double z = 0;
    private double xSpeed = 0.0;
    private double ySpeed = 0.0;
    private double zSpeed = 0.0;
    private int xDirection = 0;
    private int yDirection = 0;
    private int zDirection = 0;
    private long timeStartMoveX;
    private long timeStartMoveY;
    private final double maxSpeed;
//    private final double ACCELERATION = 10.0;
//    private final double FRICTION = 5.0;
    private final double GRAVITY_ACCELERATION = 9.81;

    private String name = "";
    private String occupation = "";
    private int age;

    private Circle sprite;

    Player(int playerNumber, double x, double y, double maxSpeed) {
        this.playerNumber = playerNumber;
        this.x = x;
        this.y = y;
        this.maxSpeed = maxSpeed;
        sprite = new Circle(x, y, 5);
        sprite.setFill(Paint.valueOf("#008099"));
        sprite.setStroke(Paint.valueOf("#00404C"));
        sprite.setStrokeWidth(1);
    }

    public Shape getSprite() {
        return sprite;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isFinished() { return finished; }

    public boolean isComputer() {
        return computer;
    }

    public double[] getLocation() {
        return new double[] {x, y};
    }

    public void toggleMove(long now, int xDirection, int yDirection) {
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
        xSpeed = xDirection != 0 ? maxSpeed : 0;
        ySpeed = yDirection != 0 ? maxSpeed : 0;
        x += xDirection * xSpeed;
        y += yDirection * ySpeed;
        sprite.setCenterX(x);
        sprite.setCenterY(y);
    }

    public boolean isMoving() {
        return xSpeed > 0.0 || ySpeed > 0.0;
    }

    public void kill() {
        alive = false;
        xSpeed = 0.0;
        ySpeed = 0.0;
        this.sprite.setFill(Paint.valueOf("#FFB6B6"));
        this.sprite.setStrokeWidth(0);
    }
}