package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LabScheduleTask Model Class
 * Represents a routine preventive maintenance task or schedule.
 * Package: model
 */
public class LabScheduleTask {

    private String taskId;
    private String labId;
    private String labName;
    private String taskName;
    private String frequency; // Daily, Weekly, Bi-Weekly, Monthly, Semester-End
    private Date lastCompletedDate;
    private Date nextDueDate;
    private String assignedOfficer;
    private String status; // Completed, Due Soon, Overdue
    private String checklistDetails;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public LabScheduleTask() {
    }

    public LabScheduleTask(String taskId, String labId, String labName, String taskName,
                           String frequency, Date lastCompletedDate, Date nextDueDate,
                           String assignedOfficer, String status, String checklistDetails) {
        this.taskId = taskId;
        this.labId = labId;
        this.labName = labName;
        this.taskName = taskName;
        this.frequency = frequency;
        this.lastCompletedDate = lastCompletedDate;
        this.nextDueDate = nextDueDate;
        this.assignedOfficer = assignedOfficer;
        this.status = status;
        this.checklistDetails = checklistDetails;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Date getLastCompletedDate() {
        return lastCompletedDate;
    }

    public String getFormattedLastCompletedDate() {
        return lastCompletedDate != null ? DATE_FORMAT.format(lastCompletedDate) : "Never";
    }

    public void setLastCompletedDate(Date lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    public Date getNextDueDate() {
        return nextDueDate;
    }

    public String getFormattedNextDueDate() {
        return nextDueDate != null ? DATE_FORMAT.format(nextDueDate) : "-";
    }

    public void setNextDueDate(Date nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getAssignedOfficer() {
        return assignedOfficer;
    }

    public void setAssignedOfficer(String assignedOfficer) {
        this.assignedOfficer = assignedOfficer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChecklistDetails() {
        return checklistDetails;
    }

    public void setChecklistDetails(String checklistDetails) {
        this.checklistDetails = checklistDetails;
    }
}
