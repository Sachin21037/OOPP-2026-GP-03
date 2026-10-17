package model;

/**
 * LabFacility Model Class
 * Represents a laboratory facility managed by a Technical Officer.
 * Package: model
 * Mapped to FAMS lab infrastructure and timetables.
 */
public class LabFacility {

    private String labId;
    private String labName;
    private String location;
    private String department;
    private int totalWorkstations;
    private int operationalWorkstations;
    private String status; // Operational, Maintenance Required, Under Maintenance, Occupied
    private String inChargeOfficerId;
    private String inChargeOfficerName;
    private String equipmentSummary;

    public LabFacility() {
    }

    public LabFacility(String labId, String labName, String location, String department,
                       int totalWorkstations, int operationalWorkstations, String status,
                       String inChargeOfficerId, String inChargeOfficerName, String equipmentSummary) {
        this.labId = labId;
        this.labName = labName;
        this.location = location;
        this.department = department;
        this.totalWorkstations = totalWorkstations;
        this.operationalWorkstations = operationalWorkstations;
        this.status = status;
        this.inChargeOfficerId = inChargeOfficerId;
        this.inChargeOfficerName = inChargeOfficerName;
        this.equipmentSummary = equipmentSummary;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getTotalWorkstations() {
        return totalWorkstations;
    }

    public void setTotalWorkstations(int totalWorkstations) {
        this.totalWorkstations = totalWorkstations;
    }

    public int getOperationalWorkstations() {
        return operationalWorkstations;
    }

    public void setOperationalWorkstations(int operationalWorkstations) {
        this.operationalWorkstations = operationalWorkstations;
    }

    public int getFaultyWorkstations() {
        return Math.max(0, totalWorkstations - operationalWorkstations);
    }

    public double getOperationalPercentage() {
        if (totalWorkstations <= 0) return 100.0;
        return ((double) operationalWorkstations / totalWorkstations) * 100.0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInChargeOfficerId() {
        return inChargeOfficerId;
    }

    public void setInChargeOfficerId(String inChargeOfficerId) {
        this.inChargeOfficerId = inChargeOfficerId;
    }

    public String getInChargeOfficerName() {
        return inChargeOfficerName;
    }

    public void setInChargeOfficerName(String inChargeOfficerName) {
        this.inChargeOfficerName = inChargeOfficerName;
    }

    public String getEquipmentSummary() {
        return equipmentSummary;
    }

    public void setEquipmentSummary(String equipmentSummary) {
        this.equipmentSummary = equipmentSummary;
    }

    @Override
    public String toString() {
        return labName + " (" + labId + ")";
    }
}
