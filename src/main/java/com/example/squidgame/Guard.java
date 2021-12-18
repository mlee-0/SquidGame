package com.example.squidgame;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Guard extends Entity {
    private enum Rank { CIRCLE, TRIANGLE, SQUARE }
    private Rank rank;

    private Circle sprite;

    Guard(double x, double y) {
        sprite = new Circle(x, y, 9);
        sprite.setFill(Paint.valueOf(Colors.BLACK));
        sprite.setStroke(Paint.valueOf(Colors.PINK));
        sprite.setStrokeWidth(3);
    }

    public void move() {
        super.move();
        sprite.setCenterX(x);
        sprite.setCenterY(y);
    }

    public Shape getSprite() { return sprite; }
}
