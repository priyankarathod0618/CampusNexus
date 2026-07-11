package campusnexus.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Deliberate RandomAccessFile demo (Java syllabus topic 7).
 * Every record is padded/truncated to a fixed width so any entry can be
 * jumped to directly with seek() instead of reading the file sequentially.
 */
public class ActivityLogger {
    private static final String LOG_FILE = "data/activity_log.txt";
    private static final int RECORD_SIZE = 120;
    private static final String LINE_SEP = System.lineSeparator();

    public static void log(String action) {
        try {
            File dir = new File("data");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String raw = "[" + timestamp + "] " + action;
            String entry = String.format("%-" + RECORD_SIZE + "s",
                    raw.length() > RECORD_SIZE ? raw.substring(0, RECORD_SIZE) : raw);

            try (RandomAccessFile raf = new RandomAccessFile(LOG_FILE, "rw")) {
                raf.seek(raf.length());
                raf.writeBytes(entry + LINE_SEP);
            }
        } catch (IOException e) {
            System.out.println("Could not write activity log: " + e.getMessage());
        }
    }

    // Jumps directly to a specific record index using seek() - true random access
    public static void printRecord(int recordIndex) {
        try (RandomAccessFile raf = new RandomAccessFile(LOG_FILE, "r")) {
            long recordLength = RECORD_SIZE + LINE_SEP.length();
            long position = (long) recordIndex * recordLength;

            if (position >= raf.length()) {
                System.out.println("No log record at index " + recordIndex);
                return;
            }

            raf.seek(position);
            byte[] buffer = new byte[RECORD_SIZE];
            raf.readFully(buffer);
            System.out.println(new String(buffer).trim());
        } catch (IOException e) {
            System.out.println("No activity log yet.");
        }
    }

    public static void printAll() {
        try (RandomAccessFile raf = new RandomAccessFile(LOG_FILE, "r")) {
            long recordLength = RECORD_SIZE + LINE_SEP.length();
            long totalRecords = raf.length() / recordLength;

            System.out.println();
            System.out.println("----- Admin Activity Log (" + totalRecords + " entries) -----");

            for (long i = 0; i < totalRecords; i++) {
                raf.seek(i * recordLength);
                byte[] buffer = new byte[RECORD_SIZE];
                raf.readFully(buffer);
                System.out.println(new String(buffer).trim());
            }
        } catch (IOException e) {
            System.out.println("No activity log yet.");
        }
    }
}
