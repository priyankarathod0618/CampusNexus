package campusnexus.dao;

import campusnexus.config.DatabaseConfig;

import java.sql.*;
import java.util.*;

public class AnalyticsDAO {

    public Map<String,String> getOverallStats(int collegeId) throws SQLException {
        String sql = """
            SELECT
            (SELECT COUNT(*) FROM student_profiles WHERE college_id=?) total_students,
            (SELECT COUNT(*) FROM teacher_profiles WHERE college_id=?) total_teachers,
            (SELECT COUNT(*) FROM events WHERE college_id=?) total_events,
            (SELECT ROUND(AVG(fees),2) FROM colleges WHERE id=?) avg_college_fee,
            (SELECT MAX(price) FROM marketplace_items WHERE college_id=?) max_item_price,
            (SELECT MIN(price) FROM marketplace_items WHERE status='AVAILABLE' AND college_id=?) min_available_price,
            (SELECT COALESCE(SUM(price),0) FROM marketplace_items WHERE status='SOLD' AND college_id=?) total_sales
            """;

        Map<String,String> stats = new LinkedHashMap<>();

        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            for(int i=1;i<=7;i++) ps.setInt(i,collegeId);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    stats.put("Total Students",rs.getString("total_students"));
                    stats.put("Total Teachers",rs.getString("total_teachers"));
                    stats.put("Total Events",rs.getString("total_events"));
                    stats.put("Average College Fee",rs.getString("avg_college_fee"));
                    stats.put("Highest Marketplace Price Ever Listed",rs.getString("max_item_price"));
                    stats.put("Cheapest Available Item",rs.getString("min_available_price"));
                    stats.put("Total Marketplace Sales",rs.getString("total_sales"));
                }
            }
        }
        return stats;
    }

    public List<String> getPopularEvents(int collegeId) throws SQLException{
        String sql="""
            SELECT e.title,COUNT(er.student_id) registrations
            FROM events e
            LEFT JOIN event_registrations er ON e.id=er.event_id
            WHERE e.college_id=?
            GROUP BY e.id,e.title
            HAVING COUNT(er.student_id)>=1
            ORDER BY registrations DESC
            """;
        return runSimpleQuery(sql,collegeId,"title","registrations"," registrations");
    }

    public List<String> getCollegesWithStudentCounts(int collegeId) throws SQLException{
        String sql="""
            SELECT c.name,COUNT(sp.user_id) student_count
            FROM colleges c
            LEFT JOIN student_profiles sp ON c.id=sp.college_id
            WHERE c.id=?
            GROUP BY c.id,c.name
            """;
        return runSimpleQuery(sql,collegeId,"name","student_count"," students");
    }

    public List<String> getStudentsNeedingFollowUp(int collegeId) throws SQLException{
        String sql="""
            SELECT DISTINCT u.name,u.email,'Unresolved complaint' reason
            FROM hostel_complaints hc
            JOIN users u ON hc.student_id=u.id
            JOIN student_profiles sp ON u.id=sp.user_id
            WHERE hc.status<>'RESOLVED' AND sp.college_id=?

            UNION

            SELECT DISTINCT u.name,u.email,'Unanswered question' reason
            FROM questions q
            JOIN users u ON q.student_id=u.id
            JOIN student_profiles sp ON u.id=sp.user_id
            WHERE sp.college_id=?
            AND q.status='OPEN'
            AND NOT EXISTS(
                SELECT 1 FROM question_replies r
                WHERE r.question_id=q.id
            )
            ORDER BY name
            """;

        List<String> list=new ArrayList<>();

        try(Connection conn=DatabaseConfig.getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){

            ps.setInt(1,collegeId);
            ps.setInt(2,collegeId);

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    list.add(rs.getString("name")+" ("+
                            rs.getString("email")+") - "+
                            rs.getString("reason"));
                }
            }
        }
        return list;
    }

    public List<String> getCollegeTeacherFullOverview(int collegeId) throws SQLException{
        String sql="""
            SELECT c.name AS college_name,
                   u.name AS teacher_name
            FROM teacher_profiles tp
            JOIN users u ON tp.user_id=u.id
            JOIN colleges c ON tp.college_id=c.id
            WHERE c.id=?
            ORDER BY u.name
            """;

        List<String> list=new ArrayList<>();

        try(Connection conn=DatabaseConfig.getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){

            ps.setInt(1,collegeId);

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    list.add(rs.getString("college_name")+" - "+
                            rs.getString("teacher_name"));
                }
            }
        }
        return list;
    }

    private List<String> runSimpleQuery(String sql,int collegeId,
                                        String labelCol,String countCol,
                                        String suffix) throws SQLException{

        List<String> list=new ArrayList<>();

        try(Connection conn=DatabaseConfig.getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){

            ps.setInt(1,collegeId);

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    list.add(rs.getString(labelCol)+": "+
                            rs.getInt(countCol)+suffix);
                }
            }
        }
        return list;
    }
}