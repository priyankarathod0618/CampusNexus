package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.Person;
import campusnexus.model.Student;
import campusnexus.model.Teacher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean rollNumberExists(String rollNumber) throws SQLException {
        String sql = "SELECT user_id FROM student_profiles WHERE roll_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean employeeIdExists(String employeeId) throws SQLException {
        String sql = "SELECT user_id FROM teacher_profiles WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertUser(String name, String email, String password, String role) throws SQLException {
        String sql = "INSERT INTO users (name, email, password, role, must_change_password) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert user, no ID generated.");
    }

    public void insertStudentProfile(int userId, int collegeId, String rollNumber, String branch,
                                     int year, String hostelBlock, String phone) throws SQLException {
        String sql = "INSERT INTO student_profiles (user_id, college_id, roll_number, branch, year, hostel_block, phone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, collegeId);
            ps.setString(3, rollNumber);
            ps.setString(4, branch);
            ps.setInt(5, year);
            ps.setString(6, hostelBlock);
            ps.setString(7, phone);
            ps.executeUpdate();
        }
    }

    public void insertTeacherProfile(int userId, int collegeId, String employeeId, String department,
                                     String subject, String phone) throws SQLException {
        String sql = "INSERT INTO teacher_profiles (user_id, college_id, employee_id, department, subject, phone) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, collegeId);
            ps.setString(3, employeeId);
            ps.setString(4, department);
            ps.setString(5, subject);
            ps.setString(6, phone);
            ps.executeUpdate();
        }
    }

    // LEFT JOIN both profile tables so one query works for either role
    public Person findByEmail(String email) throws SQLException {
        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.role, u.must_change_password,
                       sp.roll_number, sp.branch, sp.year, sp.hostel_block, sp.phone AS sp_phone,
                       sp.college_id AS sp_college_id, c1.name AS sp_college_name,
                       tp.employee_id, tp.department, tp.subject, tp.phone AS tp_phone,
                       tp.college_id AS tp_college_id, c2.name AS tp_college_name
                FROM users u
                LEFT JOIN student_profiles sp ON u.id = sp.user_id
                LEFT JOIN teacher_profiles tp ON u.id = tp.user_id
                LEFT JOIN colleges c1 ON sp.college_id = c1.id
                LEFT JOIN colleges c2 ON tp.college_id = c2.id
                WHERE u.email = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String role = rs.getString("role");
                boolean mustChange = rs.getBoolean("must_change_password");

                if ("STUDENT".equals(role)) {
                    return new Student(
                            rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                            rs.getString("password"), mustChange,
                            rs.getString("roll_number"), rs.getString("branch"), rs.getInt("year"),
                            rs.getString("hostel_block"), rs.getString("sp_phone"),
                            rs.getInt("sp_college_id"), rs.getString("sp_college_name")
                    );
                } else {
                    return new Teacher(
                            rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                            rs.getString("password"), mustChange,
                            rs.getString("employee_id"), rs.getString("department"), rs.getString("subject"),
                            rs.getString("tp_phone"), rs.getInt("tp_college_id"), rs.getString("tp_college_name")
                    );
                }
            }
        }
    }

    public void updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ?, must_change_password = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // INNER JOIN - only students that have a matching college row
    public List<Student> findAllStudents() throws SQLException {
        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.must_change_password,
                       sp.roll_number, sp.branch, sp.year, sp.hostel_block, sp.phone, sp.college_id,
                       c.name AS college_name
                FROM users u
                INNER JOIN student_profiles sp ON u.id = sp.user_id
                INNER JOIN colleges c ON sp.college_id = c.id
                WHERE u.role = 'STUDENT'
                ORDER BY u.name
                """;

        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                        rs.getString("password"), rs.getBoolean("must_change_password"),
                        rs.getString("roll_number"), rs.getString("branch"), rs.getInt("year"),
                        rs.getString("hostel_block"), rs.getString("phone"),
                        rs.getInt("college_id"), rs.getString("college_name")
                ));
            }
        }
        return students;
    }

    public List<Teacher> findAllTeachers() throws SQLException {
        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.must_change_password,
                       tp.employee_id, tp.department, tp.subject, tp.phone, tp.college_id,
                       c.name AS college_name
                FROM users u
                INNER JOIN teacher_profiles tp ON u.id = tp.user_id
                INNER JOIN colleges c ON tp.college_id = c.id
                WHERE u.role = 'TEACHER'
                ORDER BY u.name
                """;

        List<Teacher> teachers = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                teachers.add(new Teacher(
                        rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                        rs.getString("password"), rs.getBoolean("must_change_password"),
                        rs.getString("employee_id"), rs.getString("department"), rs.getString("subject"),
                        rs.getString("phone"), rs.getInt("college_id"), rs.getString("college_name")
                ));
            }
        }
        return teachers;
    }

    // Queries the SQL VIEW directly (vw_student_directory) - Unit 7 topic demo
    public List<String> findStudentDirectoryFromView() throws SQLException {
        String sql = "SELECT name, email, branch, year, hostel_block, college_name " +
                "FROM vw_student_directory ORDER BY name";

        List<String> lines = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lines.add(rs.getString("name") + " | " + rs.getString("branch") + " Year " + rs.getInt("year")
                        + " | " + rs.getString("hostel_block") + " | " + rs.getString("college_name"));
            }
        }
        return lines;
    }
}
