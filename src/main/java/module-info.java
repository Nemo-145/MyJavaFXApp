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
    requires com.almasb.fxgl.all;

    opens com.example.myjavafxapp.controllers to javafx.fxml;
    opens com.example.myjavafxapp to javafx.fxml; // Открываем основной пакет
    exports com.example.myjavafxapp; // Экспортируем основной пакет
    exports com.example.myjavafxapp.models; // Экспортируем модельный пакет
}
