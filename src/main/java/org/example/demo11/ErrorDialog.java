package org.example.demo11;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Dialog reutilizabil pentru afisarea erorilor (ex: probleme de DB / JDBC).
 */
public final class ErrorDialog {
    private ErrorDialog() {}

    public static void show(Window owner, String title, String header, String message, Throwable cause) {
        Runnable showAction = () -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                if (owner != null) {
                    alert.initOwner(owner);
                }
                alert.initModality(Modality.APPLICATION_MODAL);

                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(message);

                if (cause != null) {
                    StringWriter sw = new StringWriter();
                    cause.printStackTrace(new PrintWriter(sw));

                    TextArea details = new TextArea(sw.toString());
                    details.setEditable(false);
                    details.setWrapText(true);
                    details.setPrefRowCount(10);

                    alert.getDialogPane().setExpandableContent(details);
                }

                alert.showAndWait();
            } catch (Exception dialogException) {
                // Daca dialogul nu poate fi afisat, macar sa avem feedback in consola.
                dialogException.printStackTrace();
            }
        };

        if (Platform.isFxApplicationThread()) {
            showAction.run();
        } else {
            Platform.runLater(showAction);
        }
    }
}

