package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> findByCollegeId(int collegeId) {

        List<Course> courses = new ArrayList<>();

        String sql = "SELECT * FROM courses WHERE college_id=? ORDER BY course_name";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, collegeId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getInt("college_id"),
                        rs.getString("course_name"),
                        rs.getString("duration"),
                        rs.getDouble("annual_fee")
                ));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return courses;
    }
}