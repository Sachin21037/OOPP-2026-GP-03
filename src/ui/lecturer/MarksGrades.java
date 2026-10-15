package ui;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Lecturer Marks / Grades UI for Faculty Academic Management System (FAMS).
 * Follows the existing FAMS Lecturer UI styling, color scheme, and navigation.
 * UI-only stage: no direct database queries or SQL connections.
 */
public class MarksGrades extends JFrame {

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
    private static final Color ACTIVE_BG = new Color(232, 240, 248);

    private static final Font BODY = new Font("Arial", Font.PLAIN, 13);

    // Active lecturer identifier
    private String lecturerId;

    // UI Components
    private JTable marksTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    private JComboBox<String> courseDropdown;
    private JComboBox<String> semesterDropdown;
    private JTextField searchField;

    private JLabel totalStudentsValue;
    private JLabel averageMarksValue;
    private JLabel highestMarksValue;
    private JLabel lowestMarksValue;
    private JLabel recordCountLabel;
    private JLabel emptyNoticeLabel;

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

        // Initial summary and empty-state check
        updateSummary();
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

        menu.add(createMenuButton("Attendance", false, () -> {
            new Attendance().setVisible(true);
            dispose();
        }));

        // Marks / Grades is active on this page
        menu.add(createMenuButton("Marks / Grades", true, () -> {
            // Already on Marks / Grades page
        }));

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
        button.setBackground(active ? ACTIVE_BG : WHITE);
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

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    button.setBackground(new Color(245, 248, 252));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(active ? ACTIVE_BG : WHITE);
            }
        });

        return button;
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

        JLabel subtitleLabel = new JLabel("View and record student marks, assessment scores, and final grades.");
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
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 17, 13, 17)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 28));
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

        courseDropdown = new JComboBox<>(new String[]{
                "All Courses",
                "CS101 - Introduction to Programming",
                "CS102 - Object Oriented Programming",
                "CS201 - Data Structures & Algorithms",
                "CS202 - Database Management Systems",
                "CS301 - Software Engineering"
        });
        courseDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        courseDropdown.setPreferredSize(new Dimension(210, 34));
        courseDropdown.setBackground(WHITE);

        semesterDropdown = new JComboBox<>(new String[]{
                "All Semesters",
                "Year 1 - Semester 1",
                "Year 1 - Semester 2",
                "Year 2 - Semester 1",
                "Year 2 - Semester 2"
        });
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
                "No marks available. Select a course or enter marks to get started.",
                SwingConstants.CENTER
        );
        emptyNoticeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        emptyNoticeLabel.setForeground(MUTED);
        emptyNoticeLabel.setBorder(new EmptyBorder(14, 10, 14, 10));

        tableCenter.add(emptyNoticeLabel, BorderLayout.NORTH);
        tableCenter.add(scrollPane, BorderLayout.CENTER);

        tablePanel.add(tableCenter, BorderLayout.CENTER);

        // Action Buttons Toolbar at Bottom of Table Card
        JPanel bottomActions = new JPanel(new BorderLayout(10, 0));
        bottomActions.setBackground(WHITE);
        bottomActions.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel hintLabel = new JLabel("Double-click a student row or click 'View Marks' to inspect assessment breakdown.");
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        hintLabel.setForeground(MUTED);

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

        bottomActions.add(hintLabel, BorderLayout.WEST);
        bottomActions.add(buttonGroup, BorderLayout.EAST);

        tablePanel.add(bottomActions, BorderLayout.SOUTH);

        // ================= EVENT LISTENERS =================

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

        filterButton.addActionListener(e -> applySearchFilter());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            if (courseDropdown.getItemCount() > 0) courseDropdown.setSelectedIndex(0);
            if (semesterDropdown.getItemCount() > 0) semesterDropdown.setSelectedIndex(0);
            if (tableSorter != null) tableSorter.setRowFilter(null);
            updateSummary();
        });

        viewButton.addActionListener(e -> viewSelectedRecordDetails());
        enterMarksButton.addActionListener(e -> openEnterMarksDialog(false));
        editButton.addActionListener(e -> openEnterMarksDialog(true));

        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Marks changes saved successfully (UI Stage).\nDatabase persistence will be connected in the next stage.",
                    "Save Marks",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        cancelButton.addActionListener(e -> {
            marksTable.clearSelection();
            JOptionPane.showMessageDialog(
                    this,
                    "Current selection / pending action cancelled.",
                    "Cancel",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            if (tableSorter != null) tableSorter.setRowFilter(null);
            updateSummary();
            JOptionPane.showMessageDialog(
                    this,
                    "Marks view refreshed (UI Stage).",
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
        button.setForeground(primary ? WHITE : NAVY);
        button.setBackground(primary ? NAVY : WHITE);
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
                    button.setBackground(new Color(24, 52, 79));
                } else {
                    button.setBackground(new Color(245, 248, 252));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primary ? NAVY : WHITE);
            }
        });

        return button;
    }

    // ================= ACTIONS & DIALOGS =================

    private void applySearchFilter() {
        if (tableSorter == null) return;
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
        }
        updateSummary();
    }

    private void updateSummary() {
        int totalRows = tableModel != null ? tableModel.getRowCount() : 0;
        int viewRows = marksTable != null ? marksTable.getRowCount() : 0;

        if (recordCountLabel != null) {
            recordCountLabel.setText(viewRows + " RECORDS");
        }

        if (totalRows == 0) {
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setText("No marks available. Select a course or enter marks to get started.");
                emptyNoticeLabel.setVisible(true);
            }
            if (totalStudentsValue != null) totalStudentsValue.setText("-");
            if (averageMarksValue != null) averageMarksValue.setText("-");
            if (highestMarksValue != null) highestMarksValue.setText("-");
            if (lowestMarksValue != null) lowestMarksValue.setText("-");
            return;
        }

        if (emptyNoticeLabel != null) {
            if (viewRows == 0) {
                emptyNoticeLabel.setText("No students match your search filter.");
                emptyNoticeLabel.setVisible(true);
            } else {
                emptyNoticeLabel.setVisible(false);
            }
        }

        double sum = 0.0;
        double max = -1.0;
        double min = 1000.0;
        int validCount = 0;

        for (int i = 0; i < totalRows; i++) {
            Object totalObj = tableModel.getValueAt(i, 6);
            if (totalObj != null) {
                try {
                    double val = Double.parseDouble(totalObj.toString().trim());
                    sum += val;
                    if (val > max) max = val;
                    if (val < min) min = val;
                    validCount++;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (totalStudentsValue != null) {
            totalStudentsValue.setText(String.valueOf(totalRows));
        }

        if (validCount > 0) {
            double avg = sum / validCount;
            if (averageMarksValue != null) averageMarksValue.setText(String.format(java.util.Locale.US, "%.1f", avg));
            if (highestMarksValue != null) highestMarksValue.setText(String.format(java.util.Locale.US, "%.1f", max));
            if (lowestMarksValue != null) lowestMarksValue.setText(String.format(java.util.Locale.US, "%.1f", min));
        } else {
            if (averageMarksValue != null) averageMarksValue.setText("-");
            if (highestMarksValue != null) highestMarksValue.setText("-");
            if (lowestMarksValue != null) lowestMarksValue.setText("-");
        }
    }

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
        String stuId = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String stuName = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String assign = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String quiz = String.valueOf(tableModel.getValueAt(modelRow, 3));
        String mid = String.valueOf(tableModel.getValueAt(modelRow, 4));
        String fin = String.valueOf(tableModel.getValueAt(modelRow, 5));
        String tot = String.valueOf(tableModel.getValueAt(modelRow, 6));
        String grade = String.valueOf(tableModel.getValueAt(modelRow, 7));

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Student ID:"));
        panel.add(new JLabel(stuId));
        panel.add(new JLabel("Student Name:"));
        panel.add(new JLabel(stuName));
        panel.add(new JLabel("Assignment Marks:"));
        panel.add(new JLabel(assign));
        panel.add(new JLabel("Quiz Marks:"));
        panel.add(new JLabel(quiz));
        panel.add(new JLabel("Midterm Exam Marks:"));
        panel.add(new JLabel(mid));
        panel.add(new JLabel("Final Exam Marks:"));
        panel.add(new JLabel(fin));
        panel.add(new JLabel("Total Marks:"));
        panel.add(new JLabel(tot));
        panel.add(new JLabel("Final Grade:"));
        panel.add(new JLabel(grade));

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Student Marks Details - " + stuId,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void openEnterMarksDialog(boolean isEdit) {
        int selectedModelRow = -1;
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
            selectedModelRow = marksTable.convertRowIndexToModel(selectedRow);
        }

        JTextField idField = new JTextField(isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 0)) : "STU001", 12);
        JTextField nameField = new JTextField(isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 1)) : "", 15);
        JTextField assignField = new JTextField(isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 2)) : "", 6);
        JTextField quizField = new JTextField(isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 3)) : "", 6);
        JTextField midField = new JTextField(isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 4)) : "", 6);
        JTextField finalField = new JTextField(isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 5)) : "", 6);

        if (isEdit) {
            idField.setEditable(false);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Student ID:"));
        form.add(idField);
        form.add(new JLabel("Student Name:"));
        form.add(nameField);
        form.add(new JLabel("Assignment (Max 20):"));
        form.add(assignField);
        form.add(new JLabel("Quiz (Max 10):"));
        form.add(quizField);
        form.add(new JLabel("Midterm Exam (Max 20):"));
        form.add(midField);
        form.add(new JLabel("Final Exam (Max 50):"));
        form.add(finalField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                isEdit ? "Edit Student Marks" : "Enter Student Marks",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String stuId = idField.getText().trim();
            String stuName = nameField.getText().trim();
            String assignStr = assignField.getText().trim();
            String quizStr = quizField.getText().trim();
            String midStr = midField.getText().trim();
            String finStr = finalField.getText().trim();

            if (stuId.isEmpty() || stuName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student ID and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double a = assignStr.isEmpty() ? 0.0 : Double.parseDouble(assignStr);
                double q = quizStr.isEmpty() ? 0.0 : Double.parseDouble(quizStr);
                double m = midStr.isEmpty() ? 0.0 : Double.parseDouble(midStr);
                double f = finStr.isEmpty() ? 0.0 : Double.parseDouble(finStr);

                if (a < 0 || a > 100 || q < 0 || q > 100 || m < 0 || m > 100 || f < 0 || f > 100) {
                    JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double total = a + q + m + f;
                String grade = computeGrade(total);

                if (isEdit) {
                    tableModel.setValueAt(stuName, selectedModelRow, 1);
                    tableModel.setValueAt(String.format(java.util.Locale.US, "%.1f", a), selectedModelRow, 2);
                    tableModel.setValueAt(String.format(java.util.Locale.US, "%.1f", q), selectedModelRow, 3);
                    tableModel.setValueAt(String.format(java.util.Locale.US, "%.1f", m), selectedModelRow, 4);
                    tableModel.setValueAt(String.format(java.util.Locale.US, "%.1f", f), selectedModelRow, 5);
                    tableModel.setValueAt(String.format(java.util.Locale.US, "%.1f", total), selectedModelRow, 6);
                    tableModel.setValueAt(grade, selectedModelRow, 7);
                    JOptionPane.showMessageDialog(this, "Marks updated for " + stuId + ".", "Edit Marks", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    tableModel.addRow(new Object[]{
                            stuId,
                            stuName,
                            String.format(java.util.Locale.US, "%.1f", a),
                            String.format(java.util.Locale.US, "%.1f", q),
                            String.format(java.util.Locale.US, "%.1f", m),
                            String.format(java.util.Locale.US, "%.1f", f),
                            String.format(java.util.Locale.US, "%.1f", total),
                            grade
                    });
                    JOptionPane.showMessageDialog(this, "Marks record added for " + stuId + ".", "Enter Marks", JOptionPane.INFORMATION_MESSAGE);
                }

                updateSummary();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric marks.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String computeGrade(double total) {
        if (total >= 85) return "A+";
        if (total >= 75) return "A";
        if (total >= 70) return "A-";
        if (total >= 65) return "B+";
        if (total >= 60) return "B";
        if (total >= 55) return "B-";
        if (total >= 50) return "C+";
        if (total >= 45) return "C";
        if (total >= 40) return "C-";
        if (total >= 30) return "D";
        return "F";
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
