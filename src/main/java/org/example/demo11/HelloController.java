package org.example.demo11;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;
import org.example.demo11.model.Episode;
import org.example.demo11.model.Show;
import org.example.demo11.service.Service;

public class HelloController {
    @FXML
    private TableView<Show> showTable;
    @FXML
    private TableColumn<Show, Number> showIdColumn;
    @FXML
    private TableColumn<Show, String> showTitleColumn;

    @FXML
    private TableView<Episode> episodeTable;
    @FXML
    private TableColumn<Episode, Number> episodeIdColumn;
    @FXML
    private TableColumn<Episode, String> episodeTitleColumn;

    @FXML
    private TextField showTitleField;
    @FXML
    private TextField episodeTitleField;
    @FXML
    private Label statusLabel;

    private final Service service = new Service();
    private final ObservableList<Show> showData = FXCollections.observableArrayList();
    private final ObservableList<Episode> episodeData = FXCollections.observableArrayList();

    // Afiseaza un mesaj de eroare "prietenoas" cand apar probleme la accesul DB.
    private void showError(String context, Throwable e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = e.toString();
        }

        statusLabel.setText("Error: " + context);

        Window owner = null;
        if (statusLabel != null && statusLabel.getScene() != null) {
            owner = statusLabel.getScene().getWindow();
        }
        ErrorDialog.show(owner, "Eroare", context, message, e);
    }

    @FXML
    public void initialize() {
        // Seteaza valorile pentru coloane (ID si nume/titlu) din obiectele model.
        showIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        showTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        episodeIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        episodeTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        showTable.setItems(showData);
        episodeTable.setItems(episodeData);

        // Listener pe selectie: cand schimbi show-ul din tabelul parinte,
        // se reincarca automat episoadele (tabelul copil).
        showTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> onShowSelected(newSel)
        );

        try {
            loadShows();
        } catch (RuntimeException e) {
            showError("Failed to load shows", e);
        }
    }

    private void loadShows() {
        try {
            // Incarca toti parintii (shows) si golește episoadele pana selectezi alt show.
            showData.clear();
            for (Show s : service.getAllShows()) {
                showData.add(s);
            }
            episodeData.clear();
            statusLabel.setText("Loaded shows");
        } catch (RuntimeException e) {
            throw e; // handled by callers (initialize/onRefresh/etc.)
        }
    }

    private void onShowSelected(Show show) {
        try {
            // Reincarca episoadele pentru show-ul curent selectat.
            episodeData.clear();
            if (show != null) {
                showTitleField.setText(show.getTitle());
                for (Episode e : service.getEpisodesByShowId(show.getId())) {
                    episodeData.add(e);
                }
                statusLabel.setText("Loaded episodes for show: " + show.getTitle());
            } else {
                showTitleField.clear();
                episodeTitleField.clear();
            }
        } catch (RuntimeException e) {
            showError("Failed to load episodes", e);
        }
    }

    @FXML
    private void onAddShow() {
        String title = showTitleField.getText();
        // Validare simpla: titlul este obligatoriu.
        if (title == null || title.isBlank()) {
            statusLabel.setText("Show title is required");
            return;
        }

        try {
            // Creeaza un nou parinte si il adauga in baza de date.
            Show show = new Show(title);
            service.addShow(show);
            loadShows();
            showTable.getSelectionModel().select(show);
            statusLabel.setText("Show added");
        } catch (RuntimeException e) {
            showError("Failed to add show", e);
        }
    }

    @FXML
    private void onUpdateShow() {
        Show selected = showTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a show to update");
            return;
        }
        String title = showTitleField.getText();
        // Validare: titlul trebuie completat.
        if (title == null || title.isBlank()) {
            statusLabel.setText("Show title is required");
            return;
        }
        try {
            // Update pe parintele selectat.
            selected.setTitle(title);
            service.updateShow(selected);
            loadShows();
            statusLabel.setText("Show updated");
        } catch (RuntimeException e) {
            showError("Failed to update show", e);
        }
    }

    @FXML
    private void onDeleteShow() {
        Show selected = showTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a show to delete");
            return;
        }
        // Confirmare inainte de stergere (evita stergerile accidentale).
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete show \"" + selected.getTitle() + "\" and all its episodes?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.removeShow(selected.getId());
                    loadShows();
                    statusLabel.setText("Show deleted");
                } catch (RuntimeException e) {
                    showError("Failed to delete show", e);
                }
            }
        });
    }

    @FXML
    private void onAddEpisode() {
        Show parent = showTable.getSelectionModel().getSelectedItem();
        if (parent == null) {
            statusLabel.setText("Select a show first");
            return;
        }
        String title = episodeTitleField.getText();
        // Validare: titlul episodului este obligatoriu.
        if (title == null || title.isBlank()) {
            statusLabel.setText("Episode title is required");
            return;
        }
        try {
            // Adauga copilul pentru parintele selectat.
            Episode episode = new Episode(title);
            episode.setShowId(parent.getId());
            service.addEpisode(episode);
            onShowSelected(parent);
            episodeTable.getSelectionModel().select(episode);
            statusLabel.setText("Episode added");
        } catch (RuntimeException e) {
            showError("Failed to add episode", e);
        }
    }

    @FXML
    private void onUpdateEpisode() {
        Episode selected = episodeTable.getSelectionModel().getSelectedItem();
        Show parent = showTable.getSelectionModel().getSelectedItem();
        if (selected == null || parent == null) {
            statusLabel.setText("Select an episode to update");
            return;
        }
        String title = episodeTitleField.getText();
        // Validare: titlul episodului trebuie completat.
        if (title == null || title.isBlank()) {
            statusLabel.setText("Episode title is required");
            return;
        }
        try {
            // Update pentru episodul selectat (mentine showId-ul din parintele curent).
            selected.setTitle(title);
            selected.setShowId(parent.getId());
            service.updateEpisode(selected);
            onShowSelected(parent);
            statusLabel.setText("Episode updated");
        } catch (RuntimeException e) {
            showError("Failed to update episode", e);
        }
    }

    @FXML
    private void onDeleteEpisode() {
        Episode selected = episodeTable.getSelectionModel().getSelectedItem();
        Show parent = showTable.getSelectionModel().getSelectedItem();
        if (selected == null || parent == null) {
            statusLabel.setText("Select an episode to delete");
            return;
        }
        // Confirmare inainte de stergerea copilului.
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete episode \"" + selected.getTitle() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.removeEpisode(selected.getId());
                    onShowSelected(parent);
                    statusLabel.setText("Episode deleted");
                } catch (RuntimeException e) {
                    showError("Failed to delete episode", e);
                }
            }
        });
    }

    @FXML
    private void onRefresh() {
        try {
            // Reincarca datele din baza de date.
            loadShows();
            statusLabel.setText("Refreshed from database");
        } catch (RuntimeException e) {
            showError("Failed to refresh data", e);
        }
    }
}
