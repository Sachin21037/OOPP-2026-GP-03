package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lecturer Assignment UI for Faculty Academic Management System (FAMS).
 * Connected to MySQL database 'fams' using database.DBConnection.
 * Restricts assignments and course access to courses assigned to the logged-in lecturer.
 */
public class Assignments extends JFrame {

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
    private static final Color ACTIVE_BG = LecturerTheme.LIGHT_BLUE;

    private static final Font BODY = new Font("Arial", Font.PLAIN, 13);

    // Active lecturer identifier
    private String lecturerId;

    // UI Components
    private JTable assignmentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    private JComboBox<Object> courseDropdown;
    private JComboBox<String> semesterDropdown;
    private JComboBox<String> statusFilterDropdown;
    private JTextField searchField;

    private JLabel totalAssignmentsValue;
    private JLabel upcomingValue;
    private JLabel dueSoonValue;
    private JLabel closedValue;
    private JLabel recordCountLabel;
    private JLabel emptyNoticeLabel;
    private JLabel subtitleLabel;

    // In-memory model representing actual database records
    private final List<CourseOfferingItem> assignedCourses = new ArrayList<>();
    private final List<AssignmentRecord> loadedRecords = new ArrayList<>();

    /**
     * Helper model for course offerings assigned to the lecturer.
     */
    private static class CourseOfferingItem {
        final String offeringId;
        final String courseCode;
        final String courseName;
        final String academicYear;
        final String semester;

        CourseOfferingItem(String offeringId, String courseCode, String courseName, String academicYear, String semester) {
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.semester = semester != null ? semester : "";
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    /**
     * Helper model for assignment records retrieved from MySQL.
     */
    private static class AssignmentRecord {
        final String assignmentId;
        final String title;
        final String offeringId;
        final String courseCode;
        final String courseName;
        final String academicYear;
        final String semester;
        final String description;
        final String assignedDate;
        final String dueDate;
        final double maxMarks;
        final double weight;
        final String status;
        final String createdBy;

        AssignmentRecord(String assignmentId, String title, String offeringId,
                         String courseCode, String courseName, String academicYear,
                         String semester, String dueDate, double maxMarks,
                         double weight, String createdBy) {
            this.assignmentId = assignmentId != null ? assignmentId : "";
            this.title = title != null ? title : "";
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.semester = semester != null ? semester : "";
            this.description = "Course Continuous Assessment (" + String.format(Locale.US, "%.0f%% weight", weight) + ")";
            this.assignedDate = "-";
            this.dueDate = dueDate != null ? dueDate : "-";
            this.maxMarks = maxMarks;
            this.weight = weight;
            this.status = computeStatus(dueDate);
            this.createdBy = createdBy != null ? createdBy : "";
        }

        private static String computeStatus(String dueDateStr) {
            if (dueDateStr == null || dueDateStr.trim().isEmpty() || "-".equals(dueDateStr.trim())) {
                return "Active";
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                Date due = sdf.parse(dueDateStr.trim());
                Date today = sdf.parse(sdf.format(new Date()));

                long diff = due.getTime() - today.getTime();
                long days = diff / (24 * 60 * 60 * 1000);

                if (days < 0) {
                    return "Closed";
                } else if (days <= 3) {
                    return "Due";
                } else {
                    return "Active";
                }
            } catch (Exception ignored) {
                return "Active";
            }
        }

        Object[] toTableRow() {
            return new Object[]{
                    assignmentId,
                    title,
                    courseCode + " - " + courseName,
                    description,
                    assignedDate,
                    dueDate,
                    String.format(Locale.US, "%.0f", maxMarks),
                    status
            };
        }
    }

    /**
     * Default constructor: resolves active lecturer ID dynamically.
     */
    public Assignments() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the active lecturer ID (e.g. "LEC001")
     */
    public Assignments(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboard.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Assignments");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        // Load courses and real assignments from MySQL
        loadDatabaseData();
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
        return LecturerTheme.createSidebar("Assignments", lecturerId, this);
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Heading Panel
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(BACKGROUND);

        JPanel headingText = new JPanel();
        headingText.setBackground(BACKGROUND);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));

        JLabel pageTitle = new JLabel("Assignments");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("Create, manage, and review course assignments and submission deadlines.");
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

        // Body Container
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(BACKGROUND);

        // Summary Section (4 Cards)
        body.add(createSummarySection(), BorderLayout.NORTH);

        // Table Panel (Search, Filters, Table, Action Buttons)
        body.add(createTablePanel(), BorderLayout.CENTER);

        content.add(body, BorderLayout.CENTER);

        return content;
    }

    // ================= SUMMARY SECTION =================

    private JPanel createSummarySection() {
        JPanel summary = new JPanel(new GridLayout(1, 4, 16, 0));
        summary.setBackground(BACKGROUND);

        totalAssignmentsValue = new JLabel("-");
        upcomingValue = new JLabel("-");
        dueSoonValue = new JLabel("-");
        closedValue = new JLabel("-");

        summary.add(createSummaryCard("TOTAL ASSIGNMENTS", totalAssignmentsValue, NAVY, "All course assignments"));
        summary.add(createSummaryCard("UPCOMING / ACTIVE", upcomingValue, TEAL, "Currently ongoing"));
        summary.add(createSummaryCard("DUE SOON", dueSoonValue, PURPLE, "Pending submission"));
        summary.add(createSummaryCard("CLOSED", closedValue, MUTED, "Submissions closed"));

        return summary;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
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
                new EmptyBorder(14, 17, 13, 17)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 28));
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

    // ================= TABLE PANEL =================

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 14));
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Panel Top: Title and Filters
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setBackground(WHITE);

        // Header Line inside card
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(WHITE);

        JLabel tableTitle = new JLabel("Course Assignments List");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 17));
        tableTitle.setForeground(TEXT);

        recordCountLabel = new JLabel("0 RECORDS");
        recordCountLabel.setFont(new Font("Arial", Font.BOLD, 10));
        recordCountLabel.setForeground(MUTED);

        titleRow.add(tableTitle, BorderLayout.WEST);
        titleRow.add(recordCountLabel, BorderLayout.EAST);

        topContainer.add(titleRow, BorderLayout.NORTH);

        // Controls / Filter Toolbar
        JPanel filterToolbar = new JPanel(new BorderLayout(10, 10));
        filterToolbar.setBackground(WHITE);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilters.setBackground(WHITE);

        courseDropdown = new JComboBox<>();
        courseDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        courseDropdown.setPreferredSize(new Dimension(190, 34));
        courseDropdown.setBackground(WHITE);

        semesterDropdown = new JComboBox<>(new String[]{"All Semesters"});
        semesterDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        semesterDropdown.setPreferredSize(new Dimension(150, 34));
        semesterDropdown.setBackground(WHITE);

        statusFilterDropdown = new JComboBox<>(new String[]{
                "All Statuses",
                "Upcoming",
                "Active",
                "Due",
                "Closed"
        });
        statusFilterDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        statusFilterDropdown.setPreferredSize(new Dimension(125, 34));
        statusFilterDropdown.setBackground(WHITE);

        searchField = new JTextField(13);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Search assignment title or ID");
        searchField.setPreferredSize(new Dimension(165, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));

        leftFilters.add(new JLabel("Course:"));
        leftFilters.add(courseDropdown);
        leftFilters.add(new JLabel("Semester:"));
        leftFilters.add(semesterDropdown);
        leftFilters.add(new JLabel("Status:"));
        leftFilters.add(statusFilterDropdown);
        leftFilters.add(new JLabel("Search:"));
        leftFilters.add(searchField);

        JButton filterButton = createActionButton("Filter", true, 78);
        JButton resetButton = createActionButton("Reset", false, 74);

        JPanel rightFilterActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightFilterActions.setBackground(WHITE);
        rightFilterActions.add(filterButton);
        rightFilterActions.add(resetButton);

        filterToolbar.add(leftFilters, BorderLayout.WEST);
        filterToolbar.add(rightFilterActions, BorderLayout.EAST);

        topContainer.add(filterToolbar, BorderLayout.SOUTH);
        tablePanel.add(topContainer, BorderLayout.NORTH);

        // Table definition matching Assignment management requirements
        String[] columns = {
                "Assignment ID",
                "Assignment Title",
                "Course",
                "Description",
                "Assigned Date",
                "Due Date",
                "Maximum Marks",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        assignmentTable = new JTable(tableModel);
        assignmentTable.setRowHeight(38);
        assignmentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        assignmentTable.setForeground(TEXT);
        assignmentTable.setBackground(WHITE);
        assignmentTable.setGridColor(BORDER);
        assignmentTable.setShowVerticalLines(false);
        assignmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignmentTable.setSelectionBackground(new Color(226, 238, 249));
        assignmentTable.setSelectionForeground(TEXT);
        assignmentTable.setFillsViewportHeight(true);
        assignmentTable.setIntercellSpacing(new Dimension(0, 1));

        tableSorter = new TableRowSorter<>(tableModel);
        assignmentTable.setRowSorter(tableSorter);

        JTableHeader header = assignmentTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(237, 242, 247));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        assignmentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());
        assignmentTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        assignmentTable.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        assignmentTable.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);
        assignmentTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        assignmentTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer());
        assignmentTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer());
        assignmentTable.getColumnModel().getColumn(7).setCellRenderer(statusRenderer());

        assignmentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        assignmentTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        assignmentTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        assignmentTable.getColumnModel().getColumn(3).setPreferredWidth(210);
        assignmentTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        assignmentTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        assignmentTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        assignmentTable.getColumnModel().getColumn(7).setPreferredWidth(95);

        // Double-click row to view details
        assignmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && assignmentTable.getSelectedRow() >= 0) {
                    viewSelectedAssignment();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        // Center container with empty state notice overlay
        JPanel tableCenter = new JPanel(new BorderLayout());
        tableCenter.setBackground(WHITE);

        emptyNoticeLabel = new JLabel(
                "No assignments available. Select a course or add a new assignment.",
                SwingConstants.CENTER
        );
        emptyNoticeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        emptyNoticeLabel.setForeground(MUTED);
        emptyNoticeLabel.setBorder(new EmptyBorder(14, 10, 14, 10));

        tableCenter.add(emptyNoticeLabel, BorderLayout.NORTH);
        tableCenter.add(scrollPane, BorderLayout.CENTER);

        tablePanel.add(tableCenter, BorderLayout.CENTER);

        // Action Buttons Toolbar at Bottom of Table Card
        JPanel bottomActions = new JPanel(new BorderLayout(16, 0));
        bottomActions.setBackground(WHITE);
        bottomActions.setBorder(new EmptyBorder(12, 0, 4, 0));

        JLabel hintLabel = new JLabel("<html>Double-click an assignment row or click 'View Assignment' to inspect details.</html>");
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        hintLabel.setForeground(MUTED);
        hintLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonGroup.setBackground(WHITE);

        JButton addButton = createActionButton("Add Assignment", true, 125);
        JButton viewButton = createActionButton("View Assignment", true, 125);
        JButton editButton = createActionButton("Edit Assignment", false, 115);
        JButton deleteButton = createActionButton("Delete Assignment", false, 130);
        JButton refreshButton = createActionButton("Refresh", false, 85);

        buttonGroup.add(addButton);
        buttonGroup.add(viewButton);
        buttonGroup.add(editButton);
        buttonGroup.add(deleteButton);
        buttonGroup.add(refreshButton);

        bottomActions.add(hintLabel, BorderLayout.CENTER);
        bottomActions.add(buttonGroup, BorderLayout.EAST);

        tablePanel.add(bottomActions, BorderLayout.SOUTH);

        // ================= EVENT LISTENERS =================

        courseDropdown.addActionListener(e -> applyFilters());
        semesterDropdown.addActionListener(e -> applyFilters());
        statusFilterDropdown.addActionListener(e -> applyFilters());

        // Live Search listener
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

        filterButton.addActionListener(e -> applyFilters());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            if (courseDropdown.getItemCount() > 0) courseDropdown.setSelectedIndex(0);
            if (semesterDropdown.getItemCount() > 0) semesterDropdown.setSelectedIndex(0);
            if (statusFilterDropdown.getItemCount() > 0) statusFilterDropdown.setSelectedIndex(0);
            applyFilters();
        });

        addButton.addActionListener(e -> openAddEditDialog(false));
        viewButton.addActionListener(e -> viewSelectedAssignment());
        editButton.addActionListener(e -> openAddEditDialog(true));
        deleteButton.addActionListener(e -> deleteSelectedAssignment());

        refreshButton.addActionListener(e -> {
            loadDatabaseData();
            JOptionPane.showMessageDialog(
                    this,
                    "Assignments refreshed from MySQL database.",
                    "Refresh",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        return tablePanel;
    }

    // ================= RENDERERS & HELPERS =================

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

                if (!selected && value != null) {
                    String s = value.toString().trim();
                    if ("Active".equalsIgnoreCase(s) || "Upcoming".equalsIgnoreCase(s)) {
                        label.setForeground(new Color(29, 130, 103));
                        label.setBackground(new Color(232, 247, 240));
                    } else if ("Due".equalsIgnoreCase(s)) {
                        label.setForeground(new Color(180, 110, 15));
                        label.setBackground(new Color(255, 248, 230));
                    } else if ("Closed".equalsIgnoreCase(s)) {
                        label.setForeground(MUTED);
                        label.setBackground(new Color(241, 245, 249));
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

    private JButton createActionButton(String text, boolean primary, int width) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(primary ? WHITE : TEXT);
        button.setBackground(primary ? LecturerTheme.SECONDARY_BLUE : WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, 34));

        button.setBorder(primary
                ? BorderFactory.createEmptyBorder(7, 10, 7, 10)
                : BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 9, 6, 9)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (primary) {
                    button.setBackground(new Color(26, 68, 190));
                } else {
                    button.setBackground(new Color(245, 248, 252));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primary ? LecturerTheme.SECONDARY_BLUE : WHITE);
            }
        });

        return button;
    }

    // ================= MYSQL DATABASE INTEGRATION =================

    /**
     * Loads courses, semesters, and assignments data from MySQL for the logged-in lecturer.
     */
    private void loadDatabaseData() {
        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            if (subtitleLabel != null) {
                subtitleLabel.setText("No active lecturer ID. Open from Lecturer Dashboard or provide an active lecturer ID.");
            }
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setText("No active lecturer session found. Please log in.");
                emptyNoticeLabel.setVisible(true);
            }
            if (recordCountLabel != null) {
                recordCountLabel.setText("NO LECTURER ID");
            }
            return;
        }

        populateCoursesAndSemesters();
        loadAssignmentsFromDatabase();
    }

    /**
     * Queries MySQL for course offerings assigned to the logged-in lecturer.
     */
    private void populateCoursesAndSemesters() {
        assignedCourses.clear();
        courseDropdown.removeAllItems();
        semesterDropdown.removeAllItems();

        courseDropdown.addItem("All Courses");
        semesterDropdown.addItem("All Semesters");

        String sql =
                "SELECT DISTINCT co.offering_id, c.course_code, c.course_name, co.academic_year, co.semester " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code";

        Set<String> distinctSemesters = new HashSet<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String offId = rs.getString("offering_id");
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    String year = rs.getString("academic_year");
                    String sem = rs.getString("semester");

                    CourseOfferingItem item = new CourseOfferingItem(offId, code, name, year, sem);
                    assignedCourses.add(item);
                    courseDropdown.addItem(item);

                    if (sem != null && !sem.trim().isEmpty()) {
                        distinctSemesters.add(sem.trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Assignments] Error populating assigned courses from MySQL: " + e.getMessage());
            showDbError("Failed to load assigned courses from MySQL database.", e);
        }

        List<String> sortedSemesters = new ArrayList<>(distinctSemesters);
        Collections.sort(sortedSemesters);
        for (String s : sortedSemesters) {
            semesterDropdown.addItem(s);
        }

        if (assignedCourses.isEmpty()) {
            if (subtitleLabel != null) {
                subtitleLabel.setText("No courses assigned to lecturer " + lecturerId + " in MySQL database.");
            }
        } else {
            if (subtitleLabel != null) {
                subtitleLabel.setText("Displaying course assignments for assigned offerings (Lecturer: " + lecturerId + ").");
            }
        }
    }

    /**
     * Queries actual assignments from MySQL assessments table restricted to lecturer's course offerings.
     */
    private boolean loadAssignmentsFromDatabase() {
        loadedRecords.clear();

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            updateSummary(new ArrayList<>());
            return false;
        }

        String sql =
                "SELECT a.assessment_id, a.assessment_name, a.offering_id, " +
                "       c.course_code, c.course_name, co.academic_year, co.semester, " +
                "       a.max_marks, a.weight_percentage, a.assessment_date, a.created_by " +
                "FROM assessments a " +
                "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                "WHERE cl.lecturer_id = ? AND a.assessment_type = 'ASSIGNMENT' " +
                "ORDER BY a.assessment_date DESC, a.assessment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String aid = rs.getString("assessment_id");
                    String aname = rs.getString("assessment_name");
                    String offId = rs.getString("offering_id");
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    String year = rs.getString("academic_year");
                    String sem = rs.getString("semester");
                    double max = rs.getDouble("max_marks");
                    if (rs.wasNull()) max = 100.0;
                    double weight = rs.getDouble("weight_percentage");
                    java.sql.Date d = rs.getDate("assessment_date");
                    String dStr = d != null ? d.toString() : "-";
                    String creator = rs.getString("created_by");

                    loadedRecords.add(new AssignmentRecord(
                            aid, aname, offId, code, name, year, sem, dStr, max, weight, creator
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Assignments] Error querying assignments from MySQL: " + e.getMessage());
            showDbError("Failed to load assignments from MySQL database.", e);
            return false;
        }

        applyFilters();
        return true;
    }

    // ================= FILTERS & SUMMARY =================

    private void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        Object selectedCourseObj = courseDropdown != null ? courseDropdown.getSelectedItem() : null;
        String filterOfferingId = null;
        if (selectedCourseObj instanceof CourseOfferingItem) {
            filterOfferingId = ((CourseOfferingItem) selectedCourseObj).offeringId;
        }

        String selectedSemester = semesterDropdown != null ? (String) semesterDropdown.getSelectedItem() : null;
        boolean filterSemester = selectedSemester != null && !selectedSemester.equals("All Semesters");

        String selectedStatus = statusFilterDropdown != null ? (String) statusFilterDropdown.getSelectedItem() : null;
        boolean filterStatus = selectedStatus != null && !selectedStatus.equals("All Statuses");

        String query = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";

        List<AssignmentRecord> filteredList = new ArrayList<>();

        for (AssignmentRecord r : loadedRecords) {
            boolean matchesCourse = (filterOfferingId == null) || r.offeringId.equalsIgnoreCase(filterOfferingId);
            boolean matchesSemester = true;
            if (filterSemester) {
                matchesSemester = selectedSemester.equalsIgnoreCase(r.semester);
            }
            boolean matchesStatus = true;
            if (filterStatus) {
                matchesStatus = selectedStatus.equalsIgnoreCase(r.status);
            }
            boolean matchesSearch = query.isEmpty()
                    || r.assignmentId.toLowerCase(Locale.ROOT).contains(query)
                    || r.title.toLowerCase(Locale.ROOT).contains(query)
                    || r.courseCode.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesSemester && matchesStatus && matchesSearch) {
                filteredList.add(r);
                tableModel.addRow(r.toTableRow());
            }
        }

        updateSummary(filteredList);
    }

    private void updateSummary(List<AssignmentRecord> currentList) {
        int count = currentList != null ? currentList.size() : 0;

        if (recordCountLabel != null) {
            recordCountLabel.setText(count + " RECORDS");
        }

        if (count == 0) {
            if (emptyNoticeLabel != null) {
                if (loadedRecords.isEmpty()) {
                    emptyNoticeLabel.setText("No assignments available. Select a course or add a new assignment.");
                } else {
                    emptyNoticeLabel.setText("No assignments match your filter or search query.");
                }
                emptyNoticeLabel.setVisible(true);
            }
            if (totalAssignmentsValue != null) totalAssignmentsValue.setText("-");
            if (upcomingValue != null) upcomingValue.setText("-");
            if (dueSoonValue != null) dueSoonValue.setText("-");
            if (closedValue != null) closedValue.setText("-");
            return;
        }

        if (emptyNoticeLabel != null) {
            emptyNoticeLabel.setVisible(false);
        }

        int activeCount = 0;
        int dueCount = 0;
        int closedCount = 0;

        for (AssignmentRecord r : currentList) {
            if ("Active".equalsIgnoreCase(r.status) || "Upcoming".equalsIgnoreCase(r.status)) {
                activeCount++;
            } else if ("Due".equalsIgnoreCase(r.status)) {
                dueCount++;
            } else if ("Closed".equalsIgnoreCase(r.status)) {
                closedCount++;
            }
        }

        if (totalAssignmentsValue != null) totalAssignmentsValue.setText(String.valueOf(count));
        if (upcomingValue != null) upcomingValue.setText(String.valueOf(activeCount));
        if (dueSoonValue != null) dueSoonValue.setText(String.valueOf(dueCount));
        if (closedValue != null) closedValue.setText(String.valueOf(closedCount));
    }

    private CourseOfferingItem findCourseOffering(String offeringId) {
        if (offeringId == null) return null;
        for (CourseOfferingItem item : assignedCourses) {
            if (item.offeringId.equalsIgnoreCase(offeringId)) {
                return item;
            }
        }
        return null;
    }

    // ================= ACTIONS & DIALOGS =================

    /**
     * Views detailed assignment info and real submission statistics from MySQL.
     */
    private void viewSelectedAssignment() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an assignment from the table first.",
                    "View Assignment",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = assignmentTable.convertRowIndexToModel(selectedRow);
        String aid = String.valueOf(tableModel.getValueAt(modelRow, 0));

        AssignmentRecord target = null;
        for (AssignmentRecord r : loadedRecords) {
            if (r.assignmentId.equalsIgnoreCase(aid)) {
                target = r;
                break;
            }
        }

        if (target == null) return;

        // Query submission statistics from marks table
        int marksCount = 0;
        double avgMark = 0.0;
        int totalEnrolled = 0;

        String marksSql = "SELECT COUNT(*) as cnt, AVG(marks) as avg_m FROM marks WHERE assessment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(marksSql)) {
            ps.setString(1, aid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    marksCount = rs.getInt("cnt");
                    avgMark = rs.getDouble("avg_m");
                }
            }
        } catch (SQLException ignored) {
        }

        String enrollSql = "SELECT COUNT(*) FROM enrollments WHERE offering_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(enrollSql)) {
            ps.setString(1, target.offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalEnrolled = rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 6));
        grid.add(new JLabel("Assignment ID:"));
        grid.add(new JLabel("<html><b>" + target.assignmentId + "</b></html>"));
        grid.add(new JLabel("Assignment Title:"));
        grid.add(new JLabel("<html><b>" + target.title + "</b></html>"));
        grid.add(new JLabel("Course:"));
        grid.add(new JLabel(target.courseCode + " - " + target.courseName));
        grid.add(new JLabel("Academic Term:"));
        grid.add(new JLabel(target.academicYear + " " + target.semester));
        grid.add(new JLabel("Due Date:"));
        grid.add(new JLabel(target.dueDate));
        grid.add(new JLabel("Maximum Marks:"));
        grid.add(new JLabel(String.format(Locale.US, "%.0f", target.maxMarks)));
        grid.add(new JLabel("Weight in CA:"));
        grid.add(new JLabel(String.format(Locale.US, "%.1f%%", target.weight)));
        grid.add(new JLabel("Status:"));
        grid.add(new JLabel("<html><font color='#1F4263'><b>" + target.status + "</b></font></html>"));
        grid.add(new JLabel("Student Submissions:"));
        grid.add(new JLabel(marksCount + " of " + totalEnrolled + " enrolled students marked"));
        grid.add(new JLabel("Class Average Score:"));
        grid.add(new JLabel(marksCount > 0 ? String.format(Locale.US, "%.1f / %.0f", avgMark, target.maxMarks) : "No marks entered yet"));
        grid.add(new JLabel("Created By Lecturer:"));
        grid.add(new JLabel(target.createdBy));

        JTextArea descArea = new JTextArea("Assessment Component: " + target.title + "\nWeight in continuous assessment: " + target.weight + "%");
        descArea.setEditable(false);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setBackground(new Color(245, 248, 252));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 8, 8, 8)
        ));

        panel.add(grid, BorderLayout.NORTH);
        panel.add(new JLabel("Assignment Details (from MySQL):"), BorderLayout.CENTER);
        panel.add(new JScrollPane(descArea), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Assignment Details - " + target.assignmentId,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * Opens Add or Edit dialog and persists changes into MySQL assessments table.
     */
    private void openAddEditDialog(boolean isEdit) {
        if (assignedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No assigned courses found for this lecturer.",
                    isEdit ? "Edit Assignment" : "Add Assignment",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        AssignmentRecord editRecord = null;
        if (isEdit) {
            int selectedRow = assignmentTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select an assignment to edit.",
                        "Edit Assignment",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            int modelRow = assignmentTable.convertRowIndexToModel(selectedRow);
            String selectedId = String.valueOf(tableModel.getValueAt(modelRow, 0));
            for (AssignmentRecord r : loadedRecords) {
                if (r.assignmentId.equalsIgnoreCase(selectedId)) {
                    editRecord = r;
                    break;
                }
            }
            if (editRecord == null) return;
        }

        JComboBox<CourseOfferingItem> courseSelect = new JComboBox<>(assignedCourses.toArray(new CourseOfferingItem[0]));
        if (isEdit && editRecord != null) {
            CourseOfferingItem item = findCourseOffering(editRecord.offeringId);
            if (item != null) {
                courseSelect.setSelectedItem(item);
            }
            courseSelect.setEnabled(false);
        } else {
            Object sel = courseDropdown.getSelectedItem();
            if (sel instanceof CourseOfferingItem) {
                courseSelect.setSelectedItem(sel);
            }
        }

        JTextField titleField = new JTextField(isEdit && editRecord != null ? editRecord.title : "", 20);
        JTextField dueDateField = new JTextField(isEdit && editRecord != null && !"-".equals(editRecord.dueDate) ? editRecord.dueDate : new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
        dueDateField.setToolTipText("Format: YYYY-MM-DD");

        JTextField maxMarksField = new JTextField(isEdit && editRecord != null ? String.format(Locale.US, "%.0f", editRecord.maxMarks) : "100", 6);
        JTextField weightField = new JTextField(isEdit && editRecord != null ? String.format(Locale.US, "%.1f", editRecord.weight) : "10.0", 6);
        weightField.setToolTipText("Continuous assessment weight percentage (0 - 100)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        if (isEdit && editRecord != null) {
            gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Assignment ID:"), gbc);
            gbc.gridx = 1; gbc.gridy = row; form.add(new JLabel("<html><b>" + editRecord.assignmentId + "</b></html>"), gbc);
            row++;
        }

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Course Offering *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(courseSelect, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Assignment Title *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(titleField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Due Date (YYYY-MM-DD) *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(dueDateField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Maximum Marks *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(maxMarksField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Weight Percentage (0-100%):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(weightField, gbc);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                isEdit ? "Edit Assignment" : "Add Assignment",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            CourseOfferingItem selectedOffering = (CourseOfferingItem) courseSelect.getSelectedItem();
            String title = titleField.getText().trim();
            String dueDateStr = dueDateField.getText().trim();
            String maxMarksStr = maxMarksField.getText().trim();
            String weightStr = weightField.getText().trim();

            if (selectedOffering == null) {
                JOptionPane.showMessageDialog(this, "Course must be selected.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Assignment title must not be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double maxMarks;
            try {
                maxMarks = Double.parseDouble(maxMarksStr);
                if (maxMarks <= 0) {
                    JOptionPane.showMessageDialog(this, "Maximum marks must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Maximum marks must be a valid numeric value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightStr);
                if (weight < 0 || weight > 100) {
                    JOptionPane.showMessageDialog(this, "Weight percentage must be between 0 and 100.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Weight percentage must be a valid numeric value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.sql.Date sqlDueDate = null;
            if (!dueDateStr.isEmpty()) {
                try {
                    Date d = sdf.parse(dueDateStr);
                    sqlDueDate = new java.sql.Date(d.getTime());
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this, "Due date must follow format YYYY-MM-DD (e.g. 2026-03-25).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Persist to MySQL database
            if (isEdit && editRecord != null) {
                updateAssignmentInMySQL(editRecord.assignmentId, title, maxMarks, weight, sqlDueDate);
            } else {
                insertAssignmentInMySQL(selectedOffering.offeringId, title, maxMarks, weight, sqlDueDate);
            }
        }
    }

    private void insertAssignmentInMySQL(String offeringId, String title, double maxMarks, double weight, java.sql.Date dueDate) {
        String insertSql =
                "INSERT INTO assessments (assessment_id, offering_id, assessment_name, assessment_type, weight_percentage, max_marks, assessment_date, created_by) " +
                "VALUES (?, ?, ?, 'ASSIGNMENT', ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            String nextId = getNextAssessmentId(conn);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, nextId);
                ps.setString(2, offeringId);
                ps.setString(3, title);
                ps.setDouble(4, weight);
                ps.setDouble(5, maxMarks);
                if (dueDate != null) {
                    ps.setDate(6, dueDate);
                } else {
                    ps.setNull(6, java.sql.Types.DATE);
                }
                ps.setString(7, lecturerId);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Assignment '" + nextId + "' added successfully to MySQL database.",
                    "Add Assignment",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadAssignmentsFromDatabase();

        } catch (SQLException e) {
            System.err.println("[Assignments] Error inserting assignment: " + e.getMessage());
            showDbError("Failed to add assignment to MySQL database.", e);
        }
    }

    private void updateAssignmentInMySQL(String assignmentId, String title, double maxMarks, double weight, java.sql.Date dueDate) {
        String updateSql =
                "UPDATE assessments SET assessment_name = ?, weight_percentage = ?, max_marks = ?, assessment_date = ? " +
                "WHERE assessment_id = ? AND offering_id IN (SELECT offering_id FROM course_lecturers WHERE lecturer_id = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, title);
            ps.setDouble(2, weight);
            ps.setDouble(3, maxMarks);
            if (dueDate != null) {
                ps.setDate(4, dueDate);
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.setString(5, assignmentId);
            ps.setString(6, lecturerId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Assignment '" + assignmentId + "' updated successfully in MySQL database.",
                        "Edit Assignment",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadAssignmentsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Update failed: you may not have permission to modify this assignment.",
                        "Permission Denied",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (SQLException e) {
            System.err.println("[Assignments] Error updating assignment: " + e.getMessage());
            showDbError("Failed to update assignment in MySQL database.", e);
        }
    }

    private void deleteSelectedAssignment() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an assignment to delete.",
                    "Delete Assignment",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = assignmentTable.convertRowIndexToModel(selectedRow);
        String aid = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String title = String.valueOf(tableModel.getValueAt(modelRow, 1));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete assignment:\n" + aid + " - " + title + "?",
                "Confirm Delete Assignment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Check foreign key constraint: student marks in 'marks' table
        try (Connection conn = DBConnection.getConnection()) {
            String checkMarksSql = "SELECT COUNT(*) FROM marks WHERE assessment_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkMarksSql)) {
                ps.setString(1, aid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        int count = rs.getInt(1);
                        JOptionPane.showMessageDialog(
                                this,
                                "Cannot delete assignment '" + aid + "' because " + count + " student mark record(s) already exist for it in MySQL database.\n" +
                                "To maintain academic records integrity, assignments with submitted marks cannot be deleted.",
                                "Deletion Blocked",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                }
            }

            // Safe parameterized delete
            String deleteSql = "DELETE FROM assessments WHERE assessment_id = ? AND offering_id IN (SELECT offering_id FROM course_lecturers WHERE lecturer_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, aid);
                ps.setString(2, lecturerId);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Assignment '" + aid + "' deleted successfully from MySQL database.",
                            "Delete Assignment",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadAssignmentsFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Deletion failed: assignment not found or access restricted.",
                            "Delete Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[Assignments] Error deleting assignment: " + e.getMessage());
            showDbError("Failed to delete assignment from MySQL database.", e);
        }
    }

    private static String getNextAssessmentId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(assessment_id, 4) AS UNSIGNED)), 0) + 1 FROM assessments";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                long nextNum = rs.getLong(1);
                return String.format(Locale.US, "ASM%03d", nextNum);
            }
        }
        return "ASM001";
    }

    private void showDbError(String message, SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n\nError: " + ex.getMessage(),
                "MySQL Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // ================= MAIN METHOD FOR TESTING =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new Assignments().setVisible(true);
        });
    }
}
