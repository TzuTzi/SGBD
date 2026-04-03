package org.example.sgbd_lab1.model;

public class Show {
    private Integer id;
    private String title;

    public Show(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public Show() {}

    public Show(String title) {
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

    @Override
    public String toString() {
        return title;
    }

}
