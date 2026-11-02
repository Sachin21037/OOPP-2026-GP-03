package model;

import java.util.Date;

/**
 * AttendanceSession Model Class
 * Represents an academic session conducted for theory/practical.
 * Strictly conforms to `attendance_sessions` in database/fams.sql.
 */
public class AttendanceSession {
    private int sessionId;
    private String sessionCode;     // e.g. "SES001"
    private int offeringId;
    private String offeringCode;    // e.g. "OFF001"
    private Date sessionDate;
    private String sessionType;     // "Theory", "Practical"
    private int sessionNumber;      // 1 through 15
    private double durationHours;   // typically 2.0 hours
    private String topic;
    private String conductedBy;     // e.g. "TO001"

    public AttendanceSession() {
    }

    public AttendanceSession(int sessionId, int offeringId, Date sessionDate, String sessionType, int sessionNumber, double durationHours) {
        this.sessionId = sessionId;
        this.sessionCode = String.format("SES%03d", sessionId);
        this.offeringId = offeringId;
        this.offeringCode = String.format("OFF%03d", offeringId);
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.sessionNumber = sessionNumber;
        this.durationHours = durationHours;
    }

    public AttendanceSession(String sessionCode, String offeringCode, Date sessionDate, String sessionType, int sessionNumber, String topic, String conductedBy) {
        this.sessionCode = sessionCode;
        try {
            this.sessionId = Integer.parseInt(sessionCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.sessionId = 1;
        }
        this.offeringCode = offeringCode;
        try {
            this.offeringId = Integer.parseInt(offeringCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.offeringId = 1;
        }
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.sessionNumber = sessionNumber;
        this.durationHours = 2.0;
        this.topic = topic;
        this.conductedBy = conductedBy;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
        this.sessionCode = String.format("SES%03d", sessionId);
    }

    public String getSessionCode() {
        if (sessionCode == null || sessionCode.isEmpty()) {
            return String.format("SES%03d", sessionId);
        }
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public int getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(int offeringId) {
        this.offeringId = offeringId;
        this.offeringCode = String.format("OFF%03d", offeringId);
    }

    public String getOfferingCode() {
        if (offeringCode == null || offeringCode.isEmpty()) {
            return String.format("OFF%03d", offeringId);
        }
        return offeringCode;
    }

    public void setOfferingCode(String offeringCode) {
        this.offeringCode = offeringCode;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(double durationHours) {
        this.durationHours = durationHours;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConductedBy() {
        return conductedBy;
    }

    public void setConductedBy(String conductedBy) {
        this.conductedBy = conductedBy;
    }

    @Override
    public String toString() {
        return "Session #" + sessionNumber + " (" + sessionType + " - " + durationHours + " hrs)";
    }
}
