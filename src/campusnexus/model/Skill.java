package campusnexus.model;

import java.time.LocalDateTime;

public class Skill {
    private final int id;
    private final int studentId;
    private final String studentName;
    private final String skillName;
    private final String description;
    private final LocalDateTime createdAt;

    public Skill(int id, int studentId, String studentName, String skillName, String description,
                 LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.skillName = skillName;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getSkillName() { return skillName; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
