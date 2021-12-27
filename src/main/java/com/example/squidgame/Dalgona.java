package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

import java.io.IOException;

public class Dalgona extends Game {
    private String[] files = new String[] {
            "dalgona_circle.png", "dalgona_triangle.png", "dalgona_star.png", "dalgona_umbrella.png"
    };
    private Image image;
    private static final int IMAGE_SIZE = (int) (Entity.Y_MAX + Entity.Y_MIN);
    private static final int DRAW_SIZE = 5;
    private final Canvas canvas;
    private final GraphicsContext gc;

    private final Main app;
    private VBox root;
    private final Scene scene;
    private final Game2Controller controller;

    private Player player;

    Dalgona(Main app) {
        this.app = app;
        NAME = "Dalgona";
        TIME_LIMIT = (long) (2 * 60 * 1e9);
        player = app.getHumanPlayer();

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
        gc.setFill(Paint.valueOf(Colors.BLACK));
        controller.pane.setMaxWidth(IMAGE_SIZE);
        controller.pane.setMaxHeight(IMAGE_SIZE);
        controller.buttonFinish.setFocusTraversable(false);
        controller.buttonFinish.setOnAction(event -> {
            checkPassed();
        });

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

    public void checkPassed() {
        boolean passed = false;
        if (passed) {
            stop();
        }
        else {
            player.kill();
        }
    }

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

        player.move();
        if (player.isCutting()) {
            gc.fillOval(
                    player.getXLocation() - DRAW_SIZE/2,
                    player.getYLocation() - DRAW_SIZE/2,
                    DRAW_SIZE,
                    DRAW_SIZE
            );
        }
    }

    @Override
    public void start() {
        super.start();
        image = new Image(
                getClass().getResource(files[random.nextInt(files.length)]).toExternalForm(),
                IMAGE_SIZE, IMAGE_SIZE, true, true
        );
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void stop() {
        super.stop();
        app.setScenePlayerboard();
    }
}
