package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a laboratory equipment maintenance record or fault ticket.
 */
public class LabMaintenanceRecord {

    private String maintenanceId;
    private String labId;
    private String labName;
    private String equipmentCode;
    private String equipmentType; // Workstation / PC, Network Equipment, Multimedia / Projector, Power / UPS, Software / OS, Other
    private String issueDescription;
    private String maintenanceType; // Hardware Repair, Preventive Servicing, Software Upgrade, Emergency Replacement, Calibration
    private String priority; // Critical, High, Medium, Low
    private String status; // Pending, In Progress, Resolved, Scheduled
    private Date reportedDate;
    private Date scheduledDate;
    private Date resolvedDate;
    private String technicianInCharge;
    private String actionTaken;
    private double estimatedCost;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public LabMaintenanceRecord() {
    }

    public LabMaintenanceRecord(String maintenanceId, String labId, String labName,
                                String equipmentCode, String equipmentType, String issueDescription,
                                String maintenanceType, String priority, String status,
                                Date reportedDate, Date scheduledDate, Date resolvedDate,
                                String technicianInCharge, String actionTaken, double estimatedCost) {
        this.maintenanceId = maintenanceId;
        this.labId = labId;
        this.labName = labName;
        this.equipmentCode = equipmentCode;
        this.equipmentType = equipmentType;
        this.issueDescription = issueDescription;
        this.maintenanceType = maintenanceType;
        this.priority = priority;
        this.status = status;
        this.reportedDate = reportedDate;
        this.scheduledDate = scheduledDate;
        this.resolvedDate = resolvedDate;
        this.technicianInCharge = technicianInCharge;
        this.actionTaken = actionTaken;
        this.estimatedCost = estimatedCost;
    }

    public String getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(String maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getReportedDate() {
        return reportedDate;
    }

    public String getFormattedReportedDate() {
        return reportedDate != null ? DATE_FORMAT.format(reportedDate) : "-";
    }

    public void setReportedDate(Date reportedDate) {
        this.reportedDate = reportedDate;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public String getFormattedScheduledDate() {
        return scheduledDate != null ? DATE_FORMAT.format(scheduledDate) : "-";
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Date getResolvedDate() {
        return resolvedDate;
    }

    public String getFormattedResolvedDate() {
        return resolvedDate != null ? DATE_FORMAT.format(resolvedDate) : "-";
    }

    public void setResolvedDate(Date resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public String getTechnicianInCharge() {
        return technicianInCharge;
    }

    public void setTechnicianInCharge(String technicianInCharge) {
        this.technicianInCharge = technicianInCharge;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
}
