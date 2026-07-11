package campusnexus.model;

import java.time.LocalDateTime;

public class Announcement {
    private final int id;
    private final String teacherName;
    private final String title;
    private final String message;
    private final String targetBranch;
    private final Integer targetYear;
    private final LocalDateTime createdAt;

    public Announcement(int id, String teacherName, String title, String message,
                        String targetBranch, Integer targetYear, LocalDateTime createdAt) {
        this.id = id;
        this.teacherName = teacherName;
        this.title = title;
        this.message = message;
        this.targetBranch = targetBranch;
        this.targetYear = targetYear;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getTeacherName() { return teacherName; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTargetBranch() { return targetBranch; }
    public Integer getTargetYear() { return targetYear; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
