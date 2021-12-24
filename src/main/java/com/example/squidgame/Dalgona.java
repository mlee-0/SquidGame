package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class Dalgona extends Game {
    private String[] files = new String[] {
            "dalgona_circle.png", "dalgona_triangle.png", "dalgona_star.png", "dalgona_umbrella.png"
    };
    private Image image;
    private static final int IMAGE_SIZE = 500;
    private final Canvas canvas;
    private GraphicsContext gc;

    private final Main app;
    private VBox root;
    private final Scene scene;
    private final Game2Controller controller;

    Dalgona(Main app) {
        this.app = app;
        NAME = "Dalgona";
        TIME_LIMIT = (long) (2 * 60 * 1e9);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game2.fxml"));
        try {
            root = fxmlLoader.load();
        }
        catch (IOException e) {
            root = new VBox();
        }
        root.getChildren().add(0, app.getDashboard());

        controller = fxmlLoader.getController();
        canvas = controller.canvas;
        canvas.setWidth(IMAGE_SIZE);
        canvas.setHeight(IMAGE_SIZE);
        gc = canvas.getGraphicsContext2D();

        scene = new Scene(root, Entity.X_MAX + 20, Entity.Y_MAX + 100);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    app.getHumanPlayer().setMoveX(-1);
                    break;
                case RIGHT:
                    app.getHumanPlayer().setMoveX(+1);
                    break;
                case UP:
                    app.getHumanPlayer().setMoveY(-1);
                    break;
                case DOWN:
                    app.getHumanPlayer().setMoveY(+1);
                    break;
                case SPACE:
                    app.getHumanPlayer().setCutting(true);
                    break;
                case L:
                    app.getHumanPlayer().setLicking(true);
                    break;
            }
        });
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    stop();
                    System.out.printf("Quitting %s\n", NAME);
                    break;
                case LEFT:
                case RIGHT:
                    app.getHumanPlayer().setMoveX(0);
                    break;
                case UP:
                case DOWN:
                    app.getHumanPlayer().setMoveY(0);
                    break;
                case SPACE:
                    app.getHumanPlayer().setCutting(false);
                    break;
                case L:
                    app.getHumanPlayer().setLicking(false);
                    break;
            }
        });
    }

    public Scene getScene() { return scene; }
    public VBox getRoot() { return root; }
    public Pane getPane() { return controller.pane; }

    @Override
    public void handle(long now) {
        this.now = now;
        if (previous == 0) {
            previous = now;
            return;
        }
        elapsed += (now - previous);
        previous = now;
        app.updateTimer((TIME_LIMIT - elapsed) / 1e9);

        app.getHumanPlayer().move();
    }

    @Override
    public void start() {
        super.start();
        image = new Image(
                getClass().getResource(files[random.nextInt(files.length)]).toExternalForm(),
                500, 500, true, true
        );
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void stop() {
        super.stop();
        app.setScenePlayerboard();
    }
}
