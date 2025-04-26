module com.tankbattle {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    
    requires java.desktop;
    
    requires transitive batik.all;
    requires com.google.gson;
    
    exports com.tankbattle;
    exports com.tankbattle.model.level;
    exports com.tankbattle.model.save;
    exports com.tankbattle.model.entity;
    exports com.tankbattle.model.enums;
    exports com.tankbattle.view;
    exports com.tankbattle.controller;
    exports com.tankbattle.model;

    opens com.tankbattle to javafx.fxml;
    opens com.tankbattle.view to javafx.fxml;
    opens com.tankbattle.controller to javafx.fxml;
    
    opens com.tankbattle.model.level to com.google.gson;
    opens com.tankbattle.model.save to com.google.gson;
}