package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Dalgona extends Game {
    private final String[] files = new String[] {
            "dalgona_circle.png", "dalgona_triangle.png", "dalgona_star.png", "dalgona_umbrella.png"
    };
    private Image image;
    private static final int IMAGE_SIZE = 500;
    private static final int DRAW_SIZE = 5;
    private final Canvas canvas;
    private final GraphicsContext gc;

    private final Game2Controller controller;

    Dalgona() {
        NAME = "Dalgona";
        TIME_LIMIT = (long) (2 * 60 * 1e9);
        startingPosition = new double[] {IMAGE_SIZE/2.0, IMAGE_SIZE/2.0};
        playerSpeedRange = new double[] {0.25, 0.25};

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
            Player human = Main.getApp().getHumanPlayer();
            switch (event.getCode()) {
                case LEFT:
                    human.setMoveX(-1);
                    break;
                case RIGHT:
                    human.setMoveX(+1);
                    break;
                case UP:
                    human.setMoveY(-1);
                    break;
                case DOWN:
                    human.setMoveY(+1);
                    break;
                case SPACE:
                    human.setCutting(true);
                    break;
                case L:
                    human.setLicking(true);
                    break;
            }
        });
        scene.setOnKeyReleased(event -> {
            Player human = app.getHumanPlayer();
            switch (event.getCode()) {
                case ESCAPE:
                    stop();
                    System.out.printf("Quitting %s\n", NAME);
                    break;
                case LEFT:
                case RIGHT:
                    human.setMoveX(0);
                    break;
                case UP:
                case DOWN:
                    human.setMoveY(0);
                    break;
                case SPACE:
                    human.setCutting(false);
                    break;
                case L:
                    human.setLicking(false);
                    break;
            }
        });
    }

    public Scene getScene() { return scene; }
    public Pane getPane() { return controller.pane; }

    public void checkPassed() {
        if (!app.getHumanPlayer().isAlive() && !app.getHumanPlayer().isPlaying()) {
            return;
        }

        // Get the canvas image.
        WritableImage writableImage = new WritableImage(IMAGE_SIZE, IMAGE_SIZE);
        canvas.snapshot(null, writableImage);
        PixelReader pixelReaderCanvas = writableImage.getPixelReader();
        // Get the original image.
        PixelReader pixelReaderOriginal = image.getPixelReader();

        double numberMatches = 0;
        double numberChecked = 0;
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
        double matchPercent = numberMatches / numberChecked;
        System.out.printf("%.1f%% matching\n", matchPercent * 100);

        boolean passed = matchPercent >= 0.6;
        Player human = app.getHumanPlayer();
        if (passed) {
            human.stop();
        }
        else {
            human.kill();
        }
    }

    @Override
    public void handle(long now) {
        super.handle(now);

        Player human = app.getHumanPlayer();
        human.move();
        if (human.isCutting()) {
            gc.fillOval(
                    human.getX() - DRAW_SIZE/2.0,
                    human.getY() - DRAW_SIZE/2.0,
                    DRAW_SIZE,
                    DRAW_SIZE
            );
        }

        if (random.nextFloat() < 0.01 * elapsed/TIME_LIMIT) {
            Player[] playingPlayers = app.getPlayingPlayers();
            int index = random.nextInt(playingPlayers.length);
            if (playingPlayers[index].isComputer()) {
                playingPlayers[index].kill();
            }
        }
    }

    @Override
    protected void handlePlayer(Player player) {
        super.handlePlayer(player);

        if (elapsed >= TIME_LIMIT) {
            if (player.isComputer()) {
                player.stop();
            }
            else {
                player.kill();
            }
        }
    }

    @Override
    public void start() {
        super.start();
        controller.pane.getChildren().add(app.getHumanPlayer().getSprite());
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
