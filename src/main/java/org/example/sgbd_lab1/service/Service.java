package org.example.sgbd_lab1.service;

import org.example.sgbd_lab1.DAO.ActorDAO;
import org.example.sgbd_lab1.DAO.EpisodeDAO;
import org.example.sgbd_lab1.DAO.ShowDAO;
import org.example.sgbd_lab1.model.Actor;
import org.example.sgbd_lab1.model.Episode;
import org.example.sgbd_lab1.model.Show;

import java.util.Optional;

public class Service {
    private final ActorDAO actorDAO = ActorDAO.getInstance();
    private final EpisodeDAO episodeDAO = EpisodeDAO.getInstance();
    private final ShowDAO showDAO = ShowDAO.getInstance();

    public Iterable<Actor> getAllActors() {
        return actorDAO.getAllActors();
    }

    public Optional<Actor> getActorById(Integer id) {
        return actorDAO.getActorById(id);
    }

    public void addActor(Actor actor) {
        actorDAO.addActor(actor);
    }

    public void deleteActor(Actor actor) {
        actorDAO.removeActor(actor.getId());
    }

    public void updateActor(Actor actor) {
        actorDAO.updateActor(actor);
    }


    public Iterable<Episode> getAllEpisodes() {
        return episodeDAO.getAllEpisodes();
    }

    public Iterable<Episode> getEpisodesByShowId(Integer showId) {
        return episodeDAO.getEpisodesByShowId(showId);
    }

    public void addEpisode(Episode episode) {
        episodeDAO.addEpisode(episode);
    }

    public void deleteEpisode(Episode episode) {
        episodeDAO.removeEpisode(episode.getId());
    }

    public void updateEpisode(Episode episode) {
        episodeDAO.updateEpisode(episode);
    }

    public Iterable<Show> getAllShows() {
        return showDAO.getAllShows();
    }

    public Optional<Show> getShowById(Integer id) {
        return showDAO.getShowById(id);
    }

    public void addShow(Show show) {
        showDAO.addShow(show);
    }

    public void deleteShow(Show show) {
        showDAO.removeShow(show.getId());
    }

    public void updateShow(Show show) {
        showDAO.updateShow(show);
    }

}
