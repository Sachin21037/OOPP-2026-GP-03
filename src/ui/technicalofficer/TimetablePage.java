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
import java.util.ArrayList;
import java.util.List;

/**
 * Academic Timetable View for Technical Officers in FAMS.
 * Allows Technical Officers to view timetables of their department.
 * Provides both a Weekly Timetable Matrix View (Monday to Friday/Saturday) and a Detailed Table View.
 * Supports filtering by Academic Year, Semester, Day of Week, Session Type, and Department.
 * View-only: Strictly complies with "Can see timetables of their department".
 */
public class TimetablePage extends JPanel {

    // Theme palette harmonized with FAMS Technical Officer Dashboard
    private final Color darkNavy = new Color(27, 67, 103);       // #1B4367 exact navy
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

    public void setOnNavigateToNotices(Runnable onNavigateToNotices) {
        this.onNavigateToNotices = onNavigateToNotices;
    }

    public void setOnNavigateToAttendance(Runnable onNavigateToAttendance) {
        this.onNavigateToAttendance = onNavigateToAttendance;
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
        mainContainer.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header Panel
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
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        // Top Row: Title + Subtitle on LEFT, Quick Action Buttons on RIGHT
        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Department Academic Timetables");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textDark);

        subtitleLabel = new JLabel("Weekly lecture, laboratory, and tutorial schedule for " + officerDepartment);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitleLabel);

        // Action Buttons Row (Right) - All with BLACK letters
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);

        JButton resetDeptBtn = new JButton("My Department");
        resetDeptBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resetDeptBtn.setForeground(Color.BLACK);
        resetDeptBtn.setBackground(Color.WHITE);
        resetDeptBtn.setFocusPainted(false);
        resetDeptBtn.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225), 1), new EmptyBorder(7, 14, 7, 14)));
        resetDeptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetDeptBtn.setToolTipText("Filter back directly to your assigned department timetable");
        resetDeptBtn.addActionListener(e -> {
            if (deptFilter != null) {
                deptFilter.setSelectedIndex(0);
            }
        });

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.BLACK);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(7, 14, 7, 14)));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshTimetable());

        JButton viewNoticesBtn = new JButton("Department Notices");
        viewNoticesBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewNoticesBtn.setForeground(Color.BLACK);
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

        actionsPanel.add(resetDeptBtn);
        actionsPanel.add(refreshBtn);
        actionsPanel.add(viewNoticesBtn);

        topRow.add(titleBox, BorderLayout.WEST);
        topRow.add(actionsPanel, BorderLayout.EAST);

        // Bottom Row: Summary Badges positioned cleanly on their own row
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statsRow.setOpaque(false);

        totalSlotsBadge = createPillBadge("Total Slots: 0", textDark, new Color(241, 245, 249));
        labSlotsBadge = createPillBadge("Lab Practicals: 0", tealLab, tealLabBg);
        lectureSlotsBadge = createPillBadge("Lectures: 0", purpleLec, purpleLecBg);

        statsRow.add(totalSlotsBadge);
        statsRow.add(labSlotsBadge);
        statsRow.add(lectureSlotsBadge);

        header.add(topRow, BorderLayout.NORTH);
        header.add(statsRow, BorderLayout.SOUTH);

        return header;
    }

    private JLabel createPillBadge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(3, 10, 3, 10)));
        return l;
    }

    private JPanel createFilterAndControlsPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 16, 12, 16)));

        // Upper Filter Row: Combos
        JPanel upperFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        upperFilters.setOpaque(false);

        // 1. Department Filter
        upperFilters.add(createFilterLabel("Department:"));
        deptFilter = new JComboBox<>();
        populateDepartmentFilter();
        deptFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deptFilter.setBackground(Color.WHITE);
        deptFilter.setPreferredSize(new Dimension(240, 32));
        deptFilter.addActionListener(e -> applyFilters());
        upperFilters.add(deptFilter);

        // 2. Academic Year Filter
        upperFilters.add(createFilterLabel("Year:"));
        academicYearFilter = new JComboBox<>(new String[] { "All Years", "2026/2027", "2025/2026", "2024/2025" });
        academicYearFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        academicYearFilter.setBackground(Color.WHITE);
        academicYearFilter.setPreferredSize(new Dimension(110, 32));
        academicYearFilter.addActionListener(e -> applyFilters());
        upperFilters.add(academicYearFilter);

        // 3. Semester Filter
        upperFilters.add(createFilterLabel("Semester:"));
        semesterFilter = new JComboBox<>(new String[] { "All Semesters", "SEMESTER 1", "SEMESTER 2" });
        semesterFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        semesterFilter.setBackground(Color.WHITE);
        semesterFilter.setPreferredSize(new Dimension(120, 32));
        semesterFilter.addActionListener(e -> applyFilters());
        upperFilters.add(semesterFilter);

        // 4. Day of Week Filter
        upperFilters.add(createFilterLabel("Day:"));
        dayFilter = new JComboBox<>(new String[] {
                "All Days", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
        });
        dayFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dayFilter.setBackground(Color.WHITE);
        dayFilter.setPreferredSize(new Dimension(110, 32));
        dayFilter.addActionListener(e -> applyFilters());
        upperFilters.add(dayFilter);

        // Lower Controls Row: Search + View Mode Switcher
        JPanel lowerControls = new JPanel(new BorderLayout(14, 0));
        lowerControls.setOpaque(false);

        // Search Field
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        searchBox.add(createFilterLabel("Search:"));

        searchField = new JTextField(24);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search by course code, title, venue, or lecturer...");
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

        // View Mode Switcher: Weekly View vs Table View (Both with solid BLACK letters)
        JPanel viewSwitcher = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        viewSwitcher.setOpaque(false);

        weeklyViewBtn = new JButton("Weekly View (Mon - Fri)");
        weeklyViewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        weeklyViewBtn.setForeground(Color.BLACK);
        weeklyViewBtn.setBackground(new Color(230, 242, 255));
        weeklyViewBtn.setFocusPainted(false);
        weeklyViewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        weeklyViewBtn.setBorder(new CompoundBorder(new LineBorder(primaryBlue, 2), new EmptyBorder(6, 14, 6, 14)));
        weeklyViewBtn.addActionListener(e -> switchView("WEEKLY"));

        tableViewBtn = new JButton("Detailed Table View");
        tableViewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableViewBtn.setForeground(Color.BLACK);
        tableViewBtn.setBackground(Color.WHITE);
        tableViewBtn.setFocusPainted(false);
        tableViewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tableViewBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 14, 6, 14)));
        tableViewBtn.addActionListener(e -> switchView("TABLE"));

        viewSwitcher.add(weeklyViewBtn);
        viewSwitcher.add(tableViewBtn);

        lowerControls.add(searchBox, BorderLayout.WEST);
        lowerControls.add(viewSwitcher, BorderLayout.EAST);

        card.add(upperFilters, BorderLayout.NORTH);
        card.add(lowerControls, BorderLayout.SOUTH);

        return card;
    }

    private void populateDepartmentFilter() {
        if (deptFilter == null) return;
        deptFilter.removeAllItems();
        deptFilter.addItem("My Department (" + officerDepartment + ")");
        deptFilter.addItem("All Departments");

        List<String> allDepts = timetableService.getAvailableDepartments();
        for (String d : allDepts) {
            if (!d.equalsIgnoreCase(officerDepartment)) {
                deptFilter.addItem(d);
            }
        }
    }

    private JLabel createFilterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(textMuted);
        return l;
    }

    private JPanel createTableViewPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        String[] columns = {
                "Slot ID", "Day", "Time", "Course Code", "Course Name",
                "Type", "Venue", "Batch", "Lecturer(s)", "Department"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        timetableTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timetableTable.setRowHeight(34);
        timetableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableTable.setShowGrid(true);
        timetableTable.setShowHorizontalLines(true);
        timetableTable.setShowVerticalLines(true);
        timetableTable.setGridColor(borderColor);
        timetableTable.setSelectionBackground(new Color(237, 243, 248));
        timetableTable.setSelectionForeground(darkNavy);
        timetableTable.setDefaultRenderer(Object.class, new AlternateRowRenderer());

        setupTableHeader(timetableTable);

        // Column widths
        timetableTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // Slot ID
        timetableTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // Day
        timetableTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Time
        timetableTable.getColumnModel().getColumn(3).setPreferredWidth(85);  // Course Code
        timetableTable.getColumnModel().getColumn(4).setPreferredWidth(190); // Name
        timetableTable.getColumnModel().getColumn(5).setPreferredWidth(95);  // Type
        timetableTable.getColumnModel().getColumn(6).setPreferredWidth(110); // Venue
        timetableTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Batch
        timetableTable.getColumnModel().getColumn(8).setPreferredWidth(140); // Lecturer
        timetableTable.getColumnModel().getColumn(9).setPreferredWidth(170); // Dept

        // Column 5 Type Pill Renderer
        timetableTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                String typeStr = (value != null) ? value.toString() : "Session";
                boolean isLab = typeStr.toLowerCase().contains("lab") || typeStr.toLowerCase().contains("practical");

                JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
                Color rowBg = isSelected ? new Color(237, 243, 248) : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252));
                pillPanel.setBackground(rowBg);

                JLabel badge = new JLabel(typeStr, SwingConstants.CENTER) {
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
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setForeground(isLab ? tealLab : purpleLec);
                badge.setOpaque(false);
                badge.setBorder(new EmptyBorder(2, 6, 2, 6));

                pillPanel.add(badge);
                return pillPanel;
            }
        });

        // Double-click row to view details
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

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(14, 16, 14, 16)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Footer Help
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel tip = new JLabel("Tip: Double-click any row to view full session details.");
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tip.setForeground(textMuted);
        footer.add(tip, BorderLayout.WEST);

        JButton inspectBtn = new JButton("View Slot Details");
        inspectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inspectBtn.setBackground(darkNavy);
        inspectBtn.setForeground(Color.WHITE);
        inspectBtn.setFocusPainted(false);
        inspectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inspectBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        inspectBtn.addActionListener(e -> showSelectedSlotDetails());
        footer.add(inspectBtn, BorderLayout.EAST);

        tableCard.add(footer, BorderLayout.SOUTH);

        wrapper.add(tableCard, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createEmptyStatePanel() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(40, 20, 40, 20)));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel titleLabel = new JLabel("No Timetable Slots Found", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textDark);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(
                "<html><center>There are no timetable slots matching your filter criteria.<br>Try adjusting the day, academic year, semester, or search query.</center></html>",
                SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(textMuted);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resetBtn = new JButton("Reset All Filters");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(8, 16, 8, 16)));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> {
            if (deptFilter != null) deptFilter.setSelectedIndex(0);
            if (academicYearFilter != null) academicYearFilter.setSelectedIndex(0);
            if (semesterFilter != null) semesterFilter.setSelectedIndex(0);
            if (dayFilter != null) dayFilter.setSelectedIndex(0);
            if (searchField != null) searchField.setText("");
            applyFilters();
        });

        inner.add(titleLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(descLabel);
        inner.add(Box.createVerticalStrut(18));
        inner.add(resetBtn);

        emptyPanel.add(inner);
        return emptyPanel;
    }

    public void switchView(String mode) {
        this.currentView = mode;
        if ("WEEKLY".equalsIgnoreCase(mode)) {
            weeklyViewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            weeklyViewBtn.setBackground(new Color(230, 242, 255));
            weeklyViewBtn.setBorder(new CompoundBorder(new LineBorder(primaryBlue, 2), new EmptyBorder(6, 14, 6, 14)));

            tableViewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tableViewBtn.setBackground(Color.WHITE);
            tableViewBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 14, 6, 14)));

            viewCards.show(viewsContainer, "WEEKLY");
        } else {
            tableViewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            tableViewBtn.setBackground(new Color(230, 242, 255));
            tableViewBtn.setBorder(new CompoundBorder(new LineBorder(primaryBlue, 2), new EmptyBorder(6, 14, 6, 14)));

            weeklyViewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            weeklyViewBtn.setBackground(Color.WHITE);
            weeklyViewBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 14, 6, 14)));

            viewCards.show(viewsContainer, "TABLE");
        }
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

        // 1. Update Detailed Table View
        tableModel.setRowCount(0);
        for (TimetableEntry e : entries) {
            tableModel.addRow(new Object[] {
                    e.getTimetableId(),
                    e.getDayOfWeek(),
                    e.getFormattedTimeRange(),
                    e.getCourseCode(),
                    e.getCourseName(),
                    e.getSessionType(),
                    e.getVenue(),
                    e.getBatch(),
                    e.getLecturers(),
                    e.getDepartment()
            });
        }

        // 2. Build Weekly Matrix View (Mon-Fri or Mon-Sat)
        buildWeeklyMatrix(entries);

        // Show active card
        if ("WEEKLY".equalsIgnoreCase(currentView)) {
            viewCards.show(viewsContainer, "WEEKLY");
        } else {
            viewCards.show(viewsContainer, "TABLE");
        }
    }

    private void buildWeeklyMatrix(List<TimetableEntry> entries) {
        weeklyGridContainer.removeAll();

        // Check if Saturday has entries
        boolean hasSaturday = entries.stream().anyMatch(e -> "SATURDAY".equalsIgnoreCase(e.getDayOfWeek()));
        String[] days = hasSaturday
                ? new String[] { "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" }
                : new String[] { "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" };

        int cols = days.length;
        JPanel grid = new JPanel(new GridLayout(1, cols, 10, 0));
        grid.setOpaque(false);

        for (String day : days) {
            List<TimetableEntry> dayEntries = new ArrayList<>();
            for (TimetableEntry e : entries) {
                if (day.equalsIgnoreCase(e.getDayOfWeek())) {
                    dayEntries.add(e);
                }
            }
            grid.add(createDayColumnPanel(day, dayEntries));
        }

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(lightBg);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        weeklyGridContainer.add(scrollPane, BorderLayout.CENTER);
        weeklyGridContainer.revalidate();
        weeklyGridContainer.repaint();
    }

    private JPanel createDayColumnPanel(String dayName, List<TimetableEntry> dayEntries) {
        JPanel col = new JPanel(new BorderLayout(0, 8));
        col.setBackground(Color.WHITE);
        col.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 10, 12, 10)));

        // Day Column Header
        JPanel dayHeader = new JPanel(new BorderLayout());
        dayHeader.setOpaque(false);
        dayHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
        dayHeader.setPreferredSize(new Dimension(100, 32));

        JLabel nameLbl = new JLabel(dayName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(darkNavy);

        JLabel countLbl = new JLabel(dayEntries.size() + " session" + (dayEntries.size() == 1 ? "" : "s"));
        countLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLbl.setForeground(textMuted);

        dayHeader.add(nameLbl, BorderLayout.WEST);
        dayHeader.add(countLbl, BorderLayout.EAST);

        col.add(dayHeader, BorderLayout.NORTH);

        // Slots Container
        JPanel slotsList = new JPanel();
        slotsList.setLayout(new BoxLayout(slotsList, BoxLayout.Y_AXIS));
        slotsList.setOpaque(false);

        if (dayEntries.isEmpty()) {
            JPanel emptyDay = new JPanel(new GridBagLayout());
            emptyDay.setOpaque(false);
            JLabel noSess = new JLabel("No sessions");
            noSess.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noSess.setForeground(new Color(180, 190, 205));
            emptyDay.add(noSess);
            slotsList.add(Box.createVerticalGlue());
            slotsList.add(emptyDay);
            slotsList.add(Box.createVerticalGlue());
        } else {
            for (TimetableEntry slot : dayEntries) {
                slotsList.add(createSessionCard(slot));
                slotsList.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scroll = new JScrollPane(slotsList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        col.add(scroll, BorderLayout.CENTER);
        return col;
    }

    private JPanel createSessionCard(TimetableEntry slot) {
        boolean isLab = slot.isLabSession();
        Color accent = isLab ? tealLab : purpleLec;

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
        card.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Top: Time Range + Session Type Badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel timeLbl = new JLabel(slot.getFormattedTimeRange());
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        timeLbl.setForeground(Color.BLACK);

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
        typePill.setBorder(new EmptyBorder(1, 5, 1, 5));

        top.add(timeLbl, BorderLayout.WEST);
        top.add(typePill, BorderLayout.EAST);

        // Center: Course Code & Name
        JPanel mid = new JPanel(new GridLayout(2, 1, 0, 2));
        mid.setOpaque(false);

        JLabel codeLbl = new JLabel(slot.getCourseCode());
        codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        codeLbl.setForeground(textDark);

        JLabel nameLbl = new JLabel(slot.getCourseName());
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLbl.setForeground(textMuted);

        mid.add(codeLbl);
        mid.add(nameLbl);

        // Bottom: Venue & Lecturer
        JPanel btm = new JPanel(new GridLayout(2, 1, 0, 1));
        btm.setOpaque(false);

        JLabel venueLbl = new JLabel("Venue: " + slot.getVenue());
        venueLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        venueLbl.setForeground(darkNavy);

        JLabel lecLbl = new JLabel("Lecturer: " + slot.getLecturers());
        lecLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lecLbl.setForeground(textMuted);

        btm.add(venueLbl);
        btm.add(lecLbl);

        card.add(top, BorderLayout.NORTH);
        card.add(mid, BorderLayout.CENTER);
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
     * Shows a detailed view-only inspection modal for any selected timetable slot.
     */
    public void showSlotDetailsDialog(TimetableEntry slot) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Timetable Slot Details - " + slot.getTimetableId(), true);
        dialog.setSize(560, 480);
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

        // Guidance Note
        JPanel notePanel = new JPanel(new BorderLayout());
        boolean isLab = slot.isLabSession();
        notePanel.setBackground(isLab ? tealLabBg : new Color(241, 245, 249));
        notePanel.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel noteLabel = new JLabel(isLab
                ? "Laboratory Practical Session: Department computing and laboratory facilities scheduled."
                : "Department Lecture Session: Scheduled in designated lecture hall venue.");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLabel.setForeground(isLab ? tealLab : textDark);
        notePanel.add(noteLabel, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        if (onNavigateToAttendance != null) {
            JButton logAttBtn = new JButton("Log Attendance");
            logAttBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            logAttBtn.setBackground(new Color(230, 242, 255));
            logAttBtn.setForeground(Color.BLACK);
            logAttBtn.setFocusPainted(false);
            logAttBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            logAttBtn.setBorder(new CompoundBorder(new LineBorder(primaryBlue, 1), new EmptyBorder(8, 14, 8, 14)));
            logAttBtn.addActionListener(e -> {
                dialog.dispose();
                onNavigateToAttendance.run();
            });
            btnPanel.add(logAttBtn);
        }

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

    private void setupTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(darkNavy);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setOpaque(true);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setOpaque(true);
                label.setBackground(darkNavy);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(45, 90, 135)),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                return label;
            }
        });
    }

    private class AlternateRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setForeground(textDark);
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(248, 250, 252));
                }
            } else {
                c.setForeground(darkNavy);
                c.setBackground(new Color(237, 243, 248));
            }
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (column == 0 || column == 2 || column == 3 || column == 7) {
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                }
                lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            }
            return c;
        }
    }
}
