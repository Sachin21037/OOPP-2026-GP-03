package model;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Model class representing an Academic Notice in FAMS.
 * Maps directly to the 'notices' table and encapsulates notice details,
 * publishing metadata, and status.
 */
public class Notice {

    private String noticeId;        // 'notice_id' varchar(10) PK, e.g. "NOT001"
    private String title;           // 'title' varchar(200) NOT NULL
    private String description;     // 'description' text NOT NULL
    private Date noticeDate;        // 'notice_date' date NOT NULL
    private String createdBy;       // 'created_by' varchar(10) NOT NULL FK -> admins(admin_id)
    private String status;          // 'status' enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'

    // Additional display metadata (from join with users)
    private String creatorName;     // e.g. "System Administrator"
    private String creatorUsername; // e.g. "admin001"

    public Notice() {
        this.status = "ACTIVE";
        this.noticeDate = Date.valueOf(LocalDate.now());
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, String createdBy, String status) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.noticeDate = noticeDate;
        this.createdBy = createdBy;
        this.status = (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, String createdBy, String status, String creatorName, String creatorUsername) {
        this(noticeId, title, description, noticeDate, createdBy, status);
        this.creatorName = creatorName;
        this.creatorUsername = creatorUsername;
    }

    // Getters and Setters
    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(Date noticeDate) {
        this.noticeDate = noticeDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
    }

    public void setStatus(String status) {
        this.status = (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
    }

    public String getCreatorName() {
        return (creatorName != null && !creatorName.isBlank()) ? creatorName : createdBy;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    // Utility methods
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public String getFormattedDate() {
        if (noticeDate == null) return "N/A";
        return noticeDate.toString();
    }

    public String getShortDescription(int maxLength) {
        if (description == null) return "";
        String clean = description.replaceAll("\\s+", " ").trim();
        if (clean.length() <= maxLength) return clean;
        return clean.substring(0, maxLength).trim() + "...";
    }

    @Override
    public String toString() {
        return "Notice{" +
                "noticeId='" + noticeId + '\'' +
                ", title='" + title + '\'' +
                ", noticeDate=" + noticeDate +
                ", createdBy='" + createdBy + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
