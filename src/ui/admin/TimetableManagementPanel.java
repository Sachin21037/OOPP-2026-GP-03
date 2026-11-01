package ui.admin;

import model.TimetableEntry;
import service.TimetableService;
import service.TimetableService.OperationResult;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import ui.util.UIAnimationHelper;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Timetable Management and Viewer Panel for FAMS.
 * Supports dual-mode operation:
 * - ADMIN: Full management (Create, Read, Update, Delete, Search, Day/Batch/Dept filters, Metrics).
 * - LECTURER / TECHNICAL_OFFICER / UNDERGRADUATE: Read-only schedule viewer with search and filters.
 */
public class TimetableManagementPanel extends JPanel {

    // Color Palette matching FAMS Design System
    private static final Color PRIMARY_NAVY     = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE   = new Color(27, 85, 155);   // #1B559B
    private static final Color LIGHT_BLUE       = new Color(234, 242, 255); // #EAF2FF
    private static final Color MAIN_BG          = new Color(245, 247, 251); // #F5F7FB
    private static final Color CARD_BG          = Color.WHITE;
    private static final Color TEXT_PRIMARY     = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_SECONDARY   = new Color(113, 128, 150); // #718096
    private static final Color SUCCESS_COLOR    = new Color(22, 163, 74);   // #16A34A
    private static final Color WARNING_COLOR    = new Color(245, 158, 11);  // #F59E0B
    private static final Color DANGER_COLOR     = new Color(220, 38, 38);   // #DC2626
    private static final Color PURPLE_COLOR     = new Color(124, 58, 237);  // #7C3AED
    private static final Color BORDER_COLOR     = new Color(226, 232, 240); // #E2E8F0

    private final String currentUserRole;
    private final String currentUserId;
    private final boolean isAdmin;
    private final Runnable onTimetableChangedCallback;

    private final TimetableService timetableService;

    // Header & Summary Card Labels
    private JLabel lblTotalSessions;
    private JLabel lblTotalCourses;
    private JLabel lblTotalVenues;
    private JLabel lblTotalBatches;
    private JLabel lblHeaderCount;
    private JLabel lblTableStatus;

    // Search and Filter Components
    private JTextField txtSearch;
    private JComboBox<String> cmbDayFilter;
    private JComboBox<String> cmbBatchFilter;
    private JComboBox<String> cmbDeptFilter;
    private JButton btnRefresh;

    // Table & Model
    private JTable timetableTable;
    private TimetableTableModel tableModel;
    private TableRowSorter<TimetableTableModel> sorter;

    // Admin Form Components
    private JLabel lblFormMode;
    private JTextField txtTimetableId;
    private JComboBox<String> cmbOffering;
    private JComboBox<String> cmbDay;
    private JComboBox<String> cmbStartTime;
    private JComboBox<String> cmbEndTime;
    private JComboBox<String> cmbVenue;
    private JComboBox<String> cmbBatch;
    private JComboBox<String> cmbDepartment;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Offering mappings: offering_id -> full label
    private final Map<String, String> offeringMap = new LinkedHashMap<>();

    // Reader Mode Components (for non-admins)
    private JLabel lblReaderCourse;
    private JLabel lblReaderTiming;
    private JLabel lblReaderVenue;
    private JLabel lblReaderBatchDept;
    private JLabel lblReaderStaff;
    private JLabel lblReaderSemester;

    // Currently selected timetable entry
    private TimetableEntry selectedEntry = null;

    // Standardized Blue Header Banner
    private PageHeaderBanner headerBanner;

    public TimetableManagementPanel() {
        this("ADMIN", "ADM001", null);
    }

    public TimetableManagementPanel(Runnable onTimetableChangedCallback) {
        this("ADMIN", "ADM001", onTimetableChangedCallback);
    }

    public TimetableManagementPanel(String currentUserRole, String currentUserId, Runnable onTimetableChangedCallback) {
        this.currentUserRole = (currentUserRole != null && !currentUserRole.isBlank()) ? currentUserRole.trim().toUpperCase() : "ADMIN";
        this.currentUserId = (currentUserId != null && !currentUserId.isBlank()) ? currentUserId.trim() : "ADM001";
        this.isAdmin = "ADMIN".equalsIgnoreCase(this.currentUserRole);
        this.onTimetableChangedCallback = onTimetableChangedCallback;
        this.timetableService = new TimetableService();

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Top Section: Standardized Header Banner + (Summary Cards if Admin)
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        headerBanner = new PageHeaderBanner(
                isAdmin ? "Timetable Management" : "Academic Timetable",
                isAdmin ? "Manage faculty lecture sessions, laboratory practicals, and venue assignments."
                        : "Official academic schedules, lectures, and laboratory practical sessions.",
                "0 Sessions",
                "↻ Refresh",
                () -> {
                    loadTimetablesAsync();
                    if (onTimetableChangedCallback != null) {
                        onTimetableChangedCallback.run();
                    }
                }
        );
        btnRefresh = headerBanner.getActionButton();

        topContainer.add(headerBanner);
        topContainer.add(Box.createVerticalStrut(14));

        if (isAdmin) {
            topContainer.add(createSummaryCardsRow());
            topContainer.add(Box.createVerticalStrut(14));
        }

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: Master Table (Left) + Detail/Form Card (Right)
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setOpaque(false);

        JPanel tableCard = createTableCard();
        centerPanel.add(tableCard, BorderLayout.CENTER);

        JPanel rightCard = isAdmin ? createAdminFormCard() : createTimetableReaderCard();
        rightCard.setPreferredSize(new Dimension(isAdmin ? 380 : 380, 500));
        centerPanel.add(rightCard, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        // Setup custom column widths and renderers
        setupColumnRenderers();

        // Initial async data load
        loadTimetablesAsync();
    }


    // SECTION B: SUMMARY CARDS ROW (ADMIN)

    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        lblTotalSessions = new JLabel("--");
        lblTotalCourses = new JLabel("--");
        lblTotalVenues = new JLabel("--");
        lblTotalBatches = new JLabel("--");

        row.add(createMetricCard("Total Sessions", lblTotalSessions, "Weekly Time Slots", PRIMARY_NAVY, LIGHT_BLUE));
        row.add(createMetricCard("Scheduled Modules", lblTotalCourses, "Course Offerings", SUCCESS_COLOR, new Color(220, 252, 231)));
        row.add(createMetricCard("Active Venues", lblTotalVenues, "Labs & Lecture Halls", WARNING_COLOR, new Color(254, 243, 199)));
        row.add(createMetricCard("Active Batches", lblTotalBatches, "Undergraduate Cohorts", PURPLE_COLOR, new Color(243, 232, 255)));

        return row;
    }

    private JPanel createMetricCard(String title, JLabel valueLbl, String subtext, Color accent, Color pillBg) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        titleLbl.setForeground(TEXT_SECONDARY);

        valueLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 18));
        valueLbl.setForeground(PRIMARY_NAVY);

        JLabel subLbl = new JLabel("  " + subtext + "  ");
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        subLbl.setForeground(accent);
        subLbl.setOpaque(true);
        subLbl.setBackground(pillBg);
        subLbl.setBorder(new EmptyBorder(1, 4, 1, 4));

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLbl);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // SECTION C: TIMETABLE TABLE CARD (MASTER VIEW)

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 1. Toolbar: Search + Day/Batch/Dept Filters
        JPanel toolbar = createToolbar();
        card.add(toolbar, BorderLayout.NORTH);

        // 2. Table
        tableModel = new TimetableTableModel();
        sorter = new TableRowSorter<>(tableModel);
        timetableTable = new JTable(tableModel);
        timetableTable.setRowSorter(sorter);
        timetableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableTable.setRowHeight(38);
        timetableTable.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        timetableTable.getTableHeader().setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        timetableTable.getTableHeader().setBackground(new Color(248, 250, 252));
        timetableTable.getTableHeader().setForeground(TEXT_PRIMARY);
        timetableTable.getTableHeader().setPreferredSize(new Dimension(0, 34));
        timetableTable.getTableHeader().setReorderingAllowed(false);
        timetableTable.setShowGrid(false);
        timetableTable.setIntercellSpacing(new Dimension(0, 1));

        timetableTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // 3. Table Footer Status
        lblTableStatus = new JLabel("Loading timetable slots...");
        lblTableStatus.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        lblTableStatus.setForeground(TEXT_SECONDARY);
        lblTableStatus.setBorder(new EmptyBorder(6, 2, 0, 0));
        card.add(lblTableStatus, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        // Search Input with animated focus glow and pill shape
        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Search course, venue...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTimetables(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTimetables(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTimetables(); }
        });
        JPanel searchBox = UIAnimationHelper.createPillSearchBox(txtSearch, 175, 34, 20);
        leftPanel.add(searchBox);

        // Day of Week Filter
        String[] days = {"All Days", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        cmbDayFilter = new JComboBox<>(days);
        styleFilterCombo(cmbDayFilter, 105);
        cmbDayFilter.addActionListener(e -> filterTimetables());
        leftPanel.add(cmbDayFilter);

        // Batch Filter
        String[] batches = {"All Batches", "2024/2025"};
        cmbBatchFilter = new JComboBox<>(batches);
        styleFilterCombo(cmbBatchFilter, 105);
        cmbBatchFilter.addActionListener(e -> filterTimetables());
        leftPanel.add(cmbBatchFilter);

        // Reset Button
        JButton btnReset = UIAnimationHelper.createAnimatedSecondaryButton("Reset", TEXT_SECONDARY, BORDER_COLOR, 18, e -> resetFilters());
        leftPanel.add(btnReset);

        toolbar.add(leftPanel, BorderLayout.CENTER);
        return toolbar;
    }

    private void styleFilterCombo(JComboBox<String> cmb, int width) {
        cmb.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        cmb.setBackground(Color.WHITE);
        cmb.setPreferredSize(new Dimension(width, 34));
    }

    private void setupColumnRenderers() {
        // Col 0: Timetable ID Pill Badge
        timetableTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        timetableTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String id = (value != null) ? value.toString() : "";
                JLabel badge = new JLabel(" " + id + " ");
                badge.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
                badge.setOpaque(true);
                badge.setBackground(new Color(241, 245, 249));
                badge.setForeground(SECONDARY_BLUE);
                badge.setBorder(new EmptyBorder(2, 6, 2, 6));

                p.add(badge);
                return p;
            }
        });

        // Col 1: Day of Week Badge
        timetableTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        timetableTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String day = (value != null) ? value.toString() : "";
                JLabel pill = new JLabel("  " + day + "  ");
                pill.setFont(AdminDashboard.getModernFont(Font.BOLD, 9));
                pill.setOpaque(true);
                pill.setBorder(new EmptyBorder(2, 6, 2, 6));

                switch (day) {
                    case "MONDAY" -> { pill.setBackground(new Color(224, 242, 254)); pill.setForeground(new Color(3, 105, 161)); }
                    case "TUESDAY" -> { pill.setBackground(new Color(254, 243, 199)); pill.setForeground(new Color(180, 83, 9)); }
                    case "WEDNESDAY" -> { pill.setBackground(new Color(220, 252, 231)); pill.setForeground(new Color(21, 128, 61)); }
                    case "THURSDAY" -> { pill.setBackground(new Color(254, 226, 226)); pill.setForeground(new Color(185, 28, 28)); }
                    case "FRIDAY" -> { pill.setBackground(new Color(243, 232, 255)); pill.setForeground(new Color(109, 40, 217)); }
                    case "SATURDAY" -> { pill.setBackground(new Color(255, 237, 213)); pill.setForeground(new Color(194, 65, 12)); }
                    default -> { pill.setBackground(new Color(241, 245, 249)); pill.setForeground(TEXT_SECONDARY); }
                }

                p.add(pill);
                return p;
            }
        });

        // Col 2: Time Range
        timetableTable.getColumnModel().getColumn(2).setPreferredWidth(95);

        // Col 3: Course (Code & Title)
        timetableTable.getColumnModel().getColumn(3).setPreferredWidth(170);

        // Col 4: Venue
        timetableTable.getColumnModel().getColumn(4).setPreferredWidth(105);

        // Col 5: Batch
        timetableTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        // Col 6: Staff
        timetableTable.getColumnModel().getColumn(6).setPreferredWidth(120);
    }

    // SECTION D: ADMIN FORM CARD (ADD / EDIT / DELETE)
    private JPanel createAdminFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Form Header
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);

        lblFormMode = new JLabel("ADD TIMETABLE SLOT");
        lblFormMode.setFont(AdminDashboard.getModernFont(Font.BOLD, 13));
        lblFormMode.setForeground(PRIMARY_NAVY);
        formHeader.add(lblFormMode, BorderLayout.WEST);

        btnClear = UIAnimationHelper.createAnimatedSecondaryButton("Clear", TEXT_SECONDARY, BORDER_COLOR, 16, e -> clearForm());
        formHeader.add(btnClear, BorderLayout.EAST);

        card.add(formHeader, BorderLayout.NORTH);

        // Fields Panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        // Field 1: Timetable ID
        txtTimetableId = new JTextField();
        txtTimetableId.setEditable(false);
        txtTimetableId.setBackground(new Color(248, 250, 252));
        fieldsPanel.add(createFormField("Timetable ID", txtTimetableId, "Auto-generated identifier (e.g. TBL007)"));
        fieldsPanel.add(Box.createVerticalStrut(7));

        // Field 2: Course Offering
        cmbOffering = new JComboBox<>();
        cmbOffering.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        cmbOffering.setBackground(Color.WHITE);
        fieldsPanel.add(createFormField("Course Offering *", cmbOffering, "Module and semester schedule"));
        fieldsPanel.add(Box.createVerticalStrut(7));

        // Field 3: Day of Week
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        cmbDay = new JComboBox<>(days);
        cmbDay.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        cmbDay.setBackground(Color.WHITE);
        fieldsPanel.add(createFormField("Day of Week *", cmbDay, "Scheduled lecture or lab day"));
        fieldsPanel.add(Box.createVerticalStrut(7));

        // Field 4: Time Range (Start & End)
        JPanel timeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        timeRow.setOpaque(false);

        String[] times = {
                "08:30:00", "09:30:00", "10:30:00", "11:30:00", "12:30:00",
                "13:00:00", "14:00:00", "15:00:00", "16:00:00", "17:00:00"
        };
        cmbStartTime = new JComboBox<>(times);
        cmbStartTime.setEditable(true);
        cmbStartTime.setBackground(Color.WHITE);
        timeRow.add(createSubFormField("Start Time *", cmbStartTime));

        cmbEndTime = new JComboBox<>(new String[]{
                "10:30:00", "11:30:00", "12:30:00", "14:00:00", "15:00:00", "16:00:00", "17:00:00"
        });
        cmbEndTime.setEditable(true);
        cmbEndTime.setBackground(Color.WHITE);
        timeRow.add(createSubFormField("End Time *", cmbEndTime));

        fieldsPanel.add(timeRow);
        fieldsPanel.add(Box.createVerticalStrut(7));

        // Field 5: Venue
        String[] venues = {
                "ICT Lab 01", "ICT Lab 02", "ICT Lab 03",
                "Lecture Hall 01", "Lecture Hall 02", "Lecture Hall 03"
        };
        cmbVenue = new JComboBox<>(venues);
        cmbVenue.setEditable(true);
        cmbVenue.setBackground(Color.WHITE);
        fieldsPanel.add(createFormField("Venue / Location *", cmbVenue, "Laboratory or lecture hall"));
        fieldsPanel.add(Box.createVerticalStrut(7));

        // Field 6: Batch & Department
        JPanel batchDeptRow = new JPanel(new GridLayout(1, 2, 8, 0));
        batchDeptRow.setOpaque(false);

        cmbBatch = new JComboBox<>(new String[]{"2024/2025", "2023/2024"});
        cmbBatch.setEditable(true);
        cmbBatch.setBackground(Color.WHITE);
        batchDeptRow.add(createSubFormField("Academic Batch *", cmbBatch));

        cmbDepartment = new JComboBox<>(new String[]{"Information and Communication Technology"});
        cmbDepartment.setBackground(Color.WHITE);
        batchDeptRow.add(createSubFormField("Department *", cmbDepartment));

        fieldsPanel.add(batchDeptRow);
        fieldsPanel.add(Box.createVerticalStrut(10));

        JScrollPane formScroll = new JScrollPane(fieldsPanel);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        card.add(formScroll, BorderLayout.CENTER);

        // Action Buttons Row (Add, Update, Delete)
        JPanel actionRow = new JPanel(new GridLayout(1, 3, 8, 0));
        actionRow.setOpaque(false);

        btnAdd = UIAnimationHelper.createAnimatedPillButton("Add Slot", SECONDARY_BLUE, 20, e -> performAddTimetable());
        actionRow.add(btnAdd);

        btnUpdate = UIAnimationHelper.createAnimatedPillButton("Update", SUCCESS_COLOR, 20, e -> performUpdateTimetable());
        btnUpdate.setEnabled(false);
        actionRow.add(btnUpdate);

        btnDelete = UIAnimationHelper.createAnimatedPillButton("Delete", DANGER_COLOR, 20, e -> performDeleteTimetable());
        btnDelete.setEnabled(false);
        actionRow.add(btnDelete);

        card.add(actionRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFormField(String label, JComponent comp, String hint) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        l.setForeground(TEXT_PRIMARY);

        comp.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, 32));
        if (comp instanceof JTextField) {
            comp.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(2, 8, 2, 8)));
        }

        JLabel h = new JLabel(hint);
        h.setFont(AdminDashboard.getModernFont(Font.PLAIN, 9));
        h.setForeground(TEXT_SECONDARY);

        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.add(h, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createSubFormField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        l.setForeground(TEXT_PRIMARY);

        comp.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, 32));
        if (comp instanceof JTextField) {
            comp.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(2, 8, 2, 8)));
        }

        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void styleActionButton(JButton btn, Color bg, Color fg) {
        btn.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
    }
    // SECTION E: TIMETABLE READER CARD (NON-ADMIN)
    private JPanel createTimetableReaderCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("SESSION DETAILS");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 14));
        titleLbl.setForeground(PRIMARY_NAVY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);

        content.add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sep);
        content.add(Box.createVerticalStrut(12));

        lblReaderCourse = new JLabel("Select a session from the table");
        lblReaderCourse.setFont(AdminDashboard.getModernFont(Font.BOLD, 13));
        lblReaderCourse.setForeground(PRIMARY_NAVY);
        lblReaderCourse.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblReaderCourse);
        content.add(Box.createVerticalStrut(10));

        lblReaderTiming = createDetailItem("Time & Day:", "--", content);
        lblReaderVenue = createDetailItem("Venue / Hall:", "--", content);
        lblReaderStaff = createDetailItem("Lecturer(s):", "--", content);
        lblReaderBatchDept = createDetailItem("Batch & Dept:", "--", content);
        lblReaderSemester = createDetailItem("Academic Term:", "--", content);

        card.add(content, BorderLayout.CENTER);

        JLabel footer = new JLabel("Official Academic Timetable • Faculty of Technology");
        footer.setFont(AdminDashboard.getModernFont(Font.ITALIC, 10));
        footer.setForeground(new Color(148, 163, 184));
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createDetailItem(String title, String initialVal, JPanel parent) {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        t.setForeground(TEXT_SECONDARY);
        t.setPreferredSize(new Dimension(100, 20));

        JLabel v = new JLabel(initialVal);
        v.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        v.setForeground(TEXT_PRIMARY);

        p.add(t, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        parent.add(p);
        parent.add(Box.createVerticalStrut(8));
        return v;
    }

 // ASYNC DATA LOADING & REFRESH

    public void loadTimetablesAsync() {
        btnRefresh.setEnabled(false);
        lblTableStatus.setText("Loading academic timetable from database...");

        SwingWorker<List<TimetableEntry>, Void> worker = new SwingWorker<>() {
            private Map<String, Integer> metrics;
            private Map<String, String> offerings;

            @Override
            protected List<TimetableEntry> doInBackground() {
                if (isAdmin) {
                    metrics = timetableService.getSummaryMetrics();
                    offerings = timetableService.getAvailableOfferings();
                    return timetableService.getAllTimetables();
                } else {
                    return timetableService.getTimetablesForRole(currentUserRole, currentUserId, "2024/2025", "Information and Communication Technology");
                }
            }

            @Override
            protected void done() {
                try {
                    List<TimetableEntry> entries = get();
                    tableModel.setEntries(entries);
                    filterTimetables();

                    if (isAdmin) {
                        if (metrics != null) {
                            updateSummaryCards(metrics);
                        }
                        if (offerings != null) {
                            offeringMap.clear();
                            offeringMap.putAll(offerings);
                            updateOfferingDropdown(offerings);
                        }
                        if (selectedEntry == null) {
                            clearForm();
                        }
                    }

                    if (headerBanner != null) {
                        headerBanner.setBadgeText(entries.size() + " Sessions");
                    }
                    lblTableStatus.setText("Showing " + entries.size() + " scheduled sessions.");
                } catch (Exception e) {
                    lblTableStatus.setText("Error loading timetable: " + e.getMessage());
                } finally {
                    btnRefresh.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /**
     * Synchronous load for automated verification testing.
     */
    public void loadTimetablesSync() {
        List<TimetableEntry> entries = isAdmin
                ? timetableService.getAllTimetables()
                : timetableService.getTimetablesForRole(currentUserRole, currentUserId, "2024/2025", "Information and Communication Technology");
        tableModel.setEntries(entries);
        filterTimetables();

        if (isAdmin) {
            updateSummaryCards(timetableService.getSummaryMetrics());
            Map<String, String> offerings = timetableService.getAvailableOfferings();
            offeringMap.clear();
            offeringMap.putAll(offerings);
            updateOfferingDropdown(offerings);
            if (selectedEntry == null) {
                clearForm();
            }
        }
        if (headerBanner != null) {
            headerBanner.setBadgeText(entries.size() + " Sessions");
        }
        lblTableStatus.setText("Showing " + entries.size() + " scheduled sessions.");
    }

    private void updateSummaryCards(Map<String, Integer> metrics) {
        if (metrics == null) return;
        lblTotalSessions.setText(String.valueOf(metrics.getOrDefault("sessions", 0)));
        lblTotalCourses.setText(String.valueOf(metrics.getOrDefault("courses", 0)));
        lblTotalVenues.setText(String.valueOf(metrics.getOrDefault("venues", 0)));
        lblTotalBatches.setText(String.valueOf(metrics.getOrDefault("batches", 0)));
    }

    private void updateOfferingDropdown(Map<String, String> offerings) {
        if (cmbOffering == null) return;
        cmbOffering.removeAllItems();
        for (Map.Entry<String, String> entry : offerings.entrySet()) {
            cmbOffering.addItem(entry.getValue());
        }
    }

    private void filterTimetables() {
        String keyword = txtSearch.getText().trim();
        String dayFilter = (String) cmbDayFilter.getSelectedItem();
        String batchFilter = (String) cmbBatchFilter.getSelectedItem();

        List<RowFilter<TimetableTableModel, Integer>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword)));
        }

        if (dayFilter != null && !dayFilter.equalsIgnoreCase("All Days")) {
            filters.add(RowFilter.regexFilter("^" + dayFilter + "$", 1));
        }

        if (batchFilter != null && !batchFilter.equalsIgnoreCase("All Batches")) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(batchFilter) + "$", 5));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }

        int visible = timetableTable.getRowCount();
        int total = tableModel.getRowCount();
        lblTableStatus.setText("Showing " + visible + " of " + total + " sessions.");
    }

    private void resetFilters() {
        txtSearch.setText("");
        cmbDayFilter.setSelectedIndex(0);
        cmbBatchFilter.setSelectedIndex(0);
        filterTimetables();
    }

    private void onTableRowSelected() {
        int selectedRow = timetableTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int modelIdx = timetableTable.convertRowIndexToModel(selectedRow);
        selectedEntry = tableModel.getEntryAt(modelIdx);
        if (selectedEntry == null) return;

        if (isAdmin) {
            // Populate Admin Form
            lblFormMode.setText("EDITING SLOT [" + selectedEntry.getTimetableId() + "]");
            txtTimetableId.setText(selectedEntry.getTimetableId());

            // Select offering
            for (int i = 0; i < cmbOffering.getItemCount(); i++) {
                String item = cmbOffering.getItemAt(i);
                if (item.startsWith(selectedEntry.getOfferingId() + " -")) {
                    cmbOffering.setSelectedIndex(i);
                    break;
                }
            }

            cmbDay.setSelectedItem(selectedEntry.getDayOfWeek());
            cmbStartTime.setSelectedItem(selectedEntry.getStartTime() != null ? selectedEntry.getStartTime().toString() : "08:30:00");
            cmbEndTime.setSelectedItem(selectedEntry.getEndTime() != null ? selectedEntry.getEndTime().toString() : "10:30:00");
            cmbVenue.setSelectedItem(selectedEntry.getVenue());
            cmbBatch.setSelectedItem(selectedEntry.getBatch());
            cmbDepartment.setSelectedItem(selectedEntry.getDepartment());

            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
        } else {
            // Populate Reader Card
            lblReaderCourse.setText("<html><b>" + selectedEntry.getCourseDisplay() + "</b></html>");
            lblReaderTiming.setText(selectedEntry.getDayOfWeek() + " (" + selectedEntry.getFormattedTimeRange() + ")");
            lblReaderVenue.setText(selectedEntry.getVenue());
            lblReaderStaff.setText(selectedEntry.getLecturers());
            lblReaderBatchDept.setText("Batch " + selectedEntry.getBatch() + " • " + selectedEntry.getDepartment());
            lblReaderSemester.setText(selectedEntry.getAcademicYear() + " - " + selectedEntry.getSemester());
        }
    }

    private void clearForm() {
        selectedEntry = null;
        if (timetableTable.getSelectedRow() != -1) {
            timetableTable.clearSelection();
        }

        if (isAdmin) {
            lblFormMode.setText("ADD TIMETABLE SLOT");
            txtTimetableId.setText(timetableService.getNextSuggestedTimetableId());
            if (cmbOffering.getItemCount() > 0) cmbOffering.setSelectedIndex(0);
            cmbDay.setSelectedIndex(0);
            cmbStartTime.setSelectedItem("08:30:00");
            cmbEndTime.setSelectedItem("10:30:00");
            cmbVenue.setSelectedIndex(0);
            cmbBatch.setSelectedIndex(0);
            cmbDepartment.setSelectedIndex(0);

            btnAdd.setEnabled(true);
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
        } else {
            lblReaderCourse.setText("Select a session from the table");
            lblReaderTiming.setText("--");
            lblReaderVenue.setText("--");
            lblReaderStaff.setText("--");
            lblReaderBatchDept.setText("--");
            lblReaderSemester.setText("--");
        }
    }

    // SECTION G: CRUD ACTION HANDLERS (ADMIN ONLY)

    private void performAddTimetable() {
        if (!isAdmin) {
            showError("Permission Denied: Only administrators can schedule timetable sessions.");
            return;
        }

        String id = txtTimetableId.getText().trim();
        String offeringLabel = (String) cmbOffering.getSelectedItem();
        String offeringId = extractOfferingId(offeringLabel);
        String day = (String) cmbDay.getSelectedItem();
        String startStr = (String) cmbStartTime.getSelectedItem();
        String endStr = (String) cmbEndTime.getSelectedItem();
        String venue = (String) cmbVenue.getSelectedItem();
        String batch = (String) cmbBatch.getSelectedItem();
        String dept = (String) cmbDepartment.getSelectedItem();

        Time startTime = parseTime(startStr);
        Time endTime = parseTime(endStr);
        if (startTime == null || endTime == null) {
            showError("Invalid time format. Please use HH:mm:ss (e.g. 08:30:00).");
            return;
        }

        TimetableEntry entry = new TimetableEntry(id, offeringId, day, startTime, endTime, venue, batch, dept);
        OperationResult res = timetableService.addTimetable(entry, currentUserRole);

        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Session Scheduled", JOptionPane.INFORMATION_MESSAGE);
            loadTimetablesAsync();
            if (onTimetableChangedCallback != null) {
                onTimetableChangedCallback.run();
            }
        } else {
            showError(res.getMessage());
        }
    }

    private void performUpdateTimetable() {
        if (!isAdmin) {
            showError("Permission Denied: Only administrators can modify timetable schedules.");
            return;
        }

        if (selectedEntry == null) {
            showError("Please select a timetable slot to update.");
            return;
        }

        String offeringLabel = (String) cmbOffering.getSelectedItem();
        String offeringId = extractOfferingId(offeringLabel);
        String day = (String) cmbDay.getSelectedItem();
        String startStr = (String) cmbStartTime.getSelectedItem();
        String endStr = (String) cmbEndTime.getSelectedItem();
        String venue = (String) cmbVenue.getSelectedItem();
        String batch = (String) cmbBatch.getSelectedItem();
        String dept = (String) cmbDepartment.getSelectedItem();

        Time startTime = parseTime(startStr);
        Time endTime = parseTime(endStr);
        if (startTime == null || endTime == null) {
            showError("Invalid time format. Please use HH:mm:ss (e.g. 08:30:00).");
            return;
        }

        selectedEntry.setOfferingId(offeringId);
        selectedEntry.setDayOfWeek(day);
        selectedEntry.setStartTime(startTime);
        selectedEntry.setEndTime(endTime);
        selectedEntry.setVenue(venue);
        selectedEntry.setBatch(batch);
        selectedEntry.setDepartment(dept);

        OperationResult res = timetableService.updateTimetable(selectedEntry, currentUserRole);

        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Timetable Updated", JOptionPane.INFORMATION_MESSAGE);
            loadTimetablesAsync();
            if (onTimetableChangedCallback != null) {
                onTimetableChangedCallback.run();
            }
        } else {
            showError(res.getMessage());
        }
    }

    private void performDeleteTimetable() {
        if (!isAdmin) {
            showError("Permission Denied: Only administrators can delete timetable schedules.");
            return;
        }

        if (selectedEntry == null) {
            showError("Please select a timetable slot to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "<html>Are you sure you want to delete timetable slot <b>" + selectedEntry.getTimetableId() + "</b>?<br>" +
                        "<i>" + selectedEntry.getDayOfWeek() + " " + selectedEntry.getFormattedTimeRange() + " (" + selectedEntry.getVenue() + ")</i><br><br>" +
                        "<font color='#DC2626'>This action cannot be undone.</font></html>",
                "Confirm Slot Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            OperationResult res = timetableService.deleteTimetable(selectedEntry.getTimetableId(), currentUserRole);
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Slot Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadTimetablesAsync();
                if (onTimetableChangedCallback != null) {
                    onTimetableChangedCallback.run();
                }
            } else {
                showError(res.getMessage());
            }
        }
    }

    private String extractOfferingId(String label) {
        if (label == null) return "OFF001";
        int dash = label.indexOf(" -");
        if (dash != -1) {
            return label.substring(0, dash).trim();
        }
        return label.trim();
    }

    private Time parseTime(String str) {
        if (str == null || str.isBlank()) return null;
        str = str.trim();
        if (str.length() == 5) {
            str = str + ":00";
        }
        try {
            return Time.valueOf(str);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Timetable Management Error", JOptionPane.ERROR_MESSAGE);
    }

    // TABLE MODEL CLASS

    public static class TimetableTableModel extends AbstractTableModel {
        private final String[] columns = {"Slot ID", "Day", "Time Slot", "Course", "Venue", "Batch", "Staff"};
        private List<TimetableEntry> entries = new ArrayList<>();

        public void setEntries(List<TimetableEntry> entries) {
            this.entries = (entries != null) ? entries : new ArrayList<>();
            fireTableDataChanged();
        }

        public TimetableEntry getEntryAt(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < entries.size()) {
                return entries.get(rowIndex);
            }
            return null;
        }

        @Override public int getRowCount() { return entries.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TimetableEntry e = entries.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> e.getTimetableId();
                case 1 -> e.getDayOfWeek();
                case 2 -> e.getFormattedTimeRange();
                case 3 -> e.getCourseDisplay();
                case 4 -> e.getVenue();
                case 5 -> e.getBatch();
                case 6 -> e.getLecturers();
                default -> "";
            };
        }
    }
}
