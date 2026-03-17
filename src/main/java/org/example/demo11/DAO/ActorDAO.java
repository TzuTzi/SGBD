package org.example.demo11.DAO;

import org.example.demo11.model.Actor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public class ActorDAO {
    private static ActorDAO instance;

    private ActorDAO() {
    }

    public static ActorDAO getInstance() {
        if (instance == null) {
            instance = new ActorDAO();
        }
        return instance;
    }

    public Iterable<Actor> getAllActors() {
        ArrayList<Actor> actors = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM actor");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(1);
                String name = rs.getString(2);
                actors.add(new Actor(id, name));
            }
            return actors;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actors;
    }

    public Optional<Actor> getActorById(Integer id) {
        String sql = "SELECT * FROM actor WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Integer actorId = rs.getInt(1);
                    String name = rs.getString(2);
                    return Optional.of(new Actor(actorId, name));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
