package model;

/**
 * AttendanceScenarioResult Model Class
 * Represents evaluation of an undergraduate under FR-ATT-03 and the 5 Mandatory Scenarios (FR-ATT-04)
 */
public class AttendanceScenarioResult {
    private Undergraduate student;
    private int totalConducted;     // Out of 15 scheduled sessions per semester track
    private int attendedCount;
    private double rawPercentage;
    private int approvedMedicals;
    private double adjustedPercentage;
    private String scenarioCode;    // "SC-01", "SC-02", "SC-03", "SC-04", "SC-05"
    private String scenarioDescription;
    private String eligibilityStatus; // "ELIGIBLE", "ELIGIBLE WITH MEDICAL", "INELIGIBLE (BARRED)"
    private boolean eligible;

    public AttendanceScenarioResult() {
    }

    public AttendanceScenarioResult(Undergraduate student, int totalConducted, int attendedCount, 
                                    double rawPercentage, int approvedMedicals, double adjustedPercentage, 
                                    String scenarioCode, String scenarioDescription, 
                                    String eligibilityStatus, boolean eligible) {
        this.student = student;
        this.totalConducted = totalConducted;
        this.attendedCount = attendedCount;
        this.rawPercentage = rawPercentage;
        this.approvedMedicals = approvedMedicals;
        this.adjustedPercentage = adjustedPercentage;
        this.scenarioCode = scenarioCode;
        this.scenarioDescription = scenarioDescription;
        this.eligibilityStatus = eligibilityStatus;
        this.eligible = eligible;
    }

    public Undergraduate getStudent() {
        return student;
    }

    public void setStudent(Undergraduate student) {
        this.student = student;
    }

    public int getTotalConducted() {
        return totalConducted;
    }

    public void setTotalConducted(int totalConducted) {
        this.totalConducted = totalConducted;
    }

    public int getAttendedCount() {
        return attendedCount;
    }

    public void setAttendedCount(int attendedCount) {
        this.attendedCount = attendedCount;
    }

    public double getRawPercentage() {
        return rawPercentage;
    }

    public void setRawPercentage(double rawPercentage) {
        this.rawPercentage = rawPercentage;
    }

    public int getApprovedMedicals() {
        return approvedMedicals;
    }

    public void setApprovedMedicals(int approvedMedicals) {
        this.approvedMedicals = approvedMedicals;
    }

    public double getAdjustedPercentage() {
        return adjustedPercentage;
    }

    public void setAdjustedPercentage(double adjustedPercentage) {
        this.adjustedPercentage = adjustedPercentage;
    }

    public String getScenarioCode() {
        return scenarioCode;
    }

    public void setScenarioCode(String scenarioCode) {
        this.scenarioCode = scenarioCode;
    }

    public String getScenarioDescription() {
        return scenarioDescription;
    }

    public void setScenarioDescription(String scenarioDescription) {
        this.scenarioDescription = scenarioDescription;
    }

    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }
}
