package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

public class MyCourses extends JFrame {

    private static final Color NAVY = new Color(31, 66, 99);
    private static final Color BACKGROUND =
            new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT =
            new Color(35, 48, 65);
    private static final Color MUTED =
            new Color(100, 116, 139);
    private static final Color TEAL =
            new Color(43, 169, 155);
    private static final Color BORDER =
            new Color(220, 227, 235);
    private static final Color RED =
            new Color(190, 55, 55);

    private static final Font BODY =
            new Font("Arial", Font.PLAIN, 13);

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> semesterFilter;

    private JLabel totalCoursesValue;
    private JLabel totalStudentsValue;
    private JLabel subtitleLabel;
    private JLabel countLabel;

    // Lecturer ID for identifying the active lecturer
    private String lecturerId;

    // Loaded courses from the database
    private final List<CourseItem> loadedCourses = new ArrayList<>();

    /**
     * Inner helper class to hold course details retrieved from database.
     */
    private static class CourseItem {
        final String courseCode;
        final String courseName;
        final int studentCount;
        final String semester;
        final String academicYear;
        final int credits;
        final String batch;

        CourseItem(String courseCode, String courseName, int studentCount,
                   String semester, String academicYear, int credits, String batch) {
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.studentCount = studentCount;
            this.semester = semester != null ? semester : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.credits = credits;
            this.batch = batch != null ? batch : "";
        }

        Object[] toTableRow() {
            return new Object[]{courseCode, courseName, studentCount, semester};
        }
    }

    /**
     * Default constructor: uses active lecturer ID from LecturerDashboard or JVM property.
     */
    public MyCourses() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor allowing an explicit lecturer ID.
     *
     * @param lecturerId the lecturer ID (e.g. "LEC001")
     */
    public MyCourses(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        setTitle(
                "Faculty Academic Management System - My Courses"
        );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
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
        title.setFont(new Font("Arial", Font.BOLD, 19));

        JLabel role = new JLabel("LECTURER");
        role.setForeground(WHITE);
        role.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(title, BorderLayout.WEST);
        header.add(role, BorderLayout.EAST);

        return header;
    }

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
                "My Courses", true, () -> {}
        ));

        menu.add(createMenuButton(
                "Students", false, () -> {
                    new Student().setVisible(true);
                    dispose();
                }
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
                "Profile", false, () -> {
                    new LecturerProfile().setVisible(true);
                    dispose();
                }
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
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );
        logout.setFocusPainted(false);
        logout.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

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
                        null, "You have been logged out."
                );
            }
        });

        bottom.add(logout, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createMenuButton(
            String text, boolean active, Runnable action) {

        JButton button = new JButton(text);

        button.setFont(active
                ? new Font("Arial", Font.BOLD, 13)
                : BODY);

        button.setForeground(active ? NAVY : TEXT);
        button.setBackground(
                active ? new Color(232, 240, 248) : WHITE
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );
        button.setPreferredSize(new Dimension(175, 43));

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
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> action.run());

        button.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {
                        if (!active) {
                            button.setBackground(
                                    new Color(245, 248, 252)
                            );
                        }
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {
                        button.setBackground(
                                active
                                        ? new Color(232, 240, 248)
                                        : WHITE
                        );
                    }
                }
        );

        return button;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(
                new BorderLayout(0, 22)
        );
        content.setBackground(BACKGROUND);
        content.setBorder(
                new EmptyBorder(27, 30, 28, 30)
        );

        JPanel pageHeading = new JPanel(
                new BorderLayout()
        );
        pageHeading.setBackground(BACKGROUND);

        JPanel headingText = new JPanel();
        headingText.setBackground(BACKGROUND);
        headingText.setLayout(
                new BoxLayout(headingText, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("My Courses");
        title.setFont(new Font("Arial", Font.BOLD, 29));
        title.setForeground(TEXT);

        subtitleLabel = new JLabel(
                "View and manage the courses assigned to you."
        );
        subtitleLabel.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setBorder(
                new EmptyBorder(5, 1, 0, 0)
        );

        headingText.add(title);
        headingText.add(subtitleLabel);

        pageHeading.add(headingText, BorderLayout.WEST);

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

        pageHeading.add(portalLabel, BorderLayout.EAST);
        content.add(pageHeading, BorderLayout.NORTH);

        JPanel body = new JPanel(
                new BorderLayout(0, 20)
        );
        body.setBackground(BACKGROUND);

        JPanel summary = new JPanel(
                new GridLayout(1, 2, 16, 0)
        );
        summary.setBackground(BACKGROUND);

        totalCoursesValue = new JLabel("-");
        totalStudentsValue = new JLabel("-");

        summary.add(createSummaryCard(
                "TOTAL COURSES",
                totalCoursesValue,
                NAVY,
                "Courses assigned to you"
        ));

        summary.add(createSummaryCard(
                "TOTAL STUDENTS",
                totalStudentsValue,
                TEAL,
                "Students across all courses"
        ));

        body.add(summary, BorderLayout.NORTH);

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

        JLabel tableTitle =
                new JLabel("Courses Assigned to Me");

        tableTitle.setFont(
                new Font("Arial", Font.BOLD, 18)
        );
        tableTitle.setForeground(TEXT);

        countLabel = new JLabel("COURSES");
        countLabel.setFont(
                new Font("Arial", Font.BOLD, 10)
        );
        countLabel.setForeground(MUTED);

        tableHeading.add(tableTitle, BorderLayout.WEST);
        tableHeading.add(countLabel, BorderLayout.EAST);

        tablePanel.add(tableHeading, BorderLayout.NORTH);

        String[] columns = {
                "Course Code",
                "Course Name",
                "Students",
                "Semester"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(43);
        courseTable.setFont(BODY);
        courseTable.setForeground(TEXT);
        courseTable.setBackground(WHITE);
        courseTable.setGridColor(BORDER);
        courseTable.setShowVerticalLines(false);
        courseTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        courseTable.setSelectionBackground(
                new Color(226, 238, 249)
        );
        courseTable.setSelectionForeground(TEXT);
        courseTable.setFillsViewportHeight(true);
        courseTable.setIntercellSpacing(
                new Dimension(0, 1)
        );

        JTableHeader header = courseTable.getTableHeader();
        header.setFont(
                new Font("Arial", Font.BOLD, 12)
        );
        header.setBackground(
                new Color(237, 242, 247)
        );
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer =
                new DefaultTableCellRenderer();

        leftRenderer.setBorder(
                new EmptyBorder(0, 10, 0, 10)
        );
        leftRenderer.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        courseTable.getColumnModel().getColumn(0)
                .setCellRenderer(leftRenderer);
        courseTable.getColumnModel().getColumn(1)
                .setCellRenderer(leftRenderer);
        courseTable.getColumnModel().getColumn(2)
                .setCellRenderer(centerRenderer());
        courseTable.getColumnModel().getColumn(3)
                .setCellRenderer(leftRenderer);

        courseTable.getColumnModel().getColumn(0)
                .setPreferredWidth(130);
        courseTable.getColumnModel().getColumn(1)
                .setPreferredWidth(330);
        courseTable.getColumnModel().getColumn(2)
                .setPreferredWidth(100);
        courseTable.getColumnModel().getColumn(3)
                .setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );
        scrollPane.getViewport().setBackground(WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel tools = new JPanel(
                new BorderLayout(12, 0)
        );
        tools.setBackground(WHITE);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        filters.setBackground(WHITE);

        searchField = new JTextField(18);
        searchField.setFont(BODY);
        searchField.setToolTipText(
                "Search by course code or course name"
        );
        searchField.setPreferredSize(
                new Dimension(210, 34)
        );
        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(0, 9, 0, 9)
                )
        );

        semesterFilter = new JComboBox<>(
                new String[]{
                        "All Semesters",
                        "Semester 1",
                        "Semester 2"
                }
        );
        semesterFilter.setFont(BODY);
        semesterFilter.setPreferredSize(
                new Dimension(145, 34)
        );
        semesterFilter.setBackground(WHITE);

        JButton refreshButton =
                createActionButton("Refresh", false);

        JButton viewButton =
                createActionButton("View Course", true);

        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(semesterFilter);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.setBackground(WHITE);
        actions.add(refreshButton);
        actions.add(viewButton);

        tools.add(filters, BorderLayout.WEST);
        tools.add(actions, BorderLayout.EAST);

        tablePanel.add(tools, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filterCourses();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filterCourses();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filterCourses();
                    }
                }
        );

        semesterFilter.addActionListener(
                e -> filterCourses()
        );

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            semesterFilter.setSelectedIndex(0);
            loadCourses();

            JOptionPane.showMessageDialog(
                    this,
                    "Course list refreshed.",
                    "Refresh",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        viewButton.addActionListener(
                e -> viewSelectedCourse()
        );

        body.add(tablePanel, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);

        loadCourses();

        return content;
    }

    private JPanel createSummaryCard(
            String title,
            JLabel valueLabel,
            Color accent,
            String caption) {

        JPanel card = new JPanel(
                new BorderLayout(0, 8)
        );
        card.setBackground(WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(15, 18, 14, 18)
                )
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 11)
        );
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(
                new Font("Arial", Font.BOLD, 30)
        );
        valueLabel.setForeground(accent);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );
        captionLabel.setForeground(MUTED);

        JPanel lower = new JPanel();
        lower.setBackground(WHITE);
        lower.setLayout(
                new BoxLayout(lower, BoxLayout.Y_AXIS)
        );

        lower.add(valueLabel);
        lower.add(captionLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);

        return card;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();

        renderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        return renderer;
    }

    private JButton createActionButton(
            String text, boolean primary) {

        JButton button = new JButton(text);
        button.setFont(
                new Font("Arial", Font.BOLD, 12)
        );
        button.setForeground(primary ? WHITE : NAVY);
        button.setBackground(primary ? NAVY : WHITE);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(
                new Dimension(primary ? 125 : 95, 36)
        );

        button.setBorder(primary
                ? BorderFactory.createEmptyBorder(
                8, 14, 8, 14
        )
                : BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 13, 7, 13)
        ));

        return button;
    }

    private String formatSemester(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.equalsIgnoreCase("SEMESTER 1")) {
            return "Semester 1";
        } else if (trimmed.equalsIgnoreCase("SEMESTER 2")) {
            return "Semester 2";
        }
        return trimmed;
    }

    /**
     * Loads actual courses assigned to the active lecturer from the MySQL database.
     * Reuses DBConnection.getConnection() safely.
     */
    private void loadCourses() {
        loadedCourses.clear();
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
            if (countLabel != null) {
                countLabel.setText("NO LECTURER ID");
            }
            updateSummary();
            return;
        }

        String sql =
                "SELECT c.course_code, c.course_name, co.semester, co.academic_year, co.batch, c.credits, " +
                "       COUNT(DISTINCT e.student_id) AS student_count " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "LEFT JOIN enrollments e ON co.offering_id = e.offering_id " +
                "WHERE cl.lecturer_id = ? " +
                "GROUP BY c.course_code, c.course_name, co.semester, co.academic_year, co.batch, c.credits, co.offering_id " +
                "ORDER BY co.semester, c.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    String rawSemester = rs.getString("semester");
                    String formattedSemester = formatSemester(rawSemester);
                    String academicYear = rs.getString("academic_year");
                    String batch = rs.getString("batch");
                    int credits = rs.getInt("credits");
                    int students = rs.getInt("student_count");

                    loadedCourses.add(new CourseItem(
                            code, name, students, formattedSemester, academicYear, credits, batch
                    ));
                }
            }

            if (loadedCourses.isEmpty()) {
                if (subtitleLabel != null) {
                    subtitleLabel.setText("No courses currently assigned to lecturer " + lecturerId + ".");
                }
                if (countLabel != null) {
                    countLabel.setText("0 COURSES");
                }
            } else {
                if (subtitleLabel != null) {
                    subtitleLabel.setText("View and manage the courses assigned to you (" + lecturerId + ").");
                }
                if (countLabel != null) {
                    countLabel.setText(loadedCourses.size() + " COURSES");
                }
            }

            for (CourseItem course : loadedCourses) {
                tableModel.addRow(course.toTableRow());
            }

        } catch (SQLException e) {
            System.err.println("[MyCourses] Database error while loading courses: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error loading courses: " + e.getMessage());
            }
            if (countLabel != null) {
                countLabel.setText("DB ERROR");
            }
        }

        updateSummary();
    }

    private void filterCourses() {
        if (tableModel == null || courseTable == null) {
            return;
        }

        String query = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String semester =
                (String) semesterFilter.getSelectedItem();

        tableModel.setRowCount(0);

        for (CourseItem course : loadedCourses) {
            String code = course.courseCode.toLowerCase(Locale.ROOT);
            String name = course.courseName.toLowerCase(Locale.ROOT);
            String courseSemester = course.semester;

            boolean matchesText =
                    code.contains(query)
                            || name.contains(query);

            boolean matchesSemester =
                    semester == null
                            || semester.equals("All Semesters")
                            || semester.equalsIgnoreCase(courseSemester);

            if (matchesText && matchesSemester) {
                tableModel.addRow(course.toTableRow());
            }
        }

        if (countLabel != null && lecturerId != null) {
            countLabel.setText(tableModel.getRowCount() + " COURSES");
        }

        updateSummary();
    }

    private void updateSummary() {
        if (tableModel == null
                || totalCoursesValue == null
                || totalStudentsValue == null) {
            return;
        }

        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            totalCoursesValue.setText("-");
            totalStudentsValue.setText("-");
            return;
        }

        int students = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object val = tableModel.getValueAt(i, 2);
            if (val != null) {
                try {
                    students += Integer.parseInt(val.toString().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        totalCoursesValue.setText(
                String.valueOf(tableModel.getRowCount())
        );

        totalStudentsValue.setText(
                String.valueOf(students)
        );
    }

    private CourseItem findCourseByCode(String code) {
        if (code == null) return null;
        for (CourseItem item : loadedCourses) {
            if (item.courseCode.equalsIgnoreCase(code)) {
                return item;
            }
        }
        return null;
    }

    private void viewSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a course first.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow =
                courseTable.convertRowIndexToModel(selectedRow);

        String courseCode =
                tableModel.getValueAt(modelRow, 0).toString();

        String courseName =
                tableModel.getValueAt(modelRow, 1).toString();

        String students =
                tableModel.getValueAt(modelRow, 2).toString();

        String semester =
                tableModel.getValueAt(modelRow, 3).toString();

        CourseItem item = findCourseByCode(courseCode);
        StringBuilder details = new StringBuilder();
        details.append("Course Code: ").append(courseCode)
                .append("\nCourse Name: ").append(courseName)
                .append("\nStudents: ").append(students)
                .append("\nSemester: ").append(semester);

        if (item != null) {
            if (!item.academicYear.isEmpty()) {
                details.append("\nAcademic Year: ").append(item.academicYear);
            }
            if (item.credits > 0) {
                details.append("\nCredits: ").append(item.credits);
            }
            if (!item.batch.isEmpty()) {
                details.append("\nBatch: ").append(item.batch);
            }
        }

        JOptionPane.showMessageDialog(
                this,
                details.toString(),
                "Course Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showComingSoon(String page) {
        JOptionPane.showMessageDialog(
                this,
                page + " page will be added next.",
                page,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        String testId = args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboard.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(
                () -> new MyCourses(testId).setVisible(true)
        );
    }
}
