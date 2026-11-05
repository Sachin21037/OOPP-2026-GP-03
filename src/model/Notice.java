package model;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Model class representing an Academic Notice in FAMS.
 * Maps directly to the 'notices' table and encapsulates notice details,
 * publishing metadata, advance scheduling, status, department targeting, and attachments.
 */
public class Notice {

    private String noticeId;        // 'notice_id' varchar(10) PK, e.g. "NOT001"
    private String title;           // 'title' varchar(200) NOT NULL
    private String description;     // 'description' text NOT NULL
    private Date noticeDate;        // 'notice_date' date NOT NULL
    private String createdBy;       // 'created_by' varchar(10) NOT NULL FK
    private String authorName;      // Display author name
    private String status;          // 'status' enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'

    // Additional display metadata (from join with users)
    private String creatorName;     // e.g. "System Administrator"
    private String creatorUsername; // e.g. "admin001"

    // Department & attachment & portal metadata
    private Date fromDate;          // Start / effective date of the notice
    private Date toDate;            // End / valid-until date of the notice
    private String targetAudience;  // e.g. "ALL", "STUDENTS", "LECTURERS", "TECHNICAL_OFFICERS"
    private Date expiryDate;        // Expiry date for circular/notice (synced with toDate)
    private String targetDepartment;// e.g. "Information and Communication Technology" or "Faculty-wide"
    private String attachmentName;  // e.g. "Mid_Semester_Exam_Timetable_2026.pdf"
    private boolean urgent;         // High priority / urgent flag
    private boolean read;           // Read vs unread indicator for officer

    // Additional fields for advance announcements (Quizzes, Assignments, Exams)
    private String courseCode;
    private String courseName;
    private String offeringId;
    private String noticeType;      // "QUIZ", "ASSIGNMENT", "EXAM", "GENERAL"
    private Date eventDate;         // Scheduled advance date for the quiz or assignment
    private String venue;           // Lab, LMS, Hall, etc.

    public Notice() {
        this.status = "ACTIVE";
        this.noticeType = "GENERAL";
        this.noticeDate = Date.valueOf(LocalDate.now());
        this.fromDate = Date.valueOf(LocalDate.now());
        this.toDate = Date.valueOf(LocalDate.now().plusDays(30));
        this.expiryDate = this.toDate;
        this.targetAudience = "ALL";
        this.targetDepartment = "Information and Communication Technology";
        this.urgent = false;
        this.read = false;
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, String createdBy, String status) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.noticeDate = noticeDate;
        this.fromDate = (noticeDate != null) ? noticeDate : Date.valueOf(LocalDate.now());
        if (noticeDate != null) {
            this.toDate = Date.valueOf(noticeDate.toLocalDate().plusDays(30));
            this.expiryDate = this.toDate;
        } else {
            this.toDate = Date.valueOf(LocalDate.now().plusDays(30));
            this.expiryDate = this.toDate;
        }
        this.targetAudience = "ALL";
        this.createdBy = createdBy;
        this.status = (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
        this.targetDepartment = "Information and Communication Technology";
        this.urgent = computeUrgency(title, description);
        this.noticeType = inferType(title, description);
        this.read = false;
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, String createdBy, String authorName, String status) {
        this(noticeId, title, description, noticeDate, createdBy, status);
        this.authorName = authorName;
        this.creatorName = authorName;
    }

    public Notice(String noticeId, String title, String description, Date noticeDate, String createdBy, String status, String creatorName, String creatorUsername) {
        this(noticeId, title, description, noticeDate, createdBy, status);
        this.creatorName = creatorName;
        this.creatorUsername = creatorUsername;
        this.authorName = creatorName;
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
        this.authorName = creatorName;
        this.noticeType = inferType(title, description);
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
        if (this.noticeType == null || "GENERAL".equalsIgnoreCase(this.noticeType)) {
            this.noticeType = inferType(title, this.description);
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
        if (this.noticeType == null || "GENERAL".equalsIgnoreCase(this.noticeType)) {
            this.noticeType = inferType(this.title, description);
        }
    }

    public Date getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(Date noticeDate) {
        this.noticeDate = noticeDate;
        if (this.fromDate == null) {
            this.fromDate = noticeDate;
        }
    }

    public Date getFromDate() {
        return (fromDate != null) ? fromDate : noticeDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return (toDate != null) ? toDate : expiryDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
        this.expiryDate = toDate;
    }

    public String getTargetAudience() {
        return (targetAudience != null && !targetAudience.isBlank()) ? targetAudience : "ALL";
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getDisplayAudience() {
        String aud = getTargetAudience();
        if ("ALL".equalsIgnoreCase(aud.trim())) return "All";
        StringBuilder sb = new StringBuilder();
        String upper = aud.toUpperCase();
        if (upper.contains("STUDENT") || upper.contains("UNDERGRADUATE")) {
            sb.append("Students");
        }
        if (upper.contains("LECTURER")) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Lecturers");
        }
        if (upper.contains("TECHNICAL") || upper.contains("OFFICER")) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Technical Officers");
        }
        return sb.length() > 0 ? sb.toString() : aud;
    }

    public boolean matchesRole(String role) {
        if (role == null || role.isBlank() || "ADMIN".equalsIgnoreCase(role.trim())) {
            return true;
        }
        String aud = getTargetAudience().toUpperCase();
        if ("ALL".equals(aud)) {
            return true;
        }
        String r = role.toUpperCase();
        if (r.contains("STUDENT") || r.contains("UNDERGRADUATE")) {
            return aud.contains("STUDENT") || aud.contains("UNDERGRADUATE");
        }
        if (r.contains("LECTURER")) {
            return aud.contains("LECTURER");
        }
        if (r.contains("TECHNICAL") || r.contains("OFFICER")) {
            return aud.contains("TECHNICAL") || aud.contains("OFFICER");
        }
        return true;
    }

    public Date getExpiryDate() {
        return (expiryDate != null) ? expiryDate : toDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
        this.toDate = expiryDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAuthorName() {
        if (authorName != null && !authorName.isBlank()) return authorName;
        if (creatorName != null && !creatorName.isBlank()) return creatorName;
        return createdBy;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
        if (this.creatorName == null) this.creatorName = authorName;
    }

    public String getStatus() {
        return (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
    }

    public void setStatus(String status) {
        this.status = (status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE";
    }

    public String getCreatorName() {
        if (creatorName != null && !creatorName.isBlank()) return creatorName;
        if (authorName != null && !authorName.isBlank()) return authorName;
        return createdBy;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
        if (this.authorName == null) this.authorName = creatorName;
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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
    }

    public String getNoticeType() {
        if (noticeType == null) {
            noticeType = inferType(title, description);
        }
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    // Helper to determine type from keywords
    private String inferType(String t, String d) {
        String combined = ((t != null ? t : "") + " " + (d != null ? d : "")).toUpperCase();
        if (combined.contains("QUIZ")) {
            return "QUIZ";
        } else if (combined.contains("ASSIGNMENT")) {
            return "ASSIGNMENT";
        } else if (combined.contains("EXAM") || combined.contains("MID SEMESTER") || combined.contains("FINAL")) {
            return "EXAM";
        }
        return "GENERAL";
    }

    /**
     * Returns human-readable advance notice text regarding days remaining.
     */
    public String getAdvanceTimeNotice() {
        Date targetDate = (eventDate != null) ? eventDate : noticeDate;
        if (targetDate == null) return "Recent Notice";

        LocalDate today = LocalDate.now();
        LocalDate target = targetDate.toLocalDate();
        long days = ChronoUnit.DAYS.between(today, target);

        if (days > 0) {
            return "Upcoming in " + days + " day" + (days > 1 ? "s" : "") + " (Advance Notice)";
        } else if (days == 0) {
            return "Scheduled for Today!";
        } else {
            long pastDays = Math.abs(days);
            return "Posted " + pastDays + " day" + (pastDays > 1 ? "s" : "") + " ago";
        }
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
        if (clean.length() <= maxLength) return clean;
        return clean.substring(0, maxLength).trim() + "...";
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
