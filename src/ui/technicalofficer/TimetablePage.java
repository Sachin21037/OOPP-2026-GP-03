package ui.technicalofficer;

import model.TimetableEntry;
import service.TimetableService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Academic Timetable Management for Technical Officers in FAMS.
 * Allows Technical Officers to view and schedule timetables for their department.
 * Provides both a Weekly Timetable Matrix View (Monday to Friday/Saturday) and a Detailed Table View.
 * Supports filtering by Academic Year, Semester, Day of Week, Session Type (Labs vs Lectures),
 * and Department, along with new timetable slot creation.
 */
public class TimetablePage extends JPanel {

    // Theme palette harmonized with FAMS Technical Officer Dashboard
    private final Color darkNavy = new Color(27, 66, 104);       // #1B4268
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color lightBg = new Color(245, 247, 250);       // #F5F7FA
    private final Color borderColor = new Color(226, 232, 240);   // #E2E8F0
    private final Color textDark = new Color(30, 41, 59);         // #1E293B
    private final Color textMuted = new Color(100, 116, 139);     // #64748B
    private final Color accentBlue = new Color(29, 78, 216);      // #1D4ED8
    private final Color tealLab = new Color(15, 118, 110);        // #0F766E
    private final Color tealLabBg = new Color(204, 251, 241);     // #CCFBF1
    private final Color purpleLec = new Color(126, 34, 206);      // #7E22CE
    private final Color purpleLecBg = new Color(243, 232, 255);   // #F3E8FF

    private final TimetableService timetableService = new TimetableService();
    private String officerDepartment = "Information and Communication Technology";

    // UI Filter Controls
    private JLabel subtitleLabel;
    private JComboBox<String> deptFilter;
    private JComboBox<String> academicYearFilter;
    private JComboBox<String> semesterFilter;
    private JComboBox<String> dayFilter;
    private JComboBox<String> sessionTypeFilter;
    private JTextField searchField;

    // View Switcher (Weekly Grid vs Table View)
    private JButton weeklyViewBtn;
    private JButton tableViewBtn;
    private CardLayout viewCards;
    private JPanel viewsContainer;
    private String currentView = "WEEKLY";

    // Table View Components
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    // Weekly Grid View Components
    private JPanel weeklyGridContainer;

    // Empty State Panel
    private JPanel emptyStatePanel;

    // Summary Stat Badges
    private JLabel totalSlotsBadge;
    private JLabel labSlotsBadge;
    private JLabel lectureSlotsBadge;

    // Cross-module navigation callbacks
    private Runnable onNavigateToNotices;
    private Runnable onNavigateToAttendance;
    private Runnable onNavigateToLabMaintenance;

    public void setOnNavigateToNotices(Runnable onNavigateToNotices) {
        this.onNavigateToNotices = onNavigateToNotices;
    }

    public void setOnNavigateToAttendance(Runnable onNavigateToAttendance) {
        this.onNavigateToAttendance = onNavigateToAttendance;
    }

    public void setOnNavigateToLabMaintenance(Runnable onNavigateToLabMaintenance) {
        this.onNavigateToLabMaintenance = onNavigateToLabMaintenance;
    }

    public TimetablePage() {
        this("Information and Communication Technology");
    }

    public TimetablePage(String department) {
        if (department != null && !department.isBlank()) {
            this.officerDepartment = department;
        }
        setLayout(new BorderLayout());
        setBackground(lightBg);

        initUI();
        refreshTimetable();
    }

    public void setOfficerDepartment(String newDept) {
        if (newDept != null && !newDept.isBlank() && !newDept.equalsIgnoreCase(this.officerDepartment)) {
            this.officerDepartment = newDept;
            populateDepartmentFilter();
            refreshTimetable();
        }
    }

    public String getOfficerDepartment() {
        return officerDepartment;
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 16));
        mainContainer.setBackground(lightBg);
        mainContainer.setBorder(new EmptyBorder(22, 28, 24, 28));

        // 1. Top Header Banner with Stats & Actions
        JPanel headerPanel = createHeaderPanel();

        // 2. Filter Bar & View Mode Switcher
        JPanel filterAndControlsCard = createFilterAndControlsPanel();

        // 3. Central Views Container (Weekly Grid vs Detailed Table)
        viewsContainer = new JPanel();
        viewCards = new CardLayout();
        viewsContainer.setLayout(viewCards);
        viewsContainer.setBackground(lightBg);

        // A. Weekly Grid View (Mon-Fri/Sat)
        weeklyGridContainer = new JPanel(new BorderLayout());
        weeklyGridContainer.setBackground(lightBg);

        // B. Detailed Table View
        JPanel tableViewWrapper = createTableViewPanel();

        // C. Empty State View
        emptyStatePanel = createEmptyStatePanel();

        viewsContainer.add(weeklyGridContainer, "WEEKLY");
        viewsContainer.add(tableViewWrapper, "TABLE");
        viewsContainer.add(emptyStatePanel, "EMPTY");

        // Assemble Central Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setBackground(lightBg);
        centerPanel.add(filterAndControlsCard, BorderLayout.NORTH);
        centerPanel.add(viewsContainer, BorderLayout.CENTER);

        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(centerPanel, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Department Academic Timetables");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);

        subtitleLabel = new JLabel("Weekly lecture, laboratory, and tutorial schedule for " + officerDepartment);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitleLabel);

        // Stats Badges + Quick Actions
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statsPanel.setOpaque(false);

        totalSlotsBadge = createStatBadge("Total Slots: 0", new Color(241, 245, 249), textDark);
        labSlotsBadge = createStatBadge("Lab Practicals: 0", new Color(204, 251, 241), tealLab);
        lectureSlotsBadge = createStatBadge("Lectures: 0", new Color(243, 232, 255), purpleLec);

        JButton resetDeptBtn = new JButton("My Department");
        resetDeptBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resetDeptBtn.setForeground(Color.WHITE);
        resetDeptBtn.setBackground(darkNavy);
        resetDeptBtn.setFocusPainted(false);
        resetDeptBtn.setBorder(new CompoundBorder(new LineBorder(darkNavy, 1), new EmptyBorder(7, 14, 7, 14)));
        resetDeptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetDeptBtn.setToolTipText("Filter back directly to your assigned department timetable");
        resetDeptBtn.addActionListener(e -> {
            if (deptFilter != null) {
                deptFilter.setSelectedIndex(0);
            }
        });

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(primaryBlue);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(7, 14, 7, 14)));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshTimetable());

        JButton viewNoticesBtn = new JButton("📢 Department Notices");
        viewNoticesBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewNoticesBtn.setForeground(textDark);
        viewNoticesBtn.setBackground(Color.WHITE);
        viewNoticesBtn.setFocusPainted(false);
        viewNoticesBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(7, 14, 7, 14)));
        viewNoticesBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewNoticesBtn.setToolTipText("View Department Notices and Circulars");
        viewNoticesBtn.addActionListener(e -> {
            if (onNavigateToNotices != null) {
                onNavigateToNotices.run();
            }
        });

        JButton createSlotBtn = new JButton("➕ Create Timetable Slot");
        createSlotBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createSlotBtn.setForeground(Color.WHITE);
        createSlotBtn.setBackground(primaryBlue);
        createSlotBtn.setFocusPainted(false);
        createSlotBtn.setBorder(new CompoundBorder(new LineBorder(primaryBlue, 1), new EmptyBorder(7, 14, 7, 14)));
        createSlotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createSlotBtn.setToolTipText("Create and schedule a new academic timetable session for this department");
        createSlotBtn.addActionListener(e -> showCreateTimetableDialog());

        statsPanel.add(totalSlotsBadge);
        statsPanel.add(labSlotsBadge);
        statsPanel.add(lectureSlotsBadge);
        statsPanel.add(createSlotBtn);
        statsPanel.add(resetDeptBtn);
        statsPanel.add(refreshBtn);
        statsPanel.add(viewNoticesBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        return header;
    }

    private JLabel createStatBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(fg);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(6, 14, 6, 14));
        return badge;
    }

    private JPanel createFilterAndControlsPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setBackground(Color.WHITE);
        container.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(14, 18, 14, 18)));

        // Top Row: Multi-criteria Filters
        JPanel filtersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filtersRow.setOpaque(false);

        // 1. Department Filter
        filtersRow.add(createFilterLabel("Department:"));
        deptFilter = new JComboBox<>();
        deptFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deptFilter.setBackground(Color.WHITE);
        deptFilter.setPreferredSize(new Dimension(240, 32));
        populateDepartmentFilter();
        deptFilter.addActionListener(e -> applyFilters());
        filtersRow.add(deptFilter);

        // 2. Academic Year Filter
        filtersRow.add(createFilterLabel("Year:"));
        academicYearFilter = new JComboBox<>(new String[]{"All Years", "2026/2027", "2025/2026", "2024/2025"});
        academicYearFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        academicYearFilter.setBackground(Color.WHITE);
        academicYearFilter.setPreferredSize(new Dimension(115, 32));
        academicYearFilter.addActionListener(e -> applyFilters());
        filtersRow.add(academicYearFilter);

        // 3. Semester Filter
        filtersRow.add(createFilterLabel("Semester:"));
        semesterFilter = new JComboBox<>(new String[]{"All Semesters", "SEMESTER 1", "SEMESTER 2"});
        semesterFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        semesterFilter.setBackground(Color.WHITE);
        semesterFilter.setPreferredSize(new Dimension(130, 32));
        semesterFilter.addActionListener(e -> applyFilters());
        filtersRow.add(semesterFilter);

        // 4. Day of Week Filter
        filtersRow.add(createFilterLabel("Day:"));
        dayFilter = new JComboBox<>(new String[]{"All Days", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"});
        dayFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dayFilter.setBackground(Color.WHITE);
        dayFilter.setPreferredSize(new Dimension(120, 32));
        dayFilter.addActionListener(e -> applyFilters());
        filtersRow.add(dayFilter);

        // 5. Session Type Filter (Labs vs Lectures)
        filtersRow.add(createFilterLabel("Type:"));
        sessionTypeFilter = new JComboBox<>(new String[]{"All Sessions", "Lab Practicals Only", "Lectures Only"});
        sessionTypeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sessionTypeFilter.setBackground(Color.WHITE);
        sessionTypeFilter.setPreferredSize(new Dimension(145, 32));
        sessionTypeFilter.addActionListener(e -> applyFilters());
        filtersRow.add(sessionTypeFilter);

        // Bottom Row: Search Box + View Switcher Buttons
        JPanel bottomRow = new JPanel(new BorderLayout(14, 0));
        bottomRow.setOpaque(false);

        // Search Field
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        searchBox.add(createFilterLabel("Search:"));

        searchField = new JTextField(24);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search course code, name, venue, lecturer...");
        searchField.setPreferredSize(new Dimension(280, 32));
        searchField.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(4, 8, 4, 8)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        searchBox.add(searchField);

        // View Mode Switcher: Weekly Matrix View vs Table View
        JPanel viewSwitcher = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        viewSwitcher.setOpaque(false);

        weeklyViewBtn = new JButton("📅 Weekly View (Mon - Fri)");
        styleSwitcherButton(weeklyViewBtn, true);
        weeklyViewBtn.addActionListener(e -> switchView("WEEKLY"));

        tableViewBtn = new JButton("📋 Detailed Table View");
        styleSwitcherButton(tableViewBtn, false);
        tableViewBtn.addActionListener(e -> switchView("TABLE"));

        viewSwitcher.add(weeklyViewBtn);
        viewSwitcher.add(tableViewBtn);

        bottomRow.add(searchBox, BorderLayout.WEST);
        bottomRow.add(viewSwitcher, BorderLayout.EAST);

        container.add(filtersRow, BorderLayout.NORTH);
        container.add(bottomRow, BorderLayout.SOUTH);

        return container;
    }

    private JLabel createFilterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(textMuted);
        return l;
    }

    private void styleSwitcherButton(JButton btn, boolean active) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(darkNavy);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(new LineBorder(darkNavy, 1), new EmptyBorder(6, 14, 6, 14)));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(textDark);
            btn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 14, 6, 14)));
        }
    }

    private void switchView(String mode) {
        this.currentView = mode;
        styleSwitcherButton(weeklyViewBtn, "WEEKLY".equals(mode));
        styleSwitcherButton(tableViewBtn, "TABLE".equals(mode));
        applyFilters();
    }

    private JPanel createTableViewPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(14, 16, 14, 16)));

        String[] columns = {"Slot ID", "Department", "Day", "Time Slot", "Course Offering", "Session Type", "Venue", "Batch", "Assigned Lecturer"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        timetableTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timetableTable.setRowHeight(36);
        timetableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableTable.setShowVerticalLines(false);
        timetableTable.setGridColor(new Color(241, 245, 249));
        timetableTable.setSelectionBackground(new Color(234, 242, 255));
        timetableTable.setSelectionForeground(textDark);

        // Header Styling
        JTableHeader th = timetableTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(textDark);
        th.setPreferredSize(new Dimension(0, 38));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

        // Column widths
        timetableTable.getColumnModel().getColumn(0).setPreferredWidth(75);   // Slot ID
        timetableTable.getColumnModel().getColumn(1).setPreferredWidth(160);  // Department
        timetableTable.getColumnModel().getColumn(2).setPreferredWidth(95);   // Day
        timetableTable.getColumnModel().getColumn(3).setPreferredWidth(115);  // Time
        timetableTable.getColumnModel().getColumn(4).setPreferredWidth(220);  // Course
        timetableTable.getColumnModel().getColumn(5).setPreferredWidth(130);  // Session Type
        timetableTable.getColumnModel().getColumn(6).setPreferredWidth(130);  // Venue
        timetableTable.getColumnModel().getColumn(7).setPreferredWidth(85);   // Batch
        timetableTable.getColumnModel().getColumn(8).setPreferredWidth(150);  // Lecturer

        // Day of week renderer highlighting
        timetableTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(accentBlue);
                return lbl;
            }
        });

        // Session Type Pill Badge Renderer (Lab Practical vs Lecture)
        timetableTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                String type = (value != null) ? value.toString() : "LECTURE";
                boolean isLab = type.toUpperCase().contains("LAB") || type.toUpperCase().contains("PRACTICAL");
                JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                pill.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

                JLabel tag = new JLabel(type) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(isLab ? tealLabBg : purpleLecBg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                tag.setFont(new Font("Segoe UI", Font.BOLD, 11));
                tag.setForeground(isLab ? tealLab : purpleLec);
                tag.setBorder(new EmptyBorder(3, 8, 3, 8));
                pill.add(tag);
                return pill;
            }
        });

        // Venue renderer with Lab highlighting
        timetableTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = (value != null) ? value.toString() : "";
                if (val.toLowerCase().contains("lab")) {
                    lbl.setForeground(tealLab);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    lbl.setForeground(textDark);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return lbl;
            }
        });

        // Double click to view details modal
        timetableTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && timetableTable.getSelectedRow() >= 0) {
                    showSelectedSlotDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Footer Action Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel tip = new JLabel("💡 Tip: Double-click any row to view complete course, venue equipment, and lecturer details.");
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tip.setForeground(textMuted);

        JButton viewSlotBtn = new JButton("View Slot Details");
        viewSlotBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewSlotBtn.setBackground(darkNavy);
        viewSlotBtn.setForeground(Color.WHITE);
        viewSlotBtn.setFocusPainted(false);
        viewSlotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewSlotBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        viewSlotBtn.addActionListener(e -> showSelectedSlotDetails());

        footer.add(tip, BorderLayout.WEST);
        footer.add(viewSlotBtn, BorderLayout.EAST);

        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createEmptyStatePanel() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(40, 20, 40, 20)));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel iconLabel = new JLabel("📅", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("No Timetable Schedules Available", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textDark);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><center>No scheduled lectures or laboratory sessions match the selected department or filter criteria.<br>Try selecting 'All Days', adjusting the semester, or clearing the search keyword.</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(textMuted);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resetFiltersBtn = new JButton("Reset All Filters");
        resetFiltersBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resetFiltersBtn.setBackground(darkNavy);
        resetFiltersBtn.setForeground(Color.WHITE);
        resetFiltersBtn.setFocusPainted(false);
        resetFiltersBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetFiltersBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        resetFiltersBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetFiltersBtn.addActionListener(e -> resetAllFilters());

        inner.add(iconLabel);
        inner.add(Box.createVerticalStrut(14));
        inner.add(titleLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(descLabel);
        inner.add(Box.createVerticalStrut(18));
        inner.add(resetFiltersBtn);

        emptyPanel.add(inner);
        return emptyPanel;
    }

    private void resetAllFilters() {
        if (deptFilter != null) deptFilter.setSelectedIndex(0);
        if (academicYearFilter != null) academicYearFilter.setSelectedIndex(0);
        if (semesterFilter != null) semesterFilter.setSelectedIndex(0);
        if (dayFilter != null) dayFilter.setSelectedIndex(0);
        if (sessionTypeFilter != null) sessionTypeFilter.setSelectedIndex(0);
        if (searchField != null) searchField.setText("");
        applyFilters();
    }

    private void populateDepartmentFilter() {
        if (deptFilter == null) return;
        deptFilter.removeAllItems();

        // 1. Officer's assigned department at the top
        deptFilter.addItem("My Department (" + officerDepartment + ")");

        // 2. All Departments option
        deptFilter.addItem("All Departments");

        // 3. Other distinct departments
        List<String> depts = timetableService.getAvailableDepartments();
        for (String d : depts) {
            if (!d.equalsIgnoreCase(officerDepartment)) {
                deptFilter.addItem(d);
            }
        }
        deptFilter.setSelectedIndex(0);
    }

    public void refreshTimetable() {
        applyFilters();
    }

    private void applyFilters() {
        if (viewsContainer == null) return;

        String selectedDeptItem = (deptFilter != null && deptFilter.getSelectedItem() != null)
                ? deptFilter.getSelectedItem().toString()
                : "My Department (" + officerDepartment + ")";

        String targetDept;
        if (selectedDeptItem.startsWith("My Department")) {
            targetDept = officerDepartment;
        } else if (selectedDeptItem.equalsIgnoreCase("All Departments")) {
            targetDept = "All Departments";
        } else {
            targetDept = selectedDeptItem;
        }

        if (subtitleLabel != null) {
            if (targetDept.equalsIgnoreCase(officerDepartment)) {
                subtitleLabel.setText("Weekly lecture, laboratory, and tutorial schedule for " + officerDepartment + " (Assigned Department)");
            } else if (targetDept.equalsIgnoreCase("All Departments")) {
                subtitleLabel.setText("Faculty-wide academic timetable across all departments");
            } else {
                subtitleLabel.setText("Weekly academic timetable for " + targetDept);
            }
        }

        String kw = (searchField != null) ? searchField.getText().trim() : "";
        String day = (dayFilter != null && dayFilter.getSelectedItem() != null)
                ? dayFilter.getSelectedItem().toString()
                : "All Days";
        String sessionType = (sessionTypeFilter != null && sessionTypeFilter.getSelectedItem() != null)
                ? sessionTypeFilter.getSelectedItem().toString()
                : "All Sessions";
        String acadYear = (academicYearFilter != null && academicYearFilter.getSelectedItem() != null)
                ? academicYearFilter.getSelectedItem().toString()
                : "All Years";
        String sem = (semesterFilter != null && semesterFilter.getSelectedItem() != null)
                ? semesterFilter.getSelectedItem().toString()
                : "All Semesters";

        List<TimetableEntry> entries = timetableService.searchTimetables(kw, day, targetDept, sessionType, acadYear, sem);

        // Update Stat Badges
        int total = entries.size();
        int labs = 0;
        int lectures = 0;
        for (TimetableEntry e : entries) {
            if (e.isLabSession()) labs++;
            else lectures++;
        }

        if (totalSlotsBadge != null) totalSlotsBadge.setText("Total Slots: " + total);
        if (labSlotsBadge != null) labSlotsBadge.setText("Lab Practicals: " + labs);
        if (lectureSlotsBadge != null) lectureSlotsBadge.setText("Lectures: " + lectures);

        // Check empty state
        if (entries.isEmpty()) {
            viewCards.show(viewsContainer, "EMPTY");
            return;
        }

        // Render current view
        if ("WEEKLY".equals(currentView)) {
            renderWeeklyGrid(entries);
            viewCards.show(viewsContainer, "WEEKLY");
        } else {
            renderTable(entries);
            viewCards.show(viewsContainer, "TABLE");
        }
    }

    private void renderTable(List<TimetableEntry> entries) {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (TimetableEntry e : entries) {
            tableModel.addRow(new Object[]{
                    e.getTimetableId(),
                    e.getDepartment(),
                    e.getDayOfWeek(),
                    e.getFormattedTimeRange(),
                    e.getCourseDisplay(),
                    e.getSessionType(),
                    e.getVenue(),
                    e.getBatch(),
                    e.getLecturers()
            });
        }
    }

    /**
     * Builds the Weekly Timetable Grid from Monday to Friday (and Saturday if present).
     * Fulfills requirement: "Provide a weekly timetable view from Monday to Friday."
     */
    private void renderWeeklyGrid(List<TimetableEntry> entries) {
        weeklyGridContainer.removeAll();

        // Check if Saturday has any entries
        boolean hasSaturday = false;
        for (TimetableEntry e : entries) {
            if ("SATURDAY".equalsIgnoreCase(e.getDayOfWeek())) {
                hasSaturday = true;
                break;
            }
        }

        String[] days = hasSaturday
                ? new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"}
                : new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};

        JPanel columnsPanel = new JPanel(new GridLayout(1, days.length, 12, 0));
        columnsPanel.setBackground(lightBg);

        for (String dayName : days) {
            JPanel dayColumn = createDayColumn(dayName, entries);
            columnsPanel.add(dayColumn);
        }

        JScrollPane scroll = new JScrollPane(columnsPanel);
        scroll.setBorder(null);
        scroll.setBackground(lightBg);
        scroll.getViewport().setBackground(lightBg);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        weeklyGridContainer.add(scroll, BorderLayout.CENTER);
        weeklyGridContainer.revalidate();
        weeklyGridContainer.repaint();
    }

    private JPanel createDayColumn(String dayName, List<TimetableEntry> allEntries) {
        JPanel col = new JPanel(new BorderLayout(0, 10));
        col.setBackground(Color.WHITE);
        col.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 12, 14, 12)));

        // Filter slots for this day
        List<TimetableEntry> daySlots = new ArrayList<>();
        for (TimetableEntry e : allEntries) {
            if (dayName.equalsIgnoreCase(e.getDayOfWeek())) {
                daySlots.add(e);
            }
        }

        // Column Header
        JPanel colHeader = new JPanel(new BorderLayout());
        colHeader.setOpaque(false);

        JLabel dayLabel = new JLabel(dayName);
        dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dayLabel.setForeground(darkNavy);

        JLabel countBadge = new JLabel(daySlots.size() + " session" + (daySlots.size() == 1 ? "" : "s"));
        countBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countBadge.setForeground(textMuted);

        colHeader.add(dayLabel, BorderLayout.WEST);
        colHeader.add(countBadge, BorderLayout.EAST);
        colHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
        colHeader.setPreferredSize(new Dimension(100, 28));

        // Cards Stack
        JPanel cardsStack = new JPanel();
        cardsStack.setLayout(new BoxLayout(cardsStack, BoxLayout.Y_AXIS));
        cardsStack.setOpaque(false);

        if (daySlots.isEmpty()) {
            JPanel emptyDay = new JPanel(new GridBagLayout());
            emptyDay.setOpaque(false);
            emptyDay.setPreferredSize(new Dimension(140, 120));
            JLabel noSlotLbl = new JLabel("No sessions");
            noSlotLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noSlotLbl.setForeground(new Color(156, 163, 175));
            emptyDay.add(noSlotLbl);
            cardsStack.add(emptyDay);
        } else {
            for (TimetableEntry slot : daySlots) {
                JPanel card = createSessionCard(slot);
                cardsStack.add(card);
                cardsStack.add(Box.createVerticalStrut(10));
            }
        }

        col.add(colHeader, BorderLayout.NORTH);
        col.add(cardsStack, BorderLayout.CENTER);
        return col;
    }

    private JPanel createSessionCard(TimetableEntry slot) {
        boolean isLab = slot.isLabSession();
        Color accent = isLab ? tealLab : purpleLec;
        Color bgAccent = isLab ? new Color(240, 253, 250) : new Color(250, 245, 255);

        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showSlotDetailsDialog(slot);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(237, 243, 248));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(borderColor);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                // Left accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(new EmptyBorder(10, 12, 10, 10));

        // Top: Time Range + Session Type Badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel timeLbl = new JLabel(slot.getFormattedTimeRange());
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        timeLbl.setForeground(textDark);

        JLabel typePill = new JLabel(slot.getSessionType(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isLab ? tealLabBg : purpleLecBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        typePill.setFont(new Font("Segoe UI", Font.BOLD, 9));
        typePill.setForeground(accent);
        typePill.setOpaque(false);
        typePill.setBorder(new EmptyBorder(2, 6, 2, 6));

        top.add(timeLbl, BorderLayout.WEST);
        top.add(typePill, BorderLayout.EAST);

        // Center: Course Code & Name
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel codeLbl = new JLabel(slot.getCourseCode() != null ? slot.getCourseCode() : slot.getOfferingId());
        codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        codeLbl.setForeground(accentBlue);

        JLabel nameLbl = new JLabel(slot.getCourseName() != null ? slot.getCourseName() : "");
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLbl.setForeground(textDark);

        center.add(codeLbl);
        center.add(Box.createVerticalStrut(2));
        center.add(nameLbl);

        // Bottom: Venue & Lecturer
        JPanel btm = new JPanel(new GridLayout(2, 1, 0, 2));
        btm.setOpaque(false);

        JLabel venueLbl = new JLabel("📍 " + slot.getVenue());
        venueLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        venueLbl.setForeground(isLab ? tealLab : textDark);

        JLabel lecLbl = new JLabel("👤 " + slot.getLecturers());
        lecLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lecLbl.setForeground(textMuted);

        btm.add(venueLbl);
        btm.add(lecLbl);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(btm, BorderLayout.SOUTH);

        makeClickable(card, () -> showSlotDetailsDialog(slot));

        return card;
    }

    private void showSelectedSlotDetails() {
        int selectedRow = timetableTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a timetable slot from the table to view its details.",
                    "No Slot Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String slotId = tableModel.getValueAt(selectedRow, 0).toString();
        TimetableEntry target = null;
        for (TimetableEntry e : timetableService.getAllTimetables()) {
            if (e.getTimetableId().equalsIgnoreCase(slotId)) {
                target = e;
                break;
            }
        }

        if (target != null) {
            showSlotDetailsDialog(target);
        }
    }

    /**
     * Shows a detailed inspection modal for any selected timetable slot.
     */
    public void showSlotDetailsDialog(TimetableEntry slot) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Timetable Slot Details - " + slot.getTimetableId(), true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Header
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        JLabel title = new JLabel(slot.getTimetableId() + " - " + slot.getSessionType());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(darkNavy);

        JLabel deptBadge = new JLabel(slot.getDepartment());
        deptBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deptBadge.setForeground(accentBlue);
        h.add(title, BorderLayout.WEST);
        h.add(deptBadge, BorderLayout.EAST);

        // Fields Grid
        JPanel grid = new JPanel(new GridLayout(8, 2, 8, 10));
        grid.setOpaque(false);
        grid.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(14, 16, 14, 16)));

        addDetailRow(grid, "Department:", slot.getDepartment());
        addDetailRow(grid, "Course Offering:", slot.getCourseDisplay());
        addDetailRow(grid, "Academic Year:", slot.getAcademicYear() != null ? slot.getAcademicYear() : "2026/2027");
        addDetailRow(grid, "Semester:", slot.getSemester() != null ? slot.getSemester() : "SEMESTER 1");
        addDetailRow(grid, "Day of Week:", slot.getDayOfWeek());
        addDetailRow(grid, "Scheduled Time:", slot.getFormattedTimeRange());
        addDetailRow(grid, "Assigned Venue:", slot.getVenue());
        addDetailRow(grid, "Assigned Lecturer:", slot.getLecturers());

        // Technical Officer Guidance Note
        JPanel notePanel = new JPanel(new BorderLayout());
        boolean isLab = slot.isLabSession();
        notePanel.setBackground(isLab ? tealLabBg : new Color(241, 245, 249));
        notePanel.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel noteLabel = new JLabel(isLab
                ? "⚙️ Laboratory Session: Ensure workstations, networking hardware, and safety infrastructure are ready."
                : "ℹ️ Lecture Session: Managed via departmental lecture hall schedules.");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLabel.setForeground(isLab ? tealLab : textDark);
        notePanel.add(noteLabel, BorderLayout.CENTER);

        // Action & Close Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        if (onNavigateToAttendance != null) {
            JButton logAttBtn = new JButton("📋 Log Attendance");
            logAttBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            logAttBtn.setBackground(primaryBlue);
            logAttBtn.setForeground(Color.WHITE);
            logAttBtn.setFocusPainted(false);
            logAttBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            logAttBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
            logAttBtn.addActionListener(e -> {
                dialog.dispose();
                onNavigateToAttendance.run();
            });
            btnPanel.add(logAttBtn);
        }

        if (slot.isLabSession() && onNavigateToLabMaintenance != null) {
            JButton checkLabBtn = new JButton("⚙️ Check Lab Facilities");
            checkLabBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            checkLabBtn.setBackground(tealLab);
            checkLabBtn.setForeground(Color.WHITE);
            checkLabBtn.setFocusPainted(false);
            checkLabBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            checkLabBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
            checkLabBtn.addActionListener(e -> {
                dialog.dispose();
                onNavigateToLabMaintenance.run();
            });
            btnPanel.add(checkLabBtn);
        }

        JButton deleteBtn = new JButton("🗑️ Delete Slot");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setBackground(new Color(254, 226, 226));
        deleteBtn.setForeground(new Color(220, 38, 38));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete timetable slot " + slot.getTimetableId() + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                timetableService.deleteTimetableEntry(slot.getTimetableId());
                dialog.dispose();
                refreshTimetable();
                JOptionPane.showMessageDialog(this,
                        "Timetable slot " + slot.getTimetableId() + " was deleted.",
                        "Slot Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnPanel.add(deleteBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(new Color(241, 245, 249));
        closeBtn.setForeground(textDark);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        closeBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(closeBtn);

        p.add(h, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(grid, BorderLayout.CENTER);
        center.add(notePanel, BorderLayout.SOUTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(p);
        dialog.setVisible(true);
    }

    /**
     * Dialog to create and schedule a new academic timetable slot.
     */
    public void showCreateTimetableDialog() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "Create New Academic Timetable Slot", true);
        dialog.setSize(620, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Header Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Create Timetable Session");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(darkNavy);

        JLabel descLabel = new JLabel("Add a scheduled lecture, lab practical, or tutorial session to the academic timetable.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(textMuted);
        titlePanel.add(titleLabel);
        titlePanel.add(descLabel);

        // Form Fields Container
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // 1. Slot ID (Auto-generated)
        String nextId = timetableService.getNextTimetableId();
        JTextField idField = new JTextField(nextId);
        idField.setFont(new Font("Segoe UI", Font.BOLD, 12));
        idField.setEditable(false);
        idField.setBackground(new Color(241, 245, 249));

        // 2. Department
        JComboBox<String> deptBox = new JComboBox<>();
        List<String> depts = timetableService.getAvailableDepartments();
        for (String d : depts) {
            deptBox.addItem(d);
        }
        if (officerDepartment != null && !officerDepartment.isBlank()) {
            deptBox.setSelectedItem(officerDepartment);
        }

        // 3. Course Offering
        String[] offeringDisplays = {
            "OFF001 - ICT2132 (Object Oriented Programming)",
            "OFF002 - ICT2112 (Data Structures and Algorithms)",
            "OFF003 - ICT2122 (Database Systems)",
            "OFF004 - ICT2152 (Web Development)",
            "OFF005 - ICT2142 (Software Engineering)",
            "OFF006 - BST2102 (Bio Systems Lab)",
            "OFF007 - ENG2102 (Engineering Math)"
        };
        JComboBox<String> offeringBox = new JComboBox<>(offeringDisplays);

        // 4. Session Type
        JComboBox<String> sessionTypeBox = new JComboBox<>(new String[]{"Lab Practical", "Lecture", "Tutorial"});

        // 5. Day of Week
        JComboBox<String> dayBox = new JComboBox<>(new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"});

        // 6. Time Presets
        String[] timePresets = {
            "08:30:00 - 10:30:00 (Morning Slot 1)",
            "10:30:00 - 12:30:00 (Morning Slot 2)",
            "13:00:00 - 15:00:00 (Afternoon Slot 1)",
            "15:00:00 - 17:00:00 (Afternoon Slot 2)",
            "Custom"
        };
        JComboBox<String> timePresetBox = new JComboBox<>(timePresets);
        JTextField startTimeField = new JTextField("08:30:00", 8);
        JTextField endTimeField = new JTextField("10:30:00", 8);

        timePresetBox.addActionListener(e -> {
            int idx = timePresetBox.getSelectedIndex();
            if (idx == 0) { startTimeField.setText("08:30:00"); endTimeField.setText("10:30:00"); }
            else if (idx == 1) { startTimeField.setText("10:30:00"); endTimeField.setText("12:30:00"); }
            else if (idx == 2) { startTimeField.setText("13:00:00"); endTimeField.setText("15:00:00"); }
            else if (idx == 3) { startTimeField.setText("15:00:00"); endTimeField.setText("17:00:00"); }
        });

        // 7. Venue
        JComboBox<String> venueBox = new JComboBox<>(new String[]{
            "ICT Lab 01", "ICT Lab 02", "ICT Lab 03",
            "Lecture Hall 01", "Lecture Hall 02", "Lecture Hall 03",
            "Bio Tech Lab 01", "Bio Tech Lab 02"
        });
        venueBox.setEditable(true);

        // 8. Batch
        JComboBox<String> batchBox = new JComboBox<>(new String[]{"2024/2025", "2023/2024", "2022/2023"});
        batchBox.setEditable(true);

        // 9. Academic Year & Semester
        JComboBox<String> acadYearBox = new JComboBox<>(new String[]{"2026/2027", "2025/2026", "2024/2025"});
        JComboBox<String> semBox = new JComboBox<>(new String[]{"SEMESTER 1", "SEMESTER 2"});

        // 10. Lecturer
        JTextField lecturerField = new JTextField("Kasun Perera", 16);

        // Add form rows
        int row = 0;
        addFormRow(form, gbc, row++, "Timetable Slot ID:", idField);
        addFormRow(form, gbc, row++, "Department:", deptBox);
        addFormRow(form, gbc, row++, "Course Offering:", offeringBox);
        addFormRow(form, gbc, row++, "Session Type:", sessionTypeBox);
        addFormRow(form, gbc, row++, "Day of Week:", dayBox);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        timePanel.setOpaque(false);
        timePanel.add(timePresetBox);
        timePanel.add(new JLabel("From:"));
        timePanel.add(startTimeField);
        timePanel.add(new JLabel("To:"));
        timePanel.add(endTimeField);
        addFormRow(form, gbc, row++, "Time Slot:", timePanel);

        addFormRow(form, gbc, row++, "Assigned Venue:", venueBox);
        addFormRow(form, gbc, row++, "Student Batch:", batchBox);

        JPanel acadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        acadPanel.setOpaque(false);
        acadPanel.add(acadYearBox);
        acadPanel.add(semBox);
        addFormRow(form, gbc, row++, "Academic Term:", acadPanel);

        addFormRow(form, gbc, row++, "Assigned Lecturer:", lecturerField);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton saveBtn = new JButton("Save & Schedule Slot");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(primaryBlue);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(new EmptyBorder(9, 18, 9, 18));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(textDark);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(new EmptyBorder(9, 18, 9, 18));
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String slotId = idField.getText().trim();
                String chosenDept = deptBox.getSelectedItem() != null ? deptBox.getSelectedItem().toString() : officerDepartment;
                String offeringSelection = offeringBox.getSelectedItem() != null ? offeringBox.getSelectedItem().toString() : "OFF001";
                String offId = offeringSelection.split(" ")[0];
                String dayVal = dayBox.getSelectedItem() != null ? dayBox.getSelectedItem().toString() : "MONDAY";
                String venueVal = venueBox.getSelectedItem() != null ? venueBox.getSelectedItem().toString().trim() : "ICT Lab 01";
                String batchVal = batchBox.getSelectedItem() != null ? batchBox.getSelectedItem().toString().trim() : "2024/2025";
                String yearVal = acadYearBox.getSelectedItem() != null ? acadYearBox.getSelectedItem().toString() : "2026/2027";
                String semVal = semBox.getSelectedItem() != null ? semBox.getSelectedItem().toString() : "SEMESTER 1";
                String lecVal = lecturerField.getText().trim();

                String stText = startTimeField.getText().trim();
                String etText = endTimeField.getText().trim();
                if (!stText.contains(":")) stText += ":00:00";
                else if (stText.split(":").length == 2) stText += ":00";
                if (!etText.contains(":")) etText += ":00:00";
                else if (etText.split(":").length == 2) etText += ":00";

                Time st = Time.valueOf(stText);
                Time et = Time.valueOf(etText);

                TimetableEntry entry = new TimetableEntry(slotId, offId, dayVal, st, et, venueVal, batchVal, chosenDept);
                entry.setAcademicYear(yearVal);
                entry.setSemester(semVal);
                entry.setLecturers(lecVal.isEmpty() ? "Department Staff" : lecVal);

                if (offeringSelection.contains("-")) {
                    String part = offeringSelection.substring(offeringSelection.indexOf("-") + 1).trim();
                    if (part.contains("(") && part.contains(")")) {
                        String code = part.substring(0, part.indexOf("(")).trim();
                        String name = part.substring(part.indexOf("(") + 1, part.indexOf(")")).trim();
                        entry.setCourseCode(code);
                        entry.setCourseName(name);
                    } else {
                        entry.setCourseCode(part);
                    }
                }

                boolean ok = timetableService.createTimetableEntry(entry);
                if (ok) {
                    dialog.dispose();
                    refreshTimetable();
                    JOptionPane.showMessageDialog(this,
                            "Timetable slot " + slotId + " successfully scheduled for " + chosenDept + " (" + dayVal + ").",
                            "Timetable Slot Created",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to save timetable slot to database.",
                            "Creation Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException timeEx) {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid time format. Please enter time as HH:mm:ss (e.g. 08:30:00).",
                        "Time Format Error",
                        JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "An unexpected error occurred: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(cancelBtn);
        actionPanel.add(saveBtn);

        root.add(titlePanel, BorderLayout.NORTH);
        root.add(new JScrollPane(form) {
            {
                setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, borderColor));
                getViewport().setBackground(Color.WHITE);
            }
        }, BorderLayout.CENTER);
        root.add(actionPanel, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(textDark);
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(field, gbc);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(textMuted);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        v.setForeground(textDark);

        panel.add(l);
        panel.add(v);
    }

    private static void makeClickable(Component comp, Runnable onClick) {
        if (comp == null || onClick == null) return;
        comp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                makeClickable(child, onClick);
            }
        }
    }
}
