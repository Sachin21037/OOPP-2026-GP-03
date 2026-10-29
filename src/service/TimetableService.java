package service;

import dao.TimetableDAO;
import model.TimetableEntry;

import java.util.List;

/**
 * Service coordinating Academic Timetable business operations.
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

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department) {
        return timetableDAO.searchTimetables(keyword, dayFilter, department);
    }
}
