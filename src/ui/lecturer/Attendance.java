package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Attendance extends JFrame {

    // Theme colours matching existing Lecturer pages
    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color RED = LecturerTheme.DANGER_RED;

    private static final Font BODY = new Font("Arial", Font.PLAIN, 13);

    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> courseDropdown;
    private JTextField dateField;
    private JTextField searchField;

    private JLabel totalStudentsValue;
    private JLabel presentValue;
    private JLabel absentValue;
    private JLabel attendanceRateValue;
    private JLabel recordCountLabel;
    private JLabel emptyNoticeLabel;
    private JLabel subtitleLabel;

    // Active lecturer ID
    private String lecturerId;

    // Loaded attendance records from the database
    private final List<AttendanceRecord> loadedRecords = new ArrayList<>();

    /**
     * Helper data model representing an attendance entry from the database.
     */
    private static class AttendanceRecord {
        final String attendanceId;
        final String studentId;
        final String studentName;
        final String courseCode;
        final String courseName;
        final String sessionId;
        final String sessionDate;
        final String sessionType;
        final int sessionNumber;
        final String status;
        final String remarks;

        AttendanceRecord(String attendanceId, String studentId, String studentName,
                         String courseCode, String courseName, String sessionId,
                         String sessionDate, String sessionType, int sessionNumber,
                         String status, String remarks) {
            this.attendanceId = attendanceId != null ? attendanceId : "";
            this.studentId = studentId != null ? studentId : "";
            this.studentName = studentName != null ? studentName : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.sessionId = sessionId != null ? sessionId : "";
            this.sessionDate = sessionDate != null ? sessionDate : "";
            this.sessionType = sessionType != null ? sessionType : "";
            this.sessionNumber = sessionNumber;
            this.status = formatStatus(status);
            this.remarks = remarks != null ? remarks : "";
        }

        Object[] toTableRow() {
            return new Object[]{studentId, studentName, courseCode, sessionDate, status};
        }
    }

    /**
     * Default constructor: resolves active lecturer ID dynamically.
     */
    public Attendance() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the active lecturer ID (e.g. "LEC001")
     */
    public Attendance(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboard.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Attendance Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private static String resolveActiveLecturerId() {
        String active = LecturerDashboard.getActiveLecturerId();
        if (active != null && !active.trim().isEmpty()) {
            return active.trim();
        }
        String sysProp = System.getProperty("fams.lecturer.id");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return null;
    }

    // ================= HEADER =================

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Attendance", lecturerId, this);
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(27, 30, 28, 30));

        // Page heading
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(BACKGROUND);

        JPanel headingText = new JPanel();
        headingText.setBackground(BACKGROUND);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));

        JLabel pageTitle = new JLabel("Attendance Management");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("View and manage student attendance records.");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setBorder(new EmptyBorder(5, 1, 0, 0));

        headingText.add(pageTitle);
        headingText.add(subtitleLabel);

        JLabel portalLabel = new JLabel("LECTURER PORTAL");
        portalLabel.setFont(new Font("Arial", Font.BOLD, 10));
        portalLabel.setForeground(NAVY);
        portalLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));

        heading.add(headingText, BorderLayout.WEST);
        heading.add(portalLabel, BorderLayout.EAST);

        content.add(heading, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(BACKGROUND);

        // Attendance summary section
        JPanel summary = new JPanel(new GridLayout(1, 4, 16, 0));
        summary.setBackground(BACKGROUND);

        totalStudentsValue = new JLabel("-");
        presentValue = new JLabel("-");
        absentValue = new JLabel("-");
        attendanceRateValue = new JLabel("-");

        summary.add(createSummaryCard("TOTAL STUDENTS", totalStudentsValue, NAVY, "Enrolled students"));
        summary.add(createSummaryCard("PRESENT", presentValue, TEAL, "Marked present"));
        summary.add(createSummaryCard("ABSENT", absentValue, RED, "Marked absent"));
        summary.add(createSummaryCard("ATTENDANCE RATE", attendanceRateValue, PURPLE, "Overall percentage"));

        body.add(summary, BorderLayout.NORTH);

        // Attendance records table panel
        JPanel tablePanel = new JPanel(new BorderLayout(0, 16));
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 16, 18)
        ));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setBackground(WHITE);

        JLabel tableTitle = new JLabel("Student Attendance Records");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 18));
        tableTitle.setForeground(TEXT);

        recordCountLabel = new JLabel("0 RECORDS");
        recordCountLabel.setFont(new Font("Arial", Font.BOLD, 10));
        recordCountLabel.setForeground(MUTED);

        tableHeading.add(tableTitle, BorderLayout.WEST);
        tableHeading.add(recordCountLabel, BorderLayout.EAST);

        tablePanel.add(tableHeading, BorderLayout.NORTH);

        // Table definition
        String[] columns = {
                "Student ID",
                "Student Name",
                "Course",
                "Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(39);
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 12));
        attendanceTable.setForeground(TEXT);
        attendanceTable.setBackground(WHITE);
        attendanceTable.setGridColor(BORDER);
        attendanceTable.setShowVerticalLines(false);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.setSelectionBackground(new Color(226, 238, 249));
        attendanceTable.setSelectionForeground(TEXT);
        attendanceTable.setFillsViewportHeight(true);
        attendanceTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = attendanceTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(237, 242, 247));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 39));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        attendanceTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        attendanceTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        attendanceTable.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        attendanceTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());

        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        // Row double-click to view details
        attendanceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && attendanceTable.getSelectedRow() >= 0) {
                    viewSelectedRecordDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        // Container with clean empty-state message overlay
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(WHITE);

        emptyNoticeLabel = new JLabel(
                "No attendance records available. Select a course and date to view records.",
                SwingConstants.CENTER
        );
        emptyNoticeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        emptyNoticeLabel.setForeground(MUTED);
        emptyNoticeLabel.setBorder(new EmptyBorder(12, 10, 12, 10));

        centerContainer.add(emptyNoticeLabel, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        tablePanel.add(centerContainer, BorderLayout.CENTER);

        // Filter / Controls Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 10));
        toolbar.setBackground(WHITE);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setBackground(WHITE);

        courseDropdown = new JComboBox<>(new String[]{"All Courses"});
        courseDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        courseDropdown.setPreferredSize(new Dimension(170, 34));
        courseDropdown.setBackground(WHITE);

        dateField = new JTextField(10);
        dateField.setFont(new Font("Arial", Font.PLAIN, 12));
        dateField.setToolTipText("Filter by date (YYYY-MM-DD)");
        dateField.setPreferredSize(new Dimension(115, 34));
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));

        searchField = new JTextField(14);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Filter by student ID or student name");
        searchField.setPreferredSize(new Dimension(175, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));

        filters.add(new JLabel("Course:"));
        filters.add(courseDropdown);
        filters.add(new JLabel("Date:"));
        filters.add(dateField);
        filters.add(new JLabel("Search:"));
        filters.add(searchField);

        JButton searchButton = createActionButton("Search", true);
        JButton resetButton = createActionButton("Reset", false);
        JButton refreshButton = createActionButton("Refresh", false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(WHITE);
        actions.add(searchButton);
        actions.add(resetButton);
        actions.add(refreshButton);

        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        tablePanel.add(toolbar, BorderLayout.SOUTH);

        // Action listeners
        courseDropdown.addActionListener(e -> applyFilters());

        dateField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        searchButton.addActionListener(e -> applyFilters());

        resetButton.addActionListener(e -> {
            if (courseDropdown.getItemCount() > 0) {
                courseDropdown.setSelectedIndex(0);
            }
            dateField.setText("");
            searchField.setText("");
            applyFilters();
        });

        refreshButton.addActionListener(e -> {
            loadDatabaseData();
            JOptionPane.showMessageDialog(
                    this,
                    "Attendance records refreshed from database.",
                    "Refresh",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        body.add(tablePanel, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);

        // Load data from MySQL database
        loadDatabaseData();

        return content;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryCard(String title, JLabel value, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 9)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 17, 14, 17)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 29));
        value.setForeground(TEXT);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        captionLabel.setForeground(MUTED);

        JPanel lower = new JPanel();
        lower.setBackground(WHITE);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        lower.add(value);
        lower.add(captionLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);

        return card;
    }

    // ================= RENDERERS & BUTTONS =================

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, selected, focused, row, column
                );
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 11));

                if (!selected) {
                    String val = String.valueOf(value);
                    if ("Present".equalsIgnoreCase(val)) {
                        label.setForeground(new Color(29, 130, 103));
                        label.setBackground(new Color(232, 247, 240));
                    } else if ("Absent".equalsIgnoreCase(val)) {
                        label.setForeground(RED);
                        label.setBackground(new Color(253, 235, 235));
                    } else if ("Medical".equalsIgnoreCase(val)) {
                        label.setForeground(new Color(190, 125, 45));
                        label.setBackground(new Color(255, 248, 235));
                    } else {
                        label.setForeground(TEXT);
                        label.setBackground(WHITE);
                    }
                } else {
                    label.setForeground(TEXT);
                }
                return label;
            }
        };
    }

    private JButton createActionButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(primary ? WHITE : TEXT);
        button.setBackground(primary ? LecturerTheme.SECONDARY_BLUE : WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(primary ? 100 : 80, 34));

        button.setBorder(primary
                ? BorderFactory.createEmptyBorder(7, 12, 7, 12)
                : BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 11, 6, 11)
        ));

        return button;
    }

    private static String formatStatus(String raw) {
        if (raw == null) return "Absent";
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        if ("PRESENT".equals(upper)) return "Present";
        if ("ABSENT".equals(upper)) return "Absent";
        if ("MEDICAL".equals(upper)) return "Medical";
        return raw;
    }

    // ================= DATABASE INTEGRATION =================

    /**
     * Loads courses and attendance records from MySQL for the active lecturer.
     * Uses DBConnection.getConnection() safely with try-with-resources.
     */
    private void loadDatabaseData() {
        loadedRecords.clear();
        tableModel.setRowCount(0);

        // Resolve lecturer ID
        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            if (subtitleLabel != null) {
                subtitleLabel.setText("No active lecturer ID. Open from Lecturer Dashboard or provide an active lecturer ID.");
            }
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setText("No active lecturer ID. Open from Lecturer Dashboard or provide an active lecturer ID.");
                emptyNoticeLabel.setVisible(true);
            }
            if (recordCountLabel != null) {
                recordCountLabel.setText("NO LECTURER ID");
            }
            if (courseDropdown != null && courseDropdown.getItemCount() == 0) {
                courseDropdown.addItem("Select Course");
            }
            updateSummary(loadedRecords);
            return;
        }

        // 1. Populate assigned courses in dropdown
        populateCoursesDropdown();

        // 2. Query attendance records for lecturer's assigned course offerings
        String sql =
                "SELECT a.attendance_id, a.student_id, u.first_name, u.last_name, " +
                "       c.course_code, c.course_name, s.session_id, s.session_date, s.session_type, s.session_number, " +
                "       a.status, a.remarks " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN attendance_sessions s ON co.offering_id = s.offering_id " +
                "JOIN attendance a ON s.session_id = a.session_id " +
                "JOIN users u ON a.student_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY s.session_date DESC, s.session_number DESC, c.course_code, a.student_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String attendanceId = rs.getString("attendance_id");
                    String studentId = rs.getString("student_id");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String studentName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String sessionId = rs.getString("session_id");
                    java.sql.Date d = rs.getDate("session_date");
                    String sessionDate = d != null ? d.toString() : "";
                    String sessionType = rs.getString("session_type");
                    int sessionNumber = rs.getInt("session_number");
                    String status = rs.getString("status");
                    String remarks = rs.getString("remarks");

                    loadedRecords.add(new AttendanceRecord(
                            attendanceId, studentId, studentName,
                            courseCode, courseName, sessionId,
                            sessionDate, sessionType, sessionNumber,
                            status, remarks
                    ));
                }
            }

            if (subtitleLabel != null) {
                if (loadedRecords.isEmpty()) {
                    subtitleLabel.setText("No attendance records found for lecturer " + lecturerId + ".");
                } else {
                    subtitleLabel.setText("View and manage student attendance records (" + lecturerId + ").");
                }
            }

        } catch (SQLException e) {
            System.err.println("[Attendance] Database error loading attendance: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error loading attendance: " + e.getMessage());
            }
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setText("Database error: " + e.getMessage());
                emptyNoticeLabel.setVisible(true);
            }
            if (recordCountLabel != null) {
                recordCountLabel.setText("DB ERROR");
            }
        }

        applyFilters();
    }

    private void populateCoursesDropdown() {
        if (courseDropdown == null) return;

        Object prevSelection = courseDropdown.getSelectedItem();

        courseDropdown.removeAllItems();
        courseDropdown.addItem("All Courses");

        String sql =
                "SELECT DISTINCT c.course_code, c.course_name " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    courseDropdown.addItem(code + " - " + name);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Attendance] Error loading courses dropdown: " + e.getMessage());
        }

        if (prevSelection != null) {
            for (int i = 0; i < courseDropdown.getItemCount(); i++) {
                if (prevSelection.equals(courseDropdown.getItemAt(i))) {
                    courseDropdown.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (courseDropdown.getItemCount() > 0) {
            courseDropdown.setSelectedIndex(0);
        }
    }

    private void applyFilters() {
        if (tableModel == null) return;

        tableModel.setRowCount(0);

        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            updateSummary(new ArrayList<>());
            return;
        }

        String selectedCourseItem = courseDropdown != null ? (String) courseDropdown.getSelectedItem() : null;
        String filterCourseCode = null;
        if (selectedCourseItem != null && !selectedCourseItem.equals("All Courses") && !selectedCourseItem.equals("Select Course")) {
            int dashIdx = selectedCourseItem.indexOf(" - ");
            filterCourseCode = dashIdx > 0 ? selectedCourseItem.substring(0, dashIdx).trim() : selectedCourseItem.trim();
        }

        String filterDate = dateField != null ? dateField.getText().trim() : "";
        String searchQuery = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";

        List<AttendanceRecord> filteredList = new ArrayList<>();

        for (AttendanceRecord r : loadedRecords) {
            boolean matchesCourse = (filterCourseCode == null) || r.courseCode.equalsIgnoreCase(filterCourseCode);
            boolean matchesDate = filterDate.isEmpty() || r.sessionDate.contains(filterDate);
            boolean matchesSearch = searchQuery.isEmpty()
                    || r.studentId.toLowerCase(Locale.ROOT).contains(searchQuery)
                    || r.studentName.toLowerCase(Locale.ROOT).contains(searchQuery);

            if (matchesCourse && matchesDate && matchesSearch) {
                filteredList.add(r);
                tableModel.addRow(r.toTableRow());
            }
        }

        // Empty state handling
        if (emptyNoticeLabel != null) {
            if (loadedRecords.isEmpty()) {
                emptyNoticeLabel.setText("No attendance records found for lecturer " + lecturerId + ".");
                emptyNoticeLabel.setVisible(true);
            } else if (filteredList.isEmpty()) {
                emptyNoticeLabel.setText("No attendance records match the selected filter criteria.");
                emptyNoticeLabel.setVisible(true);
            } else {
                emptyNoticeLabel.setVisible(false);
            }
        }

        updateSummary(filteredList);
    }

    private void updateSummary(List<AttendanceRecord> currentList) {
        if (totalStudentsValue == null || presentValue == null
                || absentValue == null || attendanceRateValue == null) {
            return;
        }

        if (lecturerId == null || lecturerId.trim().isEmpty() || currentList == null || currentList.isEmpty()) {
            totalStudentsValue.setText("-");
            presentValue.setText("-");
            absentValue.setText("-");
            attendanceRateValue.setText("-");
            if (recordCountLabel != null) {
                recordCountLabel.setText("0 RECORDS");
            }
            return;
        }

        int totalEntries = currentList.size();
        int presentCount = 0;
        int absentCount = 0;
        Set<String> distinctStudents = new HashSet<>();

        for (AttendanceRecord r : currentList) {
            distinctStudents.add(r.studentId);
            if ("Present".equalsIgnoreCase(r.status)) {
                presentCount++;
            } else if ("Absent".equalsIgnoreCase(r.status)) {
                absentCount++;
            }
        }

        totalStudentsValue.setText(String.valueOf(distinctStudents.size()));
        presentValue.setText(String.valueOf(presentCount));
        absentValue.setText(String.valueOf(absentCount));

        if (totalEntries > 0) {
            int rate = (int) Math.round(((double) presentCount / totalEntries) * 100);
            attendanceRateValue.setText(rate + "%");
        } else {
            attendanceRateValue.setText("0%");
        }

        if (recordCountLabel != null) {
            recordCountLabel.setText(totalEntries + " RECORDS");
        }
    }

    private void viewSelectedRecordDetails() {
        int row = attendanceTable.getSelectedRow();
        if (row < 0) return;

        int modelRow = attendanceTable.convertRowIndexToModel(row);
        String stuId = tableModel.getValueAt(modelRow, 0).toString();
        String stuName = tableModel.getValueAt(modelRow, 1).toString();
        String course = tableModel.getValueAt(modelRow, 2).toString();
        String date = tableModel.getValueAt(modelRow, 3).toString();
        String status = tableModel.getValueAt(modelRow, 4).toString();

        AttendanceRecord rec = null;
        for (AttendanceRecord r : loadedRecords) {
            if (r.studentId.equalsIgnoreCase(stuId) && r.courseCode.equalsIgnoreCase(course) && r.sessionDate.equalsIgnoreCase(date)) {
                rec = r;
                break;
            }
        }

        JPanel details = new JPanel(new GridLayout(0, 1, 5, 8));
        details.setBorder(new EmptyBorder(10, 10, 10, 10));

        details.add(new JLabel("Student ID: " + stuId));
        details.add(new JLabel("Student Name: " + stuName));
        details.add(new JLabel("Course: " + course + (rec != null && !rec.courseName.isEmpty() ? " (" + rec.courseName + ")" : "")));
        details.add(new JLabel("Session Date: " + date));
        if (rec != null && !rec.sessionId.isEmpty()) {
            details.add(new JLabel("Session: " + rec.sessionId + " (" + rec.sessionType + " #" + rec.sessionNumber + ")"));
        }
        details.add(new JLabel("Attendance Status: " + status));
        if (rec != null && !rec.remarks.isEmpty()) {
            details.add(new JLabel("Remarks: " + rec.remarks));
        }

        JOptionPane.showMessageDialog(this, details, "Attendance Record Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showComingSoon(String page) {
        JOptionPane.showMessageDialog(
                this,
                page + " page will be implemented next.",
                page,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        String testId = args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboard.setActiveLecturerId(testId);
        }

        SwingUtilities.invokeLater(() -> {
            new Attendance(testId).setVisible(true);
        });
    }
}
