package dao;

import model.LabFacility;
import model.LabMaintenanceRecord;
import model.LabScheduleTask;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Data access object for laboratory facilities, maintenance tickets, and preventive schedules.
 */
public class LabMaintenanceDAO {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // In-memory fallback stores
    private static final List<LabFacility> mockLabs = new ArrayList<>();
    private static final List<LabMaintenanceRecord> mockRecords = new ArrayList<>();
    private static final List<LabScheduleTask> mockTasks = new ArrayList<>();

    private static boolean dbInitialized = false;

    static {
        seedMockData();
    }

    public LabMaintenanceDAO() {
        initDatabaseTablesIfConnected();
    }

    private static void seedMockData() {
        try {
            // 1. Seed Facilities
            mockLabs.add(new LabFacility(
                    "LAB001", "Computer Lab 01", "Level I - Block A (Room 102)",
                    "Information and Communication Technology", 50, 48,
                    "Operational", "TO001", "Ruwan Jayasinghe",
                    "50 Intel Core i7 Workstations, 1 Interactive Projector, 2 Cisco Gigabit Switches, 10kVA UPS"
            ));

            mockLabs.add(new LabFacility(
                    "LAB002", "Software Engineering Lab 02", "Level II - Block A (Room 204)",
                    "Information and Communication Technology", 45, 45,
                    "Operational", "TO001", "Ruwan Jayasinghe",
                    "45 Dell OptiPlex Workstations (16GB RAM), Dual Display Instructor Desk, Smart Screen"
            ));

            mockLabs.add(new LabFacility(
                    "LAB003", "Networking & Systems Lab 03", "Level III - Block B (Room 305)",
                    "Information and Communication Technology", 40, 38,
                    "Maintenance Required", "TO001", "Ruwan Jayasinghe",
                    "40 HP Workstations, 8 Cisco 2901 Routers, 6 Cisco 2960 Switches, Patch Panels, Fluke Cable Testers"
            ));

            mockLabs.add(new LabFacility(
                    "LAB004", "Embedded Systems & Hardware Lab 04", "Level II - Block C (Room 210)",
                    "Information and Communication Technology", 35, 35,
                    "Operational", "TO001", "Ruwan Jayasinghe",
                    "35 Bench Stations, Digital Storage Oscilloscopes, Microcontroller Kits (ESP32/STM32), DC Power Supplies"
            ));

            // 2. Seed Maintenance Records
            mockRecords.add(new LabMaintenanceRecord(
                    "MNT001", "LAB003", "Networking & Systems Lab 03", "ROUTER-03-R2",
                    "Network Equipment", "Gigabit port 3-4 packet loss; requires Cisco IOS flash re-image and transceiver replacement.",
                    "Hardware Repair", "High", "In Progress",
                    SDF.parse("2026-03-24"), SDF.parse("2026-03-25"), null,
                    "TO001 - Ruwan Jayasinghe", "Diagnostic completed. Replacement SFP module ordered from stores.", 14500.00
            ));

            mockRecords.add(new LabMaintenanceRecord(
                    "MNT002", "LAB001", "Computer Lab 01", "WS-01-14",
                    "Workstation / PC", "Workstation reboots intermittently during practical exams. Probable faulty RAM or PSU issue.",
                    "Hardware Repair", "Medium", "Pending",
                    SDF.parse("2026-03-26"), SDF.parse("2026-03-28"), null,
                    "TO001 - Ruwan Jayasinghe", "Logged for inspection during maintenance slot.", 4500.00
            ));

            mockRecords.add(new LabMaintenanceRecord(
                    "MNT003", "LAB002", "Software Engineering Lab 02", "PRJ-02",
                    "Multimedia / Projector", "Lamp replacement warning active. Brightness is degraded below acceptable contrast for lectures.",
                    "Preventive Servicing", "Medium", "Scheduled",
                    SDF.parse("2026-03-20"), SDF.parse("2026-03-29"), null,
                    "TO001 - Ruwan Jayasinghe", "Original Sony projector bulb arrived; installation scheduled Saturday morning.", 8200.00
            ));

            mockRecords.add(new LabMaintenanceRecord(
                    "MNT004", "LAB003", "Networking & Systems Lab 03", "SWITCH-03-SW01",
                    "Network Equipment", "Core lab management console unreachable over SSH/Telnet; port 24 uplink amber LED.",
                    "Emergency Replacement", "Critical", "Pending",
                    SDF.parse("2026-03-27"), SDF.parse("2026-03-27"), null,
                    "TO001 - Ruwan Jayasinghe", "Awaiting physical console cable access before next student session.", 0.00
            ));

            mockRecords.add(new LabMaintenanceRecord(
                    "MNT005", "LAB001", "Computer Lab 01", "UPS-01-U2",
                    "Power / UPS", "Secondary 12V 7Ah battery bank exhausted; backup runtime had fallen under 3 minutes.",
                    "Hardware Repair", "High", "Resolved",
                    SDF.parse("2026-03-10"), SDF.parse("2026-03-12"), SDF.parse("2026-03-13"),
                    "TO001 - Ruwan Jayasinghe", "Replaced 4 internal lead-acid battery packs and calibrated runtime to 22 mins.", 18900.00
            ));

            mockRecords.add(new LabMaintenanceRecord(
                    "MNT006", "LAB002", "Software Engineering Lab 02", "LAB-SOFT-02",
                    "Software / OS", "Faculty semester upgrade: Installed Eclipse 2024, JDK 21 LTS, and Git across all 45 student machines.",
                    "Software Upgrade", "Low", "Resolved",
                    SDF.parse("2026-03-01"), SDF.parse("2026-03-02"), SDF.parse("2026-03-03"),
                    "TO001 - Ruwan Jayasinghe", "Automated deployment script executed via network share. All machines verified.", 0.00
            ));

            // 3. Seed Preventive Schedules
            mockTasks.add(new LabScheduleTask(
                    "TSK001", "LAB001", "Computer Lab 01",
                    "Weekly Workstation Clean & Cable Inspection", "Weekly",
                    SDF.parse("2026-03-22"), SDF.parse("2026-03-29"),
                    "TO001", "Due Soon",
                    "Blow dust from CPU fans, check keyboard/mouse cables, inspect power strip surge suppressors."
            ));

            mockTasks.add(new LabScheduleTask(
                    "TSK002", "LAB002", "Software Engineering Lab 02",
                    "Monthly OS & Windows Defender Antivirus Definition Updates", "Monthly",
                    SDF.parse("2026-03-05"), SDF.parse("2026-04-05"),
                    "TO001", "Completed",
                    "Sync Windows updates, verify offline deep scan status, update Git & browser security patches."
            ));

            mockTasks.add(new LabScheduleTask(
                    "TSK003", "LAB003", "Networking & Systems Lab 03",
                    "Cisco Switch/Router Firmware & VLAN Port Verification", "Bi-Weekly",
                    SDF.parse("2026-03-14"), SDF.parse("2026-03-28"),
                    "TO001", "Due Soon",
                    "Test VLAN tagging across trunk links, check router running-configs against lab backup repository."
            ));

            mockTasks.add(new LabScheduleTask(
                    "TSK004", "LAB004", "Embedded Systems & Hardware Lab 04",
                    "Oscilloscope & DC Power Supply Calibration", "Monthly",
                    SDF.parse("2026-03-01"), SDF.parse("2026-04-01"),
                    "TO001", "Completed",
                    "Zero-calibrate probe channels, check voltage regulation under 5A load, tag calibration labels."
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes database tables in MySQL if connected.
     */
    private void initDatabaseTablesIfConnected() {
        if (dbInitialized) return;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            Statement stmt = conn.createStatement();

            // Table 1: lab_facilities
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `lab_facilities` (" +
                    "`lab_id` varchar(10) NOT NULL PRIMARY KEY, " +
                    "`lab_name` varchar(100) NOT NULL, " +
                    "`location` varchar(100) NOT NULL, " +
                    "`department` varchar(100) DEFAULT 'Information and Communication Technology', " +
                    "`total_workstations` int DEFAULT 40, " +
                    "`operational_workstations` int DEFAULT 40, " +
                    "`status` varchar(30) DEFAULT 'Operational', " +
                    "`in_charge_officer_id` varchar(10) DEFAULT 'TO001', " +
                    "`in_charge_officer_name` varchar(100) DEFAULT 'Ruwan Jayasinghe', " +
                    "`equipment_summary` text " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // Table 2: lab_maintenance_records
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `lab_maintenance_records` (" +
                    "`maintenance_id` varchar(10) NOT NULL PRIMARY KEY, " +
                    "`lab_id` varchar(10) NOT NULL, " +
                    "`lab_name` varchar(100) NOT NULL, " +
                    "`equipment_code` varchar(50) NOT NULL, " +
                    "`equipment_type` varchar(50) NOT NULL, " +
                    "`issue_description` text NOT NULL, " +
                    "`maintenance_type` varchar(50) NOT NULL, " +
                    "`priority` varchar(20) NOT NULL, " +
                    "`status` varchar(20) NOT NULL, " +
                    "`reported_date` date NOT NULL, " +
                    "`scheduled_date` date DEFAULT NULL, " +
                    "`resolved_date` date DEFAULT NULL, " +
                    "`technician_in_charge` varchar(100) DEFAULT NULL, " +
                    "`action_taken` text DEFAULT NULL, " +
                    "`estimated_cost` decimal(10,2) DEFAULT 0.00 " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // Table 3: lab_schedule_tasks
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `lab_schedule_tasks` (" +
                    "`task_id` varchar(10) NOT NULL PRIMARY KEY, " +
                    "`lab_id` varchar(10) NOT NULL, " +
                    "`lab_name` varchar(100) NOT NULL, " +
                    "`task_name` varchar(150) NOT NULL, " +
                    "`frequency` varchar(30) NOT NULL, " +
                    "`last_completed_date` date DEFAULT NULL, " +
                    "`next_due_date` date DEFAULT NULL, " +
                    "`assigned_officer` varchar(50) DEFAULT 'TO001', " +
                    "`status` varchar(20) DEFAULT 'Due Soon', " +
                    "`checklist_details` text " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // Seed if empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `lab_facilities`");
            if (rs.next() && rs.getInt(1) == 0) {
                for (LabFacility f : mockLabs) {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO `lab_facilities` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    ps.setString(1, f.getLabId());
                    ps.setString(2, f.getLabName());
                    ps.setString(3, f.getLocation());
                    ps.setString(4, f.getDepartment());
                    ps.setInt(5, f.getTotalWorkstations());
                    ps.setInt(6, f.getOperationalWorkstations());
                    ps.setString(7, f.getStatus());
                    ps.setString(8, f.getInChargeOfficerId());
                    ps.setString(9, f.getInChargeOfficerName());
                    ps.setString(10, f.getEquipmentSummary());
                    ps.executeUpdate();
                    ps.close();
                }

                for (LabMaintenanceRecord r : mockRecords) {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO `lab_maintenance_records` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    ps.setString(1, r.getMaintenanceId());
                    ps.setString(2, r.getLabId());
                    ps.setString(3, r.getLabName());
                    ps.setString(4, r.getEquipmentCode());
                    ps.setString(5, r.getEquipmentType());
                    ps.setString(6, r.getIssueDescription());
                    ps.setString(7, r.getMaintenanceType());
                    ps.setString(8, r.getPriority());
                    ps.setString(9, r.getStatus());
                    ps.setDate(10, r.getReportedDate() != null ? new java.sql.Date(r.getReportedDate().getTime()) : null);
                    ps.setDate(11, r.getScheduledDate() != null ? new java.sql.Date(r.getScheduledDate().getTime()) : null);
                    ps.setDate(12, r.getResolvedDate() != null ? new java.sql.Date(r.getResolvedDate().getTime()) : null);
                    ps.setString(13, r.getTechnicianInCharge());
                    ps.setString(14, r.getActionTaken());
                    ps.setDouble(15, r.getEstimatedCost());
                    ps.executeUpdate();
                    ps.close();
                }

                for (LabScheduleTask t : mockTasks) {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO `lab_schedule_tasks` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    ps.setString(1, t.getTaskId());
                    ps.setString(2, t.getLabId());
                    ps.setString(3, t.getLabName());
                    ps.setString(4, t.getTaskName());
                    ps.setString(5, t.getFrequency());
                    ps.setDate(6, t.getLastCompletedDate() != null ? new java.sql.Date(t.getLastCompletedDate().getTime()) : null);
                    ps.setDate(7, t.getNextDueDate() != null ? new java.sql.Date(t.getNextDueDate().getTime()) : null);
                    ps.setString(8, t.getAssignedOfficer());
                    ps.setString(9, t.getStatus());
                    ps.setString(10, t.getChecklistDetails());
                    ps.executeUpdate();
                    ps.close();
                }
            }

            dbInitialized = true;
        } catch (SQLException e) {
            // Silently fallback to mock store if database user lacks permissions
            dbInitialized = true;
        }
    }

    // =========================================================
    // LAB FACILITIES METHODS
    // =========================================================

    public List<LabFacility> getAllLabs() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM `lab_facilities` ORDER BY `lab_id` ASC");
                List<LabFacility> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new LabFacility(
                            rs.getString("lab_id"),
                            rs.getString("lab_name"),
                            rs.getString("location"),
                            rs.getString("department"),
                            rs.getInt("total_workstations"),
                            rs.getInt("operational_workstations"),
                            rs.getString("status"),
                            rs.getString("in_charge_officer_id"),
                            rs.getString("in_charge_officer_name"),
                            rs.getString("equipment_summary")
                    ));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {
            }
        }
        return new ArrayList<>(mockLabs);
    }

    public LabFacility getLabById(String labId) {
        for (LabFacility f : getAllLabs()) {
            if (f.getLabId().equalsIgnoreCase(labId)) {
                return f;
            }
        }
        return null;
    }

    public boolean updateLab(LabFacility lab) {
        // Update in-memory
        for (int i = 0; i < mockLabs.size(); i++) {
            if (mockLabs.get(i).getLabId().equalsIgnoreCase(lab.getLabId())) {
                mockLabs.set(i, lab);
                break;
            }
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE `lab_facilities` SET `lab_name` = ?, `location` = ?, `department` = ?, " +
                        "`total_workstations` = ?, `operational_workstations` = ?, `status` = ?, " +
                        "`in_charge_officer_id` = ?, `in_charge_officer_name` = ?, `equipment_summary` = ? " +
                        "WHERE `lab_id` = ?");
                ps.setString(1, lab.getLabName());
                ps.setString(2, lab.getLocation());
                ps.setString(3, lab.getDepartment());
                ps.setInt(4, lab.getTotalWorkstations());
                ps.setInt(5, lab.getOperationalWorkstations());
                ps.setString(6, lab.getStatus());
                ps.setString(7, lab.getInChargeOfficerId());
                ps.setString(8, lab.getInChargeOfficerName());
                ps.setString(9, lab.getEquipmentSummary());
                ps.setString(10, lab.getLabId());
                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException ignored) {
            }
        }
        return true;
    }

    // =========================================================
    // MAINTENANCE RECORDS METHODS
    // =========================================================

    public List<LabMaintenanceRecord> getAllMaintenanceRecords() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM `lab_maintenance_records` ORDER BY `reported_date` DESC, `maintenance_id` DESC");
                List<LabMaintenanceRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new LabMaintenanceRecord(
                            rs.getString("maintenance_id"),
                            rs.getString("lab_id"),
                            rs.getString("lab_name"),
                            rs.getString("equipment_code"),
                            rs.getString("equipment_type"),
                            rs.getString("issue_description"),
                            rs.getString("maintenance_type"),
                            rs.getString("priority"),
                            rs.getString("status"),
                            rs.getDate("reported_date"),
                            rs.getDate("scheduled_date"),
                            rs.getDate("resolved_date"),
                            rs.getString("technician_in_charge"),
                            rs.getString("action_taken"),
                            rs.getDouble("estimated_cost")
                    ));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {
            }
        }
        return new ArrayList<>(mockRecords);
    }

    public synchronized String getNextMaintenanceId() {
        int maxId = 0;
        for (LabMaintenanceRecord r : getAllMaintenanceRecords()) {
            if (r.getMaintenanceId() != null && r.getMaintenanceId().startsWith("MNT")) {
                try {
                    int num = Integer.parseInt(r.getMaintenanceId().substring(3));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("MNT%03d", maxId + 1);
    }

    public boolean addMaintenanceRecord(LabMaintenanceRecord record) {
        if (record.getMaintenanceId() == null || record.getMaintenanceId().trim().isEmpty()) {
            record.setMaintenanceId(getNextMaintenanceId());
        }

        mockRecords.add(0, record);

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO `lab_maintenance_records` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, record.getMaintenanceId());
                ps.setString(2, record.getLabId());
                ps.setString(3, record.getLabName());
                ps.setString(4, record.getEquipmentCode());
                ps.setString(5, record.getEquipmentType());
                ps.setString(6, record.getIssueDescription());
                ps.setString(7, record.getMaintenanceType());
                ps.setString(8, record.getPriority());
                ps.setString(9, record.getStatus());
                ps.setDate(10, record.getReportedDate() != null ? new java.sql.Date(record.getReportedDate().getTime()) : null);
                ps.setDate(11, record.getScheduledDate() != null ? new java.sql.Date(record.getScheduledDate().getTime()) : null);
                ps.setDate(12, record.getResolvedDate() != null ? new java.sql.Date(record.getResolvedDate().getTime()) : null);
                ps.setString(13, record.getTechnicianInCharge());
                ps.setString(14, record.getActionTaken());
                ps.setDouble(15, record.getEstimatedCost());
                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException ignored) {
            }
        }
        return true;
    }

    public boolean updateMaintenanceRecord(LabMaintenanceRecord record) {
        for (int i = 0; i < mockRecords.size(); i++) {
            if (mockRecords.get(i).getMaintenanceId().equalsIgnoreCase(record.getMaintenanceId())) {
                mockRecords.set(i, record);
                break;
            }
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE `lab_maintenance_records` SET `lab_id` = ?, `lab_name` = ?, `equipment_code` = ?, " +
                        "`equipment_type` = ?, `issue_description` = ?, `maintenance_type` = ?, `priority` = ?, " +
                        "`status` = ?, `reported_date` = ?, `scheduled_date` = ?, `resolved_date` = ?, " +
                        "`technician_in_charge` = ?, `action_taken` = ?, `estimated_cost` = ? " +
                        "WHERE `maintenance_id` = ?");
                ps.setString(1, record.getLabId());
                ps.setString(2, record.getLabName());
                ps.setString(3, record.getEquipmentCode());
                ps.setString(4, record.getEquipmentType());
                ps.setString(5, record.getIssueDescription());
                ps.setString(6, record.getMaintenanceType());
                ps.setString(7, record.getPriority());
                ps.setString(8, record.getStatus());
                ps.setDate(9, record.getReportedDate() != null ? new java.sql.Date(record.getReportedDate().getTime()) : null);
                ps.setDate(10, record.getScheduledDate() != null ? new java.sql.Date(record.getScheduledDate().getTime()) : null);
                ps.setDate(11, record.getResolvedDate() != null ? new java.sql.Date(record.getResolvedDate().getTime()) : null);
                ps.setString(12, record.getTechnicianInCharge());
                ps.setString(13, record.getActionTaken());
                ps.setDouble(14, record.getEstimatedCost());
                ps.setString(15, record.getMaintenanceId());
                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException ignored) {
            }
        }
        return true;
    }

    public boolean deleteMaintenanceRecord(String maintenanceId) {
        mockRecords.removeIf(r -> r.getMaintenanceId().equalsIgnoreCase(maintenanceId));

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM `lab_maintenance_records` WHERE `maintenance_id` = ?");
                ps.setString(1, maintenanceId);
                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException ignored) {
            }
        }
        return true;
    }

    // =========================================================
    // SCHEDULE TASKS METHODS
    // =========================================================

    public List<LabScheduleTask> getAllScheduleTasks() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM `lab_schedule_tasks` ORDER BY `next_due_date` ASC");
                List<LabScheduleTask> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new LabScheduleTask(
                            rs.getString("task_id"),
                            rs.getString("lab_id"),
                            rs.getString("lab_name"),
                            rs.getString("task_name"),
                            rs.getString("frequency"),
                            rs.getDate("last_completed_date"),
                            rs.getDate("next_due_date"),
                            rs.getString("assigned_officer"),
                            rs.getString("status"),
                            rs.getString("checklist_details")
                    ));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {
            }
        }
        return new ArrayList<>(mockTasks);
    }

    public synchronized String getNextTaskId() {
        int maxId = 0;
        for (LabScheduleTask t : getAllScheduleTasks()) {
            if (t.getTaskId() != null && t.getTaskId().startsWith("TSK")) {
                try {
                    int num = Integer.parseInt(t.getTaskId().substring(3));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("TSK%03d", maxId + 1);
    }

    public boolean addScheduleTask(LabScheduleTask task) {
        if (task.getTaskId() == null || task.getTaskId().trim().isEmpty()) {
            task.setTaskId(getNextTaskId());
        }

        mockTasks.add(task);

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO `lab_schedule_tasks` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, task.getTaskId());
                ps.setString(2, task.getLabId());
                ps.setString(3, task.getLabName());
                ps.setString(4, task.getTaskName());
                ps.setString(5, task.getFrequency());
                ps.setDate(6, task.getLastCompletedDate() != null ? new java.sql.Date(task.getLastCompletedDate().getTime()) : null);
                ps.setDate(7, task.getNextDueDate() != null ? new java.sql.Date(task.getNextDueDate().getTime()) : null);
                ps.setString(8, task.getAssignedOfficer());
                ps.setString(9, task.getStatus());
                ps.setString(10, task.getChecklistDetails());
                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException ignored) {
            }
        }
        return true;
    }

    public boolean markTaskCompleted(String taskId, Date completedDate) {
        for (LabScheduleTask t : mockTasks) {
            if (t.getTaskId().equalsIgnoreCase(taskId)) {
                t.setLastCompletedDate(completedDate);
                t.setStatus("Completed");
                // Calculate next due date according to frequency
                Calendar cal = Calendar.getInstance();
                cal.setTime(completedDate);
                if ("Daily".equalsIgnoreCase(t.getFrequency())) cal.add(Calendar.DAY_OF_YEAR, 1);
                else if ("Weekly".equalsIgnoreCase(t.getFrequency())) cal.add(Calendar.WEEK_OF_YEAR, 1);
                else if ("Bi-Weekly".equalsIgnoreCase(t.getFrequency())) cal.add(Calendar.WEEK_OF_YEAR, 2);
                else if ("Monthly".equalsIgnoreCase(t.getFrequency())) cal.add(Calendar.MONTH, 1);
                else cal.add(Calendar.MONTH, 4);
                t.setNextDueDate(cal.getTime());
                break;
            }
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE `lab_schedule_tasks` SET `last_completed_date` = ?, `status` = 'Completed' WHERE `task_id` = ?");
                ps.setDate(1, new java.sql.Date(completedDate.getTime()));
                ps.setString(2, taskId);
                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException ignored) {
            }
        }
        return true;
    }

    public boolean deleteScheduleTask(String taskId) {
        mockTasks.removeIf(t -> t.getTaskId().equalsIgnoreCase(taskId));

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM `lab_schedule_tasks` WHERE `task_id` = ?");
                ps.setString(1, taskId);
                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException ignored) {
            }
        }
        return true;
    }
}
