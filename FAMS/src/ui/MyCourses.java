
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
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

    // Sample data - replace with database data later
    private final Object[][] courseData = {
            {"ICT2132", "Object Oriented Programming", 28,
                    "Semester 1"},
            {"ICT2142", "E-Business", 20, "Semester 1"},
            {"ICT2123", "Database Management", 22,
                    "Semester 1"},
            {"ICT2152", "Web Development", 16,
                    "Semester 2"}
    };

    public MyCourses() {
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
                "Students", false,
                () -> showComingSoon("Students")
        ));

        menu.add(createMenuButton(
                "Attendance", false,
                () -> showComingSoon("Attendance")
        ));

        menu.add(createMenuButton(
                "Marks / Grades", false,
                () -> showComingSoon("Marks / Grades")
        ));

        menu.add(createMenuButton(
                "Assignments", false,
                () -> showComingSoon("Assignments")
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

        JLabel subtitle = new JLabel(
                "View and manage the courses assigned to you."
        );
        subtitle.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );
        subtitle.setForeground(MUTED);
        subtitle.setBorder(
                new EmptyBorder(5, 1, 0, 0)
        );

        headingText.add(title);
        headingText.add(subtitle);

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

        totalCoursesValue = new JLabel("4");
        totalStudentsValue = new JLabel("86");

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

        JLabel countLabel = new JLabel("COURSES");
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

    private void loadCourses() {
        tableModel.setRowCount(0);

        for (Object[] course : courseData) {
            tableModel.addRow(course);
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

        for (Object[] course : courseData) {
            String code = course[0].toString()
                    .toLowerCase(Locale.ROOT);

            String name = course[1].toString()
                    .toLowerCase(Locale.ROOT);

            String courseSemester = course[3].toString();

            boolean matchesText =
                    code.contains(query)
                            || name.contains(query);

            boolean matchesSemester =
                    semester == null
                            || semester.equals("All Semesters")
                            || semester.equals(courseSemester);

            if (matchesText && matchesSemester) {
                tableModel.addRow(course);
            }
        }

        updateSummary();
    }

    private void updateSummary() {
        if (tableModel == null
                || totalCoursesValue == null
                || totalStudentsValue == null) {
            return;
        }

        int students = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            students += Integer.parseInt(
                    tableModel.getValueAt(i, 2).toString()
            );
        }

        totalCoursesValue.setText(
                String.valueOf(tableModel.getRowCount())
        );

        totalStudentsValue.setText(
                String.valueOf(students)
        );
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

        JOptionPane.showMessageDialog(
                this,
                "Course Code: " + courseCode
                        + "\nCourse Name: " + courseName
                        + "\nStudents: " + students
                        + "\nSemester: " + semester,
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
        SwingUtilities.invokeLater(
                () -> new MyCourses().setVisible(true)
        );
    }
}
