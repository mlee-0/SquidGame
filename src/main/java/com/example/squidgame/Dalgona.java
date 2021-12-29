package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.IOException;

public class Dalgona extends Game {
    private String[] files = new String[] {
            "dalgona_circle.png", "dalgona_triangle.png", "dalgona_star.png", "dalgona_umbrella.png"
    };
    private Image image;
    private static final int IMAGE_SIZE = 500;
    private static final int DRAW_SIZE = 5;
    private final Canvas canvas;
    private final GraphicsContext gc;

    private final Game2Controller controller;

    private Player player;

    Dalgona() {
        NAME = "Dalgona";
        TIME_LIMIT = (long) (2 * 60 * 1e9);
        player = app.getHumanPlayer();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game2.fxml"));
        setRoot(fxmlLoader);

        controller = fxmlLoader.getController();
        canvas = controller.canvas;
        canvas.setWidth(IMAGE_SIZE);
        canvas.setHeight(IMAGE_SIZE);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Paint.valueOf(Colors.BLACK));
        controller.circle.setRadius(IMAGE_SIZE / 2.0);
        controller.circle.setCenterX(IMAGE_SIZE / 2.0);
        controller.circle.setCenterY(IMAGE_SIZE / 2.0);
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
        // Get the canvas image.
        WritableImage writableImage = new WritableImage(IMAGE_SIZE, IMAGE_SIZE);
        canvas.snapshot(null, writableImage);
        PixelReader pixelReaderCanvas = writableImage.getPixelReader();
        // Get the original image.
        PixelReader pixelReaderOriginal = image.getPixelReader();

        int numberMatches = 0;
        int numberChecked = 0;
        for (int row = 0; row < IMAGE_SIZE; row++) {
            for (int column = 0; column < IMAGE_SIZE; column++) {
                Color colorCanvas = pixelReaderCanvas.getColor(row, column);
                Color colorOriginal = pixelReaderOriginal.getColor(row, column);
                boolean isOutline = colorOriginal.getOpacity() > 0;
                boolean isDrawn = colorCanvas.getBrightness() < 1;
                if (isOutline || isDrawn) {
                    numberChecked += 1;
                    if (isOutline && isDrawn) {
                        numberMatches += 1;
                    }
                }
            }
        }
        double matchPercent = (double)numberMatches / (double)numberChecked;
        System.out.printf("%.1f%% matching\n", matchPercent * 100);

        boolean passed = matchPercent >= 0.6;
        if (passed) {
            stop();
        }
        else {
            player.kill();
            app.eliminatePlayers(1);
        }
    }

    @Override
    public void handle(long now) {
        super.handle(now);

        player.move();
        if (player.isCutting()) {
            gc.fillOval(
                    player.getXLocation() - DRAW_SIZE/2,
                    player.getYLocation() - DRAW_SIZE/2,
                    DRAW_SIZE,
                    DRAW_SIZE
            );
        }

        if ((TIME_LIMIT - elapsed) < 600e9) {
            if (random.nextFloat() < 0.005) {
                Player[] playingPlayers = app.getPlayingPlayers();
                int index = random.nextInt(playingPlayers.length);
                if (playingPlayers[index].isComputer()) {
                    playingPlayers[index].kill();
                    app.eliminatePlayers(1);
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();
        image = new Image(
                getClass().getResource(files[random.nextInt(files.length)]).toExternalForm(),
                IMAGE_SIZE, IMAGE_SIZE, true, true
        );
        controller.imageView.setImage(image);
    }

    @Override
    public void stop() {
        super.stop();
        app.setScenePlayerboard();
    }
}
