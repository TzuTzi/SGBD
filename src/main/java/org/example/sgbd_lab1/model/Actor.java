package org.example.sgbd_lab1.model;

public class Actor {
    private String name;
    private Integer id;

    public Actor(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    public Actor() {}

    public Actor(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
