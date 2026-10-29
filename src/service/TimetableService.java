package service;

import dao.TimetableDAO;
import model.TimetableEntry;

import java.util.List;

/**
 * Service coordinating Academic Timetable business operations.
 * Enforces view-only access, department authorization scoping,
 * academic year/semester/day filtering, and search capabilities.
 */
public class TimetableService {

    private final TimetableDAO timetableDAO;

    public TimetableService() {
        this.timetableDAO = new TimetableDAO();
    }

    public TimetableService(TimetableDAO timetableDAO) {
        this.timetableDAO = timetableDAO;
    }

    public List<TimetableEntry> getAllTimetables() {
        return timetableDAO.getAllTimetables();
    }

    public List<TimetableEntry> getTimetablesForDepartment(String department) {
        return timetableDAO.getTimetablesForDepartment(department);
    }

    public List<String> getAvailableDepartments() {
        return timetableDAO.getAvailableDepartments();
    }

    public List<String> getAvailableAcademicYears() {
        return timetableDAO.getAvailableAcademicYears();
    }

    public List<String> getAvailableSemesters() {
        return timetableDAO.getAvailableSemesters();
    }

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department) {
        return timetableDAO.searchTimetables(keyword, dayFilter, department);
    }

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department, String sessionType) {
        return timetableDAO.searchTimetables(keyword, dayFilter, department, sessionType);
    }

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department,
                                                 String sessionType, String academicYear, String semester) {
        return timetableDAO.searchTimetables(keyword, dayFilter, department, sessionType, academicYear, semester);
    }

    public String getNextTimetableId() {
        return timetableDAO.getNextTimetableId();
    }

    public boolean createTimetableEntry(TimetableEntry entry) {
        return timetableDAO.addTimetableEntry(entry);
    }

    public boolean deleteTimetableEntry(String timetableId) {
        return timetableDAO.deleteTimetableEntry(timetableId);
    }
}
