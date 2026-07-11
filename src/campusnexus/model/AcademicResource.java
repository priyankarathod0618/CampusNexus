package campusnexus.model;

import java.time.LocalDateTime;

public class AcademicResource {
    private final int id;
    private final String title;
    private final String type;
    private final String subject;
    private final int year;
    private final String uploaderName;
    private final String description;
    private final LocalDateTime createdAt;

    public AcademicResource(int id, String title, String type, String subject, int year,
                            String uploaderName, String description, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.subject = subject;
        this.year = year;
        this.uploaderName = uploaderName;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public int getYear() { return year; }
    public String getUploaderName() { return uploaderName; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
