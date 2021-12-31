package com.example.squidgame;

import javafx.scene.Node;

public abstract class Entity {
    protected double x;
    protected double y;
    protected double z = 0;

    protected static final double X_MIN = 10;
    protected static final double X_MAX = 1190;
    protected static final double Y_MIN = 10;
    protected static final double Y_MAX = 590;

    protected double xSpeed = 0.0;
    protected double ySpeed = 0.0;
    protected double zSpeed = 0.0;
    protected double maxSpeed;

    protected int xDirection = 0;
    protected int yDirection = 0;
    protected int zDirection = 0;

//    protected long timeStartMoveX;
//    protected long timeStartMoveY;

//    protected static final double GRAVITY_ACCELERATION = 9.81;
    //    protected static final double ACCELERATION = 10.0;
    //    protected static final double FRICTION = 5.0;

    protected boolean alive = true;

    public boolean isAlive() { return alive; }
    public boolean isMoving() { return xSpeed > 0.0 || ySpeed > 0.0; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = x; }

    public abstract Node getSprite();

    public void move() {
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
        ySpeed = yDirection != 0 ? maxSpeed : 0;
        x += xDirection * xSpeed;
        y += yDirection * ySpeed;
    }
}
