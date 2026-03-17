package org.example.demo11.model;

public class Actor {
    private Integer id;
    private String name;

    public Actor(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
    public Actor() {

    }
    public Actor(String name) {
        this.name = name;

    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setId(Integer id) {
        this.id = id;
    }

}
