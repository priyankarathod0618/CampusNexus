package campusnexus.model;

import java.time.LocalDateTime;

public class HostelComplaint {
    private final int id;
    private final int studentId;
    private final String studentName;
    private final String category;
    private final String description;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime resolvedAt;

    public HostelComplaint(int id, int studentId, String studentName, String category, String description,
                           String status, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.category = category;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
