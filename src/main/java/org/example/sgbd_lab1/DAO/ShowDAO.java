package org.example.sgbd_lab1.DAO;

import org.example.sgbd_lab1.model.Show;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class ShowDAO {
    private static ShowDAO instance;

    private ShowDAO() {}

    public static ShowDAO getInstance() {
        if (instance == null) {
            instance = new ShowDAO();
        }
        return instance;
    }

    public Iterable<Show> getAllShows() {
        ArrayList<Show> shows = new ArrayList<>();
        String sql = "SELECT * FROM shows";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Integer id = rs.getInt("s_id");
                String title = rs.getString("s_title");
                shows.add(new Show(id, title));
            }
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("citire shows", e);
        }
        return shows;
    }

    public Optional<Show> getShowById(Integer id) {
        String sql = "SELECT * FROM shows WHERE s_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("s_title");
                    return Optional.of(new Show(id, title));
                }
            }
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("citire show după ID", e);
        }
        return Optional.empty();
    }

    public void addShow(Show show) {
        String sql = "INSERT INTO shows (s_title) VALUES (?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, show.getTitle());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    show.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("adăugare show", e);
        }
    }

    public void updateShow(Show show) {
        String sql = "UPDATE shows SET s_title = ? WHERE s_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, show.getTitle());
            ps.setInt(2, show.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("actualizare show", e);
        }
    }

    public void removeShow(Integer id) {
        String sql = "DELETE FROM shows WHERE s_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new org.example.sgbd_lab1.DbException("Nu se poate șterge show-ul deoarece are episoade asociate.", e);
            }
            throw org.example.sgbd_lab1.DbException.fromSQLException("ștergere show", e);
        }
    }
}
