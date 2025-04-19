module com.tankbattle {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    
    requires java.desktop;
    
    requires transitive batik.all;
    
    exports com.tankbattle;
    exports com.tankbattle.model;
    exports com.tankbattle.controller;
    exports com.tankbattle.view;
    
    opens com.tankbattle to javafx.fxml;
    opens com.tankbattle.view to javafx.fxml;
    opens com.tankbattle.controller to javafx.fxml;
}