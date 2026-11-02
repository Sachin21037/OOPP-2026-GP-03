package service;

import dao.LabMaintenanceDAO;
import model.LabFacility;
import model.LabMaintenanceRecord;
import model.LabScheduleTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * LabMaintenanceService Class
 * Business logic service layer for Laboratory Maintenance and Equipment Management.
 * Package: service
 */
public class LabMaintenanceService {

    private final LabMaintenanceDAO dao;

    public LabMaintenanceService() {
        this.dao = new LabMaintenanceDAO();
    }

    // =========================================================
    // FACILITIES
    // =========================================================

    public List<LabFacility> getAllLabs() {
        return dao.getAllLabs();
    }

    public LabFacility getLabById(String labId) {
        return dao.getLabById(labId);
    }

    public boolean updateLab(LabFacility lab) {
        return dao.updateLab(lab);
    }

    public boolean updateLabStatus(String labId, String status, int operationalCount) {
        LabFacility lab = dao.getLabById(labId);
        if (lab != null) {
            lab.setStatus(status);
            lab.setOperationalWorkstations(Math.min(lab.getTotalWorkstations(), Math.max(0, operationalCount)));
            return dao.updateLab(lab);
        }
        return false;
    }

    // =========================================================
    // MAINTENANCE RECORDS
    // =========================================================

    public List<LabMaintenanceRecord> getAllMaintenanceRecords() {
        return dao.getAllMaintenanceRecords();
    }

    public List<LabMaintenanceRecord> filterMaintenanceRecords(String labFilter, String statusFilter,
                                                               String priorityFilter, String searchKeyword) {
        List<LabMaintenanceRecord> all = dao.getAllMaintenanceRecords();
        List<LabMaintenanceRecord> filtered = new ArrayList<>();

        String kw = searchKeyword != null ? searchKeyword.trim().toLowerCase() : "";

        for (LabMaintenanceRecord r : all) {
            // Lab filter
            if (labFilter != null && !labFilter.isEmpty() && !labFilter.startsWith("--") && !labFilter.equalsIgnoreCase("All Labs")) {
                if (!r.getLabName().toLowerCase().contains(labFilter.toLowerCase()) &&
                    !r.getLabId().equalsIgnoreCase(labFilter)) {
                    continue;
                }
            }

            // Status filter
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.startsWith("--") && !statusFilter.equalsIgnoreCase("All Statuses")) {
                if (!r.getStatus().equalsIgnoreCase(statusFilter)) {
                    continue;
                }
            }

            // Priority filter
            if (priorityFilter != null && !priorityFilter.isEmpty() && !priorityFilter.startsWith("--") && !priorityFilter.equalsIgnoreCase("All Priorities")) {
                if (!r.getPriority().equalsIgnoreCase(priorityFilter)) {
                    continue;
                }
            }

            // Keyword filter
            if (!kw.isEmpty()) {
                boolean matchId = r.getMaintenanceId() != null && r.getMaintenanceId().toLowerCase().contains(kw);
                boolean matchCode = r.getEquipmentCode() != null && r.getEquipmentCode().toLowerCase().contains(kw);
                boolean matchType = r.getEquipmentType() != null && r.getEquipmentType().toLowerCase().contains(kw);
                boolean matchDesc = r.getIssueDescription() != null && r.getIssueDescription().toLowerCase().contains(kw);
                boolean matchLab = r.getLabName() != null && r.getLabName().toLowerCase().contains(kw);
                boolean matchTech = r.getTechnicianInCharge() != null && r.getTechnicianInCharge().toLowerCase().contains(kw);

                if (!matchId && !matchCode && !matchType && !matchDesc && !matchLab && !matchTech) {
                    continue;
                }
            }

            filtered.add(r);
        }

        return filtered;
    }

    public boolean addMaintenanceRecord(LabMaintenanceRecord record) {
        if (record.getReportedDate() == null) {
            record.setReportedDate(new Date());
        }
        if (record.getStatus() == null || record.getStatus().trim().isEmpty()) {
            record.setStatus("Pending");
        }
        return dao.addMaintenanceRecord(record);
    }

    public boolean updateMaintenanceRecord(LabMaintenanceRecord record) {
        return dao.updateMaintenanceRecord(record);
    }

    public boolean startMaintenance(String maintenanceId, String technician) {
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if (r.getMaintenanceId().equalsIgnoreCase(maintenanceId)) {
                r.setStatus("In Progress");
                if (technician != null && !technician.trim().isEmpty()) {
                    r.setTechnicianInCharge(technician);
                }
                return dao.updateMaintenanceRecord(r);
            }
        }
        return false;
    }

    public boolean resolveMaintenance(String maintenanceId, String actionTaken, String technician, Double cost) {
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if (r.getMaintenanceId().equalsIgnoreCase(maintenanceId)) {
                r.setStatus("Resolved");
                r.setResolvedDate(new Date());
                if (actionTaken != null && !actionTaken.trim().isEmpty()) {
                    r.setActionTaken(actionTaken);
                }
                if (technician != null && !technician.trim().isEmpty()) {
                    r.setTechnicianInCharge(technician);
                }
                if (cost != null && cost >= 0) {
                    r.setEstimatedCost(cost);
                }
                return dao.updateMaintenanceRecord(r);
            }
        }
        return false;
    }

    public boolean scheduleMaintenance(String maintenanceId, Date scheduledDate, String technician) {
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if (r.getMaintenanceId().equalsIgnoreCase(maintenanceId)) {
                r.setStatus("Scheduled");
                r.setScheduledDate(scheduledDate);
                if (technician != null && !technician.trim().isEmpty()) {
                    r.setTechnicianInCharge(technician);
                }
                return dao.updateMaintenanceRecord(r);
            }
        }
        return false;
    }

    public boolean deleteMaintenanceRecord(String maintenanceId) {
        return dao.deleteMaintenanceRecord(maintenanceId);
    }

    // =========================================================
    // PREVENTIVE SCHEDULE TASKS
    // =========================================================

    public List<LabScheduleTask> getAllScheduleTasks() {
        return dao.getAllScheduleTasks();
    }

    public boolean markTaskCompleted(String taskId) {
        return dao.markTaskCompleted(taskId, new Date());
    }

    public boolean addScheduleTask(LabScheduleTask task) {
        return dao.addScheduleTask(task);
    }

    public boolean deleteScheduleTask(String taskId) {
        return dao.deleteScheduleTask(taskId);
    }

    // =========================================================
    // STATISTICAL COUNTERS
    // =========================================================

    public int getTotalLabs() {
        return dao.getAllLabs().size();
    }

    public int getOperationalLabsCount() {
        int count = 0;
        for (LabFacility f : dao.getAllLabs()) {
            if ("Operational".equalsIgnoreCase(f.getStatus())) count++;
        }
        return count;
    }

    public int getTotalWorkstations() {
        int sum = 0;
        for (LabFacility f : dao.getAllLabs()) {
            sum += f.getTotalWorkstations();
        }
        return sum;
    }

    public int getOperationalWorkstations() {
        int sum = 0;
        for (LabFacility f : dao.getAllLabs()) {
            sum += f.getOperationalWorkstations();
        }
        return sum;
    }

    public double getWorkstationHealthPercentage() {
        int total = getTotalWorkstations();
        if (total == 0) return 100.0;
        return ((double) getOperationalWorkstations() / total) * 100.0;
    }

    public int getActiveIssuesCount() {
        int count = 0;
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if ("Pending".equalsIgnoreCase(r.getStatus()) || "In Progress".equalsIgnoreCase(r.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getPendingIssuesCount() {
        int count = 0;
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if ("Pending".equalsIgnoreCase(r.getStatus())) count++;
        }
        return count;
    }

    public int getInProgressIssuesCount() {
        int count = 0;
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if ("In Progress".equalsIgnoreCase(r.getStatus())) count++;
        }
        return count;
    }

    public int getResolvedIssuesCount() {
        int count = 0;
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if ("Resolved".equalsIgnoreCase(r.getStatus())) count++;
        }
        return count;
    }

    public int getCriticalIssuesCount() {
        int count = 0;
        for (LabMaintenanceRecord r : dao.getAllMaintenanceRecords()) {
            if ("Critical".equalsIgnoreCase(r.getPriority()) && !"Resolved".equalsIgnoreCase(r.getStatus())) {
                count++;
            }
        }
        return count;
    }
}
