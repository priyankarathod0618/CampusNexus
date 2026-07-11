package campusnexus.model;

import java.time.LocalDateTime;

public class QuestionReply {
    private final int id;
    private final int questionId;
    private final String authorName;
    private final String replyText;
    private final LocalDateTime createdAt;

    public QuestionReply(int id, int questionId, String authorName, String replyText, LocalDateTime createdAt) {
        this.id = id;
        this.questionId = questionId;
        this.authorName = authorName;
        this.replyText = replyText;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getAuthorName() { return authorName; }
    public String getReplyText() { return replyText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
