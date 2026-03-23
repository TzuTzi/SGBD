package org.example.demo11.DAO;

import org.example.demo11.model.Show;

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
        // Interogare: preia toti parintii (shows).
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
            throw new DatabaseAccessException("Nu s-au putut incarca show-urile din baza de date.", e);
        }
        return shows;
    }

    public Optional<Show> getShowById(Integer id) {
        // Interogare: preia un singur show dupa ID.
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
            throw new DatabaseAccessException("Nu s-a putut incarca show-ul dupa id.", e);
        }
        return Optional.empty();
    }

    public void addShow(Show show) {
        // INSERT parametrizat: previne SQL injection si adauga un parinte nou.
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
            throw new DatabaseAccessException("Nu s-a putut adauga show-ul in baza de date.", e);
        }
    }

    public void updateShow(Show show) {
        // UPDATE parametrizat: modifica titlul show-ului existent.
        String sql = "UPDATE shows SET s_title = ? WHERE s_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, show.getTitle());
            ps.setInt(2, show.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Nu s-a putut actualiza show-ul in baza de date.", e);
        }
    }

    public void removeShow(Integer id) {
        // DELETE parametrizat: sterge show-ul (cu episoade, daca ai ON DELETE CASCADE).
        String sql = "DELETE FROM shows WHERE s_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Nu s-a putut sterge show-ul din baza de date.", e);
        }
    }
}