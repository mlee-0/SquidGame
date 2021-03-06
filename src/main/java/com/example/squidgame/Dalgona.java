package com.example.squidgame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Dalgona extends Game {
    private final String[] files = new String[] {
            "dalgona_circle.png", "dalgona_triangle.png", "dalgona_star.png", "dalgona_umbrella.png"
    };
    private Image image;
    private PixelReader imagePixelReader;
    private static final int IMAGE_SIZE = 500;
    private static final int DRAW_SIZE = 5;
    private static final int LICK_SIZE = 50;
    private final Canvas canvas;
    private final GraphicsContext gc;

    private final MediaPlayer music = new MediaPlayer(new Media(getClass().getResource("game_2.mp3").toExternalForm()));

    private final ControllerGame2 controller;

    Dalgona() {
        NAME = "Dalgona";
        TIME_LIMIT = (long) (2 * 60 * 1e9);
        X_MIN = 10;
        X_MAX = IMAGE_SIZE - 10;
        Y_MIN = 10;
        Y_MAX = IMAGE_SIZE - 10;
        startingPosition = new double[] {IMAGE_SIZE/2.0, IMAGE_SIZE/2.0};
        playerSpeedRange = new double[] {0.2, 0.2};

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game2.fxml"));
        setRoot(fxmlLoader);

        controller = fxmlLoader.getController();
        canvas = controller.canvas;
        canvas.setWidth(IMAGE_SIZE);
        canvas.setHeight(IMAGE_SIZE);
        gc = canvas.getGraphicsContext2D();
        gc.setGlobalBlendMode(BlendMode.DARKEN);
        controller.circle.setFill(Colors.DALGONA);
        controller.circle.setRadius(IMAGE_SIZE / 2.0);
        controller.circle.setCenterX(IMAGE_SIZE / 2.0);
        controller.circle.setCenterY(IMAGE_SIZE / 2.0);
        controller.pane.setMaxWidth(IMAGE_SIZE);
        controller.pane.setMaxHeight(IMAGE_SIZE);
        controller.buttonFinish.setFocusTraversable(false);
        controller.buttonFinish.setOnAction(event -> {
            checkPassed();
        });

        scene = new Scene(root, X_MAX + 20, Y_MAX + 100);
        KeyEventHandler handler = new KeyEventHandler();
        scene.setOnKeyPressed(handler);
        scene.setOnKeyReleased(handler);

        music.setCycleCount(MediaPlayer.INDEFINITE);
        music.setVolume(Main.getApp().getVolumeMusic());
    }

    public Scene getScene() { return scene; }
    public Pane getPane() { return controller.pane; }

    public void checkPassed() {
        if (!human.isAlive() && !human.isPlaying()) {
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
                // Whether this pixel should be drawn on.
                boolean isTarget = colorOriginal.getOpacity() > 0;
                // Whether this pixel is drawn on.
                boolean isDrawn = colorCanvas.getBrightness() == 0;
                if (isTarget || isDrawn) {
                    numberChecked += 1;
                    if (isTarget && isDrawn) {
                        numberMatches += 1;
                    }
                }
            }
        }
        double matchPercent = numberMatches / numberChecked;
        System.out.printf("%.1f%% matching\n", matchPercent * 100);

        boolean passed = matchPercent >= 0.6;
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

        human.move();
        if (human.isCutting()) {
            int x = (int) human.getX();
            int y = (int) human.getY();
            WritableImage writableImage = new WritableImage(IMAGE_SIZE, IMAGE_SIZE);
            canvas.snapshot(null, writableImage);

            // Check if any pixel within a specified distance is licked.
            boolean lickedNearby = false;
            loop:
            for (int distance = 1; distance < LICK_SIZE / 8; distance++) {
                for (int row: new int[] {-distance, distance}) {
                    for (int column: new int[] {-distance, distance}) {
                        double saturation = writableImage.getPixelReader().getColor(x+column, y+row).getSaturation();
                        if (saturation > 0.0) {
                            lickedNearby = true;
                            break loop;
                        }
                    }
                }
            }
            // Human is cutting a licked area.
            if (lickedNearby) {
                // The distance in all directions from the player to be searched for a cutting location.
                int searchDistance = LICK_SIZE / 4;
                // The smallest distance found.
                double minDistance = IMAGE_SIZE;
                // The row and column at which the smallest distance was found.
                int rowCut = -1;
                int columnCut = -1;
                for (int column = x - searchDistance; column < x + searchDistance; column++) {
                    for (int row = y - searchDistance; row < y + searchDistance; row++) {
                        if (imagePixelReader.getColor(column, row).getOpacity() > 0) {
                            double distance = Math.sqrt(Math.pow(column - x, 2) + Math.pow(row - y, 2));
                            if (distance < minDistance) {
                                minDistance = distance;
                                rowCut = row;
                                columnCut = column;
                            }
                        }
                    }
                }
                // Cut on the line at the closest pixel found, if any.
                if (rowCut > 0 && columnCut > 0) {
                    gc.fillOval(columnCut - DRAW_SIZE/2.0, rowCut - DRAW_SIZE/2.0, DRAW_SIZE, DRAW_SIZE);
                }
            }
            // Human is cutting a dry pixel.
            else {
                gc.fillOval(human.getX() - DRAW_SIZE/2.0, human.getY() - DRAW_SIZE/2.0, DRAW_SIZE, DRAW_SIZE);
            }
        }
        else if (human.isLicking()) {
            gc.fillOval(human.getX() - LICK_SIZE/2.0, human.getY() - LICK_SIZE/2.0, LICK_SIZE, LICK_SIZE);
        }

        if (random.nextFloat() < 0.01 * elapsed/TIME_LIMIT) {
            Player[] playingPlayers = app.getPlayingPlayers(true);
            playingPlayers[random.nextInt(playingPlayers.length)].kill();
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
        controller.pane.getChildren().add(human.getSprite());
        image = new Image(
                getClass().getResource(files[random.nextInt(files.length)]).toExternalForm(),
                IMAGE_SIZE, IMAGE_SIZE, true, true
        );
        imagePixelReader = image.getPixelReader();
        controller.imageView.setImage(image);
        music.play();
    }

    @Override
    public void stop() {
        super.stop();
        music.stop();
        app.setScenePlayerboard();
    }

    protected void onCPress() {
        gc.setFill(Paint.valueOf(Colors.BLACK));
        gc.setGlobalAlpha(1.0);
        human.setCutting(true);
        human.setLicking(false);
    }
    protected void onCRelease() { human.setCutting(false);}
    protected void onLPress() {
        gc.setFill(Colors.DALGONA_DARK);
        gc.setGlobalAlpha(0.01);
        human.setLicking(true);
        human.setCutting(false);
    }
    protected void onLRelease() { human.setLicking(false); }
}