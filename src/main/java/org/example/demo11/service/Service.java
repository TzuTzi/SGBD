package org.example.demo11.service;

import org.example.demo11.DAO.EpisodeDAO;
import org.example.demo11.DAO.ShowDAO;
import org.example.demo11.model.Episode;
import org.example.demo11.model.Show;

public class Service {
    private final ShowDAO showDAO = ShowDAO.getInstance();
    private final EpisodeDAO episodeDAO = EpisodeDAO.getInstance();

    public Iterable<Show> getAllShows() {
        return showDAO.getAllShows();
    }

    public void addShow(Show show) {
        showDAO.addShow(show);
    }

    public void updateShow(Show show) {
        showDAO.updateShow(show);
    }

    public void removeShow(Integer id) {
        showDAO.removeShow(id);
    }

    public Iterable<Episode> getEpisodesByShowId(Integer showId) {
        return episodeDAO.getEpisodesByShowId(showId);
    }

    public void addEpisode(Episode episode) {
        episodeDAO.addEpisode(episode);
    }

    public void updateEpisode(Episode episode) {
        episodeDAO.updateEpisode(episode);
    }

    public void removeEpisode(Integer id) {
        episodeDAO.removeEpisode(id);
    }
}
