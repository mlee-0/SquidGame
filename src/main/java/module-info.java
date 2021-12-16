module com.example.squidgame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.squidgame to javafx.fxml;
    exports com.example.squidgame;
}