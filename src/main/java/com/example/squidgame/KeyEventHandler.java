package com.example.squidgame;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;

public class KeyEventHandler implements EventHandler<KeyEvent> {
    // Whether each key is currently pressed or not.
    private static final HashMap<KeyCode, Boolean> KEY_STATES = new HashMap<>();

    public KeyEventHandler() {
        KEY_STATES.put(KeyCode.ESCAPE, false);
        KEY_STATES.put(KeyCode.LEFT, false);
        KEY_STATES.put(KeyCode.RIGHT, false);
        KEY_STATES.put(KeyCode.UP, false);
        KEY_STATES.put(KeyCode.DOWN, false);
        KEY_STATES.put(KeyCode.C, false);
        KEY_STATES.put(KeyCode.L, false);
    }

    public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        if (!KEY_STATES.containsKey(code)) {
            return;
        }
        boolean pressed = KEY_STATES.get(code);
        Game game = Main.getGame();

        if (event.getEventType() == KeyEvent.KEY_PRESSED && !pressed) {
            switch (code) {
                case ESCAPE:
                    game.onEscapePress(); break;
                case LEFT:
                    game.onLeftPress(); break;
                case RIGHT:
                    game.onRightPress(); break;
                case UP:
                    game.onUpPress(); break;
                case DOWN:
                    game.onDownPress(); break;
                case SPACE:
                    game.onSpacePress(); break;
                case C:
                    game.onCPress(); break;
                case L:
                    game.onLPress(); break;
            }
            if (KEY_STATES.containsKey(code)) {
                KEY_STATES.replace(code, true);
            }
        }
        else if (event.getEventType() == KeyEvent.KEY_RELEASED && pressed) {
            switch (code) {
                case ESCAPE:
                    game.onEscapeRelease(); break;
                case LEFT:
                    game.onLeftRelease(); break;
                case RIGHT:
                    game.onRightRelease(); break;
                case UP:
                    game.onUpRelease(); break;
                case DOWN:
                    game.onDownRelease(); break;
                case SPACE:
                    game.onSpaceRelease(); break;
                case C:
                    game.onCRelease(); break;
                case L:
                    game.onLRelease(); break;
            }
            if (KEY_STATES.containsKey(code)) {
                KEY_STATES.replace(code, false);
            }
        }
    }
}
