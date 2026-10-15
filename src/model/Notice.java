package model;

import java.sql.Date;

public class Notice {
    private String noticeId;
    private String title;
    private String description;
    private Date noticeDate;
    private String createdBy;
    private String authorName;
    private String status;

    public Notice() {}

    public String getNoticeId() { return noticeId; }
    public void setNoticeId(String noticeId) { this.noticeId = noticeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getNoticeDate() { return noticeDate; }
    public void setNoticeDate(Date noticeDate) { this.noticeDate = noticeDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
