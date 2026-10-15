package ui.technicalofficer;

import model.AttendanceRecord;
import model.AttendanceScenarioResult;
import model.AttendanceSession;
import model.CourseOffering;
import model.Undergraduate;
import service.AttendanceService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AttendanceManagementPage View Class
 * Package: ui.technicalofficer
 * Implements:
 * - FR-ATT-01: Session Attendance Logging
 * - FR-ATT-03: Multi-Track Attendance Calculation (Theory/Practical/Combined)
 * - FR-ATT-04: Mandatory Five Attendance Scenarios Engine
 * - FR-ATT-05: Multi-View Track Filtering
 * - FR-ATT-06: Whole Batch Attendance Summary
 * - FR-ATT-07: Individual Attendance Profile & Warning
 */
public class AttendanceManagementPage extends JPanel {

    // Theme colors matching Technical Officer Dashboard
    private final Color darkNavy = new Color(27, 67, 103);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color cardWhite = Color.WHITE;
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color successGreen = new Color(20, 165, 145);
    private final Color alertRed = new Color(210, 60, 60);
    private final Color pendingPurple = new Color(115, 75, 180);
    private final Color borderColor = new Color(225, 228, 232);
    private final Color textDark = new Color(35, 45, 55);
    private final Color textMuted = new Color(110, 120, 130);
    private final Color tableStripe = new Color(249, 251, 253);

    // Business service
    private final AttendanceService service;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Top Navigation Tabs
    private CardLayout tabCardLayout;
    private JPanel tabContentContainer;
    private JButton tab1Btn;
    private JButton tab2Btn;
    private JButton tab3Btn;

    // === TAB 1: Session Logging Components ===
    private JComboBox<CourseOffering> courseOfferingCombo;
    private JTextField sessionDateField;
    private JComboBox<String> sessionTypeCombo;
    private JTextField sessionNumberField;
    private JTextField durationField;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel totalStudentsLabel;
    private JLabel presentCountLabel;
    private JLabel absentCountLabel;
    private JLabel medicalCountLabel;

    // === TAB 2: Batch Eligibility Components ===
    private JComboBox<CourseOffering> summaryOfferingCombo;
    private JComboBox<String> trackFilterCombo;
    private JComboBox<String> eligibilityFilterCombo;
    private JTextField summarySearchField;
    private JTable summaryTable;
    private DefaultTableModel summaryTableModel;
    private JLabel sumTotalLabel;
    private JLabel sumEligibleLabel;
    private JLabel sumEligibleMedLabel;
    private JLabel sumIneligibleLabel;

    // === TAB 3: Individual Student History Components ===
    private JTextField studentSearchField;
    private JComboBox<CourseOffering> studentOfferingCombo;
    private JLabel studentProfileNameLabel;
    private JLabel studentProfileIndexLabel;
    private JLabel studentProfileBatchLabel;
    private JLabel studentProfileStatsLabel;
    private JLabel studentWarningBanner;
    private JTable studentHistoryTable;
    private DefaultTableModel studentHistoryModel;

    public AttendanceManagementPage() {
        this.service = new AttendanceService();
        setLayout(new BorderLayout());
        setBackground(lightBackground);

        initComponents();
        loadOfferingsData();
        updateLiveCounters();
    }

    private void initComponents() {
        JPanel mainWrapper = new JPanel(new BorderLayout(0, 10));
        mainWrapper.setBackground(lightBackground);
        mainWrapper.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Header Panel with Title and Sub-Tab Strip
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(lightBackground);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(lightBackground);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel mainTitle = new JLabel("Attendance Management System");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 22));
        mainTitle.setForeground(textDark);

        JLabel subtitle = new JLabel("Record laboratory attendance sessions, evaluate exam eligibility (80% rule), and track student profiles.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titlePanel.add(mainTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        // Sub-Navigation Tabs
        JPanel subTabsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        subTabsPanel.setBackground(lightBackground);

        tab1Btn = createTabButton("Record Session Attendance", true);
        tab2Btn = createTabButton("Batch Eligibility & 5 Scenarios", false);
        tab3Btn = createTabButton("Student Profile & History", false);

        tab1Btn.addActionListener(e -> switchSubTab("TAB1", tab1Btn));
        tab2Btn.addActionListener(e -> {
            switchSubTab("TAB2", tab2Btn);
            refreshBatchSummary();
        });
        tab3Btn.addActionListener(e -> switchSubTab("TAB3", tab3Btn));

        subTabsPanel.add(tab1Btn);
        subTabsPanel.add(tab2Btn);
        subTabsPanel.add(tab3Btn);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(subTabsPanel, BorderLayout.EAST);

        // Central Multi-Tab Container
        tabCardLayout = new CardLayout();
        tabContentContainer = new JPanel(tabCardLayout);
        tabContentContainer.setBackground(lightBackground);

        tabContentContainer.add(createSessionLoggingTab(), "TAB1");
        tabContentContainer.add(createBatchSummaryTab(), "TAB2");
        tabContentContainer.add(createStudentHistoryTab(), "TAB3");

        mainWrapper.add(headerPanel, BorderLayout.NORTH);
        mainWrapper.add(tabContentContainer, BorderLayout.CENTER);

        add(mainWrapper, BorderLayout.CENTER);
    }

    // =========================================================================
    // SUB-TAB 1: Session Attendance Logging (FR-ATT-01)
    // =========================================================================
    private JPanel createSessionLoggingTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(lightBackground);

        panel.add(createSessionConfigCard(), BorderLayout.NORTH);
        panel.add(createRosterTablePanel(), BorderLayout.CENTER);
        panel.add(createSessionActionFooter(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSessionConfigCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel sectionTitle = new JLabel("Session Configuration (FR-ATT-01)");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 13));
        sectionTitle.setForeground(darkNavy);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        courseOfferingCombo = new JComboBox<>();
        styleComboBox(courseOfferingCombo);
        courseOfferingCombo.addActionListener(e -> autoLoadEnrolledBatch());

        sessionDateField = new JTextField(dateFormat.format(new Date()));
        styleTextField(sessionDateField);

        sessionTypeCombo = new JComboBox<>(new String[]{"-- Select Session Type --", "Practical", "Theory", "Tutorial"});
        sessionTypeCombo.setSelectedIndex(1);
        styleComboBox(sessionTypeCombo);

        sessionNumberField = new JTextField("1");
        styleTextField(sessionNumberField);

        durationField = new JTextField("2.0");
        styleTextField(durationField);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formGrid.add(createLabeledField("Course Offering:", courseOfferingCombo), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1;
        formGrid.add(createLabeledField("Session Date (YYYY-MM-DD):", sessionDateField), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 1;
        formGrid.add(createLabeledField("Session Type:", sessionTypeCombo), gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        formGrid.add(createLabeledField("Session No (1 - 15):", sessionNumberField), gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2;
        formGrid.add(createLabeledField("Duration (Contact Hours):", durationField), gbc);

        card.add(sectionTitle, BorderLayout.NORTH);
        card.add(formGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRosterTablePanel() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("Batch Student Attendance Roster");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 13));
        tableTitle.setForeground(darkNavy);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);

        JButton loadBatchBtn = createStyledButton("Load Enrolled Batch", new Color(45, 90, 135), Color.WHITE);
        loadBatchBtn.addActionListener(e -> autoLoadEnrolledBatch());

        JButton addRowBtn = createStyledButton("+ Add Student", primaryBlue, Color.WHITE);
        addRowBtn.addActionListener(e -> addNewRosterRow());

        JButton removeRowBtn = createStyledButton("- Remove Student", Color.WHITE, textDark);
        removeRowBtn.addActionListener(e -> removeSelectedRosterRow());

        controls.add(loadBatchBtn);
        controls.add(addRowBtn);
        controls.add(removeRowBtn);

        headerPanel.add(tableTitle, BorderLayout.WEST);
        headerPanel.add(controls, BorderLayout.EAST);

        String[] columns = {"#", "Student ID", "Index No", "Student Name", "Attendance Status", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(32);
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 13));
        attendanceTable.setGridColor(new Color(230, 235, 240));
        attendanceTable.setShowGrid(true);
        attendanceTable.setSelectionBackground(new Color(225, 237, 248));
        attendanceTable.setSelectionForeground(darkNavy);
        attendanceTable.setFillsViewportHeight(true);

        setupTableHeader(attendanceTable);

        setColumnWidth(attendanceTable, 0, 45, 50);
        setColumnWidth(attendanceTable, 1, 90, 100);
        setColumnWidth(attendanceTable, 2, 130, 150);
        setColumnWidth(attendanceTable, 3, 200, 260);
        setColumnWidth(attendanceTable, 4, 150, 170);
        setColumnWidth(attendanceTable, 5, 180, 300);

        // Status Combo editor (Present, Absent, Medical)
        JComboBox<String> statusEditor = new JComboBox<>(new String[]{"Present", "Absent", "Medical"});
        statusEditor.setFont(new Font("Arial", Font.PLAIN, 13));
        attendanceTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusEditor));
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(new AttendanceStatusRenderer());
        attendanceTable.setDefaultRenderer(String.class, new AlternateRowRenderer());

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE ||
                e.getType() == TableModelEvent.INSERT ||
                e.getType() == TableModelEvent.DELETE) {
                updateLiveCounters();
            }
        });

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSessionActionFooter() {
        JPanel footerCard = createCardPanel();
        footerCard.setLayout(new BorderLayout(10, 0));

        // Real-time Counters Strip
        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        counterPanel.setOpaque(false);

        totalStudentsLabel = createBadge("Total Students: 0", darkNavy, new Color(236, 242, 248));
        presentCountLabel = createBadge("Present: 0", successGreen, new Color(230, 247, 244));
        absentCountLabel = createBadge("Absent: 0", alertRed, new Color(254, 237, 237));
        medicalCountLabel = createBadge("Medical: 0", pendingPurple, new Color(245, 238, 255));

        counterPanel.add(totalStudentsLabel);
        counterPanel.add(presentCountLabel);
        counterPanel.add(absentCountLabel);
        counterPanel.add(medicalCountLabel);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton clearBtn = createStyledButton("Clear", Color.WHITE, textDark);
        clearBtn.addActionListener(e -> clearTable());

        JButton markAllPresentBtn = createStyledButton("Mark All Present", primaryBlue, Color.WHITE);
        markAllPresentBtn.addActionListener(e -> markAllPresent());

        JButton saveSessionBtn = createStyledButton("Save Attendance", successGreen, Color.WHITE);
        saveSessionBtn.addActionListener(e -> saveCurrentSession());

        actionPanel.add(clearBtn);
        actionPanel.add(markAllPresentBtn);
        actionPanel.add(saveSessionBtn);

        footerCard.add(counterPanel, BorderLayout.WEST);
        footerCard.add(actionPanel, BorderLayout.EAST);
        return footerCard;
    }

    // =========================================================================
    // SUB-TAB 2: Whole Batch Attendance Summary & 5 Scenarios Engine (FR-ATT-04/06)
    // =========================================================================
    private JPanel createBatchSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(lightBackground);

        // Filter Strip Card
        JPanel filterCard = createCardPanel();
        filterCard.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Batch Eligibility Evaluation (80% Mandatory Rule & 5 Attendance Scenarios)");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(darkNavy);

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        summaryOfferingCombo = new JComboBox<>();
        styleComboBox(summaryOfferingCombo);
        summaryOfferingCombo.addActionListener(e -> refreshBatchSummary());

        trackFilterCombo = new JComboBox<>(new String[]{"Combined", "Practical", "Theory"});
        styleComboBox(trackFilterCombo);
        trackFilterCombo.addActionListener(e -> refreshBatchSummary());

        eligibilityFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Eligible Outright", "Eligible with Medical", "Barred (<80%)"});
        styleComboBox(eligibilityFilterCombo);
        eligibilityFilterCombo.addActionListener(e -> filterSummaryTable());

        summarySearchField = new JTextField("");
        styleTextField(summarySearchField);
        summarySearchField.addActionListener(e -> filterSummaryTable());

        JButton filterBtn = createStyledButton("Evaluate Batch", primaryBlue, Color.WHITE);
        filterBtn.addActionListener(e -> refreshBatchSummary());

        gbc.gridx = 0; gbc.gridy = 0;
        filterRow.add(createLabeledField("Offering:", summaryOfferingCombo), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        filterRow.add(createLabeledField("Evaluation Track:", trackFilterCombo), gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        filterRow.add(createLabeledField("Eligibility Status:", eligibilityFilterCombo), gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        filterRow.add(createLabeledField("Search Student:", summarySearchField), gbc);

        gbc.gridx = 4; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        filterRow.add(filterBtn, gbc);

        filterCard.add(title, BorderLayout.NORTH);
        filterCard.add(filterRow, BorderLayout.CENTER);

        // Summary Table Card
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] columns = {
                "#", "Index No", "Student Name", "Conducted", "Attended",
                "Raw %", "Approved Medicals", "Adjusted %", "Scenario Code", "Eligibility Status", "Verdict & Remarks"
        };

        summaryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int c) { return String.class; }
        };

        summaryTable = new JTable(summaryTableModel);
        summaryTable.setRowHeight(32);
        summaryTable.setFont(new Font("Arial", Font.PLAIN, 13));
        summaryTable.setGridColor(new Color(230, 235, 240));
        summaryTable.setShowGrid(true);
        summaryTable.setSelectionBackground(new Color(225, 237, 248));
        summaryTable.setSelectionForeground(darkNavy);
        summaryTable.setFillsViewportHeight(true);

        setupTableHeader(summaryTable);

        setColumnWidth(summaryTable, 0, 40, 45);
        setColumnWidth(summaryTable, 1, 110, 125);
        setColumnWidth(summaryTable, 2, 160, 220);
        setColumnWidth(summaryTable, 3, 75, 85);
        setColumnWidth(summaryTable, 4, 70, 80);
        setColumnWidth(summaryTable, 5, 75, 85);
        setColumnWidth(summaryTable, 6, 120, 135);
        setColumnWidth(summaryTable, 7, 85, 95);
        setColumnWidth(summaryTable, 8, 100, 110);
        setColumnWidth(summaryTable, 9, 140, 160);
        setColumnWidth(summaryTable, 10, 220, 350);

        summaryTable.getColumnModel().getColumn(8).setCellRenderer(new ScenarioCodeRenderer());
        summaryTable.getColumnModel().getColumn(9).setCellRenderer(new EligibilityStatusRenderer());
        summaryTable.setDefaultRenderer(String.class, new AlternateRowRenderer());

        JScrollPane scroll = new JScrollPane(summaryTable);
        scroll.setBorder(new LineBorder(borderColor, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        tableCard.add(scroll, BorderLayout.CENTER);

        // Stats Footer
        JPanel statsCard = createCardPanel();
        statsCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        sumTotalLabel = createBadge("Evaluated: 0", darkNavy, new Color(236, 242, 248));
        sumEligibleLabel = createBadge("Eligible (>80%): 0", successGreen, new Color(230, 247, 244));
        sumEligibleMedLabel = createBadge("Eligible (Medical): 0", primaryBlue, new Color(235, 245, 255));
        sumIneligibleLabel = createBadge("Barred (<80%): 0", alertRed, new Color(254, 237, 237));

        statsCard.add(sumTotalLabel);
        statsCard.add(sumEligibleLabel);
        statsCard.add(sumEligibleMedLabel);
        statsCard.add(sumIneligibleLabel);

        panel.add(filterCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        panel.add(statsCard, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshBatchSummary() {
        CourseOffering selected = (CourseOffering) summaryOfferingCombo.getSelectedItem();
        if (selected == null) return;

        String track = (String) trackFilterCombo.getSelectedItem();
        List<AttendanceScenarioResult> results = service.getBatchSummary(selected.getOfferingId(), track);

        summaryTableModel.setRowCount(0);
        int eligibleCount = 0;
        int medEligibleCount = 0;
        int barredCount = 0;

        int idx = 1;
        for (AttendanceScenarioResult r : results) {
            if ("ELIGIBLE".equalsIgnoreCase(r.getEligibilityStatus())) {
                eligibleCount++;
            } else if ("ELIGIBLE (MEDICAL)".equalsIgnoreCase(r.getEligibilityStatus())) {
                medEligibleCount++;
            } else {
                barredCount++;
            }

            summaryTableModel.addRow(new Object[]{
                    String.valueOf(idx++),
                    r.getStudent().getIndexNo(),
                    r.getStudent().getFullName(),
                    String.valueOf(r.getTotalConducted()),
                    String.valueOf(r.getAttendedCount()),
                    String.format("%.1f%%", r.getRawPercentage()),
                    String.valueOf(r.getApprovedMedicals()),
                    String.format("%.1f%%", r.getAdjustedPercentage()),
                    r.getScenarioCode(),
                    r.getEligibilityStatus(),
                    r.getScenarioDescription()
            });
        }

        sumTotalLabel.setText("Evaluated: " + results.size());
        sumEligibleLabel.setText("Eligible (>80%): " + eligibleCount);
        sumEligibleMedLabel.setText("Eligible (Medical): " + medEligibleCount);
        sumIneligibleLabel.setText("Barred (<80%): " + barredCount);
    }

    private void filterSummaryTable() {
        String search = summarySearchField.getText().trim().toLowerCase();
        String filter = (String) eligibilityFilterCombo.getSelectedItem();

        refreshBatchSummary();

        if (search.isEmpty() && (filter == null || "All Statuses".equalsIgnoreCase(filter))) {
            return;
        }

        for (int i = summaryTableModel.getRowCount() - 1; i >= 0; i--) {
            String index = ((String) summaryTableModel.getValueAt(i, 1)).toLowerCase();
            String name = ((String) summaryTableModel.getValueAt(i, 2)).toLowerCase();
            String status = (String) summaryTableModel.getValueAt(i, 9);

            boolean matchSearch = search.isEmpty() || index.contains(search) || name.contains(search);
            boolean matchStatus = true;

            if ("Eligible Outright".equalsIgnoreCase(filter)) {
                matchStatus = "ELIGIBLE".equalsIgnoreCase(status);
            } else if ("Eligible with Medical".equalsIgnoreCase(filter)) {
                matchStatus = "ELIGIBLE (MEDICAL)".equalsIgnoreCase(status);
            } else if ("Barred (<80%)".equalsIgnoreCase(filter)) {
                matchStatus = status.contains("BARRED");
            }

            if (!matchSearch || !matchStatus) {
                summaryTableModel.removeRow(i);
            }
        }
    }

    // =========================================================================
    // SUB-TAB 3: Individual Student Profile & Session History (FR-ATT-07)
    // =========================================================================
    private JPanel createStudentHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(lightBackground);

        // Search Bar Card
        JPanel searchCard = createCardPanel();
        searchCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        studentSearchField = new JTextField("TG/2024/2005");
        styleTextField(studentSearchField);

        studentOfferingCombo = new JComboBox<>();
        styleComboBox(studentOfferingCombo);

        JButton searchBtn = createStyledButton("Find Student Profile", primaryBlue, Color.WHITE);
        searchBtn.addActionListener(e -> searchStudentProfile());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        searchCard.add(createLabeledField("Enter Student Index No / ID / Name:", studentSearchField), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        searchCard.add(createLabeledField("Course Offering:", studentOfferingCombo), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        searchCard.add(searchBtn, gbc);

        // Student Profile Header & Warning Banner
        JPanel profileContainer = new JPanel();
        profileContainer.setLayout(new BoxLayout(profileContainer, BoxLayout.Y_AXIS));
        profileContainer.setOpaque(false);

        JPanel profileCard = createCardPanel();
        profileCard.setLayout(new GridLayout(2, 2, 10, 5));

        studentProfileNameLabel = new JLabel("Student Name: Sanduni Fernando");
        studentProfileNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        studentProfileNameLabel.setForeground(textDark);

        studentProfileIndexLabel = new JLabel("Index No: TG/2024/2005  (ID: STU005)");
        studentProfileIndexLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        studentProfileIndexLabel.setForeground(textMuted);

        studentProfileBatchLabel = new JLabel("Batch: 2024/2025  |  Dept: ICT");
        studentProfileBatchLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        studentProfileBatchLabel.setForeground(textMuted);

        studentProfileStatsLabel = new JLabel("Attended: 10/15 (Raw: 66.7%) | Adjusted: 86.7%");
        studentProfileStatsLabel.setFont(new Font("Arial", Font.BOLD, 13));
        studentProfileStatsLabel.setForeground(primaryBlue);

        profileCard.add(studentProfileNameLabel);
        profileCard.add(studentProfileIndexLabel);
        profileCard.add(studentProfileBatchLabel);
        profileCard.add(studentProfileStatsLabel);

        studentWarningBanner = new JLabel("✓ EXAM ELIGIBILITY CONFIRMED: Student satisfies the minimum 80% attendance requirement. Eligible for final examination.");
        studentWarningBanner.setFont(new Font("Arial", Font.BOLD, 12));
        studentWarningBanner.setOpaque(true);
        studentWarningBanner.setBackground(new Color(230, 247, 244));
        studentWarningBanner.setForeground(new Color(15, 125, 110));
        studentWarningBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 230, 220), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        profileContainer.add(profileCard);
        profileContainer.add(Box.createVerticalStrut(8));
        profileContainer.add(studentWarningBanner);

        // History Table Card
        JPanel historyCard = createCardPanel();
        historyCard.setLayout(new BorderLayout(0, 8));

        JLabel historyTitle = new JLabel("Session-by-Session Attendance Audit Log");
        historyTitle.setFont(new Font("Arial", Font.BOLD, 13));
        historyTitle.setForeground(darkNavy);

        String[] cols = {"Session #", "Session Date", "Session Type", "Recorded Status", "Medical Certificate Attached", "Audit Remarks"};
        studentHistoryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int c) { return String.class; }
        };

        studentHistoryTable = new JTable(studentHistoryModel);
        studentHistoryTable.setRowHeight(30);
        studentHistoryTable.setFont(new Font("Arial", Font.PLAIN, 13));
        studentHistoryTable.setGridColor(new Color(230, 235, 240));
        studentHistoryTable.setShowGrid(true);
        studentHistoryTable.setSelectionBackground(new Color(225, 237, 248));
        studentHistoryTable.setSelectionForeground(darkNavy);
        studentHistoryTable.setFillsViewportHeight(true);

        setupTableHeader(studentHistoryTable);

        setColumnWidth(studentHistoryTable, 0, 75, 85);
        setColumnWidth(studentHistoryTable, 1, 110, 120);
        setColumnWidth(studentHistoryTable, 2, 100, 110);
        setColumnWidth(studentHistoryTable, 3, 130, 140);
        setColumnWidth(studentHistoryTable, 4, 180, 200);
        setColumnWidth(studentHistoryTable, 5, 200, 350);

        studentHistoryTable.getColumnModel().getColumn(3).setCellRenderer(new AttendanceStatusRenderer());
        studentHistoryTable.setDefaultRenderer(String.class, new AlternateRowRenderer());

        JScrollPane histScroll = new JScrollPane(studentHistoryTable);
        histScroll.setBorder(new LineBorder(borderColor, 1));
        histScroll.getViewport().setBackground(Color.WHITE);

        historyCard.add(historyTitle, BorderLayout.NORTH);
        historyCard.add(histScroll, BorderLayout.CENTER);

        panel.add(searchCard, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 8));
        centerWrapper.setOpaque(false);
        centerWrapper.add(profileContainer, BorderLayout.NORTH);
        centerWrapper.add(historyCard, BorderLayout.CENTER);

        panel.add(centerWrapper, BorderLayout.CENTER);

        return panel;
    }

    private void searchStudentProfile() {
        String query = studentSearchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student Index Number, ID, or Name.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        CourseOffering offering = (CourseOffering) studentOfferingCombo.getSelectedItem();
        int offeringId = offering != null ? offering.getOfferingId() : 1;

        Undergraduate student = service.findStudentByIndex(query);
        if (student == null) {
            JOptionPane.showMessageDialog(this, "No student record found matching: " + query, "Student Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        studentProfileNameLabel.setText("Student Name: " + student.getFullName());
        studentProfileIndexLabel.setText("Index No: " + student.getIndexNo() + "  (ID: " + student.getStudentCode() + ")");
        studentProfileBatchLabel.setText("Batch: " + student.getBatch() + "  |  Dept: " + student.getDepartment());

        List<AttendanceRecord> history = service.getStudentHistory(offeringId, student.getIndexNo());
        studentHistoryModel.setRowCount(0);

        int attended = 0;
        int medicals = 0;
        int total = history.size();
        if (total == 0) total = 15;

        for (AttendanceRecord r : history) {
            String medAttached = "-";
            if ("Medical".equalsIgnoreCase(r.getStatus())) {
                medicals++;
                medAttached = "Official Medical Certificate";
            } else if ("Present".equalsIgnoreCase(r.getStatus())) {
                attended++;
            }

            String dateStr = r.getSessionDate() != null ? dateFormat.format(r.getSessionDate()) : "2026-02-10";

            studentHistoryModel.addRow(new Object[]{
                    "Session " + r.getSessionNumber(),
                    dateStr,
                    r.getSessionType(),
                    r.getStatus(),
                    medAttached,
                    r.getRemarks()
            });
        }

        double raw = util.AttendanceCalculator.calculatePercentage(attended, total);
        double adjusted = util.AttendanceCalculator.calculateAdjustedPercentage(attended, medicals, total);

        studentProfileStatsLabel.setText(String.format("Attended: %d/%d (Raw: %.1f%%) | Adjusted: %.1f%%", attended, total, raw, adjusted));

        if (adjusted >= 80.0) {
            studentWarningBanner.setBackground(new Color(230, 247, 244));
            studentWarningBanner.setForeground(new Color(15, 125, 110));
            studentWarningBanner.setText(String.format("✓ EXAM ELIGIBILITY CONFIRMED: Student satisfies the minimum 80%% attendance requirement (Effective %.1f%%). Eligible to sit for continuous assessments & finals.", adjusted));
        } else {
            studentWarningBanner.setBackground(new Color(254, 237, 237));
            studentWarningBanner.setForeground(new Color(190, 40, 40));
            studentWarningBanner.setText(String.format("⚠ ACADEMIC WARNING: Attendance is currently %.1f%% (Below the mandatory 80.0%% threshold). Student is BARRED from final examinations under Faculty By-Laws unless valid medical certificates are submitted.", adjusted));
        }
    }

    // =========================================================================
    // Core Helper Methods and Event Listeners
    // =========================================================================
    private void loadOfferingsData() {
        courseOfferingCombo.removeAllItems();
        summaryOfferingCombo.removeAllItems();
        studentOfferingCombo.removeAllItems();

        List<CourseOffering> offerings = service.getCourseOfferings();
        for (CourseOffering o : offerings) {
            courseOfferingCombo.addItem(o);
            summaryOfferingCombo.addItem(o);
            studentOfferingCombo.addItem(o);
        }
    }

    private void autoLoadEnrolledBatch() {
        CourseOffering selected = (CourseOffering) courseOfferingCombo.getSelectedItem();
        if (selected == null) return;

        List<Undergraduate> students = service.getStudentsForOffering(selected.getOfferingId());
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        tableModel.setRowCount(0);
        int idx = 1;
        for (Undergraduate u : students) {
            tableModel.addRow(new Object[]{
                    String.valueOf(idx++),
                    u.getStudentCode(),
                    u.getIndexNo(),
                    u.getFullName(),
                    "Present",
                    ""
            });
        }
        updateLiveCounters();
    }

    private void addNewRosterRow() {
        int nextIndex = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{String.valueOf(nextIndex), "STU" + String.format("%03d", nextIndex), "TG/2024/" + (2000 + nextIndex), "Student " + nextIndex, "Present", ""});
        int lastRow = tableModel.getRowCount() - 1;
        attendanceTable.setRowSelectionInterval(lastRow, lastRow);
        attendanceTable.scrollRectToVisible(attendanceTable.getCellRect(lastRow, 0, true));
        updateLiveCounters();
    }

    private void removeSelectedRosterRow() {
        int selected = attendanceTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student row to remove from the roster.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        tableModel.removeRow(selected);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(String.valueOf(i + 1), i, 0);
        }
        updateLiveCounters();
    }

    private void markAllPresent() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("Present", i, 4);
        }
        updateLiveCounters();
    }

    private void clearTable() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("Absent", i, 4);
            tableModel.setValueAt("", i, 5);
        }
        updateLiveCounters();
    }

    private void updateLiveCounters() {
        int total = tableModel.getRowCount();
        int present = 0;
        int absent = 0;
        int medical = 0;

        for (int i = 0; i < total; i++) {
            Object val = tableModel.getValueAt(i, 4);
            String st = val != null ? val.toString() : "";
            if ("Present".equalsIgnoreCase(st)) present++;
            else if ("Absent".equalsIgnoreCase(st)) absent++;
            else if ("Medical".equalsIgnoreCase(st)) medical++;
        }

        totalStudentsLabel.setText("Total Students: " + total);
        presentCountLabel.setText("Present: " + present);
        absentCountLabel.setText("Absent: " + absent);
        medicalCountLabel.setText("Medical: " + medical);
    }

    private void saveCurrentSession() {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        CourseOffering offering = (CourseOffering) courseOfferingCombo.getSelectedItem();
        if (offering == null) {
            JOptionPane.showMessageDialog(this, "Please select a Course Offering.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String type = (String) sessionTypeCombo.getSelectedItem();
        if (type == null || sessionTypeCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a valid Session Type (Practical, Theory, Tutorial).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionNoStr = sessionNumberField.getText().trim();
        String durationStr = durationField.getText().trim();
        String dateStr = sessionDateField.getText().trim();

        int sessionNo;
        double duration;
        Date sessionDate;

        try {
            sessionNo = Integer.parseInt(sessionNoStr);
            if (sessionNo < 1 || sessionNo > 15) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Session Number must be an integer between 1 and 15.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            duration = Double.parseDouble(durationStr);
            if (duration <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Duration must be a positive number of hours (e.g. 2.0).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            sessionDate = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-10-12).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cannot save attendance: No students listed in the roster.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AttendanceSession session = new AttendanceSession(0, offering.getOfferingId(), sessionDate, type, sessionNo, duration);
        List<AttendanceRecord> records = new ArrayList<>();

        int present = 0;
        int absent = 0;
        int medical = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String sidStr = (String) tableModel.getValueAt(i, 1);
            int sid = 0;
            try { 
                sid = Integer.parseInt(sidStr.replaceAll("\\D+", "")); 
            } catch (Exception ignored) {}
            String index = (String) tableModel.getValueAt(i, 2);
            String name = (String) tableModel.getValueAt(i, 3);
            String status = (String) tableModel.getValueAt(i, 4);
            String remarks = (String) tableModel.getValueAt(i, 5);

            if ("Present".equalsIgnoreCase(status)) present++;
            else if ("Absent".equalsIgnoreCase(status)) absent++;
            else if ("Medical".equalsIgnoreCase(status)) medical++;

            records.add(new AttendanceRecord(sid, index, name, status, remarks));
        }

        boolean success = service.recordSessionAttendance(session, records);
        if (success) {
            String message = String.format(
                    "Session Attendance recorded successfully!\n\n" +
                    "--------------------------------------------------\n" +
                    "Course Offering : %s\n" +
                    "Session Date    : %s\n" +
                    "Session Number  : Session %d (%s)\n" +
                    "Contact Hours   : %.1f Hours\n" +
                    "--------------------------------------------------\n" +
                    "Total Enrolled  : %d Students\n" +
                    "Present Count   : %d\n" +
                    "Absent Count    : %d\n" +
                    "Medical Logged  : %d\n" +
                    "--------------------------------------------------\n" +
                    "Database status : Persisted with ACID transaction guarantee.",
                    offering.getOfferingInfo(), dateStr, sessionNo, type, duration,
                    records.size(), present, absent, medical
            );
            JOptionPane.showMessageDialog(this, message, "Attendance Recorded", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save attendance records to database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchSubTab(String tabName, JButton activeBtn) {
        tabCardLayout.show(tabContentContainer, tabName);
        resetTabButtonStyles();
        activeBtn.setBackground(primaryBlue);
        activeBtn.setForeground(Color.WHITE);
        activeBtn.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void resetTabButtonStyles() {
        for (JButton b : new JButton[]{tab1Btn, tab2Btn, tab3Btn}) {
            b.setBackground(Color.WHITE);
            b.setForeground(textDark);
            b.setFont(new Font("Arial", Font.PLAIN, 12));
        }
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = createStyledButton(text, active ? primaryBlue : Color.WHITE, active ? Color.WHITE : textDark);
        return btn;
    }

    // =========================================================================
    // UI Helpers & Custom Renderers
    // =========================================================================
    private JPanel createLabeledField(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(textDark);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(cardWhite);
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return card;
    }

    private JLabel createBadge(String text, Color textColor, Color bgColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(textColor);
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setBorder(new CompoundBorder(
                new LineBorder(textColor, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return label;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        boolean isWhite = Color.WHITE.equals(bg);
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(isWhite ? new Color(225, 230, 238) : bg.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(isWhite ? new Color(242, 245, 248) : bg.darker());
                } else {
                    g2.setColor(bg);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                if (isWhite) {
                    g2.setColor(borderColor);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(fg);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 12, 6, 12));
        return button;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Arial", Font.PLAIN, 13));
        textField.setForeground(textDark);
        textField.setBackground(Color.WHITE);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 28));
        textField.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        comboBox.setForeground(textDark);
        comboBox.setBackground(Color.WHITE);
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 28));
    }

    private void setupTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setBackground(new Color(240, 244, 248));
        header.setForeground(darkNavy);
        header.setReorderingAllowed(false);
    }

    private void setColumnWidth(JTable table, int colIndex, int preferredWidth, int maxWidth) {
        TableColumn col = table.getColumnModel().getColumn(colIndex);
        col.setPreferredWidth(preferredWidth);
        if (maxWidth > 0 && colIndex == 0) {
            col.setMaxWidth(maxWidth);
            col.setMinWidth(preferredWidth);
        }
    }

    // Status Renderer for Tab 1 & Tab 3
    private class AttendanceStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));

            String status = value != null ? value.toString() : "";
            if ("Present".equalsIgnoreCase(status)) {
                label.setForeground(new Color(15, 125, 110));
                label.setBackground(isSelected ? new Color(205, 235, 230) : new Color(230, 247, 244));
            } else if ("Absent".equalsIgnoreCase(status)) {
                label.setForeground(new Color(190, 40, 40));
                label.setBackground(isSelected ? new Color(250, 215, 215) : new Color(254, 237, 237));
            } else if ("Medical".equalsIgnoreCase(status)) {
                label.setForeground(pendingPurple);
                label.setBackground(isSelected ? new Color(235, 225, 250) : new Color(245, 238, 255));
            } else {
                label.setForeground(textDark);
                label.setBackground(isSelected ? new Color(225, 237, 248) : Color.WHITE);
            }

            label.setBorder(new EmptyBorder(2, 6, 2, 6));
            return label;
        }
    }

    // Scenario Code Renderer (SC-01 through SC-05)
    private class ScenarioCodeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));

            String code = value != null ? value.toString() : "";
            if ("SC-01".equals(code) || "SC-02".equals(code)) {
                label.setForeground(new Color(15, 125, 110));
                label.setBackground(isSelected ? new Color(205, 235, 230) : new Color(230, 247, 244));
            } else if ("SC-03".equals(code)) {
                label.setForeground(new Color(190, 40, 40));
                label.setBackground(isSelected ? new Color(250, 215, 215) : new Color(254, 237, 237));
            } else if ("SC-04".equals(code) || "SC-05".equals(code)) {
                label.setForeground(new Color(45, 100, 160));
                label.setBackground(isSelected ? new Color(215, 230, 250) : new Color(235, 245, 255));
            } else {
                label.setForeground(textDark);
                label.setBackground(isSelected ? new Color(225, 237, 248) : Color.WHITE);
            }

            label.setBorder(new EmptyBorder(2, 6, 2, 6));
            return label;
        }
    }

    // Eligibility Status Renderer (ELIGIBLE, ELIGIBLE WITH MEDICAL, BARRED)
    private class EligibilityStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));

            String status = value != null ? value.toString() : "";
            if ("ELIGIBLE".equalsIgnoreCase(status)) {
                label.setForeground(new Color(15, 125, 110));
                label.setBackground(isSelected ? new Color(205, 235, 230) : new Color(230, 247, 244));
            } else if ("ELIGIBLE (MEDICAL)".equalsIgnoreCase(status)) {
                label.setForeground(new Color(45, 100, 160));
                label.setBackground(isSelected ? new Color(215, 230, 250) : new Color(235, 245, 255));
            } else if (status.contains("BARRED") || status.contains("INELIGIBLE")) {
                label.setForeground(new Color(190, 40, 40));
                label.setBackground(isSelected ? new Color(250, 215, 215) : new Color(254, 237, 237));
            } else {
                label.setForeground(textDark);
                label.setBackground(isSelected ? new Color(225, 237, 248) : Color.WHITE);
            }

            label.setBorder(new EmptyBorder(2, 6, 2, 6));
            return label;
        }
    }

    // Striped Row Renderer
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
                    c.setBackground(tableStripe);
                }
            } else {
                c.setForeground(darkNavy);
                c.setBackground(new Color(225, 237, 248));
            }

            if (column == 0 || column == 1 || column == 2) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (c instanceof JLabel) {
                ((JLabel) c).setBorder(new EmptyBorder(0, 6, 0, 6));
            }

            return c;
        }
    }
}
