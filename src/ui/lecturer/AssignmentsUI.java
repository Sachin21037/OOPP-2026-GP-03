package ui.lecturer;

import dao.AssignmentsDAO;
import dao.AssignmentsDAO.AssignmentRecord;
import dao.AssignmentsDAO.AssignmentSummary;
import dao.AssignmentsDAO.CourseOfferingItem;

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
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * AssignmentsUI - Presentation layer for Lecturer Assignments Management.
 * Matches the official FAMS portal design and provides full DB-backed CRUD operations.
 */
public class AssignmentsUI extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color BUTTON_BLUE = new Color(29, 78, 216); // Royal Blue #1D4ED8
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color SLATE = new Color(100, 116, 139);

    protected String lecturerId;

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable assignmentTable;

    private JComboBox<String> courseFilterCombo;
    private JComboBox<String> semesterFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JTextField searchField;

    private JLabel totalAssignmentsValue;
    private JLabel activeAssignmentsValue;
    private JLabel dueSoonValue;
    private JLabel closedAssignmentsValue;

    private JLabel subtitleLabel;
    private JLabel tableCountLabel;

    private final List<AssignmentRecord> loadedAssignments = new ArrayList<>();
    private final List<CourseOfferingItem> courseOfferings = new ArrayList<>();

    public AssignmentsUI() {
        this(resolveActiveLecturerId());
    }

    public AssignmentsUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : resolveActiveLecturerId();

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Assignments");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadDatabaseData();
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
        return "LEC001";
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Assignments", lecturerId, this);
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Top Section: Title Bar + Summary Cards
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // 1. Page Title Bar: Left Title + Subtitle | Right: LECTURER PORTAL badge
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel mainTitle = new JLabel("Assignments");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 26));
        mainTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("Displaying course assignments for assigned offerings (Lecturer: " + (lecturerId != null ? lecturerId : "LEC001") + ").");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);

        titles.add(mainTitle);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // Right Badge
        JPanel portalBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        portalBadge.setBackground(WHITE);
        portalBadge.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        JLabel portalLbl = new JLabel("LECTURER PORTAL");
        portalLbl.setFont(new Font("Arial", Font.BOLD, 11));
        portalLbl.setForeground(TEXT);
        portalBadge.add(portalLbl);

        titleBar.add(titles, BorderLayout.WEST);
        titleBar.add(portalBadge, BorderLayout.EAST);
        topSection.add(titleBar);
        topSection.add(Box.createVerticalStrut(16));

        // 2. Summary Cards (4 in horizontal row)
        topSection.add(createSummarySection());
        content.add(topSection, BorderLayout.NORTH);

        // 3. Main Table Container Card
        content.add(createTablePanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummarySection() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(0, 92));

        totalAssignmentsValue = new JLabel("0");
        activeAssignmentsValue = new JLabel("0");
        dueSoonValue = new JLabel("0");
        closedAssignmentsValue = new JLabel("0");

        summaryPanel.add(createSummaryCard("TOTAL ASSIGNMENTS", totalAssignmentsValue, NAVY, "All course assignments"));
        summaryPanel.add(createSummaryCard("UPCOMING / ACTIVE", activeAssignmentsValue, TEAL, "Currently ongoing"));
        summaryPanel.add(createSummaryCard("DUE SOON", dueSoonValue, PURPLE, "Pending submission"));
        summaryPanel.add(createSummaryCard("CLOSED", closedAssignmentsValue, SLATE, "Submissions closed"));

        return summaryPanel;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color stripeColor, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(226, 232, 240));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                // 3px top horizontal stripe
                g2.setColor(stripeColor);
                g2.fillRect(0, 0, getWidth(), 3);
                // 4px left vertical stripe
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 11));
        titleLbl.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 26));
        value.setForeground(TEXT);

        JLabel capLbl = new JLabel(caption);
        capLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        capLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(capLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout(0, 12));
        tableContainer.setBackground(WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 14, 18)
        ));

        // Container Top Area: Title Bar + Filter Bar
        JPanel containerTop = new JPanel();
        containerTop.setLayout(new BoxLayout(containerTop, BoxLayout.Y_AXIS));
        containerTop.setOpaque(false);

        // Header Row: Course Assignments List | X RECORDS
        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);

        JLabel listTitle = new JLabel("Course Assignments List");
        listTitle.setFont(new Font("Arial", Font.BOLD, 15));
        listTitle.setForeground(TEXT);

        tableCountLabel = new JLabel("0 RECORDS");
        tableCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        tableCountLabel.setForeground(MUTED);

        listHeader.add(listTitle, BorderLayout.WEST);
        listHeader.add(tableCountLabel, BorderLayout.EAST);
        containerTop.add(listHeader);
        containerTop.add(Box.createVerticalStrut(10));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);

        JLabel courseLbl = new JLabel("Course:");
        courseLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        courseLbl.setForeground(TEXT);

        courseFilterCombo = new JComboBox<>(new String[]{"All Courses"});
        courseFilterCombo.setFont(LecturerTheme.FONT_PLAIN_13);
        courseFilterCombo.setPreferredSize(new Dimension(150, 32));
        courseFilterCombo.addActionListener(e -> applyFilters());

        JLabel semLbl = new JLabel("Semester:");
        semLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        semLbl.setForeground(TEXT);

        semesterFilterCombo = new JComboBox<>(new String[]{"All Semesters"});
        semesterFilterCombo.setFont(LecturerTheme.FONT_PLAIN_13);
        semesterFilterCombo.setPreferredSize(new Dimension(130, 32));
        semesterFilterCombo.addActionListener(e -> applyFilters());

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        statusLbl.setForeground(TEXT);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Active", "Due Soon", "Closed"});
        statusFilterCombo.setFont(LecturerTheme.FONT_PLAIN_13);
        statusFilterCombo.setPreferredSize(new Dimension(120, 32));
        statusFilterCombo.addActionListener(e -> applyFilters());

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        searchLbl.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(LecturerTheme.FONT_PLAIN_13);
        searchField.setPreferredSize(new Dimension(150, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        JButton resetBtn = createStyledButton("Reset", WHITE, TEXT, 75);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            if (courseFilterCombo.getItemCount() > 0) courseFilterCombo.setSelectedIndex(0);
            if (semesterFilterCombo.getItemCount() > 0) semesterFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
        });

        filterBar.add(courseLbl);
        filterBar.add(courseFilterCombo);
        filterBar.add(Box.createHorizontalStrut(4));
        filterBar.add(semLbl);
        filterBar.add(semesterFilterCombo);
        filterBar.add(Box.createHorizontalStrut(4));
        filterBar.add(statusLbl);
        filterBar.add(statusFilterCombo);
        filterBar.add(Box.createHorizontalStrut(4));
        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(Box.createHorizontalStrut(4));
        filterBar.add(resetBtn);

        containerTop.add(filterBar);
        tableContainer.add(containerTop, BorderLayout.NORTH);

        // Table
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
            public boolean isCellEditable(int row, int column) { return false; }
        };

        rowSorter = new TableRowSorter<>(tableModel);
        assignmentTable = new JTable(tableModel);
        assignmentTable.setRowSorter(rowSorter);
        assignmentTable.setRowHeight(34);
        assignmentTable.setFont(LecturerTheme.FONT_PLAIN_13);
        assignmentTable.setGridColor(new Color(241, 245, 249));
        assignmentTable.setSelectionBackground(new Color(239, 246, 255));
        assignmentTable.setSelectionForeground(NAVY);
        assignmentTable.setShowGrid(true);

        JTableHeader th = assignmentTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(new Color(51, 65, 85));
        th.setPreferredSize(new Dimension(0, 34));

        assignmentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        assignmentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        assignmentTable.getColumnModel().getColumn(2).setPreferredWidth(210);
        assignmentTable.getColumnModel().getColumn(3).setPreferredWidth(220);
        assignmentTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        assignmentTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        assignmentTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        assignmentTable.getColumnModel().getColumn(7).setPreferredWidth(85);

        assignmentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 10, 0, 0));
        assignmentTable.getColumnModel().getColumn(1).setCellRenderer(leftIndentRenderer);
        assignmentTable.getColumnModel().getColumn(2).setCellRenderer(leftIndentRenderer);
        assignmentTable.getColumnModel().getColumn(3).setCellRenderer(leftIndentRenderer);

        assignmentTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        assignmentTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer());
        assignmentTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer());
        assignmentTable.getColumnModel().getColumn(7).setCellRenderer(statusRenderer());

        assignmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedAssignment();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom Toolbar: Left helper text | Right 5 Action Buttons
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel helperText = new JLabel("<html>Double-click an assignment row or click 'View<br>Assignment' to inspect details.</html>");
        helperText.setFont(new Font("Arial", Font.PLAIN, 12));
        helperText.setForeground(MUTED);

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bottomButtons.setOpaque(false);

        // 1. [ Add Assignment ] (Royal Blue)
        JButton addBtn = createStyledButton("Add Assignment", BUTTON_BLUE, Color.WHITE, 115);
        addBtn.addActionListener(e -> openAddEditDialog(false));

        // 2. [ View Assignment ] (Royal Blue)
        JButton viewBtn = createStyledButton("View Assignment", BUTTON_BLUE, Color.WHITE, 115);
        viewBtn.addActionListener(e -> viewSelectedAssignment());

        // 3. [ Edit Assignment ] (White with gray border)
        JButton editBtn = createStyledButton("Edit Assignment", WHITE, TEXT, 110);
        editBtn.addActionListener(e -> openAddEditDialog(true));

        // 4. [ Delete Assignment ] (White with gray border)
        JButton deleteBtn = createStyledButton("Delete Assignment", WHITE, TEXT, 118);
        deleteBtn.addActionListener(e -> deleteSelectedAssignment());

        // 5. [ Refresh ] (White with gray border)
        JButton refreshBtn = createStyledButton("Refresh", WHITE, TEXT, 72);
        refreshBtn.addActionListener(e -> loadDatabaseData());

        bottomButtons.add(addBtn);
        bottomButtons.add(viewBtn);
        bottomButtons.add(editBtn);
        bottomButtons.add(deleteBtn);
        bottomButtons.add(refreshBtn);

        bottomBar.add(helperText, BorderLayout.WEST);
        bottomBar.add(bottomButtons, BorderLayout.EAST);
        tableContainer.add(bottomBar, BorderLayout.SOUTH);

        return tableContainer;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String s = value != null ? value.toString() : "";

                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                    l.setFont(LecturerTheme.FONT_BOLD_12);
                    return l;
                }

                if ("Active".equalsIgnoreCase(s) || "Upcoming".equalsIgnoreCase(s)) {
                    l.setBackground(new Color(220, 252, 231)); // Soft green
                    l.setForeground(new Color(22, 163, 74));   // Green text
                } else if ("Due Soon".equalsIgnoreCase(s) || "Due".equalsIgnoreCase(s)) {
                    l.setBackground(new Color(254, 243, 199)); // Soft amber
                    l.setForeground(new Color(217, 119, 6));   // Amber text
                } else {
                    l.setBackground(new Color(241, 245, 249)); // Soft slate/gray
                    l.setForeground(new Color(100, 116, 139)); // Slate text
                }
                l.setOpaque(true);
                l.setFont(LecturerTheme.FONT_BOLD_12);
                return l;
            }
        };
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int minWidth) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(WHITE.equals(bg) ? new Color(243, 244, 246) : bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (WHITE.equals(bg)) {
                    g2.setColor(new Color(209, 213, 219));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }

                g2.setFont(getFont());
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(LecturerTheme.FONT_BOLD_12);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Math.max(minWidth, 80), 34));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        return btn;
    }

    /**
     * Loads assignments and course offerings via AssignmentsDAO from MySQL.
     */
    public void loadDatabaseData() {
        loadedAssignments.clear();
        courseOfferings.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        try {
            List<CourseOfferingItem> offerings = AssignmentsDAO.loadCourseOfferings(lecturerId);
            courseOfferings.addAll(offerings);

            // Populate course and semester dropdowns
            courseFilterCombo.removeAllItems();
            courseFilterCombo.addItem("All Courses");
            Set<String> sems = new TreeSet<>();
            for (CourseOfferingItem item : courseOfferings) {
                courseFilterCombo.addItem(item.courseCode);
                if (!item.semester.isEmpty()) sems.add(item.semester);
            }

            semesterFilterCombo.removeAllItems();
            semesterFilterCombo.addItem("All Semesters");
            for (String s : sems) {
                semesterFilterCombo.addItem(s);
            }

            List<AssignmentRecord> assignments = AssignmentsDAO.loadAssignments(lecturerId);
            loadedAssignments.addAll(assignments);

            if (subtitleLabel != null) {
                subtitleLabel.setText("Displaying course assignments for assigned offerings (Lecturer: " + (lecturerId != null ? lecturerId : "LEC001") + ").");
            }
        } catch (SQLException e) {
            System.err.println("[AssignmentsUI] Error loading assignments: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error: " + e.getMessage());
            }
        }

        applyFilters();
    }

    public void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        String selCourse = courseFilterCombo != null ? (String) courseFilterCombo.getSelectedItem() : null;
        String selSem = semesterFilterCombo != null ? (String) semesterFilterCombo.getSelectedItem() : null;
        String selStatus = statusFilterCombo != null ? (String) statusFilterCombo.getSelectedItem() : null;
        String search = searchField != null ? searchField.getText().trim() : "";

        List<AssignmentRecord> filtered = AssignmentsDAO.filterAssignments(loadedAssignments, selCourse, selSem, selStatus, search);

        for (AssignmentRecord a : filtered) {
            tableModel.addRow(a.toTableRow());
        }

        if (tableCountLabel != null) {
            tableCountLabel.setText(filtered.size() + " RECORDS");
        }

        AssignmentSummary summary = AssignmentsDAO.calculateSummary(filtered);
        if (totalAssignmentsValue != null) totalAssignmentsValue.setText(String.valueOf(summary.totalAssignments));
        if (activeAssignmentsValue != null) activeAssignmentsValue.setText(String.valueOf(summary.activeCount));
        if (dueSoonValue != null) dueSoonValue.setText(String.valueOf(summary.dueCount));
        if (closedAssignmentsValue != null) closedAssignmentsValue.setText(String.valueOf(summary.closedCount));
    }

    private void viewSelectedAssignment() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an assignment from the table to view details.",
                    "View Assignment",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = assignmentTable.convertRowIndexToModel(selectedRow);
        String aid = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String title = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String course = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String desc = String.valueOf(tableModel.getValueAt(modelRow, 3));
        String assignedDate = String.valueOf(tableModel.getValueAt(modelRow, 4));
        String due = String.valueOf(tableModel.getValueAt(modelRow, 5));
        String max = String.valueOf(tableModel.getValueAt(modelRow, 6));
        String status = String.valueOf(tableModel.getValueAt(modelRow, 7));

        int marksCount = 0;
        try {
            marksCount = AssignmentsDAO.countSubmittedMarks(aid);
        } catch (SQLException ignored) {
        }

        JDialog dialog = new JDialog(this, "Assignment Details - " + aid, true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(8, 2, 10, 10));
        p.setBackground(WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        p.add(makeFieldLabel("Assignment ID:"));
        p.add(makeFieldValue(aid));

        p.add(makeFieldLabel("Assignment Title:"));
        p.add(makeFieldValue(title));

        p.add(makeFieldLabel("Course:"));
        p.add(makeFieldValue(course));

        p.add(makeFieldLabel("Description / Scope:"));
        p.add(makeFieldValue(desc));

        p.add(makeFieldLabel("Assigned Date:"));
        p.add(makeFieldValue(assignedDate));

        p.add(makeFieldLabel("Due Date:"));
        p.add(makeFieldValue(due));

        p.add(makeFieldLabel("Maximum Marks:"));
        p.add(makeFieldValue(max));

        p.add(makeFieldLabel("Status / Submitted:"));
        p.add(makeFieldValue(status + " (" + marksCount + " student marks)"));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(WHITE);
        JButton closeBtn = createStyledButton("Close", BUTTON_BLUE, Color.WHITE, 90);
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);

        dialog.add(p, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LecturerTheme.FONT_BOLD_12);
        l.setForeground(MUTED);
        return l;
    }

    private JLabel makeFieldValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LecturerTheme.FONT_PLAIN_13);
        l.setForeground(TEXT);
        return l;
    }

    private void openAddEditDialog(boolean isEdit) {
        if (courseOfferings.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No assigned course offerings available. Cannot create assignments.",
                    "Unavailable",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aidToEdit = null;
        AssignmentRecord recordToEdit = null;
        if (isEdit) {
            int selectedRow = assignmentTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select an assignment from the table to edit.",
                        "Edit Assignment",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = assignmentTable.convertRowIndexToModel(selectedRow);
            aidToEdit = String.valueOf(tableModel.getValueAt(modelRow, 0));
            for (AssignmentRecord a : loadedAssignments) {
                if (a.assignmentId.equalsIgnoreCase(aidToEdit)) {
                    recordToEdit = a;
                    break;
                }
            }
        }

        JDialog dialog = new JDialog(this, isEdit ? "Edit Assignment - " + aidToEdit : "Add New Assignment", true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(WHITE);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 14));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JComboBox<CourseOfferingItem> offeringCombo = new JComboBox<>(courseOfferings.toArray(new CourseOfferingItem[0]));
        offeringCombo.setFont(LecturerTheme.FONT_PLAIN_13);

        // Pre-select course offering if editing
        if (recordToEdit != null) {
            for (int i = 0; i < offeringCombo.getItemCount(); i++) {
                CourseOfferingItem itm = offeringCombo.getItemAt(i);
                if (itm.offeringId.equalsIgnoreCase(recordToEdit.offeringId) || itm.courseCode.equalsIgnoreCase(recordToEdit.courseCode)) {
                    offeringCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        JTextField titleField = new JTextField(recordToEdit != null ? recordToEdit.title : "");
        titleField.setFont(LecturerTheme.FONT_PLAIN_13);

        JTextField maxMarksField = new JTextField(recordToEdit != null ? String.format(Locale.US, "%.0f", recordToEdit.maxMarks) : "100");
        maxMarksField.setFont(LecturerTheme.FONT_PLAIN_13);

        JTextField weightField = new JTextField(recordToEdit != null ? String.format(Locale.US, "%.0f", recordToEdit.weight) : "10");
        weightField.setFont(LecturerTheme.FONT_PLAIN_13);

        String initialDate = (recordToEdit != null && !"-".equals(recordToEdit.dueDate))
                ? recordToEdit.dueDate
                : new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        JTextField dueDateField = new JTextField(initialDate);
        dueDateField.setFont(LecturerTheme.FONT_PLAIN_13);

        form.add(makeFieldLabel("Course Offering:"));
        form.add(offeringCombo);

        form.add(makeFieldLabel("Assignment Title:"));
        form.add(titleField);

        form.add(makeFieldLabel("Maximum Marks:"));
        form.add(maxMarksField);

        form.add(makeFieldLabel("Weight (%):"));
        form.add(weightField);

        form.add(makeFieldLabel("Due Date (YYYY-MM-DD):"));
        form.add(dueDateField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(WHITE);
        btnPanel.setBorder(new EmptyBorder(0, 0, 10, 20));

        JButton cancelBtn = createStyledButton("Cancel", WHITE, TEXT, 85);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton(isEdit ? "Save Changes" : "Create", BUTTON_BLUE, Color.WHITE, 120);

        final String finalAid = aidToEdit;
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter an assignment title.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double maxM;
            try {
                maxM = Double.parseDouble(maxMarksField.getText().trim());
                if (maxM <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Maximum marks must be greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric value for Maximum Marks.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightField.getText().trim());
                if (weight < 0 || weight > 100) {
                    JOptionPane.showMessageDialog(dialog, "Weight percentage must be between 0 and 100.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric value for Weight (%).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date dueD = null;
            String dateTxt = dueDateField.getText().trim();
            if (!dateTxt.isEmpty() && !"-".equals(dateTxt)) {
                try {
                    dueD = Date.valueOf(dateTxt);
                } catch (IllegalArgumentException dateEx) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid date in YYYY-MM-DD format (e.g. 2026-11-15).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            CourseOfferingItem selectedOffering = (CourseOfferingItem) offeringCombo.getSelectedItem();
            String offId = selectedOffering != null ? selectedOffering.offeringId : "";

            try {
                if (isEdit) {
                    boolean ok = AssignmentsDAO.updateAssignment(finalAid, offId, title, maxM, weight, dueD, lecturerId);
                    if (ok) {
                        JOptionPane.showMessageDialog(dialog, "Assignment '" + finalAid + "' updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadDatabaseData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to update assignment in database.", "Update Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    String newId = AssignmentsDAO.insertAssignment(offId, title, maxM, weight, dueD, lecturerId);
                    JOptionPane.showMessageDialog(dialog, "Assignment '" + newId + "' created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadDatabaseData();
                }
            } catch (SQLException sqlEx) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + sqlEx.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedAssignment() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an assignment from the table to delete.",
                    "Delete Assignment",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = assignmentTable.convertRowIndexToModel(selectedRow);
        String aid = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String title = String.valueOf(tableModel.getValueAt(modelRow, 1));

        try {
            int marksCount = AssignmentsDAO.countSubmittedMarks(aid);
            String confirmMessage;
            if (marksCount > 0) {
                confirmMessage = "Assignment '" + aid + " - " + title + "' currently has " + marksCount + " student mark record(s) in the database.\n\n" +
                        "Deleting this assignment will permanently remove this assessment and its student marks.\n" +
                        "Are you sure you want to proceed?";
            } else {
                confirmMessage = "Are you sure you want to delete assignment:\n" + aid + " - " + title + "?";
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    confirmMessage,
                    "Confirm Delete Assignment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            boolean deleted = AssignmentsDAO.deleteAssignment(aid, lecturerId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Assignment '" + aid + "' deleted successfully.", "Assignment Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadDatabaseData();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete assignment (record not found).", "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        String testId = (args != null && args.length > 0) ? args[0] : null;
        if (testId != null) {
            LecturerDashboardUI.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new AssignmentsUI(testId).setVisible(true);
        });
    }
}
