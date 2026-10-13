package controller;

import dao.AttendanceDAO;
import model.AttendanceRecord;
import model.AttendanceScenarioResult;
import model.AttendanceSession;
import model.CourseOffering;
import model.Undergraduate;

import java.util.List;

/**
 * AttendanceController Class
 * Controller layer coordinating business flow between UI Views, DAOs, and Calculator
 * per Three-Tier MVC Architecture (SRS Section 2.2).
 */
public class AttendanceController {

    private final AttendanceDAO attendanceDAO;

    public AttendanceController() {
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
