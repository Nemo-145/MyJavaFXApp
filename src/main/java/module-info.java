module com.example.myjavafxapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jasperreports;
    requires org.slf4j;

    // Экспорты
    exports com.example.myjavafxapp.controllers;
    exports com.example.myjavafxapp;
    exports com.example.myjavafxapp.models;

    // Открытие для FXML
    opens com.example.myjavafxapp.controllers to javafx.fxml;
    opens com.example.myjavafxapp to javafx.fxml;
    exports com.example.myjavafxapp.utils;
    opens com.example.myjavafxapp.utils to javafx.fxml;
}
