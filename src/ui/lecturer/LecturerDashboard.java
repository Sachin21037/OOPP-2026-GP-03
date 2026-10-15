package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerDashboard extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color RED = LecturerTheme.DANGER_RED;

    private static final Font BODY =
            new Font("Arial", Font.PLAIN, 13);

    // Static active lecturer ID to support session persistence across pages
    private static String activeLecturerId = null;

    // Instance lecturer ID for this dashboard
    private final String lecturerId;
    private String lecturerDisplayName = "Lecturer";
    private String myCoursesValue = "-";
    private String studentsValue = "-";
    private String assignmentsValue = "-";
    private String pendingMarksValue = "-";
    private String coursesCaption = "Assigned courses";
    private String studentsCaption = "Across your courses";
    private String assignmentsCaption = "Created this semester";
    private String pendingMarksCaption = "Need your attention";
    private String welcomeSubtitle = "Manage your courses, students, attendance and academic activities.";
    private final List<ActivityItem> activityItems = new ArrayList<>();

    private static class ActivityItem {
        final String text;
        final String category;
        final Color accent;

        ActivityItem(String text, String category, Color accent) {
            this.text = text;
            this.category = category;
            this.accent = accent;
        }
    }

    /**
     * Default constructor: uses currently active lecturer ID or JVM property.
     */
    public LecturerDashboard() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the lecturer ID from the database (e.g., "LEC001")
     */
    public LecturerDashboard(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            activeLecturerId = this.lecturerId;
        }

        // Fetch live database metrics before building UI
        loadDashboardData();

        setTitle("Faculty Academic Management System - Lecturer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createDashboardContent(), BorderLayout.CENTER);
    }

    public static void setActiveLecturerId(String id) {
        activeLecturerId = (id != null && !id.trim().isEmpty()) ? id.trim() : null;
    }

    public static String getActiveLecturerId() {
        return activeLecturerId;
    }

    private static String resolveActiveLecturerId() {
        if (activeLecturerId != null && !activeLecturerId.trim().isEmpty()) {
            return activeLecturerId.trim();
        }
        String sysProp = System.getProperty("fams.lecturer.id");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return null;
    }

    /**
     * Loads live data and statistics from the MySQL 'fams' database.
     * Handles exceptions safely without crashing the UI.
     */
    private void loadDashboardData() {
        if (lecturerId == null) {
            coursesCaption = "Assigned courses (No lecturer ID set)";
            studentsCaption = "Across courses (No lecturer ID set)";
            assignmentsCaption = "Assignments (No lecturer ID set)";
            pendingMarksCaption = "Pending marks (No lecturer ID set)";
            welcomeSubtitle = "Manage your courses, students, attendance and academic activities. (Pass a lecturer ID to view personal data)";
            loadPublicNotices();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Fetch Lecturer Name and Designation
            String nameQuery = "SELECT u.first_name, u.last_name, l.designation " +
                    "FROM lecturers l " +
                    "JOIN users u ON l.lecturer_id = u.user_id " +
                    "WHERE l.lecturer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(nameQuery)) {
                ps.setString(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String first = rs.getString("first_name");
                        String last = rs.getString("last_name");
                        String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                        if (!fullName.isEmpty()) {
                            lecturerDisplayName = fullName;
                            LecturerTheme.setLecturerDisplayName(lecturerId, fullName);
                        }
                    } else {
                        welcomeSubtitle = "Lecturer ID '" + lecturerId + "' not found in database.";
                    }
                }
            }

            // 2. Count Assigned Courses (offerings assigned to this lecturer)
            String coursesQuery = "SELECT COUNT(DISTINCT cl.offering_id) " +
                    "FROM course_lecturers cl " +
                    "WHERE cl.lecturer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(coursesQuery)) {
                ps.setString(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        myCoursesValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 3. Count Distinct Students Enrolled in Lecturer's Course Offerings
            String studentsQuery = "SELECT COUNT(DISTINCT e.student_id) " +
                    "FROM course_lecturers cl " +
                    "JOIN enrollments e ON cl.offering_id = e.offering_id " +
                    "WHERE cl.lecturer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(studentsQuery)) {
                ps.setString(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentsValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 4. Count Assignments Created for Lecturer's Offerings
            String assignmentsQuery = "SELECT COUNT(*) " +
                    "FROM assessments a " +
                    "JOIN course_lecturers cl ON a.offering_id = cl.offering_id " +
                    "WHERE cl.lecturer_id = ? AND a.assessment_type = 'ASSIGNMENT'";
            try (PreparedStatement ps = conn.prepareStatement(assignmentsQuery)) {
                ps.setString(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        assignmentsValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 5. Count Pending Marks (enrolled students who do not have a mark recorded in marks table)
            String pendingMarksQuery = "SELECT COUNT(*) " +
                    "FROM assessments a " +
                    "JOIN course_lecturers cl ON a.offering_id = cl.offering_id " +
                    "JOIN enrollments e ON a.offering_id = e.offering_id " +
                    "LEFT JOIN marks m ON a.assessment_id = m.assessment_id AND e.student_id = m.student_id " +
                    "WHERE cl.lecturer_id = ? AND m.mark_id IS NULL";
            try (PreparedStatement ps = conn.prepareStatement(pendingMarksQuery)) {
                ps.setString(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pendingMarksValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 6. Load Recent Lecturer Activities
            loadLecturerActivities(conn);

        } catch (SQLException e) {
            System.err.println("[LecturerDashboard] Database error while loading statistics: " + e.getMessage());
            coursesCaption = "Assigned courses (DB Error)";
            studentsCaption = "Across courses (DB Error)";
            assignmentsCaption = "Assignments (DB Error)";
            pendingMarksCaption = "Pending marks (DB Error)";
            welcomeSubtitle = "Database connection error: " + e.getMessage();
            if (activityItems.isEmpty()) {
                activityItems.add(new ActivityItem("Database connection unavailable", "DATABASE", RED));
            }
        }
    }

    private void loadLecturerActivities(Connection conn) {
        // Attendance sessions for lecturer's offerings
        String attSql = "SELECT s.session_type, s.session_number, c.course_code " +
                "FROM attendance_sessions s " +
                "JOIN course_lecturers cl ON s.offering_id = cl.offering_id " +
                "JOIN course_offerings co ON s.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY s.session_date DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(attSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    activityItems.add(new ActivityItem(
                            "Attendance session " + rs.getInt("session_number") + " updated for " + rs.getString("course_code"),
                            "ATTENDANCE",
                            TEAL
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        // Recent assignment/assessment created
        String asmSql = "SELECT a.assessment_name, c.course_code " +
                "FROM assessments a " +
                "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE a.created_by = ? " +
                "ORDER BY a.assessment_id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(asmSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    activityItems.add(new ActivityItem(
                            "Created " + rs.getString("assessment_name") + " (" + rs.getString("course_code") + ")",
                            "ASSIGNMENT",
                            NAVY
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        // Marks submitted by lecturer
        String marksSql = "SELECT a.assessment_name, c.course_code " +
                "FROM marks m " +
                "JOIN assessments a ON m.assessment_id = a.assessment_id " +
                "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE m.entered_by = ? " +
                "GROUP BY a.assessment_name, c.course_code " +
                "ORDER BY MAX(m.entered_at) DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(marksSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    activityItems.add(new ActivityItem(
                            "Marks submitted for " + rs.getString("assessment_name") + " (" + rs.getString("course_code") + ")",
                            "MARKS",
                            PURPLE
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        // Course material uploaded
        String matSql = "SELECT cm.title, c.course_code " +
                "FROM course_materials cm " +
                "JOIN course_offerings co ON cm.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cm.lecturer_id = ? " +
                "ORDER BY cm.uploaded_at DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(matSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    activityItems.add(new ActivityItem(
                            "Course material: " + rs.getString("title") + " (" + rs.getString("course_code") + ")",
                            "MATERIALS",
                            new Color(190, 125, 45)
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        if (activityItems.isEmpty()) {
            activityItems.add(new ActivityItem(
                    "No recent activities recorded for lecturer " + lecturerId,
                    "INFO",
                    MUTED
            ));
        }
    }

    private void loadPublicNotices() {
        try (Connection conn = DBConnection.getConnection()) {
            String noticeSql = "SELECT title FROM notices WHERE status = 'ACTIVE' ORDER BY notice_date DESC LIMIT 3";
            try (PreparedStatement ps = conn.prepareStatement(noticeSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    activityItems.add(new ActivityItem(
                            rs.getString("title"),
                            "NOTICE",
                            NAVY
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        if (activityItems.isEmpty()) {
            activityItems.add(new ActivityItem(
                    "No active lecturer session. Provide a lecturer ID (e.g. LEC001) to view activities.",
                    "INFO",
                    MUTED
            ));
        }
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Dashboard", lecturerId, this);
    }

    private JPanel createDashboardContent() {
        JPanel content = new JPanel(
                new BorderLayout(0, 22)
        );

        content.setBackground(BACKGROUND);
        content.setBorder(
                new EmptyBorder(28, 30, 30, 30)
        );

        JPanel welcome = new JPanel();
        welcome.setBackground(BACKGROUND);
        welcome.setLayout(
                new BoxLayout(welcome, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Welcome, " + lecturerDisplayName);
        heading.setFont(new Font("Arial", Font.BOLD, 29));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(welcomeSubtitle);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        subtitle.setBorder(
                new EmptyBorder(5, 1, 0, 0)
        );

        welcome.add(heading);
        welcome.add(subtitle);

        content.add(welcome, BorderLayout.NORTH);

        JPanel main = new JPanel(
                new BorderLayout(0, 22)
        );
        main.setBackground(BACKGROUND);

        JPanel stats = new JPanel(
                new GridLayout(1, 4, 16, 0)
        );
        stats.setBackground(BACKGROUND);

        stats.add(createStatCard(
                "MY COURSES", myCoursesValue, NAVY,
                coursesCaption
        ));

        stats.add(createStatCard(
                "STUDENTS", studentsValue, TEAL,
                studentsCaption
        ));

        stats.add(createStatCard(
                "ASSIGNMENTS", assignmentsValue,
                new Color(48, 77, 111),
                assignmentsCaption
        ));

        stats.add(createStatCard(
                "PENDING MARKS", pendingMarksValue, PURPLE,
                pendingMarksCaption
        ));

        main.add(stats, BorderLayout.NORTH);

        JPanel activity = new JPanel(
                new BorderLayout(0, 16)
        );

        activity.setBackground(WHITE);
        activity.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(20, 22, 20, 22)
                )
        );

        JPanel activityHeading = new JPanel(
                new BorderLayout()
        );
        activityHeading.setBackground(WHITE);

        JLabel activityTitle =
                new JLabel("Lecturer Activities");

        activityTitle.setFont(
                new Font("Arial", Font.BOLD, 18)
        );
        activityTitle.setForeground(TEXT);

        JLabel recent = new JLabel("RECENT UPDATES");
        recent.setFont(new Font("Arial", Font.BOLD, 10));
        recent.setForeground(MUTED);

        activityHeading.add(
                activityTitle, BorderLayout.WEST
        );
        activityHeading.add(recent, BorderLayout.EAST);

        activity.add(activityHeading, BorderLayout.NORTH);

        JPanel activityList = new JPanel();
        activityList.setBackground(WHITE);
        activityList.setLayout(
                new BoxLayout(activityList, BoxLayout.Y_AXIS)
        );

        for (ActivityItem item : activityItems) {
            activityList.add(createActivity(
                    item.text,
                    item.category,
                    item.accent
            ));
        }

        activity.add(activityList, BorderLayout.CENTER);

        main.add(activity, BorderLayout.CENTER);
        content.add(main, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(
            String title, String value,
            Color accent, String caption) {

        JPanel card = new JPanel(
                new BorderLayout(0, 12)
        );

        card.setBackground(WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(17, 18, 16, 18)
                )
        );

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 11)
        );
        titleLabel.setForeground(MUTED);

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Arial", Font.BOLD, 13));

        top.add(titleLabel, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);

        JLabel number = new JLabel(value);
        number.setFont(
                new Font("Arial", Font.BOLD, 34)
        );
        number.setForeground(accent);

        JLabel note = new JLabel(caption);
        note.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );
        note.setForeground(MUTED);

        JPanel lower = new JPanel();
        lower.setBackground(WHITE);
        lower.setLayout(
                new BoxLayout(lower, BoxLayout.Y_AXIS)
        );

        lower.add(number);
        lower.add(Box.createVerticalStrut(3));
        lower.add(note);

        card.add(top, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActivity(
            String text, String category, Color accent) {

        JPanel row = new JPanel(
                new BorderLayout(12, 0)
        );

        row.setBackground(WHITE);
        row.setBorder(
                new EmptyBorder(12, 0, 12, 0)
        );
        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 58)
        );

        JPanel marker = new JPanel();
        marker.setBackground(accent);
        marker.setPreferredSize(new Dimension(4, 34));

        JLabel description = new JLabel(text);
        description.setFont(BODY);
        description.setForeground(TEXT);

        JLabel tag = new JLabel(category);
        tag.setFont(new Font("Arial", Font.BOLD, 10));
        tag.setForeground(accent);
        tag.setBorder(
                new EmptyBorder(4, 8, 4, 8)
        );
        tag.setOpaque(true);

        tag.setBackground(new Color(
                Math.min(255, accent.getRed() + 105),
                Math.min(255, accent.getGreen() + 105),
                Math.min(255, accent.getBlue() + 105)
        ));

        row.add(marker, BorderLayout.WEST);
        row.add(description, BorderLayout.CENTER);
        row.add(tag, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 60)
        );

        return wrapper;
    }

    private void showComingSoon(String page) {
        JOptionPane.showMessageDialog(
                this,
                page + " page will be added next.",
                page,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /*public static void main(String[] args) {
        // Allows testing from command line or IntelliJ Run Configurations:
        // Pass lecturer ID as first argument (e.g., LEC001) or set -Dfams.lecturer.id=LEC001
        String lecturerArg = args.length > 0 ? args[0] : null;
        SwingUtilities.invokeLater(
                () -> new LecturerDashboard(lecturerArg).setVisible(true)
        );
    }*/
    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> new LecturerDashboard("LEC001").setVisible(true)
        );
    }
}
