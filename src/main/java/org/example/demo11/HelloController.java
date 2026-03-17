package org.example.demo11;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    public void initialize() {
        showIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        showTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        episodeIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        episodeTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        showTable.setItems(showData);
        episodeTable.setItems(episodeData);

        showTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> onShowSelected(newSel)
        );

        loadShows();
    }

    private void loadShows() {
        showData.clear();
        for (Show s : service.getAllShows()) {
            showData.add(s);
        }
        episodeData.clear();
        statusLabel.setText("Loaded shows");
    }

    private void onShowSelected(Show show) {
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
    }

    @FXML
    private void onAddShow() {
        String title = showTitleField.getText();
        if (title == null || title.isBlank()) {
            statusLabel.setText("Show title is required");
            return;
        }
        Show show = new Show(title);
        service.addShow(show);
        loadShows();
        showTable.getSelectionModel().select(show);
        statusLabel.setText("Show added");
    }

    @FXML
    private void onUpdateShow() {
        Show selected = showTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a show to update");
            return;
        }
        String title = showTitleField.getText();
        if (title == null || title.isBlank()) {
            statusLabel.setText("Show title is required");
            return;
        }
        selected.setTitle(title);
        service.updateShow(selected);
        loadShows();
        statusLabel.setText("Show updated");
    }

    @FXML
    private void onDeleteShow() {
        Show selected = showTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a show to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete show \"" + selected.getTitle() + "\" and all its episodes?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.removeShow(selected.getId());
                loadShows();
                statusLabel.setText("Show deleted");
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
        if (title == null || title.isBlank()) {
            statusLabel.setText("Episode title is required");
            return;
        }
        Episode episode = new Episode(title);
        episode.setShowId(parent.getId());
        service.addEpisode(episode);
        onShowSelected(parent);
        episodeTable.getSelectionModel().select(episode);
        statusLabel.setText("Episode added");
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
        if (title == null || title.isBlank()) {
            statusLabel.setText("Episode title is required");
            return;
        }
        selected.setTitle(title);
        selected.setShowId(parent.getId());
        service.updateEpisode(selected);
        onShowSelected(parent);
        statusLabel.setText("Episode updated");
    }

    @FXML
    private void onDeleteEpisode() {
        Episode selected = episodeTable.getSelectionModel().getSelectedItem();
        Show parent = showTable.getSelectionModel().getSelectedItem();
        if (selected == null || parent == null) {
            statusLabel.setText("Select an episode to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete episode \"" + selected.getTitle() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.removeEpisode(selected.getId());
                onShowSelected(parent);
                statusLabel.setText("Episode deleted");
            }
        });
    }

    @FXML
    private void onRefresh() {
        loadShows();
        statusLabel.setText("Refreshed from database");
    }
}
