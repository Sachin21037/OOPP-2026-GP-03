package model;

import java.util.Date;

/**
 * AttendanceSession Model Class
 * Represents an academic session conducted for theory/practical/tutorial
 * strictly conforming to Class Diagram Group 03
 */
public class AttendanceSession {
    private int sessionId;
    private int offeringId;
    private Date sessionDate;
    private String sessionType;     // "Theory", "Practical", "Tutorial"
    private int sessionNumber;      // 1 through 15
    private double durationHours;   // typically 2.0 hours per credit standard

    public AttendanceSession() {
    }

    public AttendanceSession(int sessionId, int offeringId, Date sessionDate, String sessionType, int sessionNumber, double durationHours) {
        this.sessionId = sessionId;
        this.offeringId = offeringId;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.sessionNumber = sessionNumber;
        this.durationHours = durationHours;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(int offeringId) {
        this.offeringId = offeringId;
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

    @Override
    public String toString() {
        return "Session #" + sessionNumber + " (" + sessionType + " - " + durationHours + " hrs)";
    }
}
