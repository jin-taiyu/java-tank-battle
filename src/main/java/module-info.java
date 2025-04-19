module com.tankbattle {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    
    exports com.tankbattle;
    opens com.tankbattle to javafx.fxml;
}