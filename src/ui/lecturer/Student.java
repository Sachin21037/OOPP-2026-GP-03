
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Locale;

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

    /*
     * Sample data for GUI testing only.
     * Replace this with database data later.
     */
    private final Object[][] studentData = {
            {"stu01", "ICT/2023/001", "Kamal Perera",
                    "ICT2132", "2023", "Active"},
            {"stu02", "ICT/2023/002", "Nimali Silva",
                    "ICT2142", "2023", "Active"},
            {"stu03", "ICT/2023/003", "Amal Fernando",
                    "ICT2123", "2023", "Active"},
            {"stu04", "ICT/2023/004", "Sajini Kumari",
                    "ICT2152", "2023", "Active"},
            {"stu05", "ICT/2024/001", "Nuwan Jayasuriya",
                    "ICT2132", "2024", "Active"},
            {"stu06", "ICT/2024/002", "Tharushi Silva",
                    "ICT2142", "2024", "Inactive"},
            {"stu07", "ICT/2024/003", "Kasun Madushan",
                    "ICT2123", "2024", "Active"},
            {"stu08", "ICT/2024/004", "Dinithi Perera",
                    "ICT2152", "2024", "Active"}
    };

    public Student() {

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

        JLabel subtitle = new JLabel(
                "View and manage students enrolled in your courses."
        );

        subtitle.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        subtitle.setForeground(MUTED);
        subtitle.setBorder(
                new EmptyBorder(5, 1, 0, 0)
        );

        headingText.add(pageTitle);
        headingText.add(subtitle);

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

        totalStudentsValue = new JLabel("8");
        activeStudentsValue = new JLabel("7");
        courseCountValue = new JLabel("4");

        summary.add(createSummaryCard(
                "TOTAL STUDENTS",
                totalStudentsValue,
                NAVY,
                "Students in this sample list"
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

        JLabel tableHint = new JLabel(
                "Select a student to view details"
        );

        tableHint.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );

        tableHint.setForeground(MUTED);

        tableHeading.add(
                tableTitle, BorderLayout.WEST
        );

        tableHeading.add(
                tableHint, BorderLayout.EAST
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
                        "All Courses",
                        "ICT2132",
                        "ICT2142",
                        "ICT2123",
                        "ICT2152"
                }
        );

        batchFilter = new JComboBox<>(
                new String[]{
                        "All Batches",
                        "2023",
                        "2024"
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

            courseFilter.setSelectedIndex(0);
            batchFilter.setSelectedIndex(0);

            loadStudents();

        });

        viewButton.addActionListener(
                e -> viewSelectedStudent()
        );

        studentTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        // Selection is handled by View Details.
                    }
                });

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

                    if ("Active".equals(value)) {
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

    private void loadStudents() {

        tableModel.setRowCount(0);

        for (Object[] student : studentData) {
            tableModel.addRow(student);
        }

        updateSummary();
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

        for (Object[] student : studentData) {

            String id = student[0].toString()
                    .toLowerCase(Locale.ROOT);

            String index = student[1].toString()
                    .toLowerCase(Locale.ROOT);

            String name = student[2].toString()
                    .toLowerCase(Locale.ROOT);

            String course = student[3].toString();

            String batch = student[4].toString();

            boolean matchesSearch =
                    id.contains(query)
                            || index.contains(query)
                            || name.contains(query);

            boolean matchesCourse =
                    selectedCourse == null
                            || selectedCourse.equals("All Courses")
                            || selectedCourse.equals(course);

            boolean matchesBatch =
                    selectedBatch == null
                            || selectedBatch.equals("All Batches")
                            || selectedBatch.equals(batch);

            if (matchesSearch
                    && matchesCourse
                    && matchesBatch) {

                tableModel.addRow(student);
            }
        }

        updateSummary();
    }

    // ================= UPDATE SUMMARY =================

    private void updateSummary() {

        if (tableModel == null) {
            return;
        }

        int total = tableModel.getRowCount();
        int active = 0;
        java.util.Set<String> courses =
                new java.util.HashSet<>();

        for (int i = 0; i < total; i++) {

            String status =
                    tableModel.getValueAt(i, 5).toString();

            String course =
                    tableModel.getValueAt(i, 3).toString();

            if ("Active".equals(status)) {
                active++;
            }

            courses.add(course);
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

        JPanel details = new JPanel(
                new GridLayout(0, 1, 5, 8)
        );

        details.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );

        details.add(new JLabel("Student ID: " + id));
        details.add(new JLabel("Index Number: " + index));
        details.add(new JLabel("Student Name: " + name));
        details.add(new JLabel("Course Code: " + course));
        details.add(new JLabel("Batch: " + batch));
        details.add(new JLabel("Status: " + status));

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

        SwingUtilities.invokeLater(() -> {
            new Student().setVisible(true);
        });
    }
}
