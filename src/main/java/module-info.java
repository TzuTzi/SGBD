module org.example.sgbd_lab1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens org.example.sgbd_lab1 to javafx.fxml;
    exports org.example.sgbd_lab1;
}