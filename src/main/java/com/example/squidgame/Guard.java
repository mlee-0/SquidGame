package com.example.squidgame;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Guard extends Entity {
    public enum Rank { CIRCLE, TRIANGLE, SQUARE }
    private Rank rank;

    private final int SIZE = 18;
    private Circle sprite;

    Guard(double x, double y, Rank rank) {
        this.rank = rank;
        String filename = "";
        switch (this.rank) {
            case CIRCLE:
                filename = "guard_circle.png"; break;
            case TRIANGLE:
                filename = "guard_triangle.png"; break;
            case SQUARE:
                filename = "guard_square.png"; break;
        }

        sprite = new Circle(x, y, SIZE/2);
        sprite.setFill(Paint.valueOf(Colors.BLACK));
        sprite.setStroke(Paint.valueOf(Colors.PINK));
        sprite.setStrokeWidth(3);
        Image image = new Image(getClass().getResourceAsStream(filename), SIZE, SIZE, false, true);
        sprite.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
    }

    public void move() {
        super.move();
        sprite.setCenterX(x);
        sprite.setCenterY(y);
    }

    public Circle getSprite() { return sprite; }
}
