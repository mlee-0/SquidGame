package com.example.squidgame;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;

import java.util.Random;

abstract public class Game extends AnimationTimer {
    protected String NAME;
    protected static final Random random = new Random();

    protected long TIME_LIMIT;
    protected long elapsed = 0;
    protected long now;
    protected long previous;

    abstract protected Scene getScene();
}
