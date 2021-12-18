package com.example.squidgame;

public class Entity {
    protected double x;
    protected double y;
//    protected double z = 0;

    protected static final double X_MIN = 10;
    protected static final double X_MAX = 990;
    protected static final double Y_MIN = 10;
    protected static final double Y_MAX = 590;

    protected double xSpeed = 0.0;
    protected double ySpeed = 0.0;
//    protected double zSpeed = 0.0;
    protected double maxSpeed;

    protected int xDirection = 0;
    protected int yDirection = 0;
//    protected int zDirection = 0;

//    protected long timeStartMoveX;
//    protected long timeStartMoveY;

//    protected static final double GRAVITY_ACCELERATION = 9.81;
    //    protected static final double ACCELERATION = 10.0;
    //    protected static final double FRICTION = 5.0;

    protected boolean alive = true;

    public boolean isAlive() { return alive; }
    public boolean isMoving() { return xSpeed > 0.0 || ySpeed > 0.0; }
    public double[] getLocation() { return new double[] {x, y}; }

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
        ySpeed = yDirection != 0 ? 0.25 : 0;
        x += xDirection * xSpeed;
        y += yDirection * ySpeed;
    }
}
