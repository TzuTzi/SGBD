package org.example.demo11.model;

public class Episode {
    private Integer id;
    private String title;
    private Integer showId;

    public Episode(Integer id, String title, Integer showId) {
        this.id = id;
        this.title = title;
        this.showId = showId;
    }

    public Episode() {}

    public Episode(String title) {
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getShowId() {
        return showId;
    }

    public void setShowId(Integer showId) {
        this.showId = showId;
    }

    @Override
    public String toString() {
        return title;
    }
}