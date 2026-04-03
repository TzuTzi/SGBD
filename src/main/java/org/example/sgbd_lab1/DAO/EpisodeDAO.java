package org.example.sgbd_lab1.DAO;

import org.example.sgbd_lab1.model.Episode;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class EpisodeDAO {
    private static EpisodeDAO instance;

    private EpisodeDAO() {}

    public static EpisodeDAO getInstance() {
        if (instance == null) {
            instance = new EpisodeDAO();
        }
        return instance;
    }

    public Iterable<Episode> getAllEpisodes() {
        ArrayList<Episode> episodes = new ArrayList<>();
        String sql = "SELECT e.*, s.s_title FROM episodes e " +
                "JOIN shows s ON e.s_id = s.s_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Episode episode = extractEpisodeFromResultSet(rs);
                episodes.add(episode);
            }
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("citire episodes", e);
        }
        return episodes;
    }

    public Iterable<Episode> getEpisodesByShowId(Integer showId) {
        ArrayList<Episode> episodes = new ArrayList<>();
        String sql = "SELECT e.*, s.s_title FROM episodes e " +
                "JOIN shows s ON e.s_id = s.s_id " +
                "WHERE e.s_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Episode episode = extractEpisodeFromResultSet(rs);
                    episodes.add(episode);
                }
            }
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("citire episoade pentru show selectat", e);
        }
        return episodes;
    }

    public Optional<Episode> getEpisodeById(Integer id) {
        String sql = "SELECT e.*, s.s_title FROM episodes e " +
                "JOIN shows s ON e.s_id = s.s_id " +
                "WHERE e.e_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractEpisodeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("citire episod după ID", e);
        }
        return Optional.empty();
    }

    public void addEpisode(Episode episode) {

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO episodes (s_id, e_title) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS
             )) {

            ps.setInt(1, episode.getShowId());
            ps.setString(2, episode.getTitle());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    episode.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("adăugare episod", e);
        }
    }

    public void updateEpisode(Episode episode) {
        String sql = "UPDATE episodes SET s_id = ?, e_title = ? WHERE e_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, episode.getShowId());
            ps.setString(2, episode.getTitle());
            ps.setInt(3, episode.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("actualizare episod", e);
        }
    }

    public void removeEpisode(Integer id) {
        String sql = "DELETE FROM episodes WHERE e_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw org.example.sgbd_lab1.DbException.fromSQLException("ștergere episod", e);
        }
    }

    private Episode extractEpisodeFromResultSet(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("e_id");
        Integer showId = rs.getInt("s_id");
        String title = rs.getString("e_title");
        return new Episode(id, title, showId);
    }
}
