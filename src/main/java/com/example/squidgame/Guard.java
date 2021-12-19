package com.example.squidgame;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Guard extends Entity {
    private enum Rank { CIRCLE, TRIANGLE, SQUARE }
    private Rank rank;

    private final int SIZE = 18;
    private Circle sprite;

    Guard(double x, double y) {
        sprite = new Circle(x, y, SIZE/2);
        sprite.setFill(Paint.valueOf(Colors.BLACK));
        sprite.setStroke(Paint.valueOf(Colors.PINK));
        sprite.setStrokeWidth(3);
        sprite.setFill(new ImagePattern(
                new Image(getClass().getResourceAsStream("guard_square.png")), 0, 0, 1, 1, true
        ));
    }

    public void move() {
        super.move();
        sprite.setCenterX(x);
        sprite.setCenterY(y);
    }

    public Circle getSprite() { return sprite; }
}
