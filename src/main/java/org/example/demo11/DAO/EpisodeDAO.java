package org.example.demo11.DAO;

import org.example.demo11.model.Episode;

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
        // Interogare: preia episoadele impreuna cu titlul show-ului (JOIN).
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
            throw new DatabaseAccessException("Nu s-au putut incarca episoadele din baza de date.", e);
        }
        return episodes;
    }

    public Iterable<Episode> getEpisodesByShowId(Integer showId) {
        ArrayList<Episode> episodes = new ArrayList<>();
        // Interogare: preia doar episoadele apartinand show-ului selectat (1-n).
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
            throw new DatabaseAccessException("Nu s-au putut incarca episoadele pentru show-ul selectat.", e);
        }
        return episodes;
    }

    public Optional<Episode> getEpisodeById(Integer id) {
        // Interogare: preia un episod dupa ID.
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
            throw new DatabaseAccessException("Nu s-a putut incarca episodul dupa id.", e);
        }
        return Optional.empty();
    }

    public void addEpisode(Episode episode) {
        // INSERT parametrizat: adauga un copil pentru showId-ul primit.
        String sql = "INSERT INTO episodes (s_id, e_title) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, episode.getShowId());
            ps.setString(2, episode.getTitle());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    episode.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Nu s-a putut adauga episodul in baza de date.", e);
        }
    }

    public void updateEpisode(Episode episode) {
        // UPDATE parametrizat: modifica episodul existent (mentine relatia cu show-ul).
        String sql = "UPDATE episodes SET s_id = ?, e_title = ? WHERE e_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, episode.getShowId());
            ps.setString(2, episode.getTitle());
            ps.setInt(3, episode.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Nu s-a putut actualiza episodul in baza de date.", e);
        }
    }

    public void removeEpisode(Integer id) {
        // DELETE parametrizat: sterge episodul dupa ID.
        String sql = "DELETE FROM episodes WHERE e_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Nu s-a putut sterge episodul din baza de date.", e);
        }
    }

    private Episode extractEpisodeFromResultSet(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("e_id");
        Integer showId = rs.getInt("s_id");
        String title = rs.getString("e_title");
        return new Episode(id, title, showId);
    }
}
