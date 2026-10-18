package service;

import dao.TechnicalOfficerDAO;
import model.LabFacility;
import model.TechnicalOfficer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TechnicalOfficerService Class
 * Business logic service layer for Technical Officer profile, credentials, and statistics.
 * Package: service
 */
public class TechnicalOfficerService {

    private final TechnicalOfficerDAO dao;
    private final LabMaintenanceService labService;
    private final MedicalService medicalService;

    public TechnicalOfficerService() {
        this.dao = new TechnicalOfficerDAO();
        this.labService = new LabMaintenanceService();
        this.medicalService = new MedicalService();
    }

    public TechnicalOfficer getProfile(String officerId) {
        return dao.getOfficerById(officerId);
    }

    public boolean updateProfile(TechnicalOfficer officer) {
        if (officer == null) return false;
        if (officer.getFirstName() == null || officer.getFirstName().trim().isEmpty()) return false;
        if (officer.getEmail() == null || !officer.getEmail().contains("@")) return false;
        return dao.updateOfficer(officer);
    }

    public boolean changePassword(String officerId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }
        return dao.changePassword(officerId, currentPassword, newPassword);
    }

    public List<String[]> getAuditLogs() {
        return dao.getAuditLogs();
    }

    public void addAuditLog(String category, String description, String status) {
        dao.addAuditLog(category, description, status);
    }

    public List<LabFacility> getAssignedLabs() {
        return labService.getAllLabs();
    }

    /**
     * Aggregates real-time operational statistics for the logged-in Technical Officer.
     */
    public Map<String, Object> getProfileStats() {
        Map<String, Object> stats = new HashMap<>();
        List<LabFacility> labs = labService.getAllLabs();

        int totalLabs = labs.size();
        int totalWorkstations = 0;
        int operationalWorkstations = 0;

        for (LabFacility lab : labs) {
            totalWorkstations += lab.getTotalWorkstations();
            operationalWorkstations += lab.getOperationalWorkstations();
        }

        double uptime = totalWorkstations > 0 ? ((double) operationalWorkstations / totalWorkstations) * 100.0 : 100.0;
        int maintenanceTickets = labService.getAllMaintenanceRecords().size();
        int medicalsProcessed = medicalService.getAllMedicals().size();

        stats.put("totalLabs", totalLabs);
        stats.put("totalWorkstations", totalWorkstations);
        stats.put("operationalWorkstations", operationalWorkstations);
        stats.put("uptimePercentage", String.format("%.1f%%", uptime));
        stats.put("maintenanceTickets", maintenanceTickets);
        stats.put("medicalsProcessed", medicalsProcessed);

        return stats;
    }
}
