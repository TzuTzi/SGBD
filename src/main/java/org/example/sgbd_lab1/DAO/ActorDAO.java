package org.example.sgbd_lab1.DAO;

import org.example.sgbd_lab1.model.Actor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Optional;

public class ActorDAO {
    private static ActorDAO instance;

    private ActorDAO() {}

    public static ActorDAO getInstance() {
        if( instance == null)
            instance = new ActorDAO();
        return instance;
    }

    public Iterable<Actor> getAllActors() {
        ArrayList<Actor> actors = new ArrayList<>();
        try(Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM actors")){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Integer id = rs.getInt("id");
                String name = rs.getString("name");
                Actor actor = new Actor(name, id);
                actors.add(actor);
            }

        } catch(SQLException e){
            throw new org.example.sgbd_lab1.DbException("Could not load actors.", e);
        }
        return actors;
    }

    public Optional<Actor> getActorById(Integer id) {
        try(Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM actors WHERE id = ?")){
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String name = rs.getString("name");
                Actor actor = new Actor(name, id);
                return Optional.of(actor);
            }
        } catch(SQLException e){
            throw new org.example.sgbd_lab1.DbException("Could not load actor by id.", e);
        }
        return Optional.empty();
    }

    public void addActor(Actor actor) {
        try(Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO actors (name) VALUES (?)")){
            ps.setString(1, actor.getName());
            ps.executeUpdate();
        } catch(SQLException e){
            throw new org.example.sgbd_lab1.DbException("Could not add actor.", e);
        }
    }

    public void removeActor(Integer id) {
        try(Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM actors WHERE id = ?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch(SQLException e){
            throw new org.example.sgbd_lab1.DbException("Could not delete actor.", e);
        }
    }

    public void updateActor(Actor actor) {
        try(Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE actors SET name = ? WHERE id = ?")){
            ps.setString(1, actor.getName());
            ps.setInt(2, actor.getId());
            ps.executeUpdate();
        } catch(SQLException e){
            throw new org.example.sgbd_lab1.DbException("Could not update actor.", e);
        }
    }

}
