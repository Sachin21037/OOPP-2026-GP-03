package model;

import java.sql.Date;

public class AttendanceRecord {
    private String attendanceId;
    private String sessionId;
    private String studentId;
    private String offeringId;
    private String courseCode;
    private String courseName;
    private Date sessionDate;
    private String sessionType;
    private int sessionNumber;
    private String topic;
    private String status;
    private String remarks;
    private String medicalStatus;
    private String medicalId;

    public AttendanceRecord() {}

    public String getMedicalStatus() { return medicalStatus; }
    public void setMedicalStatus(String medicalStatus) { this.medicalStatus = medicalStatus; }

    public String getMedicalId() { return medicalId; }
    public void setMedicalId(String medicalId) { this.medicalId = medicalId; }

    public String getOfferingId() { return offeringId; }
    public void setOfferingId(String offeringId) { this.offeringId = offeringId; }

    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getDisplaySummary() {
        return courseCode + " - " + (sessionType != null ? sessionType : "Session") + " #" + sessionNumber + " (" + sessionDate + "): " + (topic != null ? topic : "Lecture");
    }
}
