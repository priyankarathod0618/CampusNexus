package campusnexus.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A second, deliberately visible File I/O demo (Java syllabus topic 7) -
 * separate from ActivityLogger's RandomAccessFile usage. This one uses the
 * more common FileWriter/BufferedWriter pattern, plus real File class
 * methods: exists(), mkdirs(), length(), getAbsolutePath().
 */
public class ReportExportService {
    private static final String EXPORT_DIR = "reports";

    public static String exportComplaintReport(Map<String, Integer> summary) throws IOException {
        List<String> lines = new ArrayList<>();
        summary.forEach((block, count) -> lines.add(block + ": " + count + " open complaint(s)"));
        return writeReport("complaint_report", lines);
    }

    public static String exportStudentDirectory(List<String> lines) throws IOException {
        return writeReport("student_directory", lines);
    }

    public static String exportFollowUpList(List<String> lines) throws IOException {
        return writeReport("students_needing_followup", lines);
    }

    private static String writeReport(String baseName, List<String> lines) throws IOException {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File(dir, baseName + "_" + timestamp + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("CampusNexus Report - " + baseName);
            writer.newLine();
            writer.write("Generated: " + LocalDateTime.now());
            writer.newLine();
            writer.write("=".repeat(50));
            writer.newLine();

            if (lines.isEmpty()) {
                writer.write("(no data)");
                writer.newLine();
            } else {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        return file.getAbsolutePath() + " (" + file.length() + " bytes)";
    }
}
