package com.example.squidgame;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class Game1Controller {
    @FXML
    public Pane pane;
    @FXML
    public Rectangle finishLine;

    @FXML
    public Label labelTimer;
    @FXML
    public Label labelPlayerNumber;
    @FXML
    public Label labelPrize;
}
