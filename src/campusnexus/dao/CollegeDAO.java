package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.College;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CollegeDAO {
    public List<College> findAll() {
        String sql = """
                SELECT c.id, c.name, c.city, c.code, c.email_domain, c.fees,
                       c.hostel_available, c.facilities,
                       COALESCE(AVG(cr.rating), 0) AS average_rating
                FROM colleges c
                LEFT JOIN college_ratings cr ON c.id = cr.college_id
                GROUP BY c.id, c.name, c.city, c.code, c.email_domain, c.fees,
                         c.hostel_available, c.facilities
                ORDER BY c.name
                """;

        return fetchColleges(sql);
    }

    // Uses LIKE + CONCAT for a partial, case-insensitive match instead of an exact one
    public List<College> searchByCity(String city) {
        String sql = """
                SELECT c.id, c.name, c.city, c.code, c.email_domain, c.fees,
                       c.hostel_available, c.facilities,
                       COALESCE(AVG(cr.rating), 0) AS average_rating
                FROM colleges c
                LEFT JOIN college_ratings cr ON c.id = cr.college_id
                WHERE LOWER(c.city) LIKE LOWER(CONCAT('%', ?, '%'))
                GROUP BY c.id, c.name, c.city, c.code, c.email_domain, c.fees,
                         c.hostel_available, c.facilities
                ORDER BY c.name
                """;

        List<College> colleges = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, city);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    colleges.add(mapCollege(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching colleges: " + e.getMessage());
        }

        return colleges;
    }

    public List<College> findByIds(List<Integer> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        String sql = """
                SELECT c.id, c.name, c.city, c.code, c.email_domain, c.fees,
                       c.hostel_available, c.facilities,
                       COALESCE(AVG(cr.rating), 0) AS average_rating
                FROM colleges c
                LEFT JOIN college_ratings cr ON c.id = cr.college_id
                WHERE c.id IN (%s)
                GROUP BY c.id, c.name, c.city, c.code, c.email_domain, c.fees,
                         c.hostel_available, c.facilities
                ORDER BY c.name
                """.formatted(placeholders);

        List<College> colleges = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                statement.setInt(i + 1, ids.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    colleges.add(mapCollege(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error comparing colleges: " + e.getMessage());
        }

        return colleges;
    }

    private List<College> fetchColleges(String sql) {
        List<College> colleges = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                colleges.add(mapCollege(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Error loading colleges: " + e.getMessage());
        }

        return colleges;
    }

    private College mapCollege(ResultSet resultSet) throws SQLException {
        return new College(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("city"),
                resultSet.getString("code"),
                resultSet.getString("email_domain"),
                resultSet.getDouble("fees"),
                resultSet.getBoolean("hostel_available"),
                resultSet.getString("facilities"),
                resultSet.getDouble("average_rating")
        );
    }
}
