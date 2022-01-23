package com.example.squidgame;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;

public class ControllerGame4 {
    @FXML
    public Pane pane;
    @FXML
    public Label labelMarblesLeft;
    @FXML
    public Label labelMarblesRight;
    @FXML
    public Label labelGuess;
    @FXML
    public Pane paneLeft;
    @FXML
    public Pane paneRight;
    @FXML
    public Pane paneCenter;
    @FXML
    public Button buttonBetLeft;
    @FXML
    public Button buttonBetRight;
    @FXML
    public Spinner<Integer> spinnerLeft;
    @FXML
    public Spinner<Integer> spinnerRight;
    @FXML
    public Button buttonEven;
    @FXML
    public Button buttonOdd;
}