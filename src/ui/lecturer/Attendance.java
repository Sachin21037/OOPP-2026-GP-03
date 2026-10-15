package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class Attendance extends JFrame {

    // Theme colours matching existing Lecturer pages
    private static final Color NAVY = new Color(31, 66, 99);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(35, 48, 65);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color TEAL = new Color(43, 169, 155);
    private static final Color PURPLE = new Color(132, 78, 190);
    private static final Color BORDER = new Color(220, 227, 235);
    private static final Color RED = new Color(190, 55, 55);

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

    public Attendance() {
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

    // ================= HEADER =================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(new EmptyBorder(0, 26, 0, 26));

        JLabel title = new JLabel("FACULTY ACADEMIC MANAGEMENT SYSTEM");
        title.setForeground(WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 19));

        JLabel role = new JLabel("LECTURER");
        role.setForeground(WHITE);
        role.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(title, BorderLayout.WEST);
        header.add(role, BorderLayout.EAST);

        return header;
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JPanel menu = new JPanel();
        menu.setBackground(WHITE);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(25, 14, 12, 14));

        JLabel menuTitle = new JLabel("LECTURER MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 11));
        menuTitle.setForeground(MUTED);
        menuTitle.setBorder(new EmptyBorder(0, 9, 18, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(menuTitle);

        menu.add(createMenuButton("Dashboard", false, () -> {
            new LecturerDashboard().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("My Courses", false, () -> {
            new MyCourses().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("Students", false, () -> {
            new Student().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("Attendance", true, () -> {
            // Already on Attendance page
        }));

        menu.add(createMenuButton("Marks / Grades", false, () -> showComingSoon("Marks / Grades")));

        menu.add(createMenuButton("Assignments", false, () -> showComingSoon("Assignments")));

        menu.add(createMenuButton("Profile", false, () -> {
            new LecturerProfile().setVisible(true);
            dispose();
        }));

        sidebar.add(menu, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(WHITE);
        bottom.setBorder(new EmptyBorder(10, 14, 22, 14));

        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Arial", Font.BOLD, 13));
        logout.setForeground(RED);
        logout.setBackground(WHITE);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10, 10, 10, 10));
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
                JOptionPane.showMessageDialog(null, "You have been logged out.");
            }
        });

        bottom.add(logout, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createMenuButton(String text, boolean active, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(active ? new Font("Arial", Font.BOLD, 13) : BODY);
        button.setForeground(active ? NAVY : TEXT);
        button.setBackground(active ? new Color(232, 240, 248) : WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(175, 43));

        button.setBorder(BorderFactory.createCompoundBorder(
                active ? BorderFactory.createMatteBorder(0, 4, 0, 0, NAVY)
                        : BorderFactory.createEmptyBorder(0, 4, 0, 0),
                new EmptyBorder(0, 13, 0, 8)
        ));

        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!active) {
                    button.setBackground(new Color(245, 248, 252));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(active ? new Color(232, 240, 248) : WHITE);
            }
        });

        return button;
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

        JLabel subtitle = new JLabel("View and manage student attendance records.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        subtitle.setBorder(new EmptyBorder(5, 1, 0, 0));

        headingText.add(pageTitle);
        headingText.add(subtitle);

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

        courseDropdown = new JComboBox<>(new String[]{"Select Course"});
        courseDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        courseDropdown.setPreferredSize(new Dimension(150, 34));
        courseDropdown.setBackground(WHITE);

        dateField = new JTextField(10);
        dateField.setFont(new Font("Arial", Font.PLAIN, 12));
        dateField.setToolTipText("Date format: YYYY-MM-DD");
        dateField.setPreferredSize(new Dimension(115, 34));
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));

        searchField = new JTextField(14);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Filter by student ID or student name");
        searchField.setPreferredSize(new Dimension(180, 34));
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

        // UI-only action listeners (no database calls)
        searchButton.addActionListener(e -> {
            String selectedCourse = (String) courseDropdown.getSelectedItem();
            if ("Select Course".equals(selectedCourse)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a course to search attendance records.",
                        "Course Required",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Searching attendance records for " + selectedCourse + "...\n(Database integration pending)",
                        "Attendance Search",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        resetButton.addActionListener(e -> {
            courseDropdown.setSelectedIndex(0);
            dateField.setText("");
            searchField.setText("");
            tableModel.setRowCount(0);
            updateSummary();
            emptyNoticeLabel.setText("No attendance records available. Select a course and date to view records.");
        });

        refreshButton.addActionListener(e -> {
            updateSummary();
            JOptionPane.showMessageDialog(
                    this,
                    "Attendance view refreshed.",
                    "Refresh",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        body.add(tablePanel, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);

        updateSummary();

        return content;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryCard(String title, JLabel value, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 9));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 17, 14, 17)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 29));
        value.setForeground(accent);

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
                    if ("Present".equalsIgnoreCase(String.valueOf(value))) {
                        label.setForeground(new Color(29, 130, 103));
                        label.setBackground(new Color(232, 247, 240));
                    } else if ("Absent".equalsIgnoreCase(String.valueOf(value))) {
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

    private JButton createActionButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(primary ? WHITE : NAVY);
        button.setBackground(primary ? NAVY : WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(primary ? 110 : 85, 34));

        button.setBorder(primary
                ? BorderFactory.createEmptyBorder(7, 12, 7, 12)
                : BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 11, 6, 11)
        ));

        return button;
    }

    private void updateSummary() {
        if (tableModel == null) {
            return;
        }

        int total = tableModel.getRowCount();
        if (total == 0) {
            totalStudentsValue.setText("-");
            presentValue.setText("-");
            absentValue.setText("-");
            attendanceRateValue.setText("-");
            if (recordCountLabel != null) {
                recordCountLabel.setText("0 RECORDS");
            }
            return;
        }

        int presentCount = 0;
        int absentCount = 0;

        for (int i = 0; i < total; i++) {
            Object statusObj = tableModel.getValueAt(i, 4);
            if (statusObj != null) {
                String status = statusObj.toString();
                if ("Present".equalsIgnoreCase(status)) {
                    presentCount++;
                } else if ("Absent".equalsIgnoreCase(status)) {
                    absentCount++;
                }
            }
        }

        totalStudentsValue.setText(String.valueOf(total));
        presentValue.setText(String.valueOf(presentCount));
        absentValue.setText(String.valueOf(absentCount));

        int rate = (int) Math.round(((double) presentCount / total) * 100);
        attendanceRateValue.setText(rate + "%");

        if (recordCountLabel != null) {
            recordCountLabel.setText(total + " RECORDS");
        }
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
        SwingUtilities.invokeLater(() -> {
            new Attendance().setVisible(true);
        });
    }
}
