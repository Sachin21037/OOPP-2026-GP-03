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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lecturer Assignment UI for Faculty Academic Management System (FAMS).
 * Follows the existing FAMS Lecturer UI design system, styling, and navigation.
 * UI-only stage: no direct database queries or SQL connections.
 */
public class Assignments extends JFrame {

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
    private JTable assignmentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    private JComboBox<String> courseDropdown;
    private JComboBox<String> semesterDropdown;
    private JComboBox<String> statusFilterDropdown;
    private JTextField searchField;

    private JLabel totalAssignmentsValue;
    private JLabel upcomingValue;
    private JLabel dueSoonValue;
    private JLabel closedValue;
    private JLabel recordCountLabel;
    private JLabel emptyNoticeLabel;

    // Counter for generated assignment IDs during in-memory UI interactions
    private int nextAssignmentCounter = 1;

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

        // Initial summary and empty-state check (0 initial records)
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

        menu.add(createMenuButton("Marks / Grades", false, () -> {
            new MarksGrades().setVisible(true);
            dispose();
        }));

        // Assignments is active on this page
        menu.add(createMenuButton("Assignments", true, () -> {
            // Already on Assignments page
        }));

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

        JLabel pageTitle = new JLabel("Assignments");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(TEXT);

        JLabel subtitleLabel = new JLabel("Create, manage, and review course assignments and submission deadlines.");
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

        courseDropdown = new JComboBox<>(new String[]{"All Courses"});
        courseDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        courseDropdown.setPreferredSize(new Dimension(190, 34));
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
        JPanel bottomActions = new JPanel(new BorderLayout(10, 0));
        bottomActions.setBackground(WHITE);
        bottomActions.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel hintLabel = new JLabel("Double-click an assignment row or click 'View Assignment' to inspect details.");
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        hintLabel.setForeground(MUTED);

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

        bottomActions.add(hintLabel, BorderLayout.WEST);
        bottomActions.add(buttonGroup, BorderLayout.EAST);

        tablePanel.add(bottomActions, BorderLayout.SOUTH);

        // ================= EVENT LISTENERS =================

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

        courseDropdown.addActionListener(e -> applyFilters());
        semesterDropdown.addActionListener(e -> applyFilters());
        statusFilterDropdown.addActionListener(e -> applyFilters());
        filterButton.addActionListener(e -> applyFilters());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            if (courseDropdown.getItemCount() > 0) courseDropdown.setSelectedIndex(0);
            if (semesterDropdown.getItemCount() > 0) semesterDropdown.setSelectedIndex(0);
            if (statusFilterDropdown.getItemCount() > 0) statusFilterDropdown.setSelectedIndex(0);
            if (tableSorter != null) tableSorter.setRowFilter(null);
            updateSummary();
        });

        addButton.addActionListener(e -> openAddEditDialog(false));
        viewButton.addActionListener(e -> viewSelectedAssignment());
        editButton.addActionListener(e -> openAddEditDialog(true));
        deleteButton.addActionListener(e -> deleteSelectedAssignment());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            if (courseDropdown.getItemCount() > 0) courseDropdown.setSelectedIndex(0);
            if (semesterDropdown.getItemCount() > 0) semesterDropdown.setSelectedIndex(0);
            if (statusFilterDropdown.getItemCount() > 0) statusFilterDropdown.setSelectedIndex(0);
            if (tableSorter != null) tableSorter.setRowFilter(null);
            updateSummary();
            JOptionPane.showMessageDialog(
                    this,
                    "Assignments view refreshed (UI Stage).",
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

    // ================= FILTERS & SUMMARY =================

    private void applyFilters() {
        if (tableSorter == null) return;

        String query = searchField != null ? searchField.getText().trim() : "";
        String statusFilter = statusFilterDropdown != null ? (String) statusFilterDropdown.getSelectedItem() : "All Statuses";

        List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();

        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
        }

        if (statusFilter != null && !statusFilter.equals("All Statuses")) {
            filters.add(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(statusFilter) + "$", 7));
        }

        if (filters.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.andFilter(filters));
        }

        updateSummary();
    }

    private void updateSummary() {
        int totalRows = tableModel != null ? tableModel.getRowCount() : 0;
        int viewRows = assignmentTable != null ? assignmentTable.getRowCount() : 0;

        if (recordCountLabel != null) {
            recordCountLabel.setText(viewRows + " RECORDS");
        }

        if (totalRows == 0) {
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setText("No assignments available. Select a course or add a new assignment.");
                emptyNoticeLabel.setVisible(true);
            }
            if (totalAssignmentsValue != null) totalAssignmentsValue.setText("-");
            if (upcomingValue != null) upcomingValue.setText("-");
            if (dueSoonValue != null) dueSoonValue.setText("-");
            if (closedValue != null) closedValue.setText("-");
            return;
        }

        if (emptyNoticeLabel != null) {
            if (viewRows == 0) {
                emptyNoticeLabel.setText("No assignments match your filter or search query.");
                emptyNoticeLabel.setVisible(true);
            } else {
                emptyNoticeLabel.setVisible(false);
            }
        }

        int activeCount = 0;
        int dueCount = 0;
        int closedCount = 0;

        for (int i = 0; i < totalRows; i++) {
            Object statusObj = tableModel.getValueAt(i, 7);
            if (statusObj != null) {
                String s = statusObj.toString().trim();
                if ("Active".equalsIgnoreCase(s) || "Upcoming".equalsIgnoreCase(s)) {
                    activeCount++;
                } else if ("Due".equalsIgnoreCase(s)) {
                    dueCount++;
                } else if ("Closed".equalsIgnoreCase(s)) {
                    closedCount++;
                }
            }
        }

        if (totalAssignmentsValue != null) totalAssignmentsValue.setText(String.valueOf(totalRows));
        if (upcomingValue != null) upcomingValue.setText(String.valueOf(activeCount));
        if (dueSoonValue != null) dueSoonValue.setText(String.valueOf(dueCount));
        if (closedValue != null) closedValue.setText(String.valueOf(closedCount));
    }

    // ================= ACTIONS & DIALOGS =================

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
        String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String title = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String course = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String desc = String.valueOf(tableModel.getValueAt(modelRow, 3));
        String assignedDate = String.valueOf(tableModel.getValueAt(modelRow, 4));
        String dueDate = String.valueOf(tableModel.getValueAt(modelRow, 5));
        String maxMarks = String.valueOf(tableModel.getValueAt(modelRow, 6));
        String status = String.valueOf(tableModel.getValueAt(modelRow, 7));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 6));
        grid.add(new JLabel("Assignment ID:"));
        grid.add(new JLabel("<html><b>" + id + "</b></html>"));
        grid.add(new JLabel("Assignment Title:"));
        grid.add(new JLabel("<html><b>" + title + "</b></html>"));
        grid.add(new JLabel("Course:"));
        grid.add(new JLabel(course));
        grid.add(new JLabel("Assigned Date:"));
        grid.add(new JLabel(assignedDate));
        grid.add(new JLabel("Due Date:"));
        grid.add(new JLabel(dueDate));
        grid.add(new JLabel("Maximum Marks:"));
        grid.add(new JLabel(maxMarks));
        grid.add(new JLabel("Status:"));
        grid.add(new JLabel("<html><font color='#1F4263'><b>" + status + "</b></font></html>"));

        JTextArea descArea = new JTextArea(desc != null && !desc.isEmpty() ? desc : "No description provided.");
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setBackground(new Color(245, 248, 252));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 8, 8, 8)
        ));

        panel.add(grid, BorderLayout.NORTH);
        panel.add(new JLabel("Description:"), BorderLayout.CENTER);
        panel.add(new JScrollPane(descArea), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Assignment Details - " + id,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void openAddEditDialog(boolean isEdit) {
        int selectedModelRow = -1;
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
            selectedModelRow = assignmentTable.convertRowIndexToModel(selectedRow);
        }

        String initialId = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 0)) : String.format(Locale.US, "ASN%03d", nextAssignmentCounter);
        String initialTitle = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 1)) : "";
        String initialCourse = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 2)) : "";
        String initialDesc = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 3)) : "";
        String initialAssignedDate = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 4)) : new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String initialDueDate = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 5)) : "";
        String initialMaxMarks = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 6)) : "100";
        String initialStatus = isEdit ? String.valueOf(tableModel.getValueAt(selectedModelRow, 7)) : "Active";

        JTextField titleField = new JTextField(initialTitle, 20);
        JTextField courseField = new JTextField(initialCourse, 20);
        courseField.setToolTipText("Enter course code or name (e.g., CS101)");

        JTextArea descArea = new JTextArea(initialDesc, 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(4, 4, 4, 4)
        ));

        JTextField assignedDateField = new JTextField(initialAssignedDate, 10);
        assignedDateField.setToolTipText("Format: YYYY-MM-DD");

        JTextField dueDateField = new JTextField(initialDueDate, 10);
        dueDateField.setToolTipText("Format: YYYY-MM-DD");

        JTextField maxMarksField = new JTextField(initialMaxMarks, 6);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "Upcoming", "Due", "Closed"});
        statusBox.setSelectedItem(initialStatus);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Assignment ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(new JLabel("<html><b>" + initialId + "</b></html>"), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Assignment Title *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(titleField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Course *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(courseField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(new JScrollPane(descArea), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Assigned Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(assignedDateField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Due Date (YYYY-MM-DD) *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(dueDateField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Maximum Marks *:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(maxMarksField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; form.add(statusBox, gbc);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                isEdit ? "Edit Assignment" : "Add Assignment",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String course = courseField.getText().trim();
            String desc = descArea.getText().trim();
            String assignedDate = assignedDateField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String maxMarksStr = maxMarksField.getText().trim();
            String status = (String) statusBox.getSelectedItem();

            // 1. Validation: Title must not be empty
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Assignment title must not be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Validation: Course must be selected / entered
            if (course.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Course must be specified.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Validation: Maximum marks must be a positive number
            double maxMarks;
            try {
                maxMarks = Double.parseDouble(maxMarksStr);
                if (maxMarks <= 0) {
                    JOptionPane.showMessageDialog(this, "Maximum marks must be a positive number greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Maximum marks must be a valid numeric value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 4. Validation: Due date must not be earlier than assigned date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            if (!assignedDate.isEmpty() && !dueDate.isEmpty()) {
                try {
                    Date dAssigned = sdf.parse(assignedDate);
                    Date dDue = sdf.parse(dueDate);
                    if (dDue.before(dAssigned)) {
                        JOptionPane.showMessageDialog(this, "Due date must not be earlier than the assigned date.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this, "Dates must follow the format YYYY-MM-DD (e.g. 2026-03-15).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (isEdit) {
                tableModel.setValueAt(title, selectedModelRow, 1);
                tableModel.setValueAt(course, selectedModelRow, 2);
                tableModel.setValueAt(desc, selectedModelRow, 3);
                tableModel.setValueAt(assignedDate, selectedModelRow, 4);
                tableModel.setValueAt(dueDate, selectedModelRow, 5);
                tableModel.setValueAt(String.format(Locale.US, "%.0f", maxMarks), selectedModelRow, 6);
                tableModel.setValueAt(status, selectedModelRow, 7);
                JOptionPane.showMessageDialog(this, "Assignment '" + initialId + "' updated successfully (UI Stage).", "Assignment Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                tableModel.addRow(new Object[]{
                        initialId,
                        title,
                        course,
                        desc,
                        assignedDate,
                        dueDate,
                        String.format(Locale.US, "%.0f", maxMarks),
                        status
                });
                nextAssignmentCounter++;
                JOptionPane.showMessageDialog(this, "Assignment '" + initialId + "' added successfully (UI Stage).", "Assignment Added", JOptionPane.INFORMATION_MESSAGE);
            }

            updateSummary();
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
        String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String title = String.valueOf(tableModel.getValueAt(modelRow, 1));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete assignment:\n" + id + " - " + title + "?",
                "Confirm Delete Assignment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(modelRow);
            updateSummary();
            JOptionPane.showMessageDialog(
                    this,
                    "Assignment '" + id + "' deleted successfully (UI Stage).",
                    "Deleted",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
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
