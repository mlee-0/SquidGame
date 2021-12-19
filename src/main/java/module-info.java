module com.example.squidgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.squidgame to javafx.fxml;
    exports com.example.squidgame;
}