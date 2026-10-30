package util;

import model.AttendanceScenarioResult;
import model.Undergraduate;

/**
 * AttendanceCalculator Utility Class
 * Implements mathematical logic and policy evaluation for:
 * - FR-ATT-03 (Multi-Track Attendance Calculation)
 * - FR-ATT-04 (Mandatory Five-Scenario Evaluation as specified in ICT2132 Guidelines)
 */
public class AttendanceCalculator {

    public static final double ELIGIBILITY_THRESHOLD = 80.0;
    public static final int STANDARD_TOTAL_SESSIONS = 15;

    /**
     * Calculate raw attendance percentage
     */
    public static double calculatePercentage(int attended, int totalSessions) {
        if (totalSessions <= 0) return 0.0;
        double pct = ((double) attended / totalSessions) * 100.0;
        return Math.round(pct * 10.0) / 10.0;
    }

    /**
     * Calculate adjusted attendance percentage taking into account approved medicals
     * Formula per SRS Section 3.3.1: ((Attended + Approved Medicals) / Total Sessions) * 100
     */
    public static double calculateAdjustedPercentage(int attended, int approvedMedicals, int totalSessions) {
        if (totalSessions <= 0) return 0.0;
        int effective = Math.min(attended + approvedMedicals, totalSessions);
        double pct = ((double) effective / totalSessions) * 100.0;
        return Math.round(pct * 10.0) / 10.0;
    }

    /**
     * Evaluate a student against the 5 Mandatory Attendance Scenarios
     */
    public static AttendanceScenarioResult evaluateScenario(Undergraduate student, int totalConducted, 
                                                            int attendedCount, int approvedMedicals, 
                                                            boolean hasSubmittedMedical) {
        if (totalConducted <= 0) {
            totalConducted = STANDARD_TOTAL_SESSIONS;
        }

        double raw = calculatePercentage(attendedCount, totalConducted);
        double adjusted = calculateAdjustedPercentage(attendedCount, approvedMedicals, totalConducted);

        String code;
        String desc;
        String status;
        boolean eligible;

        if (raw > ELIGIBILITY_THRESHOLD && !hasSubmittedMedical) {
            // SC-01: Raw > 80%, No Medical
            code = "SC-01";
            desc = "Attended > 80% without requiring medical concession.";
            status = "ELIGIBLE";
            eligible = true;
        } else if (Math.abs(raw - ELIGIBILITY_THRESHOLD) < 0.001 && !hasSubmittedMedical) {
            // SC-02: Raw == 80%, No Medical
            code = "SC-02";
            desc = "Attended exactly 80.0% (12/15 sessions). Minimum threshold met.";
            status = "ELIGIBLE";
            eligible = true;
        } else if (raw < ELIGIBILITY_THRESHOLD && !hasSubmittedMedical) {
            // SC-03: Raw < 80%, No Medical
            code = "SC-03";
            desc = "Attended < 80.0% and no approved medical. Barred from examination.";
            status = "INELIGIBLE (BARRED)";
            eligible = false;
        } else if (raw >= ELIGIBILITY_THRESHOLD && hasSubmittedMedical && approvedMedicals > 0) {
            // SC-04: Raw >= 80%, Medical Approved
            code = "SC-04";
            desc = "Satisfies threshold outright; medical certificate logged for audit.";
            status = "ELIGIBLE";
            eligible = true;
        } else {
            // SC-05: Raw < 80%, Medical Submitted (Approved vs Rejected)
            code = "SC-05";
            if (approvedMedicals > 0 && adjusted >= ELIGIBILITY_THRESHOLD) {
                desc = "Raw < 80%, but adjusted >= 80.0% with approved medicals. Concession granted.";
                status = "ELIGIBLE (MEDICAL)";
                eligible = true;
            } else {
                desc = "Adjusted attendance is below 80.0% despite medical evaluation. Barred.";
                status = "INELIGIBLE (BARRED)";
                eligible = false;
            }
        }

        return new AttendanceScenarioResult(
                student, totalConducted, attendedCount, raw,
                approvedMedicals, adjusted, code, desc, status, eligible
        );
    }
}
