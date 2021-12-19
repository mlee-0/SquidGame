package com.example.squidgame;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Doll extends Entity {
    private final int SIZE = 50;
    private ImageView sprite;

    Doll(double x, double y) {
        sprite = new ImageView(new Image(getClass().getResourceAsStream("doll.png")));
        sprite.setFitHeight(SIZE);
        sprite.setFitWidth(SIZE);
        sprite.relocate(-SIZE/2, -SIZE/2);
        sprite.setX(x);
        sprite.setY(y);
    }

    public ImageView getSprite() { return sprite; }
}
