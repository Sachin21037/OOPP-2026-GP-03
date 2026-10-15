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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class Student extends JFrame {

    // Theme colours matching the existing Lecturer pages
    private static final Color NAVY =
            new Color(31, 66, 99);
    private static final Color BACKGROUND =
            new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT =
            new Color(35, 48, 65);
    private static final Color MUTED =
            new Color(100, 116, 139);
    private static final Color TEAL =
            new Color(43, 169, 155);
    private static final Color PURPLE =
            new Color(132, 78, 190);
    private static final Color BORDER =
            new Color(220, 227, 235);
    private static final Color RED =
            new Color(190, 55, 55);

    private JTable studentTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> courseFilter;
    private JComboBox<String> batchFilter;

    private JLabel totalStudentsValue;
    private JLabel activeStudentsValue;
    private JLabel courseCountValue;
    private JLabel subtitleLabel;
    private JLabel tableHintLabel;

    // Active lecturer ID
    private String lecturerId;

    // Real student records loaded from database
    private final List<StudentItem> loadedStudents = new ArrayList<>();

    /**
     * Inner helper class to hold student details retrieved from database.
     */
    private static class StudentItem {
        final String studentId;
        final String indexNumber;
        final String studentName;
        final String courseCode;
        final String courseName;
        final String batch;
        final String status;
        final String email;
        final String studentStatus;

        StudentItem(String studentId, String indexNumber, String studentName,
                    String courseCode, String courseName, String batch,
                    String status, String email, String studentStatus) {
            this.studentId = studentId != null ? studentId : "";
            this.indexNumber = indexNumber != null ? indexNumber : "";
            this.studentName = studentName != null ? studentName : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.batch = batch != null ? batch : "";
            this.status = status != null ? status : "Active";
            this.email = email != null ? email : "";
            this.studentStatus = studentStatus != null ? studentStatus : "";
        }

        Object[] toTableRow() {
            return new Object[]{studentId, indexNumber, studentName, courseCode, batch, status};
        }
    }

    /**
     * Default constructor: uses active lecturer ID from LecturerDashboard or JVM property.
     */
    public Student() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the lecturer ID (e.g. "LEC001")
     */
    public Student(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        setTitle(
                "Faculty Academic Management System - Students"
        );

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

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(
                new EmptyBorder(0, 26, 0, 26)
        );

        JLabel title = new JLabel(
                "FACULTY ACADEMIC MANAGEMENT SYSTEM"
        );

        title.setForeground(WHITE);
        title.setFont(
                new Font("Arial", Font.BOLD, 19)
        );

        JLabel role = new JLabel("LECTURER");

        role.setForeground(WHITE);
        role.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        header.add(title, BorderLayout.WEST);
        header.add(role, BorderLayout.EAST);

        return header;
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());

        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(205, 0));

        sidebar.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 0, 1, BORDER
                )
        );

        JPanel menu = new JPanel();

        menu.setBackground(WHITE);
        menu.setLayout(
                new BoxLayout(menu, BoxLayout.Y_AXIS)
        );

        menu.setBorder(
                new EmptyBorder(25, 14, 12, 14)
        );

        JLabel menuTitle = new JLabel("LECTURER MENU");

        menuTitle.setFont(
                new Font("Arial", Font.BOLD, 11)
        );

        menuTitle.setForeground(MUTED);
        menuTitle.setBorder(
                new EmptyBorder(0, 9, 18, 0)
        );

        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(menuTitle);

        menu.add(createMenuButton(
                "Dashboard", false, () -> {
                    new LecturerDashboard().setVisible(true);
                    dispose();
                }
        ));

        menu.add(createMenuButton(
                "My Courses", false, () -> {
                    new MyCourses().setVisible(true);
                    dispose();
                }
        ));

        menu.add(createMenuButton(
                "Students", true, () -> {}
        ));

        menu.add(createMenuButton(
                "Attendance", false, () -> {
                    new Attendance().setVisible(true);
                    dispose();
                }
        ));

        menu.add(createMenuButton(
                "Marks / Grades", false, () -> {
                    new MarksGrades().setVisible(true);
                    dispose();
                }
        ));

        menu.add(createMenuButton(
                "Assignments", false, () -> {
                    new Assignments().setVisible(true);
                    dispose();
                }
        ));

        menu.add(createMenuButton(
                "My Profile", false,
                () -> showComingSoon("My Profile")
        ));

        sidebar.add(menu, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());

        bottom.setBackground(WHITE);
        bottom.setBorder(
                new EmptyBorder(10, 14, 22, 14)
        );

        JButton logout = new JButton("Logout");

        logout.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        logout.setForeground(RED);
        logout.setBackground(WHITE);
        logout.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        logout.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );

        logout.setFocusPainted(false);

        logout.addActionListener(e -> {

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                LecturerDashboard.setActiveLecturerId(null);
                dispose();

                JOptionPane.showMessageDialog(
                        null,
                        "You have been logged out."
                );
            }
        });

        bottom.add(logout, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createMenuButton(
            String text,
            boolean active,
            Runnable action) {

        JButton button = new JButton(text);

        button.setFont(
                active
                        ? new Font("Arial", Font.BOLD, 13)
                        : new Font("Arial", Font.PLAIN, 13)
        );

        button.setForeground(active ? NAVY : TEXT);

        button.setBackground(
                active
                        ? new Color(232, 240, 248)
                        : WHITE
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );

        button.setPreferredSize(
                new Dimension(175, 43)
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        active
                                ? BorderFactory.createMatteBorder(
                                0, 4, 0, 0, NAVY
                        )
                                : BorderFactory.createEmptyBorder(
                                0, 4, 0, 0
                        ),
                        new EmptyBorder(0, 13, 0, 8)
                )
        );

        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.addActionListener(e -> action.run());

        return button;
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {

        JPanel content = new JPanel(new BorderLayout(0, 22));

        content.setBackground(BACKGROUND);
        content.setBorder(
                new EmptyBorder(27, 30, 28, 30)
        );

        // Page heading
        JPanel heading = new JPanel(new BorderLayout());

        heading.setBackground(BACKGROUND);

        JPanel headingText = new JPanel();

        headingText.setBackground(BACKGROUND);
        headingText.setLayout(
                new BoxLayout(
                        headingText, BoxLayout.Y_AXIS
                    )
        );

        JLabel pageTitle = new JLabel("Students");

        pageTitle.setFont(
                new Font("Arial", Font.BOLD, 29)
        );

        pageTitle.setForeground(TEXT);

        subtitleLabel = new JLabel(
                "View and manage students enrolled in your courses."
        );

        subtitleLabel.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setBorder(
                new EmptyBorder(5, 1, 0, 0)
        );

        headingText.add(pageTitle);
        headingText.add(subtitleLabel);

        JLabel portalLabel = new JLabel("LECTURER PORTAL");

        portalLabel.setFont(
                new Font("Arial", Font.BOLD, 10)
        );

        portalLabel.setForeground(NAVY);

        portalLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(8, 10, 8, 10)
                )
        );

        heading.add(headingText, BorderLayout.WEST);
        heading.add(portalLabel, BorderLayout.EAST);

        content.add(heading, BorderLayout.NORTH);

        // Main body
        JPanel body = new JPanel(
                new BorderLayout(0, 20)
        );

        body.setBackground(BACKGROUND);

        // Summary cards
        JPanel summary = new JPanel(
                new GridLayout(1, 3, 16, 0)
        );

        summary.setBackground(BACKGROUND);

        totalStudentsValue = new JLabel("-");
        activeStudentsValue = new JLabel("-");
        courseCountValue = new JLabel("-");

        summary.add(createSummaryCard(
                "TOTAL STUDENTS",
                totalStudentsValue,
                NAVY,
                "Enrolled across your courses"
        ));

        summary.add(createSummaryCard(
                "ACTIVE STUDENTS",
                activeStudentsValue,
                TEAL,
                "Currently marked active"
        ));

        summary.add(createSummaryCard(
                "COURSES",
                courseCountValue,
                PURPLE,
                "Courses represented in the list"
        ));

        body.add(summary, BorderLayout.NORTH);

        // Student table panel
        JPanel tablePanel = new JPanel(
                new BorderLayout(0, 16)
        );

        tablePanel.setBackground(WHITE);

        tablePanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(18, 18, 16, 18)
                )
        );

        JPanel tableHeading = new JPanel(
                new BorderLayout()
        );

        tableHeading.setBackground(WHITE);

        JLabel tableTitle = new JLabel(
                "Student Directory"
        );

        tableTitle.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        tableTitle.setForeground(TEXT);

        tableHintLabel = new JLabel(
                "Select a student to view details"
        );

        tableHintLabel.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );

        tableHintLabel.setForeground(MUTED);

        tableHeading.add(
                tableTitle, BorderLayout.WEST
        );

        tableHeading.add(
                tableHintLabel, BorderLayout.EAST
        );

        tablePanel.add(
                tableHeading, BorderLayout.NORTH
        );

        // Table model
        String[] columns = {
                "Student ID",
                "Index Number",
                "Student Name",
                "Course Code",
                "Batch",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);

        studentTable.setRowHeight(39);

        studentTable.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        studentTable.setForeground(TEXT);
        studentTable.setBackground(WHITE);
        studentTable.setGridColor(BORDER);

        studentTable.setShowVerticalLines(false);

        studentTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        studentTable.setSelectionBackground(
                new Color(226, 238, 249)
        );

        studentTable.setSelectionForeground(TEXT);

        studentTable.setFillsViewportHeight(true);

        studentTable.setIntercellSpacing(
                new Dimension(0, 1)
        );

        JTableHeader header =
                studentTable.getTableHeader();

        header.setFont(
                new Font("Arial", Font.BOLD, 11)
        );

        header.setBackground(
                new Color(237, 242, 247)
        );

        header.setForeground(NAVY);

        header.setPreferredSize(
                new Dimension(0, 39)
        );

        header.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer =
                new DefaultTableCellRenderer();

        leftRenderer.setBorder(
                new EmptyBorder(0, 8, 0, 8)
        );

        studentTable.getColumnModel()
                .getColumn(0)
                .setCellRenderer(leftRenderer);

        studentTable.getColumnModel()
                .getColumn(1)
                .setCellRenderer(leftRenderer);

        studentTable.getColumnModel()
                .getColumn(2)
                .setCellRenderer(leftRenderer);

        studentTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(leftRenderer);

        studentTable.getColumnModel()
                .getColumn(4)
                .setCellRenderer(centerRenderer());

        studentTable.getColumnModel()
                .getColumn(5)
                .setCellRenderer(statusRenderer());

        studentTable.getColumnModel()
                .getColumn(0).setPreferredWidth(85);

        studentTable.getColumnModel()
                .getColumn(1).setPreferredWidth(125);

        studentTable.getColumnModel()
                .getColumn(2).setPreferredWidth(180);

        studentTable.getColumnModel()
                .getColumn(3).setPreferredWidth(100);

        studentTable.getColumnModel()
                .getColumn(4).setPreferredWidth(75);

        studentTable.getColumnModel()
                .getColumn(5).setPreferredWidth(90);

        JScrollPane scrollPane =
                new JScrollPane(studentTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        scrollPane.getViewport().setBackground(WHITE);

        tablePanel.add(
                scrollPane, BorderLayout.CENTER
        );

        // Search and filters
        JPanel toolbar = new JPanel(
                new BorderLayout(10, 10)
        );

        toolbar.setBackground(WHITE);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );

        filters.setBackground(WHITE);

        searchField = new JTextField(15);

        searchField.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        searchField.setToolTipText(
                "Search student ID, index number or name"
        );

        searchField.setPreferredSize(
                new Dimension(190, 34)
        );

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(0, 9, 0, 9)
                )
        );

        courseFilter = new JComboBox<>(
                new String[]{
                        "All Courses"
                }
        );

        batchFilter = new JComboBox<>(
                new String[]{
                        "All Batches"
                }
        );

        courseFilter.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        batchFilter.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        courseFilter.setPreferredSize(
                new Dimension(125, 34)
        );

        batchFilter.setPreferredSize(
                new Dimension(120, 34)
        );

        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(courseFilter);
        filters.add(batchFilter);

        JButton refreshButton =
                createActionButton("Refresh", false);

        JButton viewButton =
                createActionButton("View Details", true);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );

        actions.setBackground(WHITE);
        actions.add(refreshButton);
        actions.add(viewButton);

        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        tablePanel.add(toolbar, BorderLayout.SOUTH);

        // Search listener
        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filterStudents();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filterStudents();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filterStudents();
                    }
                }
        );

        courseFilter.addActionListener(
                e -> filterStudents()
        );

        batchFilter.addActionListener(
                e -> filterStudents()
        );

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            if (courseFilter.getItemCount() > 0) courseFilter.setSelectedIndex(0);
            if (batchFilter.getItemCount() > 0) batchFilter.setSelectedIndex(0);
            loadStudents();
            JOptionPane.showMessageDialog(
                    this,
                    "Student list refreshed.",
                    "Refresh",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        viewButton.addActionListener(
                e -> viewSelectedStudent()
        );

        body.add(tablePanel, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);

        loadStudents();

        return content;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryCard(
            String title,
            JLabel value,
            Color accent,
            String description) {

        JPanel card = new JPanel(
                new BorderLayout(0, 9)
        );

        card.setBackground(WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(15, 17, 14, 17)
                )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 11)
        );

        titleLabel.setForeground(MUTED);

        value.setFont(
                new Font("Arial", Font.BOLD, 29)
        );

        value.setForeground(accent);

        JLabel descriptionLabel =
                new JLabel(description);

        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 10)
        );

        descriptionLabel.setForeground(MUTED);

        JPanel lower = new JPanel();

        lower.setBackground(WHITE);

        lower.setLayout(
                new BoxLayout(lower, BoxLayout.Y_AXIS)
        );

        lower.add(value);
        lower.add(descriptionLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);

        return card;
    }

    // ================= TABLE RENDERERS =================

    private DefaultTableCellRenderer centerRenderer() {

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();

        renderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        return renderer;
    }

    private DefaultTableCellRenderer statusRenderer() {

        return new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean selected,
                    boolean focused,
                    int row,
                    int column) {

                JLabel label = (JLabel)
                        super.getTableCellRendererComponent(
                                table,
                                value,
                                selected,
                                focused,
                                row,
                                column
                        );

                label.setHorizontalAlignment(
                        SwingConstants.CENTER
                );

                label.setFont(
                        new Font("Arial", Font.BOLD, 11)
                );

                if (!selected) {

                    if ("Active".equalsIgnoreCase(String.valueOf(value))) {
                        label.setForeground(
                                new Color(29, 130, 103)
                        );

                        label.setBackground(
                                new Color(232, 247, 240)
                        );

                    } else {
                        label.setForeground(RED);

                        label.setBackground(
                                new Color(253, 235, 235)
                        );
                    }

                } else {
                    label.setForeground(TEXT);
                }

                return label;
            }
        };
    }

    // ================= BUTTONS =================

    private JButton createActionButton(
            String text,
            boolean primary) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        button.setForeground(
                primary ? WHITE : NAVY
        );

        button.setBackground(
                primary ? NAVY : WHITE
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setPreferredSize(
                new Dimension(
                        primary ? 120 : 90,
                        35
                )
        );

        button.setBorder(
                primary
                        ? BorderFactory.createEmptyBorder(
                        7, 12, 7, 12
                )
                        : BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER
                        ),
                        new EmptyBorder(
                                6, 11, 6, 11
                        )
                )
        );

        return button;
    }

    // ================= LOAD STUDENTS =================

    /**
     * Loads actual student records enrolled in the active lecturer's course offerings from the database.
     * Uses DBConnection.getConnection() safely and handles errors without crashing.
     */
    private void loadStudents() {
        loadedStudents.clear();
        tableModel.setRowCount(0);

        // Resolve lecturer ID
        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        // If no lecturer ID is available
        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            if (subtitleLabel != null) {
                subtitleLabel.setText("No active lecturer ID. Open from Lecturer Dashboard or provide an active lecturer ID.");
            }
            if (tableHintLabel != null) {
                tableHintLabel.setText("NO LECTURER ID");
            }
            updateSummary();
            return;
        }

        String sql =
                "SELECT ug.student_id, ug.index_no, u.first_name, u.last_name, u.email, " +
                "       c.course_code, c.course_name, ug.batch, ug.student_status, " +
                "       u.status AS user_status, e.status AS enrollment_status " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN enrollments e ON co.offering_id = e.offering_id " +
                "JOIN undergraduates ug ON e.student_id = ug.student_id " +
                "JOIN users u ON ug.student_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code, ug.student_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String indexNo = rs.getString("index_no");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String email = rs.getString("email");
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String batch = rs.getString("batch");
                    String studentStatus = rs.getString("student_status");
                    String userStatus = rs.getString("user_status");

                    // Status formatting matching table status renderer ("Active" vs "Inactive")
                    String displayStatus = "Active";
                    if (userStatus != null && !userStatus.equalsIgnoreCase("ACTIVE")) {
                        displayStatus = "Inactive";
                    }

                    loadedStudents.add(new StudentItem(
                            studentId, indexNo, fullName, courseCode, courseName,
                            batch, displayStatus, email, studentStatus
                    ));
                }
            }

            // Populate dynamic filter options
            populateFilterDropdowns();

            if (loadedStudents.isEmpty()) {
                if (subtitleLabel != null) {
                    subtitleLabel.setText("No students enrolled in courses assigned to lecturer " + lecturerId + ".");
                }
                if (tableHintLabel != null) {
                    tableHintLabel.setText("0 STUDENTS");
                }
            } else {
                if (subtitleLabel != null) {
                    subtitleLabel.setText("View and manage students enrolled in your courses (" + lecturerId + ").");
                }
                if (tableHintLabel != null) {
                    tableHintLabel.setText(loadedStudents.size() + " STUDENTS ENROLLED");
                }
            }

            for (StudentItem student : loadedStudents) {
                tableModel.addRow(student.toTableRow());
            }

        } catch (SQLException e) {
            System.err.println("[Student] Database error while loading students: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error loading students: " + e.getMessage());
            }
            if (tableHintLabel != null) {
                tableHintLabel.setText("DB ERROR");
            }
        }

        updateSummary();
    }

    private void populateFilterDropdowns() {
        if (courseFilter == null || batchFilter == null) {
            return;
        }

        String currentCourse = (String) courseFilter.getSelectedItem();
        String currentBatch = (String) batchFilter.getSelectedItem();

        // Update Course Filter
        courseFilter.removeAllItems();
        courseFilter.addItem("All Courses");
        Set<String> courses = new TreeSet<>();
        for (StudentItem s : loadedStudents) {
            if (!s.courseCode.isEmpty()) courses.add(s.courseCode);
        }
        for (String c : courses) {
            courseFilter.addItem(c);
        }

        // Update Batch Filter
        batchFilter.removeAllItems();
        batchFilter.addItem("All Batches");
        Set<String> batches = new TreeSet<>();
        for (StudentItem s : loadedStudents) {
            if (!s.batch.isEmpty()) batches.add(s.batch);
        }
        for (String b : batches) {
            batchFilter.addItem(b);
        }

        if (currentCourse != null && courses.contains(currentCourse)) {
            courseFilter.setSelectedItem(currentCourse);
        } else {
            courseFilter.setSelectedIndex(0);
        }

        if (currentBatch != null && batches.contains(currentBatch)) {
            batchFilter.setSelectedItem(currentBatch);
        } else {
            batchFilter.setSelectedIndex(0);
        }
    }

    // ================= SEARCH AND FILTER =================

    private void filterStudents() {

        if (tableModel == null) {
            return;
        }

        String query = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String selectedCourse =
                (String) courseFilter.getSelectedItem();

        String selectedBatch =
                (String) batchFilter.getSelectedItem();

        tableModel.setRowCount(0);

        for (StudentItem student : loadedStudents) {

            String id = student.studentId.toLowerCase(Locale.ROOT);
            String index = student.indexNumber.toLowerCase(Locale.ROOT);
            String name = student.studentName.toLowerCase(Locale.ROOT);
            String course = student.courseCode;
            String batch = student.batch;

            boolean matchesSearch =
                    id.contains(query)
                            || index.contains(query)
                            || name.contains(query);

            boolean matchesCourse =
                    selectedCourse == null
                            || selectedCourse.equals("All Courses")
                            || selectedCourse.equalsIgnoreCase(course);

            boolean matchesBatch =
                    selectedBatch == null
                            || selectedBatch.equals("All Batches")
                            || batch.contains(selectedBatch)
                            || selectedBatch.equals(batch);

            if (matchesSearch
                    && matchesCourse
                    && matchesBatch) {

                tableModel.addRow(student.toTableRow());
            }
        }

        updateSummary();
    }

    // ================= UPDATE SUMMARY =================

    private void updateSummary() {

        if (tableModel == null
                || totalStudentsValue == null
                || activeStudentsValue == null
                || courseCountValue == null) {
            return;
        }

        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            totalStudentsValue.setText("-");
            activeStudentsValue.setText("-");
            courseCountValue.setText("-");
            return;
        }

        int total = tableModel.getRowCount();
        int active = 0;
        Set<String> courses = new java.util.HashSet<>();

        for (int i = 0; i < total; i++) {

            Object statusObj = tableModel.getValueAt(i, 5);
            Object courseObj = tableModel.getValueAt(i, 3);

            if (statusObj != null && "Active".equalsIgnoreCase(statusObj.toString())) {
                active++;
            }

            if (courseObj != null) {
                courses.add(courseObj.toString());
            }
        }

        totalStudentsValue.setText(
                String.valueOf(total)
        );

        activeStudentsValue.setText(
                String.valueOf(active)
        );

        courseCountValue.setText(
                String.valueOf(courses.size())
        );
    }

    private StudentItem findStudentByIdAndCourse(String id, String course) {
        if (id == null) return null;
        for (StudentItem item : loadedStudents) {
            if (item.studentId.equalsIgnoreCase(id) &&
                    (course == null || item.courseCode.equalsIgnoreCase(course))) {
                return item;
            }
        }
        return null;
    }

    // ================= VIEW STUDENT DETAILS =================

    private void viewSelectedStudent() {

        int selectedRow =
                studentTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student from the table.",
                    "No Student Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int row = studentTable.convertRowIndexToModel(
                selectedRow
        );

        String id =
                tableModel.getValueAt(row, 0).toString();

        String index =
                tableModel.getValueAt(row, 1).toString();

        String name =
                tableModel.getValueAt(row, 2).toString();

        String course =
                tableModel.getValueAt(row, 3).toString();

        String batch =
                tableModel.getValueAt(row, 4).toString();

        String status =
                tableModel.getValueAt(row, 5).toString();

        StudentItem item = findStudentByIdAndCourse(id, course);

        JPanel details = new JPanel(
                new GridLayout(0, 1, 5, 8)
        );

        details.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );

        details.add(new JLabel("Student ID: " + id));
        details.add(new JLabel("Index Number: " + index));
        details.add(new JLabel("Student Name: " + name));
        if (item != null && !item.email.isEmpty()) {
            details.add(new JLabel("Email Address: " + item.email));
        }
        details.add(new JLabel("Course Code: " + course + (item != null && !item.courseName.isEmpty() ? " (" + item.courseName + ")" : "")));
        details.add(new JLabel("Batch: " + batch));
        details.add(new JLabel("Account Status: " + status));
        if (item != null && !item.studentStatus.isEmpty()) {
            details.add(new JLabel("Academic Status: " + item.studentStatus));
        }

        JOptionPane.showMessageDialog(
                this,
                details,
                "Student Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= PLACEHOLDER PAGES =================

    private void showComingSoon(String page) {

        JOptionPane.showMessageDialog(
                this,
                page + " page will be implemented next.",
                page,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        String testId = args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboard.setActiveLecturerId(testId);
        }

        SwingUtilities.invokeLater(() -> {
            new Student(testId).setVisible(true);
        });
    }
}
