package ui.lecturer;

import dao.AttendanceDAO;
import dao.AttendanceDAO.AttendanceRecord;
import dao.AttendanceDAO.AttendanceSummary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AttendanceUI - User interface for Viewing and Filtering Student Attendance.
 * Layout and controls aligned with Faculty Academic Management System specifications.
 */
public class AttendanceUI extends JFrame {

    private final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private final Color WHITE = LecturerTheme.CARD_BG;
    private final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private final Color TEAL = new Color(13, 148, 136);
    private final Color PURPLE = new Color(124, 58, 237);
    private final Color RED = new Color(220, 38, 38);
    private final Color BORDER = LecturerTheme.BORDER_COLOR;
    private final Color PRIMARY_BLUE = new Color(29, 78, 216); // Royal blue #1D4ED8

    private String lecturerId;

    private DefaultTableModel tableModel;
    private JTable attendanceTable;
    private JComboBox<String> courseDropdown;
    private JTextField dateField;
    private JTextField searchField;
    private JLabel subtitleLabel;
    private JLabel recordCountLabel;

    private JLabel totalStudentsValue;
    private JLabel presentValue;
    private JLabel absentValue;
    private JLabel attendanceRateValue;

    private final List<AttendanceRecord> loadedRecords = new ArrayList<>();

    public AttendanceUI() {
        this(resolveActiveLecturerId());
    }

    public AttendanceUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Attendance Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadData();
    }

    private static String resolveActiveLecturerId() {
        String active = LecturerDashboardUI.getActiveLecturerId();
        if (active != null && !active.trim().isEmpty()) {
            return active.trim();
        }
        String sysProp = System.getProperty("fams.lecturer.id");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return null;
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Attendance", lecturerId, this);
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(BACKGROUND);
        main.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Header & Summary Section
        JPanel topSection = new JPanel();
        topSection.setBackground(BACKGROUND);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        // 1. Heading row: "Attendance Management" on left, "LECTURER PORTAL" on right
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(BACKGROUND);

        JPanel titles = new JPanel();
        titles.setBackground(BACKGROUND);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Attendance Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(TEXT);

        String activeDisplayId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
        subtitleLabel = new JLabel("View and manage student attendance records (" + activeDisplayId + ").");
        subtitleLabel.setFont(LecturerTheme.FONT_PLAIN_13);
        subtitleLabel.setForeground(MUTED);

        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // LECTURER PORTAL badge on top right
        JPanel portalBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        portalBadge.setBackground(WHITE);
        portalBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(4, 12, 4, 12)
        ));
        JLabel portalLabel = new JLabel("LECTURER PORTAL");
        portalLabel.setFont(new Font("Arial", Font.BOLD, 11));
        portalLabel.setForeground(new Color(30, 41, 59));
        portalBadge.add(portalLabel);

        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(portalBadge, BorderLayout.EAST);
        topSection.add(titleRow);
        topSection.add(Box.createVerticalStrut(18));

        // 2. Summary Cards (TOTAL STUDENTS, PRESENT, ABSENT, ATTENDANCE RATE)
        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryRow.setBackground(BACKGROUND);
        summaryRow.setPreferredSize(new Dimension(0, 96));

        totalStudentsValue = new JLabel("-");
        presentValue = new JLabel("-");
        absentValue = new JLabel("-");
        attendanceRateValue = new JLabel("-");

        summaryRow.add(createSummaryCard("TOTAL STUDENTS", totalStudentsValue, NAVY, "Enrolled students"));
        summaryRow.add(createSummaryCard("PRESENT", presentValue, TEAL, "Marked present"));
        summaryRow.add(createSummaryCard("ABSENT", absentValue, RED, "Marked absent"));
        summaryRow.add(createSummaryCard("ATTENDANCE RATE", attendanceRateValue, PURPLE, "Overall percentage"));

        topSection.add(summaryRow);
        main.add(topSection, BorderLayout.NORTH);

        // 3. Table Container Card
        JPanel tableContainer = new JPanel(new BorderLayout(0, 14));
        tableContainer.setBackground(WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Table Header: "Student Attendance Records" (left) and "600 RECORDS" (right)
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setBackground(WHITE);

        JLabel tableTitle = new JLabel("Student Attendance Records");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 15));
        tableTitle.setForeground(TEXT);

        recordCountLabel = new JLabel("0 RECORDS");
        recordCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        recordCountLabel.setForeground(MUTED);

        tableHeaderBar.add(tableTitle, BorderLayout.WEST);
        tableHeaderBar.add(recordCountLabel, BorderLayout.EAST);
        tableContainer.add(tableHeaderBar, BorderLayout.NORTH);

        // Table with 5 columns matching screenshot
        String[] columns = {"Student ID", "Student Name", "Course", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(38);
        attendanceTable.setFont(LecturerTheme.FONT_PLAIN_13);
        attendanceTable.setGridColor(BORDER);
        attendanceTable.setSelectionBackground(LecturerTheme.LIGHT_BLUE);
        attendanceTable.setSelectionForeground(NAVY);
        attendanceTable.setShowGrid(true);

        JTableHeader th = attendanceTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(130);

        attendanceTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 12, 0, 0));
        attendanceTable.getColumnModel().getColumn(1).setCellRenderer(leftIndentRenderer);

        attendanceTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer());
        attendanceTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // 4. Bottom Toolbar: Left: Course, Date, Search | Right: Search, Reset, Refresh
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(WHITE);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Left controls: Course: [ All Courses ▾ ]  Date: [ ]  Search: [ ]
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftControls.setBackground(WHITE);

        JLabel courseLbl = new JLabel("Course:");
        courseLbl.setFont(new Font("Arial", Font.BOLD, 12));
        courseLbl.setForeground(TEXT);

        courseDropdown = new JComboBox<>();
        courseDropdown.setPreferredSize(new Dimension(145, 32));
        courseDropdown.setFont(LecturerTheme.FONT_PLAIN_13);
        courseDropdown.setBackground(WHITE);
        courseDropdown.addActionListener(e -> applyFilters());

        JLabel dateLbl = new JLabel("Date:");
        dateLbl.setFont(new Font("Arial", Font.BOLD, 12));
        dateLbl.setForeground(TEXT);

        dateField = new JTextField();
        dateField.setPreferredSize(new Dimension(110, 32));
        dateField.setFont(LecturerTheme.FONT_PLAIN_13);
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 8, 0, 8)
        ));
        dateField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("Arial", Font.BOLD, 12));
        searchLbl.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(160, 32));
        searchField.setFont(LecturerTheme.FONT_PLAIN_13);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 8, 0, 8)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        leftControls.add(courseLbl);
        leftControls.add(courseDropdown);
        leftControls.add(dateLbl);
        leftControls.add(dateField);
        leftControls.add(searchLbl);
        leftControls.add(searchField);

        // Right controls: [ Search ] [ Reset ] [ Refresh ]
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setBackground(WHITE);

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(LecturerTheme.FONT_BOLD_12);
        searchBtn.setBackground(PRIMARY_BLUE);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBtn.setPreferredSize(new Dimension(90, 32));
        searchBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(LecturerTheme.FONT_BOLD_12);
        resetBtn.setBackground(WHITE);
        resetBtn.setForeground(TEXT);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.setPreferredSize(new Dimension(80, 32));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(4, 12, 4, 12)
        ));
        resetBtn.addActionListener(e -> {
            dateField.setText("");
            searchField.setText("");
            if (courseDropdown.getItemCount() > 0) {
                courseDropdown.setSelectedIndex(0);
            }
            applyFilters();
        });

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(LecturerTheme.FONT_BOLD_12);
        refreshBtn.setBackground(WHITE);
        refreshBtn.setForeground(TEXT);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(85, 32));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(4, 12, 4, 12)
        ));
        refreshBtn.addActionListener(e -> {
            dateField.setText("");
            searchField.setText("");
            loadData();
        });

        rightControls.add(searchBtn);
        rightControls.add(resetBtn);
        rightControls.add(refreshBtn);

        bottomBar.add(leftControls, BorderLayout.WEST);
        bottomBar.add(rightControls, BorderLayout.EAST);

        tableContainer.add(bottomBar, BorderLayout.SOUTH);
        main.add(tableContainer, BorderLayout.CENTER);

        return main;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                // Top accent stripe & left accent stripe matching screenshot
                g2.setColor(accent);
                g2.fillRect(0, 0, getWidth(), 3);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 11));
        titleLbl.setForeground(MUTED);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(TEXT);

        JLabel captionLbl = new JLabel(caption);
        captionLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        captionLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(captionLbl, BorderLayout.SOUTH);
        return card;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column
            ) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String status = value != null ? value.toString() : "";
                if (!isSelected) {
                    if ("Present".equalsIgnoreCase(status) || "PRESENT".equalsIgnoreCase(status)) {
                        l.setBackground(new Color(236, 253, 245)); // soft mint green #ECFDF5
                        l.setForeground(new Color(5, 150, 105));   // dark green #059669
                        l.setFont(new Font("Arial", Font.BOLD, 12));
                    } else if ("Medical".equalsIgnoreCase(status) || "MEDICAL".equalsIgnoreCase(status)) {
                        l.setBackground(new Color(239, 246, 255)); // soft blue #EFF6FF
                        l.setForeground(new Color(37, 99, 235));   // blue #2563EB
                        l.setFont(new Font("Arial", Font.BOLD, 12));
                    } else {
                        l.setBackground(new Color(254, 242, 242)); // soft pink/red #FEF2F2
                        l.setForeground(new Color(220, 38, 38));   // red #DC2626
                        l.setFont(new Font("Arial", Font.BOLD, 12));
                    }
                }
                return l;
            }
        };
    }

    /**
     * Loads courses and attendance records using AttendanceDAO service.
     */
    public void loadData() {
        loadedRecords.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        // Populate course dropdown from AttendanceDAO
        if (courseDropdown != null) {
            courseDropdown.removeAllItems();
            courseDropdown.addItem("All Courses");
            List<String> courses = AttendanceDAO.loadAssignedCourses(lecturerId);
            for (String c : courses) {
                courseDropdown.addItem(c);
            }
            if (courseDropdown.getItemCount() > 0) {
                courseDropdown.setSelectedIndex(0);
            }
        }

        try {
            List<AttendanceRecord> records = AttendanceDAO.loadAttendanceRecords(lecturerId);
            loadedRecords.addAll(records);

            if (subtitleLabel != null) {
                String idLabel = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
                subtitleLabel.setText("View and manage student attendance records (" + idLabel + ").");
            }
        } catch (SQLException e) {
            System.err.println("[AttendanceUI] DB Error: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error: " + e.getMessage());
            }
        }

        applyFilters();
    }

    /**
     * Applies filters using AttendanceDAO service.
     */
    public void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        String selectedItem = courseDropdown != null ? (String) courseDropdown.getSelectedItem() : null;
        String filterCourseCode = null;
        if (selectedItem != null && !selectedItem.equals("All Courses") && selectedItem.contains(" - ")) {
            filterCourseCode = selectedItem.substring(0, selectedItem.indexOf(" - ")).trim();
        } else if (selectedItem != null && !selectedItem.equals("All Courses")) {
            filterCourseCode = selectedItem.trim();
        }

        String filterDate = dateField != null ? dateField.getText().trim() : "";
        String searchQuery = searchField != null ? searchField.getText().trim() : "";

        List<AttendanceRecord> filtered = AttendanceDAO.filterRecords(loadedRecords, filterCourseCode, filterDate, searchQuery);

        Set<String> uniqueStudents = new HashSet<>();
        for (AttendanceRecord r : filtered) {
            if (!r.studentId.isEmpty()) {
                uniqueStudents.add(r.studentId);
            }

            String displayStatus = "Present";
            if ("ABSENT".equalsIgnoreCase(r.status)) {
                displayStatus = "Absent";
            } else if ("MEDICAL".equalsIgnoreCase(r.status)) {
                displayStatus = "Medical";
            } else if ("PRESENT".equalsIgnoreCase(r.status)) {
                displayStatus = "Present";
            } else {
                displayStatus = r.status;
            }

            tableModel.addRow(new Object[]{
                    r.studentId,
                    r.studentName.isEmpty() ? "Unknown Student" : r.studentName,
                    r.courseCode,
                    r.sessionDate.isEmpty() ? "-" : r.sessionDate,
                    displayStatus
            });
        }

        if (recordCountLabel != null) {
            recordCountLabel.setText(filtered.size() + " RECORDS");
        }

        // Update summary metrics from AttendanceDAO
        AttendanceSummary summary = AttendanceDAO.calculateSummary(filtered);
        int studentsCount = uniqueStudents.isEmpty() ? summary.totalStudents : uniqueStudents.size();
        if (totalStudentsValue != null) totalStudentsValue.setText(String.valueOf(studentsCount));
        if (presentValue != null) presentValue.setText(String.valueOf(summary.presentCount));
        if (absentValue != null) absentValue.setText(String.valueOf(summary.absentCount));
        if (attendanceRateValue != null) attendanceRateValue.setText(Math.round(summary.attendanceRate) + "%");
    }

    public static void main(String[] args) {
        String testId = args != null && args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboardUI.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new AttendanceUI(testId).setVisible(true);
        });
    }
}
