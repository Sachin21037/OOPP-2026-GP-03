package service;

import dao.AttendanceDAO;
import model.*;

import java.util.List;

/**
 * AttendanceService Class
 * Business logic service layer for Technical Officer Attendance operations.
 * Coordinates between UI Views, DAOs, and Calculator per 3-Tier Architecture.
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }

    public List<CourseOffering> getCourseOfferings() {
        return attendanceDAO.getAllCourseOfferings();
    }

    public List<Undergraduate> getStudentsForOffering(int offeringId) {
        return attendanceDAO.getStudentsByOffering(offeringId);
    }

    public boolean recordSessionAttendance(AttendanceSession session, List<AttendanceRecord> records) {
        if (session == null || records == null || records.isEmpty()) {
            return false;
        }
        return attendanceDAO.saveAttendanceSession(session, records);
    }

    public List<AttendanceScenarioResult> getBatchSummary(int offeringId, String trackFilter) {
        return attendanceDAO.getBatchAttendanceSummary(offeringId, trackFilter);
    }

    public List<AttendanceRecord> getStudentHistory(int offeringId, String studentIndexNo) {
        return attendanceDAO.getStudentSessionHistory(offeringId, studentIndexNo);
    }

    public Undergraduate findStudentByIndex(String query) {
        if (query == null || query.trim().isEmpty()) return null;
        String q = query.trim().toLowerCase();
        List<CourseOffering> offerings = getCourseOfferings();
        for (CourseOffering o : offerings) {
            for (Undergraduate u : getStudentsForOffering(o.getOfferingId())) {
                if (q.equalsIgnoreCase(u.getIndexNo().toLowerCase()) ||
                    q.equalsIgnoreCase(u.getStudentCode().toLowerCase()) ||
                    u.getFullName().toLowerCase().contains(q)) {
                    return u;
                }
            }
        }
        return null;
    }
}
