package com.example.squidgame;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Doll extends Entity {
    private final int SIZE = 100;
    private ImageView sprite;
    private Image imageGreen = new Image(getClass().getResourceAsStream("doll_green.png"), SIZE, SIZE, false, true);
    private Image imageRed = new Image(getClass().getResourceAsStream("doll_red.png"), SIZE, SIZE, false, true);

    Doll(double x, double y) {
        sprite = new ImageView(imageRed);
        sprite.setFitHeight(SIZE);
        sprite.setFitWidth(SIZE);
        sprite.relocate(-SIZE/2, -SIZE/2);
        sprite.setX(x);
        sprite.setY(y);
    }

    public ImageView getSprite() { return sprite; }

    public void setState(RedLightGreenLight.State state) {
        switch (state) {
            case GREEN:
                sprite.setImage(imageGreen); break;
            case TURNING:
                sprite.setImage(imageRed); break;
        }
    }
}
