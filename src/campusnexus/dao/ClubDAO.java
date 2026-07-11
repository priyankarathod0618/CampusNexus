package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.exception.DuplicateClubMembershipException;
import campusnexus.model.Club;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class ClubDAO {

    public List<Club> findAll() throws SQLException {
        String sql = "SELECT id, name, category, description FROM clubs ORDER BY name";
        List<Club> clubs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clubs.add(mapClub(rs));
            }
        }
        return clubs;
    }

    public List<Club> findByStudent(int studentId) throws SQLException {
        String sql = """
                SELECT c.id, c.name, c.category, c.description
                FROM clubs c
                INNER JOIN club_members cm ON c.id = cm.club_id
                WHERE cm.student_id = ?
                ORDER BY c.name
                """;
        List<Club> clubs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clubs.add(mapClub(rs));
                }
            }
        }
        return clubs;
    }

    // Composite primary key (club_id, student_id) makes a duplicate join a constraint violation
    public void joinClub(int clubId, int studentId) throws DuplicateClubMembershipException, SQLException {
        String sql = "INSERT INTO club_members (club_id, student_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateClubMembershipException("You have already joined this club.");
        }
    }

    private Club mapClub(ResultSet rs) throws SQLException {
        return new Club(rs.getInt("id"), rs.getString("name"), rs.getString("category"), rs.getString("description"));
    }
}
