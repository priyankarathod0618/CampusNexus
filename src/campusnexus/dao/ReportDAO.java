package campusnexus.dao;

import campusnexus.config.DatabaseConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDAO {

    // Calls sp_unresolved_complaints_report(), which uses a CURSOR internally (schema.sql)
    public Map<String, Integer> unresolvedComplaintsByHostelBlock() throws SQLException {
        Map<String, Integer> summary = new LinkedHashMap<>();
        String call = "{CALL sp_unresolved_complaints_report()}";

        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall(call);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                summary.put(rs.getString("hostel_block"), rs.getInt("open_count"));
            }
        }
        return summary;
    }
}
