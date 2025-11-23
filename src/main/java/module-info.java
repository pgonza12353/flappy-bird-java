module com.example.flappybird {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens com.example.flappybird to javafx.fxml;
    exports com.example.flappybird;
}