package model;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Model class representing an Academic Notice in FAMS.
 * Maps directly to the 'notices' table and encapsulates notice details,
 * publishing metadata, expiration, target department, and attachment document.
 */
public class Notice {

    private String noticeId;        // 'notice_id' varchar(10) PK, e.g. "NOT001"
    private String title;           // 'title' varchar(200) NOT NULL
    private String description;     // 'description' text NOT NULL
    private Date noticeDate;        // 'notice_date' date NOT NULL
    private String createdBy;       // 'created_by' varchar(10) NOT NULL FK -> admins(admin_id)
    private String status;          // 'status' enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'

    // Additional display & department metadata
    private String creatorName;     // e.g. "System Administrator"
    private String creatorUsername; // e.g. "admin001"
    private Date expiryDate;        // Expiry date for circular/notice
    private String targetDepartment;// e.g. "Information and Communication Technology" or "Faculty-wide"
    private String attachmentName;  // e.g. "Mid_Semester_Exam_Timetable_2026.pdf"
    private boolean urgent;         // High priority / urgent flag
    private boolean read;           // Read vs unread indicator for officer

    public Notice() {
        this.status = "ACTIVE";
        this.noticeDate = Date.valueOf(LocalDate.now());
        this.expiryDate = Date.valueOf(LocalDate.now().plusDays(30));
        this.targetDepartment = "Information and Communication Technology";
        this.urgent = false;
        this.read = false;
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, String createdBy, String status) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.noticeDate = noticeDate;
        this.createdBy = createdBy;
        this.status = (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
        if (noticeDate != null) {
            this.expiryDate = Date.valueOf(noticeDate.toLocalDate().plusDays(30));
        }
        this.targetDepartment = "Information and Communication Technology";
        this.urgent = computeUrgency(title, description);
        this.read = false;
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, String createdBy, String status, String creatorName, String creatorUsername) {
        this(noticeId, title, description, noticeDate, createdBy, status);
        this.creatorName = creatorName;
        this.creatorUsername = creatorUsername;
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, Date expiryDate,
                  String createdBy, String status, String targetDepartment, String attachmentName,
                  boolean urgent, String creatorName) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.noticeDate = noticeDate;
        this.expiryDate = expiryDate;
        this.createdBy = createdBy;
        this.status = (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
        this.targetDepartment = targetDepartment;
        this.attachmentName = attachmentName;
        this.urgent = urgent || computeUrgency(title, description);
        this.creatorName = creatorName;
        this.read = false;
    }

    private static boolean computeUrgency(String title, String desc) {
        String combined = ((title != null ? title : "") + " " + (desc != null ? desc : "")).toLowerCase();
        return combined.contains("urgent") || combined.contains("exam") || combined.contains("deadline") || combined.contains("immediate");
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
        if (computeUrgency(title, this.description)) {
            this.urgent = true;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        if (computeUrgency(this.title, description)) {
            this.urgent = true;
        }
    }

    public Date getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(Date noticeDate) {
        this.noticeDate = noticeDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
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

    public String getTargetDepartment() {
        return (targetDepartment != null && !targetDepartment.isBlank()) ? targetDepartment : "Faculty-wide";
    }

    public void setTargetDepartment(String targetDepartment) {
        this.targetDepartment = targetDepartment;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public boolean hasAttachment() {
        return attachmentName != null && !attachmentName.isBlank();
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status) && !isExpired();
    }

    public boolean isExpired() {
        if (expiryDate == null) return false;
        Date today = Date.valueOf(LocalDate.now());
        return expiryDate.before(today);
    }

    public String getFormattedDate() {
        if (noticeDate == null) return "N/A";
        return noticeDate.toString();
    }

    public String getFormattedExpiryDate() {
        if (expiryDate == null) return "No Expiry";
        return expiryDate.toString();
    }

    public String getShortDescription(int maxLength) {
        if (description == null) return "";
        String clean = description.replaceAll("\\s+", " ").trim();
        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, Math.min(clean.length(), maxLength - 3)) + "...";
    }

    @Override
    public String toString() {
        return "Notice{" +
                "noticeId='" + noticeId + '\'' +
                ", title='" + title + '\'' +
                ", noticeDate=" + noticeDate +
                ", expiryDate=" + expiryDate +
                ", department='" + targetDepartment + '\'' +
                ", status='" + status + '\'' +
                ", urgent=" + urgent +
                '}';
    }
}
