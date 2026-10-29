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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Lecturer Marks / Grades UI for Faculty Academic Management System (FAMS).
 * Connected to MariaDB database 'fams' using database.DBConnection.
 * Restricts marks and course access to courses assigned to the logged-in lecturer.
 */
public class MarksGrades extends JFrame {

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
    private JTable marksTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    private JComboBox<Object> courseDropdown;
    private JComboBox<String> semesterDropdown;
    private JTextField searchField;

    private JLabel totalStudentsValue;
    private JLabel averageMarksValue;
    private JLabel highestMarksValue;
    private JLabel lowestMarksValue;
    private JLabel recordCountLabel;
    private JLabel emptyNoticeLabel;
    private JLabel subtitleLabel;

    // In-memory model representing actual database records
    private final List<CourseOfferingItem> assignedCourses = new ArrayList<>();
    private final List<StudentMarksRecord> loadedRecords = new ArrayList<>();
    private final List<StudentMarksRecord> displayedRecords = new ArrayList<>();

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
     * Helper model for student marks and grades retrieved from MariaDB.
     */
    private static class StudentMarksRecord {
        final String studentId;
        final String studentName;
        final String offeringId;
        final String courseCode;
        final Double assignmentMark;
        final Double quizMark;
        final Double midExamMark;
        final Double finalExamMark;
        final Double caMark;
        final Double totalMark;
        final String grade;
        final Double gradePoint;
        final Double attendancePercentage;
        final Integer caEligible;
        final Integer finalEligible;
        final Double sgpa;
        final Double cgpa;

        StudentMarksRecord(String studentId, String studentName, String offeringId, String courseCode,
                           Double assignmentMark, Double quizMark, Double midExamMark,
                           Double finalExamMark, Double caMark, Double totalMark,
                           String grade, Double gradePoint,
                           Double attendancePercentage, Integer caEligible, Integer finalEligible,
                           Double sgpa, Double cgpa) {
            this.studentId = studentId != null ? studentId : "";
            this.studentName = studentName != null ? studentName : "";
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.assignmentMark = assignmentMark;
            this.quizMark = quizMark;
            this.midExamMark = midExamMark;
            this.finalExamMark = finalExamMark;
            this.caMark = caMark;
            this.totalMark = totalMark;
            this.grade = grade != null ? grade : "-";
            this.gradePoint = gradePoint;
            this.attendancePercentage = attendancePercentage;
            this.caEligible = caEligible;
            this.finalEligible = finalEligible;
            this.sgpa = sgpa;
            this.cgpa = cgpa;
        }

        Object[] toTableRow() {
            return new Object[]{
                    studentId,
                    studentName,
                    assignmentMark != null ? String.format(Locale.US, "%.1f", assignmentMark) : "-",
                    quizMark != null ? String.format(Locale.US, "%.1f", quizMark) : "-",
                    midExamMark != null ? String.format(Locale.US, "%.1f", midExamMark) : "-",
                    finalExamMark != null ? String.format(Locale.US, "%.1f", finalExamMark) : "-",
                    totalMark != null ? String.format(Locale.US, "%.1f", totalMark) : "-",
                    grade != null && !grade.isEmpty() ? grade : "-"
            };
        }
    }

    /**
     * Default constructor: resolves active lecturer ID dynamically.
     */
    public MarksGrades() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the active lecturer ID (e.g. "LEC001")
     */
    public MarksGrades(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboard.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Marks / Grades");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        // Load courses and initial marks from MariaDB
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
        return LecturerTheme.createSidebar("Marks / Grades", lecturerId, this);
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

        JLabel pageTitle = new JLabel("Marks / Grades");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("View and record student marks, assessment scores, and final grades.");
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

        totalStudentsValue = new JLabel("-");
        averageMarksValue = new JLabel("-");
        highestMarksValue = new JLabel("-");
        lowestMarksValue = new JLabel("-");

        summary.add(createSummaryCard("TOTAL STUDENTS", totalStudentsValue, NAVY, "Enrolled in course"));
        summary.add(createSummaryCard("AVERAGE MARKS", averageMarksValue, TEAL, "Class average score"));
        summary.add(createSummaryCard("HIGHEST MARKS", highestMarksValue, PURPLE, "Top recorded score"));
        summary.add(createSummaryCard("LOWEST MARKS", lowestMarksValue, RED, "Minimum score"));

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

        JLabel tableTitle = new JLabel("Student Assessment Marks & Grades");
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
        courseDropdown.setPreferredSize(new Dimension(210, 34));
        courseDropdown.setBackground(WHITE);

        semesterDropdown = new JComboBox<>(new String[]{"All Semesters"});
        semesterDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        semesterDropdown.setPreferredSize(new Dimension(150, 34));
        semesterDropdown.setBackground(WHITE);

        searchField = new JTextField(14);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Search student ID or student name");
        searchField.setPreferredSize(new Dimension(175, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));

        leftFilters.add(new JLabel("Course:"));
        leftFilters.add(courseDropdown);
        leftFilters.add(new JLabel("Semester:"));
        leftFilters.add(semesterDropdown);
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

        // Table definition matching actual FAMS assessment requirements
        String[] columns = {
                "Student ID",
                "Student Name",
                "Assignment",
                "Quiz",
                "Mid Exam",
                "Final Exam",
                "Total Marks",
                "Grade"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        marksTable = new JTable(tableModel);
        marksTable.setRowHeight(38);
        marksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        marksTable.setForeground(TEXT);
        marksTable.setBackground(WHITE);
        marksTable.setGridColor(BORDER);
        marksTable.setShowVerticalLines(false);
        marksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        marksTable.setSelectionBackground(new Color(226, 238, 249));
        marksTable.setSelectionForeground(TEXT);
        marksTable.setFillsViewportHeight(true);
        marksTable.setIntercellSpacing(new Dimension(0, 1));

        tableSorter = new TableRowSorter<>(tableModel);
        marksTable.setRowSorter(tableSorter);

        JTableHeader header = marksTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(237, 242, 247));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        marksTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        marksTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        marksTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(6).setCellRenderer(scoreRenderer());
        marksTable.getColumnModel().getColumn(7).setCellRenderer(gradeRenderer());

        marksTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        marksTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        marksTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        marksTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        marksTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        marksTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        marksTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        marksTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        // Double-click row to view details
        marksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && marksTable.getSelectedRow() >= 0) {
                    viewSelectedRecordDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(marksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        // Center container with empty state notice
        JPanel tableCenter = new JPanel(new BorderLayout());
        tableCenter.setBackground(WHITE);

        emptyNoticeLabel = new JLabel(
                "No marks available. Select a course to view marks.",
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

        JLabel hintLabel = new JLabel("<html>Double-click a student row or click 'View Marks' to inspect assessment breakdown.</html>");
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        hintLabel.setForeground(MUTED);
        hintLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonGroup.setBackground(WHITE);

        JButton viewButton = createActionButton("View Marks", true, 105);
        JButton enterMarksButton = createActionButton("Enter Marks", true, 105);
        JButton editButton = createActionButton("Edit Marks", false, 95);
        JButton saveButton = createActionButton("Save", false, 75);
        JButton cancelButton = createActionButton("Cancel", false, 80);
        JButton refreshButton = createActionButton("Refresh", false, 85);

        buttonGroup.add(viewButton);
        buttonGroup.add(enterMarksButton);
        buttonGroup.add(editButton);
        buttonGroup.add(saveButton);
        buttonGroup.add(cancelButton);
        buttonGroup.add(refreshButton);

        bottomActions.add(hintLabel, BorderLayout.CENTER);
        bottomActions.add(buttonGroup, BorderLayout.EAST);

        tablePanel.add(bottomActions, BorderLayout.SOUTH);

        // ================= EVENT LISTENERS =================

        courseDropdown.addActionListener(e -> applyFilters());
        semesterDropdown.addActionListener(e -> applyFilters());

        // Live Search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });

        filterButton.addActionListener(e -> applyFilters());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            if (courseDropdown.getItemCount() > 0) courseDropdown.setSelectedIndex(0);
            if (semesterDropdown.getItemCount() > 0) semesterDropdown.setSelectedIndex(0);
            applyFilters();
        });

        viewButton.addActionListener(e -> viewSelectedRecordDetails());
        enterMarksButton.addActionListener(e -> openEnterMarksDialog(false));
        editButton.addActionListener(e -> openEnterMarksDialog(true));

        saveButton.addActionListener(e -> {
            boolean success = loadMarksFromDatabase();
            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Marks have been saved successfully to MySQL database.",
                        "Save Marks",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        cancelButton.addActionListener(e -> {
            marksTable.clearSelection();
            searchField.setText("");
            applyFilters();
            JOptionPane.showMessageDialog(
                    this,
                    "Current selection cancelled and original database view restored.",
                    "Cancel",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        refreshButton.addActionListener(e -> {
            loadDatabaseData();
            JOptionPane.showMessageDialog(
                    this,
                    "Marks & course records refreshed from MariaDB.",
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

    private DefaultTableCellRenderer scoreRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, selected, focused, row, column
                );
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setForeground(selected ? TEXT : NAVY);
                return label;
            }
        };
    }

    private DefaultTableCellRenderer gradeRenderer() {
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
                    String g = value.toString().trim();
                    if (g.startsWith("A")) {
                        label.setForeground(new Color(29, 130, 103));
                        label.setBackground(new Color(232, 247, 240));
                    } else if (g.startsWith("B")) {
                        label.setForeground(NAVY);
                        label.setBackground(new Color(232, 240, 248));
                    } else if (g.startsWith("C")) {
                        label.setForeground(new Color(180, 120, 20));
                        label.setBackground(new Color(255, 248, 230));
                    } else if (g.startsWith("D") || g.startsWith("F") || g.startsWith("E")) {
                        label.setForeground(RED);
                        label.setBackground(new Color(253, 235, 235));
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

    // ================= DATABASE INTEGRATION =================

    /**
     * Loads courses, semesters, and marks data from MariaDB for the logged-in lecturer.
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

        try (Connection conn = DBConnection.getConnection()) {
            syncAllResultsAndGpa(conn);
        } catch (SQLException e) {
            System.err.println("[MarksGrades] Warning: automatic results synchronization: " + e.getMessage());
        }

        populateCoursesAndSemesters();
        loadMarksFromDatabase();
    }

    /**
     * Queries MariaDB for course offerings assigned to the logged-in lecturer.
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
            System.err.println("[MarksGrades] Error populating assigned courses: " + e.getMessage());
            showDbError("Failed to load assigned courses from database.", e);
        }

        List<String> sortedSemesters = new ArrayList<>(distinctSemesters);
        Collections.sort(sortedSemesters);
        for (String s : sortedSemesters) {
            semesterDropdown.addItem(s);
        }

        if (assignedCourses.isEmpty()) {
            if (subtitleLabel != null) {
                subtitleLabel.setText("No courses assigned to lecturer " + lecturerId + " in the database.");
            }
        } else {
            if (subtitleLabel != null) {
                subtitleLabel.setText("Displaying assessment marks for assigned courses (Lecturer: " + lecturerId + ").");
            }
        }
    }

    /**
     * Queries actual student marks from the database restricted to the lecturer's course offerings.
     * Returns true if loaded successfully, false on error.
     */
    private boolean loadMarksFromDatabase() {
        loadedRecords.clear();

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            updateSummary(new ArrayList<>());
            return false;
        }

        // Query combining enrollments, actual marks, and final results
        String sql =
                "SELECT e.student_id, u.first_name, u.last_name, " +
                "       co.offering_id, c.course_code, " +
                "       MAX(CASE WHEN a.assessment_type = 'ASSIGNMENT' THEN m.marks END) AS assignment_mark, " +
                "       AVG(CASE WHEN a.assessment_type = 'QUIZ' THEN m.marks END) AS quiz_mark, " +
                "       MAX(CASE WHEN a.assessment_type = 'MID_EXAM' THEN m.marks END) AS mid_mark, " +
                "       MAX(CASE WHEN a.assessment_type = 'FINAL_EXAM' THEN m.marks END) AS final_exam_mark, " +
                "       r.ca_mark, r.final_mark, r.grade, r.grade_point, " +
                "       r.attendance_percentage, r.ca_eligible, r.final_eligible, " +
                "       r.sgpa, r.cgpa " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN enrollments e ON co.offering_id = e.offering_id " +
                "JOIN users u ON e.student_id = u.user_id " +
                "LEFT JOIN assessments a ON co.offering_id = a.offering_id " +
                "LEFT JOIN marks m ON a.assessment_id = m.assessment_id AND e.student_id = m.student_id " +
                "LEFT JOIN results r ON co.offering_id = r.offering_id AND e.student_id = r.student_id " +
                "WHERE cl.lecturer_id = ? " +
                "GROUP BY e.student_id, u.first_name, u.last_name, co.offering_id, c.course_code, " +
                "         r.ca_mark, r.final_mark, r.grade, r.grade_point, " +
                "         r.attendance_percentage, r.ca_eligible, r.final_eligible, " +
                "         r.sgpa, r.cgpa " +
                "ORDER BY c.course_code, e.student_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String stuId = rs.getString("student_id");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String stuName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String offId = rs.getString("offering_id");
                    String code = rs.getString("course_code");

                    Double assign = getDoubleOrNull(rs, "assignment_mark");
                    Double quiz = getDoubleOrNull(rs, "quiz_mark");
                    Double mid = getDoubleOrNull(rs, "mid_mark");
                    Double fin = getDoubleOrNull(rs, "final_exam_mark");
                    Double ca = getDoubleOrNull(rs, "ca_mark");
                    Double tot = getDoubleOrNull(rs, "final_mark");
                    String grade = rs.getString("grade");
                    Double gp = getDoubleOrNull(rs, "grade_point");
                    Double att = getDoubleOrNull(rs, "attendance_percentage");
                    int caElVal = rs.getInt("ca_eligible");
                    Integer caEl = rs.wasNull() ? null : caElVal;
                    int finElVal = rs.getInt("final_eligible");
                    Integer finEl = rs.wasNull() ? null : finElVal;
                    Double sgpa = getDoubleOrNull(rs, "sgpa");
                    Double cgpa = getDoubleOrNull(rs, "cgpa");

                    loadedRecords.add(new StudentMarksRecord(
                            stuId, stuName, offId, code, assign, quiz, mid, fin, ca, tot, grade, gp, att, caEl, finEl, sgpa, cgpa
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MarksGrades] Error querying student marks: " + e.getMessage());
            showDbError("Failed to load student marks from database.", e);
            return false;
        }

        applyFilters();
        return true;
    }

    private static Double getDoubleOrNull(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    // ================= FILTERS & SUMMARY =================

    private void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        displayedRecords.clear();

        Object selectedCourseObj = courseDropdown != null ? courseDropdown.getSelectedItem() : null;
        String filterOfferingId = null;
        if (selectedCourseObj instanceof CourseOfferingItem) {
            filterOfferingId = ((CourseOfferingItem) selectedCourseObj).offeringId;
        }

        String selectedSemester = semesterDropdown != null ? (String) semesterDropdown.getSelectedItem() : null;
        boolean filterSemester = selectedSemester != null && !selectedSemester.equals("All Semesters");

        String query = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";

        List<StudentMarksRecord> filteredList = new ArrayList<>();

        for (StudentMarksRecord r : loadedRecords) {
            boolean matchesCourse = (filterOfferingId == null) || r.offeringId.equalsIgnoreCase(filterOfferingId);
            boolean matchesSemester = true;
            if (filterSemester) {
                CourseOfferingItem item = findCourseOffering(r.offeringId);
                matchesSemester = item != null && selectedSemester.equalsIgnoreCase(item.semester);
            }
            boolean matchesSearch = query.isEmpty()
                    || r.studentId.toLowerCase(Locale.ROOT).contains(query)
                    || r.studentName.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesSemester && matchesSearch) {
                filteredList.add(r);
                displayedRecords.add(r);
                tableModel.addRow(r.toTableRow());
            }
        }

        updateSummary(filteredList);
    }

    private void applySearchFilter() {
        applyFilters();
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

    private void updateSummary(List<StudentMarksRecord> currentList) {
        int count = currentList != null ? currentList.size() : 0;

        if (recordCountLabel != null) {
            recordCountLabel.setText(count + " RECORDS");
        }

        if (count == 0) {
            if (emptyNoticeLabel != null) {
                if (loadedRecords.isEmpty()) {
                    emptyNoticeLabel.setText("No marks available. Select a course offering or enter marks.");
                } else {
                    emptyNoticeLabel.setText("No students match your filter / search query.");
                }
                emptyNoticeLabel.setVisible(true);
            }
            if (totalStudentsValue != null) totalStudentsValue.setText("-");
            if (averageMarksValue != null) averageMarksValue.setText("-");
            if (highestMarksValue != null) highestMarksValue.setText("-");
            if (lowestMarksValue != null) lowestMarksValue.setText("-");
            return;
        }

        if (emptyNoticeLabel != null) {
            emptyNoticeLabel.setVisible(false);
        }

        double sum = 0.0;
        double max = -1.0;
        double min = 1000.0;
        int validMarksCount = 0;

        for (StudentMarksRecord r : currentList) {
            if (r.totalMark != null) {
                double val = r.totalMark;
                sum += val;
                if (val > max) max = val;
                if (val < min) min = val;
                validMarksCount++;
            }
        }

        if (totalStudentsValue != null) {
            totalStudentsValue.setText(String.valueOf(count));
        }

        if (validMarksCount > 0) {
            double avg = sum / validMarksCount;
            if (averageMarksValue != null) averageMarksValue.setText(String.format(Locale.US, "%.1f", avg));
            if (highestMarksValue != null) highestMarksValue.setText(String.format(Locale.US, "%.1f", max));
            if (lowestMarksValue != null) lowestMarksValue.setText(String.format(Locale.US, "%.1f", min));
        } else {
            if (averageMarksValue != null) averageMarksValue.setText("-");
            if (highestMarksValue != null) highestMarksValue.setText("-");
            if (lowestMarksValue != null) lowestMarksValue.setText("-");
        }
    }

    // ================= ACTIONS & DIALOGS =================

    /**
     * Inspects and displays the full assessment breakdown for the selected student.
     */
    private void viewSelectedRecordDetails() {
        int selectedRow = marksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student record from the table first.",
                    "View Marks",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = marksTable.convertRowIndexToModel(selectedRow);
        StudentMarksRecord targetRecord = (modelRow >= 0 && modelRow < displayedRecords.size())
                ? displayedRecords.get(modelRow)
                : null;

        if (targetRecord == null) {
            return;
        }

        String stuId = targetRecord.studentId;
        String stuName = targetRecord.studentName;

        CourseOfferingItem offering = findCourseOffering(targetRecord.offeringId);
        String courseText = offering != null ? offering.courseCode + " - " + offering.courseName : targetRecord.courseCode;

        // Query database for individual assessment items
        StringBuilder breakdown = new StringBuilder();
        String assessSql =
                "SELECT a.assessment_id, a.assessment_name, a.assessment_type, a.weight_percentage, a.max_marks, m.marks, m.entered_at " +
                "FROM assessments a " +
                "LEFT JOIN marks m ON a.assessment_id = m.assessment_id AND m.student_id = ? " +
                "WHERE a.offering_id = ? " +
                "ORDER BY a.assessment_type, a.assessment_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(assessSql)) {

            ps.setString(1, stuId);
            ps.setString(2, targetRecord.offeringId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String aName = rs.getString("assessment_name");
                    String aType = rs.getString("assessment_type");
                    double weight = rs.getDouble("weight_percentage");
                    double max = rs.getDouble("max_marks");
                    double mVal = rs.getDouble("marks");
                    boolean hasMark = !rs.wasNull();

                    breakdown.append(String.format("  • %s (%s) [Weight: %.0f%%, Max: %.0f]: %s%n",
                            aName, aType, weight, max, hasMark ? String.format(Locale.US, "%.1f", mVal) : "Not Entered"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MarksGrades] Error viewing assessment details: " + e.getMessage());
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        topInfo.add(new JLabel("Student ID:"));
        topInfo.add(new JLabel("<html><b>" + stuId + "</b></html>"));
        topInfo.add(new JLabel("Student Name:"));
        topInfo.add(new JLabel("<html><b>" + stuName + "</b></html>"));
        topInfo.add(new JLabel("Course:"));
        topInfo.add(new JLabel(courseText));
        topInfo.add(new JLabel("Attendance Percentage:"));
        topInfo.add(new JLabel(targetRecord.attendancePercentage != null ? String.format(Locale.US, "%.1f%%", targetRecord.attendancePercentage) : "N/A"));
        topInfo.add(new JLabel("CA Eligibility:"));
        topInfo.add(new JLabel(targetRecord.caEligible != null ? (targetRecord.caEligible == 1 ? "<html><font color='#0D9488'><b>Eligible</b></font></html>" : "<html><font color='#E53E3E'><b>Not Eligible</b></font></html>") : "Pending / N/A"));
        topInfo.add(new JLabel("Final Exam Eligibility:"));
        topInfo.add(new JLabel(targetRecord.finalEligible != null ? (targetRecord.finalEligible == 1 ? "<html><font color='#0D9488'><b>Eligible</b></font></html>" : "<html><font color='#E53E3E'><b>Not Eligible</b></font></html>") : "Pending / N/A"));
        topInfo.add(new JLabel("Continuous Assessment (CA):"));
        topInfo.add(new JLabel(targetRecord.caMark != null ? String.format(Locale.US, "%.2f", targetRecord.caMark) : "-"));
        topInfo.add(new JLabel("Final Exam Mark:"));
        topInfo.add(new JLabel(targetRecord.finalExamMark != null ? String.format(Locale.US, "%.2f", targetRecord.finalExamMark) : "-"));
        topInfo.add(new JLabel("Total Final Mark:"));
        topInfo.add(new JLabel("<html><b>" + (targetRecord.totalMark != null ? String.format(Locale.US, "%.2f", targetRecord.totalMark) : "-") + "</b></html>"));
        topInfo.add(new JLabel("Assigned Grade:"));
        topInfo.add(new JLabel("<html><font color='#1F4263'><b>" + targetRecord.grade + "</b> (GP: " + (targetRecord.gradePoint != null ? targetRecord.gradePoint : "-") + ")</font></html>"));
        topInfo.add(new JLabel("Semester GPA (SGPA):"));
        topInfo.add(new JLabel(targetRecord.sgpa != null ? String.format(Locale.US, "%.2f", targetRecord.sgpa) : "N/A"));
        topInfo.add(new JLabel("Cumulative GPA (CGPA):"));
        topInfo.add(new JLabel("<html><b>" + (targetRecord.cgpa != null ? String.format(Locale.US, "%.2f", targetRecord.cgpa) : "N/A") + "</b></html>"));

        JTextArea assessArea = new JTextArea(breakdown.length() > 0 ? breakdown.toString() : "No assessment items found for this course offering.");
        assessArea.setEditable(false);
        assessArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        assessArea.setBackground(new Color(245, 248, 252));
        assessArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 8, 8, 8)
        ));

        panel.add(topInfo, BorderLayout.NORTH);
        panel.add(new JLabel("Assessment Breakdown (from MariaDB):"), BorderLayout.CENTER);
        panel.add(new JScrollPane(assessArea), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Student Marks Details - " + stuId,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * Opens an interactive dialog to enter or edit marks and persist them directly into MariaDB.
     */
    private void openEnterMarksDialog(boolean isEdit) {
        if (assignedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No assigned courses found for this lecturer.",
                    "Enter Marks",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Determine target course offering
        CourseOfferingItem activeOffering = null;
        Object sel = courseDropdown.getSelectedItem();
        if (sel instanceof CourseOfferingItem) {
            activeOffering = (CourseOfferingItem) sel;
        } else {
            activeOffering = assignedCourses.get(0);
        }

        StudentMarksRecord editRecord = null;
        if (isEdit) {
            int selectedRow = marksTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a student record from the table to edit.",
                        "Edit Marks",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            int modelRow = marksTable.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < displayedRecords.size()) {
                editRecord = displayedRecords.get(modelRow);
            }
            if (editRecord != null) {
                activeOffering = findCourseOffering(editRecord.offeringId);
            }
        }

        if (activeOffering == null) {
            JOptionPane.showMessageDialog(this, "Please select a specific course first.", "Enter Marks", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate lecturer permission for this offering
        if (!isOfferingAssignedToLecturer(activeOffering.offeringId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are not assigned as a lecturer for this course offering. Access denied.",
                    "Unauthorized Course",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Query assessments for this offering from MariaDB
        List<AssessmentItem> assessments = new ArrayList<>();
        String sqlAssess = "SELECT assessment_id, assessment_name, assessment_type, weight_percentage, max_marks " +
                "FROM assessments WHERE offering_id = ? ORDER BY assessment_type, assessment_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAssess)) {
            ps.setString(1, activeOffering.offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assessments.add(new AssessmentItem(
                            rs.getString("assessment_id"),
                            rs.getString("assessment_name"),
                            rs.getString("assessment_type"),
                            rs.getDouble("weight_percentage"),
                            rs.getDouble("max_marks")
                    ));
                }
            }
        } catch (SQLException e) {
            showDbError("Failed to load assessments for course offering.", e);
            return;
        }

        if (assessments.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No assessments are configured in the database for course: " + activeOffering.courseCode + ".\n" +
                    "Please configure assessments before entering marks.",
                    "No Assessments",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Load enrolled students for this offering
        List<String> enrolledStudentIds = new ArrayList<>();
        Map<String, String> studentNames = new LinkedHashMap<>();
        String sqlEnrolled = "SELECT e.student_id, u.first_name, u.last_name " +
                "FROM enrollments e " +
                "JOIN users u ON e.student_id = u.user_id " +
                "WHERE e.offering_id = ? ORDER BY e.student_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEnrolled)) {
            ps.setString(1, activeOffering.offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("student_id");
                    String fn = rs.getString("first_name");
                    String ln = rs.getString("last_name");
                    String full = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
                    enrolledStudentIds.add(sid);
                    studentNames.put(sid, full);
                }
            }
        } catch (SQLException e) {
            showDbError("Failed to load enrolled students from database.", e);
            return;
        }

        if (enrolledStudentIds.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No students are currently enrolled in " + activeOffering.courseCode + ".",
                    "No Enrolled Students",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Build the dialog form
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JComboBox<String> studentSelect = new JComboBox<>(enrolledStudentIds.toArray(new String[0]));
        JLabel studentNameLabel = new JLabel();

        form.add(new JLabel("Course Offering:"));
        form.add(new JLabel("<html><b>" + activeOffering.courseCode + " - " + activeOffering.courseName + "</b></html>"));
        form.add(new JLabel("Student ID:"));
        form.add(studentSelect);
        form.add(new JLabel("Student Name:"));
        form.add(studentNameLabel);

        // Build assessment inputs dynamically
        Map<AssessmentItem, JTextField> markFields = new LinkedHashMap<>();
        for (AssessmentItem a : assessments) {
            JTextField tf = new JTextField(8);
            markFields.put(a, tf);
            form.add(new JLabel(a.name + " (Max " + (int) a.maxMarks + ", " + (int) a.weight + "%):"));
            form.add(tf);
        }

        // Helper to reload marks for the chosen student
        final CourseOfferingItem chosenOffering = activeOffering;
        Runnable loadStudentMarks = () -> {
            String selectedStu = (String) studentSelect.getSelectedItem();
            if (selectedStu == null || selectedStu.isEmpty()) return;
            studentNameLabel.setText(studentNames.getOrDefault(selectedStu, ""));

            for (JTextField tf : markFields.values()) {
                tf.setText("");
            }

            String sqlMarks = "SELECT assessment_id, marks FROM marks WHERE student_id = ? AND assessment_id IN (" +
                    "SELECT assessment_id FROM assessments WHERE offering_id = ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlMarks)) {
                ps.setString(1, selectedStu);
                ps.setString(2, chosenOffering.offeringId);
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Double> loaded = new HashMap<>();
                    while (rs.next()) {
                        loaded.put(rs.getString("assessment_id"), rs.getDouble("marks"));
                    }
                    for (Map.Entry<AssessmentItem, JTextField> entry : markFields.entrySet()) {
                        Double m = loaded.get(entry.getKey().id);
                        if (m != null) {
                            entry.getValue().setText(String.format(Locale.US, "%.1f", m));
                        }
                    }
                }
            } catch (SQLException ignored) {
            }
        };

        if (isEdit && editRecord != null) {
            studentSelect.setSelectedItem(editRecord.studentId);
            studentSelect.setEnabled(false);
            loadStudentMarks.run();
        } else {
            studentSelect.setSelectedIndex(0);
            loadStudentMarks.run();
            studentSelect.addActionListener(e -> loadStudentMarks.run());
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                isEdit ? "Edit Student Marks" : "Enter Student Marks",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String targetStudentId = (String) studentSelect.getSelectedItem();
            if (targetStudentId == null || targetStudentId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a student.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<AssessmentItem, Double> parsedMarks = new LinkedHashMap<>();
            for (Map.Entry<AssessmentItem, JTextField> entry : markFields.entrySet()) {
                AssessmentItem a = entry.getKey();
                String text = entry.getValue().getText().trim();
                if (!text.isEmpty()) {
                    try {
                        double v = Double.parseDouble(text);
                        if (v < 0 || v > a.maxMarks) {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Marks for '" + a.name + "' must be between 0 and " + (int) a.maxMarks + ".",
                                    "Invalid Marks Range",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            return;
                        }
                        parsedMarks.put(a, v);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Invalid numeric input for '" + a.name + "'. Please enter a valid number.",
                                "Input Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                }
            }

            if (parsedMarks.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter marks for at least one assessment.",
                        "Empty Input",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Persist valid marks to MariaDB
            persistMarksAndResults(chosenOffering.offeringId, targetStudentId, parsedMarks, assessments);
        }
    }

    private static class AssessmentItem {
        final String id;
        final String name;
        final String type;
        final double weight;
        final double maxMarks;

        AssessmentItem(String id, String name, String type, double weight, double maxMarks) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.weight = weight;
            this.maxMarks = maxMarks;
        }
    }

    private boolean isOfferingAssignedToLecturer(String offeringId) {
        if (offeringId == null || lecturerId == null) return false;
        for (CourseOfferingItem item : assignedCourses) {
            if (item.offeringId.equalsIgnoreCase(offeringId)) {
                return true;
            }
        }
        String sql = "SELECT 1 FROM course_lecturers WHERE offering_id = ? AND lecturer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, offeringId);
            ps.setString(2, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Safely saves or updates marks in the 'marks' table and synchronizes the 'results' table.
     */
    private void persistMarksAndResults(String offeringId, String studentId,
                                        Map<AssessmentItem, Double> marksMap,
                                        List<AssessmentItem> allAssessments) {
        if (!isOfferingAssignedToLecturer(offeringId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are not authorized to modify marks for course offering: " + offeringId,
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String upsertMarkSql =
                "INSERT INTO marks (mark_id, assessment_id, student_id, marks, entered_by, entered_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE marks = VALUES(marks), entered_by = VALUES(entered_by), entered_at = NOW()";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Persist each assessment mark entered
                for (Map.Entry<AssessmentItem, Double> entry : marksMap.entrySet()) {
                    AssessmentItem a = entry.getKey();
                    double markVal = entry.getValue();

                    String nextMarkId = getNextId(conn, "marks", "mark_id", "MRK", 4);

                    try (PreparedStatement ps = conn.prepareStatement(upsertMarkSql)) {
                        ps.setString(1, nextMarkId);
                        ps.setString(2, a.id);
                        ps.setString(3, studentId);
                        ps.setDouble(4, markVal);
                        ps.setString(5, lecturerId);
                        ps.executeUpdate();
                    }
                }

                // 2. Query actual attendance data for this student in this course offering
                int totalSessions = 0;
                String sqlTotalSessions = "SELECT COUNT(*) FROM attendance_sessions WHERE offering_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlTotalSessions)) {
                    ps.setString(1, offeringId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            totalSessions = rs.getInt(1);
                        }
                    }
                }

                int attendedSessions = 0;
                String sqlAttended = "SELECT COUNT(*) FROM attendance a " +
                        "JOIN attendance_sessions s ON a.session_id = s.session_id " +
                        "WHERE s.offering_id = ? AND a.student_id = ? " +
                        "  AND (UPPER(a.status) = 'PRESENT' OR UPPER(a.status) = 'MEDICAL')";
                try (PreparedStatement ps = conn.prepareStatement(sqlAttended)) {
                    ps.setString(1, offeringId);
                    ps.setString(2, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            attendedSessions = rs.getInt(1);
                        }
                    }
                }

                Double attendancePercentage = null;
                boolean attendanceEligible = false;
                if (totalSessions > 0) {
                    attendancePercentage = Math.round((((double) attendedSessions / totalSessions) * 100.0) * 100.0) / 100.0;
                    attendanceEligible = (attendancePercentage >= 80.0);
                } else {
                    // Safe handling if attendance sessions data missing: check existing attendance percentage from results
                    String sqlExistingAtt = "SELECT attendance_percentage FROM results WHERE offering_id = ? AND student_id = ?";
                    try (PreparedStatement psAtt = conn.prepareStatement(sqlExistingAtt)) {
                        psAtt.setString(1, offeringId);
                        psAtt.setString(2, studentId);
                        try (ResultSet rsAtt = psAtt.executeQuery()) {
                            if (rsAtt.next()) {
                                double existingAtt = rsAtt.getDouble("attendance_percentage");
                                if (!rsAtt.wasNull()) {
                                    attendancePercentage = existingAtt;
                                    attendanceEligible = (attendancePercentage >= 80.0);
                                }
                            }
                        }
                    }
                }

                // 3. Query all current marks for this student and offering to recalculate CA & Final marks
                Map<String, Double> currentStudentMarks = new LinkedHashMap<>();
                String queryCurrent = "SELECT m.assessment_id, m.marks FROM marks m " +
                        "JOIN assessments a ON m.assessment_id = a.assessment_id " +
                        "WHERE a.offering_id = ? AND m.student_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(queryCurrent)) {
                    ps.setString(1, offeringId);
                    ps.setString(2, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            currentStudentMarks.put(rs.getString("assessment_id"), rs.getDouble("marks"));
                        }
                    }
                }

                // Compute CA mark and final exam mark based on assessment weights
                double caMark = 0.0;
                double totalCaWeight = 0.0;
                boolean hasAnyCaMark = false;
                Double finalExamMark = null;
                double finalExamWeight = 0.0;
                double finalExamMax = 100.0;

                for (AssessmentItem a : allAssessments) {
                    Double score = currentStudentMarks.get(a.id);
                    if ("FINAL_EXAM".equalsIgnoreCase(a.type)) {
                        finalExamWeight = a.weight;
                        finalExamMax = a.maxMarks > 0 ? a.maxMarks : 100.0;
                        if (score != null) {
                            finalExamMark = score;
                        }
                    } else {
                        totalCaWeight += a.weight;
                        if (score != null) {
                            hasAnyCaMark = true;
                            double maxM = a.maxMarks > 0 ? a.maxMarks : 100.0;
                            caMark += (score / maxM) * a.weight;
                        }
                    }
                }

                // CA Eligibility: student must obtain at least 50% of the total CA weight component
                boolean caEligible = false;
                if (totalCaWeight > 0 && hasAnyCaMark) {
                    double caPercentage = (caMark / totalCaWeight) * 100.0;
                    caEligible = (caPercentage >= 50.0);
                } else {
                    // Missing or 0 CA data: safe handling, cannot mark eligible
                    caEligible = false;
                }

                // Final examination eligibility: both attendance criteria and CA criteria must be met
                boolean finalEligible = attendanceEligible && caEligible;

                int caEligibleInt = caEligible ? 1 : 0;
                int finalEligibleInt = finalEligible ? 1 : 0;

                // Total Final Mark calculation:
                Double totalFinalMark = null;
                if (finalExamMark != null) {
                    double feContrib = (finalExamMark / finalExamMax) * (finalExamWeight > 0 ? finalExamWeight : 55.0);
                    totalFinalMark = Math.round((caMark + feContrib) * 100.0) / 100.0;
                } else if (hasAnyCaMark) {
                    totalFinalMark = Math.round(caMark * 100.0) / 100.0;
                }

                // Grade & Grade Point calculation:
                String grade;
                double gradePoint;
                if (!finalEligible) {
                    // Ineligible students receive F (0.0 GP) according to university examination rules
                    grade = "F";
                    gradePoint = 0.0;
                } else {
                    if (finalExamMark != null) {
                        grade = computeGrade(totalFinalMark != null ? totalFinalMark : 0.0);
                        gradePoint = computeGradePoint(grade);
                    } else {
                        grade = "-";
                        gradePoint = 0.0;
                    }
                }

                // 4. Upsert results table without duplicate student results for the offering
                String nextResultId = getNextId(conn, "results", "result_id", "RES", 4);
                String upsertResultSql =
                        "INSERT INTO results (result_id, student_id, offering_id, ca_mark, final_exam_mark, final_mark, grade, grade_point, attendance_percentage, ca_eligible, final_eligible) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "ca_mark = VALUES(ca_mark), " +
                        "final_exam_mark = VALUES(final_exam_mark), " +
                        "final_mark = VALUES(final_mark), " +
                        "grade = VALUES(grade), " +
                        "grade_point = VALUES(grade_point), " +
                        "attendance_percentage = VALUES(attendance_percentage), " +
                        "ca_eligible = VALUES(ca_eligible), " +
                        "final_eligible = VALUES(final_eligible)";

                try (PreparedStatement ps = conn.prepareStatement(upsertResultSql)) {
                    ps.setString(1, nextResultId);
                    ps.setString(2, studentId);
                    ps.setString(3, offeringId);
                    ps.setDouble(4, Math.round(caMark * 100.0) / 100.0);
                    if (finalExamMark != null) {
                        ps.setDouble(5, finalExamMark);
                    } else {
                        ps.setNull(5, java.sql.Types.DECIMAL);
                    }
                    if (totalFinalMark != null) {
                        ps.setDouble(6, totalFinalMark);
                    } else {
                        ps.setNull(6, java.sql.Types.DECIMAL);
                    }
                    if (grade != null && !"-".equals(grade)) {
                        ps.setString(7, grade);
                    } else {
                        ps.setNull(7, java.sql.Types.VARCHAR);
                    }
                    ps.setDouble(8, gradePoint);
                    if (attendancePercentage != null) {
                        ps.setDouble(9, attendancePercentage);
                    } else {
                        ps.setNull(9, java.sql.Types.DECIMAL);
                    }
                    ps.setInt(10, caEligibleInt);
                    ps.setInt(11, finalEligibleInt);
                    ps.executeUpdate();
                }

                // 5. Update SGPA & CGPA for this student across all semesters
                updateGpaForStudent(conn, studentId);

                conn.commit();

                JOptionPane.showMessageDialog(
                        this,
                        "Marks and results have been saved successfully to the database.\n" +
                        "Attendance: " + (attendancePercentage != null ? String.format(Locale.US, "%.1f%%", attendancePercentage) : "N/A") +
                        " | CA Eligible: " + (caEligible ? "Yes" : "No") +
                        " | Final Eligible: " + (finalEligible ? "Yes" : "No"),
                        "Save Marks",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Reload and refresh UI
                loadMarksFromDatabase();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[MarksGrades] Error saving marks: " + e.getMessage());
            showDbError("Failed to save marks to MySQL database.", e);
        }
    }

    private static String getNextId(Connection conn, String table, String col, String prefix, int digits) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(" + col + ", " + (prefix.length() + 1) + ") AS UNSIGNED)), 0) + 1 FROM " + table;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                long nextNum = rs.getLong(1);
                return String.format(Locale.US, prefix + "%0" + digits + "d", nextNum);
            }
        }
        return prefix + "0001";
    }

    private static String computeGrade(double mark) {
        if (mark >= 85.0) return "A+";
        if (mark >= 75.0) return "A";
        if (mark >= 70.0) return "A-";
        if (mark >= 65.0) return "B+";
        if (mark >= 60.0) return "B";
        if (mark >= 55.0) return "B-";
        if (mark >= 50.0) return "C+";
        if (mark >= 45.0) return "C";
        if (mark >= 40.0) return "C-";
        if (mark >= 30.0) return "D";
        return "F";
    }

    private static double computeGradePoint(String grade) {
        if (grade == null) return 0.0;
        switch (grade) {
            case "A+":
            case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.5;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D": return 1.0;
            default: return 0.0;
        }
    }

    /**
     * Recalculates and updates SGPA and CGPA for a specific student across all their enrolled course offerings.
     */
    public static void updateGpaForStudent(Connection conn, String studentId) throws SQLException {
        if (studentId == null || studentId.trim().isEmpty()) return;

        // 1. Recalculate SGPA for each semester the student is enrolled in
        String sqlSgpa =
                "SELECT co.academic_year, co.semester, " +
                "       SUM(c.credits * r.grade_point) AS total_qp, " +
                "       SUM(c.credits) AS total_credits " +
                "FROM results r " +
                "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE r.student_id = ? AND r.grade_point IS NOT NULL " +
                "GROUP BY co.academic_year, co.semester";

        Map<String, Double> semGpas = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sqlSgpa)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ay = rs.getString("academic_year");
                    String sem = rs.getString("semester");
                    double qp = rs.getDouble("total_qp");
                    double cr = rs.getDouble("total_credits");
                    double sgpa = (cr > 0) ? Math.round((qp / cr) * 100.0) / 100.0 : 0.0;
                    semGpas.put(ay + "##" + sem, sgpa);
                }
            }
        }

        String sqlUpdateSgpa =
                "UPDATE results r " +
                "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                "SET r.sgpa = ? " +
                "WHERE r.student_id = ? AND co.academic_year = ? AND co.semester = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSgpa)) {
            for (Map.Entry<String, Double> entry : semGpas.entrySet()) {
                String[] parts = entry.getKey().split("##", 2);
                ps.setDouble(1, entry.getValue());
                ps.setString(2, studentId);
                ps.setString(3, parts[0]);
                ps.setString(4, parts[1]);
                ps.executeUpdate();
            }
        }

        // 2. Recalculate CGPA across all courses taken by the student
        String sqlCgpa =
                "SELECT SUM(c.credits * r.grade_point) AS total_qp, " +
                "       SUM(c.credits) AS total_credits " +
                "FROM results r " +
                "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE r.student_id = ? AND r.grade_point IS NOT NULL";

        Double cgpa = null;
        try (PreparedStatement ps = conn.prepareStatement(sqlCgpa)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double cr = rs.getDouble("total_credits");
                    if (!rs.wasNull() && cr > 0) {
                        double qp = rs.getDouble("total_qp");
                        cgpa = Math.round((qp / cr) * 100.0) / 100.0;
                    }
                }
            }
        }

        if (cgpa != null) {
            String sqlUpdateCgpa = "UPDATE results SET cgpa = ? WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateCgpa)) {
                ps.setDouble(1, cgpa);
                ps.setString(2, studentId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Synchronizes all records in the results table:
     * - Recalculates attendance from attendance sessions where sessions exist
     * - Evaluates CA eligibility (>= 50% of CA weight)
     * - Evaluates Final Exam eligibility (Attendance >= 80% AND CA Eligible)
     * - Sets Grade 'F' (0.0 GP) for ineligible students
     * - Computes SGPA per semester and CGPA across all courses
     */
    public static void syncAllResultsAndGpa(Connection conn) throws SQLException {
        // 1. Synchronize attendance percentage from attendance sessions for offerings that have session data
        String updateAttSql =
                "UPDATE results r " +
                "JOIN ( " +
                "    SELECT a.student_id, s.offering_id, " +
                "           ROUND((SUM(CASE WHEN UPPER(a.status) = 'PRESENT' OR UPPER(a.status) = 'MEDICAL' THEN 1 ELSE 0 END) / (SELECT COUNT(*) FROM attendance_sessions ses WHERE ses.offering_id = s.offering_id)) * 100.0, 2) AS calc_att " +
                "    FROM attendance a " +
                "    JOIN attendance_sessions s ON a.session_id = s.session_id " +
                "    GROUP BY a.student_id, s.offering_id " +
                ") att_calc ON r.student_id = att_calc.student_id AND r.offering_id = att_calc.offering_id " +
                "SET r.attendance_percentage = att_calc.calc_att";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(updateAttSql);
        }

        // 2. Synchronize ca_eligible and final_eligible
        String updateEligibilitySql =
                "UPDATE results r " +
                "LEFT JOIN ( " +
                "    SELECT a.offering_id, " +
                "           SUM(CASE WHEN a.assessment_type != 'FINAL_EXAM' THEN a.weight_percentage ELSE 0 END) AS total_ca_weight " +
                "    FROM assessments a " +
                "    GROUP BY a.offering_id " +
                ") aw ON r.offering_id = aw.offering_id " +
                "SET r.ca_eligible = CASE " +
                "    WHEN r.ca_mark IS NOT NULL AND aw.total_ca_weight > 0 AND (r.ca_mark / aw.total_ca_weight) * 100.0 >= 50.0 THEN 1 " +
                "    WHEN r.ca_mark IS NOT NULL AND (aw.total_ca_weight IS NULL OR aw.total_ca_weight = 0) AND r.ca_mark >= 20.0 THEN 1 " +
                "    ELSE 0 " +
                "END, " +
                "r.final_eligible = CASE " +
                "    WHEN (CASE " +
                "        WHEN r.ca_mark IS NOT NULL AND aw.total_ca_weight > 0 AND (r.ca_mark / aw.total_ca_weight) * 100.0 >= 50.0 THEN 1 " +
                "        WHEN r.ca_mark IS NOT NULL AND (aw.total_ca_weight IS NULL OR aw.total_ca_weight = 0) AND r.ca_mark >= 20.0 THEN 1 " +
                "        ELSE 0 " +
                "    END) = 1 AND r.attendance_percentage IS NOT NULL AND r.attendance_percentage >= 80.0 THEN 1 " +
                "    ELSE 0 " +
                "END";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(updateEligibilitySql);
        }

        // 3. Ineligible students receive grade 'F' and grade_point 0.0
        String updateIneligibleGradesSql =
                "UPDATE results SET grade = 'F', grade_point = 0.0 WHERE final_eligible = 0 AND (grade != 'F' OR grade IS NULL OR grade_point != 0.0)";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(updateIneligibleGradesSql);
        }

        // 4. Update SGPA for all students per semester
        String updateSgpaSql =
                "UPDATE results r " +
                "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                "JOIN ( " +
                "    SELECT r2.student_id, co2.academic_year, co2.semester, " +
                "           ROUND(SUM(c2.credits * r2.grade_point) / SUM(c2.credits), 2) AS calc_sgpa " +
                "    FROM results r2 " +
                "    JOIN course_offerings co2 ON r2.offering_id = co2.offering_id " +
                "    JOIN courses c2 ON co2.course_id = c2.course_id " +
                "    WHERE r2.grade_point IS NOT NULL " +
                "    GROUP BY r2.student_id, co2.academic_year, co2.semester " +
                ") sg ON r.student_id = sg.student_id AND co.academic_year = sg.academic_year AND co.semester = sg.semester " +
                "SET r.sgpa = sg.calc_sgpa";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(updateSgpaSql);
        }

        // 5. Update CGPA for all students cumulatively
        String updateCgpaSql =
                "UPDATE results r " +
                "JOIN ( " +
                "    SELECT r2.student_id, " +
                "           ROUND(SUM(c2.credits * r2.grade_point) / SUM(c2.credits), 2) AS calc_cgpa " +
                "    FROM results r2 " +
                "    JOIN course_offerings co2 ON r2.offering_id = co2.offering_id " +
                "    JOIN courses c2 ON co2.course_id = c2.course_id " +
                "    WHERE r2.grade_point IS NOT NULL " +
                "    GROUP BY r2.student_id " +
                ") cg ON r.student_id = cg.student_id " +
                "SET r.cgpa = cg.calc_cgpa";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(updateCgpaSql);
        }
    }

    private void showDbError(String message, SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n\nError: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showComingSoon(String pageName) {
        JOptionPane.showMessageDialog(
                this,
                pageName + " feature will be available soon.",
                "Coming Soon",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= MAIN METHOD FOR TESTING =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MarksGrades().setVisible(true);
        });
    }
}
