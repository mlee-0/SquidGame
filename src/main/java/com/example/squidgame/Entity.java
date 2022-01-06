package com.example.squidgame;

import javafx.scene.Node;

public abstract class Entity {
    protected double x;
    protected double y;
    protected double z = 0;

    protected double xSpeed = 0.0;
    protected double ySpeed = 0.0;
    protected double zSpeed = 0.0;
    // A number in [0, 1] used to interpolate the speed for this instance.
    protected double relativeSpeed = 1;

    protected int xDirection = 0;
    protected int yDirection = 0;
    protected int zDirection = 0;

//    protected long timeStartMoveX;
//    protected long timeStartMoveY;

//    protected static final double GRAVITY_ACCELERATION = 9.81;
    //    protected static final double ACCELERATION = 10.0;
    //    protected static final double FRICTION = 5.0;

    protected boolean alive = true;

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public boolean isMoving() { return xSpeed > 0.0 || ySpeed > 0.0; }

    public void move() {
        // Keep in bounds.
        if (x < Main.getGame().getXMin()) {
            x = Main.getGame().getXMin();
        }
        else if (x > Main.getGame().getXMax()) {
            x = Main.getGame().getXMax();
        }
        if (y < Main.getGame().getYMin()) {
            y = Main.getGame().getYMin();
        }
        else if (y > Main.getGame().getYMax()) {
            y = Main.getGame().getYMax();
        }

        double[] speedRange = Main.getGame().playerSpeedRange;
        double speed = speedRange[0] + (speedRange[1] - speedRange[0]) * relativeSpeed;
        xSpeed = xDirection != 0 ? speed : 0;
        ySpeed = yDirection != 0 ? speed : 0;
        x += xDirection * xSpeed;
        y += yDirection * ySpeed;
    }

    public boolean isAlive() { return alive; }
    public abstract Node getSprite();
}
