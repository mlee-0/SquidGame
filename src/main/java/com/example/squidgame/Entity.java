package com.example.squidgame;

import javafx.scene.Node;

public abstract class Entity {
    protected double x;
    protected double y;
    protected double z;

    protected double xSpeed;
    protected double ySpeed;
    protected double zSpeed;
    protected boolean falling = false;
    // A number in [0, 1] used to interpolate the speed for this instance.
    protected double relativeSpeed = 1;

    protected int xDirection = 0;
    protected int yDirection = 0;
    protected int zDirection = 0;

//    protected long timeStartMoveX;
//    protected long timeStartMoveY;

    // Acceleration of gravity, in m/s per frame.
    protected static final double GRAVITY_ACCELERATION = 9.81 / 60;
    //    protected static final double ACCELERATION = 10.0;
    //    protected static final double FRICTION = 5.0;

    protected boolean alive = true;

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public int getXDirection() { return xDirection; }
    public int getYDirection() { return yDirection; }
    public void changeXDirection(int multiplier) { xDirection *= multiplier; }
    public void changeYDirection(int multiplier) { yDirection *= multiplier; }
    public boolean isMoving() { return xSpeed > 0.0 || ySpeed > 0.0; }
    public boolean isFalling() { return falling; }
    public void setFalling(boolean falling) { this.falling = falling; }

    public void keepInBounds() {
        Game game = Main.getGame();
        if (x < game.getXMin()) {
            x = game.getXMin();
        }
        else if (x > game.getXMax()) {
            x = game.getXMax();
        }
        if (y < game.getYMin()) {
            y = game.getYMin();
        }
        else if (y > game.getYMax()) {
            y = game.getYMax();
        }
        // Keep above minimum but do not restrict maximum, or else entity cannot come down.
        if (z < game.getZMin()) {
            z = game.getZMin();
        }
    }

    public void updateZSpeed() {
        // Stop moving if landed from a jump.
        if (!falling && z + zSpeed < 0) {
            zSpeed = 0;
            z = 0;
        }
        else {
            zSpeed = (falling || zSpeed != 0) ? zSpeed - GRAVITY_ACCELERATION : 0;
        }
    }

    public void move() {
        keepInBounds();
        double[] speedRange = Main.getGame().playerSpeedRange;
        double speed = speedRange[0] + (speedRange[1] - speedRange[0]) * relativeSpeed;
        xSpeed = xDirection != 0 ? speed : 0;
        ySpeed = yDirection != 0 ? speed : 0;
        updateZSpeed();
        x += xDirection * xSpeed;
        y += yDirection * ySpeed;
        z += zSpeed;
    }

    public void move(double xSpeed, double ySpeed) {
        keepInBounds();
        updateZSpeed();
        x += xSpeed;
        y += ySpeed;
        z += zSpeed;
    }

    public boolean isAlive() { return alive; }
    public abstract Node getSprite();
}
