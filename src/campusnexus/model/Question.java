package campusnexus.model;

import java.time.LocalDateTime;

public class Question {
    private final int id;
    private final int studentId;
    private final String studentName;
    private final String title;
    private final String description;
    private final String status;
    private final LocalDateTime createdAt;

    public Question(int id, int studentId, String studentName, String title, String description,
                    String status, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
