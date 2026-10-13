package view;

import controller.AttendanceController;
import model.AttendanceRecord;
import model.AttendanceScenarioResult;
import model.AttendanceSession;
import model.CourseOffering;
import model.Undergraduate;

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
 * Standard file structure: package view
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

    // Business controller
    private final AttendanceController controller;
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
        this.controller = new AttendanceController();
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

        // Header Panel with Title and Clean Sub-Tab Strip
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(lightBackground);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(lightBackground);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Attendance Management System");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Record laboratory attendance sessions, evaluate exam eligibility (80% rule), and track student profiles.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        // Sub-Navigation Tabs Strip
        JPanel tabsStrip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tabsStrip.setBackground(lightBackground);

        tab1Btn = createTabButton("Record Session Attendance", true);
        tab2Btn = createTabButton("Batch Eligibility & 5 Scenarios", false);
        tab3Btn = createTabButton("Student Profile & History", false);

        tab1Btn.addActionListener(e -> switchTab("TAB1", tab1Btn));
        tab2Btn.addActionListener(e -> {
            switchTab("TAB2", tab2Btn);
            refreshBatchSummary();
        });
        tab3Btn.addActionListener(e -> switchTab("TAB3", tab3Btn));

        tabsStrip.add(tab1Btn);
        tabsStrip.add(tab2Btn);
        tabsStrip.add(tab3Btn);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(tabsStrip, BorderLayout.SOUTH);

        // Center Container hosting all 3 Sub-Views
        tabCardLayout = new CardLayout();
        tabContentContainer = new JPanel(tabCardLayout);
        tabContentContainer.setBackground(lightBackground);

        tabContentContainer.add(createTab1SessionLogging(), "TAB1");
        tabContentContainer.add(createTab2BatchSummary(), "TAB2");
        tabContentContainer.add(createTab3StudentHistory(), "TAB3");

        mainWrapper.add(headerPanel, BorderLayout.NORTH);
        mainWrapper.add(tabContentContainer, BorderLayout.CENTER);

        add(mainWrapper, BorderLayout.CENTER);
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = createStyledButton(text, active ? primaryBlue : Color.WHITE, active ? Color.WHITE : textDark);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 10, 34));
        return btn;
    }

    private void switchTab(String tabName, JButton activeBtn) {
        tabCardLayout.show(tabContentContainer, tabName);
        JButton[] allTabs = {tab1Btn, tab2Btn, tab3Btn};
        for (JButton b : allTabs) {
            if (b == activeBtn) {
                b.setBackground(primaryBlue);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(Color.WHITE);
                b.setForeground(textDark);
            }
            b.repaint();
        }
    }

    // =========================================================================
    // TAB 1: Session Attendance Logging (FR-ATT-01)
    // =========================================================================
    private JPanel createTab1SessionLogging() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(lightBackground);

        panel.add(createSessionConfigPanel(), BorderLayout.NORTH);
        panel.add(createRosterTablePanel(), BorderLayout.CENTER);
        panel.add(createTab1FooterPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSessionConfigPanel() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel sectionTitle = new JLabel("Session Configuration (FR-ATT-01)");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 13));
        sectionTitle.setForeground(darkNavy);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        courseOfferingCombo = new JComboBox<>();
        styleComboBox(courseOfferingCombo);
        courseOfferingCombo.addActionListener(e -> autoLoadEnrolledBatch());

        sessionDateField = new JTextField("");
        styleTextField(sessionDateField);

        String[] sessionTypes = {"-- Select Session Type --", "Practical", "Theory", "Tutorial"};
        sessionTypeCombo = new JComboBox<>(sessionTypes);
        styleComboBox(sessionTypeCombo);

        sessionNumberField = new JTextField("");
        styleTextField(sessionNumberField);

        durationField = new JTextField("");
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
        setColumnWidth(attendanceTable, 2, 115, 125);
        setColumnWidth(attendanceTable, 3, 200, 320);
        setColumnWidth(attendanceTable, 4, 135, 150);
        setColumnWidth(attendanceTable, 5, 180, 400);

        JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"Present", "Absent", "Medical"});
        statusDropdown.setFont(new Font("Arial", Font.BOLD, 12));
        DefaultCellEditor statusEditor = new DefaultCellEditor(statusDropdown);
        statusEditor.setClickCountToStart(1);
        attendanceTable.getColumnModel().getColumn(4).setCellEditor(statusEditor);
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

    private JPanel createTab1FooterPanel() {
        JPanel footerCard = createCardPanel();
        footerCard.setLayout(new BorderLayout(10, 0));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statsPanel.setOpaque(false);

        totalStudentsLabel = createBadge("Total Students: 0", darkNavy, new Color(236, 242, 248));
        presentCountLabel = createBadge("Present: 0", successGreen, new Color(230, 247, 244));
        absentCountLabel = createBadge("Absent: 0", alertRed, new Color(254, 237, 237));
        medicalCountLabel = createBadge("Medical: 0", pendingPurple, new Color(245, 238, 255));

        statsPanel.add(totalStudentsLabel);
        statsPanel.add(presentCountLabel);
        statsPanel.add(absentCountLabel);
        statsPanel.add(medicalCountLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton clearBtn = createStyledButton("Clear", Color.WHITE, textDark);
        clearBtn.addActionListener(e -> clearSessionForm());

        JButton markAllPresentBtn = createStyledButton("Mark All Present", primaryBlue, Color.WHITE);
        markAllPresentBtn.addActionListener(e -> markAllPresent());

        JButton saveBtn = createStyledButton("Save Attendance", successGreen, Color.WHITE);
        saveBtn.addActionListener(e -> saveAttendanceSession());

        actionPanel.add(clearBtn);
        actionPanel.add(markAllPresentBtn);
        actionPanel.add(saveBtn);

        footerCard.add(statsPanel, BorderLayout.WEST);
        footerCard.add(actionPanel, BorderLayout.EAST);
        return footerCard;
    }

    // =========================================================================
    // TAB 2: Whole Batch Eligibility & 5 Scenarios Analyzer (FR-ATT-03, 04, 05, 06)
    // =========================================================================
    private JPanel createTab2BatchSummary() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(lightBackground);

        // Filter Bar Card
        JPanel filterCard = createCardPanel();
        filterCard.setLayout(new BorderLayout(8, 6));

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterControls.setOpaque(false);

        summaryOfferingCombo = new JComboBox<>();
        styleComboBox(summaryOfferingCombo);
        summaryOfferingCombo.addActionListener(e -> refreshBatchSummary());

        trackFilterCombo = new JComboBox<>(new String[]{"Combined (All Tracks)", "Theory", "Practical"});
        styleComboBox(trackFilterCombo);
        trackFilterCombo.addActionListener(e -> refreshBatchSummary());

        eligibilityFilterCombo = new JComboBox<>(new String[]{"All Eligibility States", "Eligible (Passed)", "Eligible (Medical Concession)", "Ineligible (Barred)"});
        styleComboBox(eligibilityFilterCombo);
        eligibilityFilterCombo.addActionListener(e -> refreshBatchSummary());

        summarySearchField = new JTextField(12);
        styleTextField(summarySearchField);

        JButton searchBtn = createStyledButton("Filter", primaryBlue, Color.WHITE);
        searchBtn.addActionListener(e -> refreshBatchSummary());

        filterControls.add(new JLabel("Course:"));
        filterControls.add(summaryOfferingCombo);
        filterControls.add(new JLabel("Track (FR-ATT-05):"));
        filterControls.add(trackFilterCombo);
        filterControls.add(new JLabel("Status:"));
        filterControls.add(eligibilityFilterCombo);
        filterControls.add(new JLabel("Search:"));
        filterControls.add(summarySearchField);
        filterControls.add(searchBtn);

        // Counter Badges
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badgesPanel.setOpaque(false);
        sumTotalLabel = createBadge("Total: 0", darkNavy, new Color(236, 242, 248));
        sumEligibleLabel = createBadge("Eligible (>80%): 0", successGreen, new Color(230, 247, 244));
        sumEligibleMedLabel = createBadge("Eligible (Med): 0", pendingPurple, new Color(245, 238, 255));
        sumIneligibleLabel = createBadge("Barred (<80%): 0", alertRed, new Color(254, 237, 237));

        badgesPanel.add(sumTotalLabel);
        badgesPanel.add(sumEligibleLabel);
        badgesPanel.add(sumEligibleMedLabel);
        badgesPanel.add(sumIneligibleLabel);

        filterCard.add(filterControls, BorderLayout.CENTER);
        filterCard.add(badgesPanel, BorderLayout.SOUTH);

        // Summary Table Card
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] cols = {
                "#", "Index No", "Student Name", "Attended / Conducted", "Raw %", 
                "Approved Med", "Adjusted %", "Scenario (FR-ATT-04)", "Eligibility Status", "Evaluation Detail"
        };
        summaryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        summaryTable = new JTable(summaryTableModel);
        summaryTable.setRowHeight(32);
        summaryTable.setFont(new Font("Arial", Font.PLAIN, 12));
        summaryTable.setGridColor(new Color(230, 235, 240));
        setupTableHeader(summaryTable);
        summaryTable.getColumnModel().getColumn(8).setCellRenderer(new EligibilityBadgeRenderer());
        summaryTable.setDefaultRenderer(Object.class, new AlternateRowRenderer());

        setColumnWidth(summaryTable, 0, 40, 45);
        setColumnWidth(summaryTable, 1, 105, 115);
        setColumnWidth(summaryTable, 2, 160, 200);
        setColumnWidth(summaryTable, 3, 130, 140);
        setColumnWidth(summaryTable, 4, 75, 85);
        setColumnWidth(summaryTable, 5, 90, 100);
        setColumnWidth(summaryTable, 6, 85, 95);
        setColumnWidth(summaryTable, 7, 130, 150);
        setColumnWidth(summaryTable, 8, 140, 155);

        JScrollPane scroll = new JScrollPane(summaryTable);
        scroll.setBorder(new LineBorder(borderColor, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(filterCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private void refreshBatchSummary() {
        if (summaryOfferingCombo.getItemCount() == 0) return;
        CourseOffering selected = (CourseOffering) summaryOfferingCombo.getSelectedItem();
        if (selected == null) return;

        String track = (String) trackFilterCombo.getSelectedItem();
        if (track != null && track.contains("Theory")) track = "Theory";
        else if (track != null && track.contains("Practical")) track = "Practical";
        else track = "Combined";

        String eligibilityFilter = (String) eligibilityFilterCombo.getSelectedItem();
        String search = summarySearchField.getText().trim().toLowerCase();

        List<AttendanceScenarioResult> results = controller.getBatchSummary(selected.getOfferingId(), track);
        summaryTableModel.setRowCount(0);

        int total = 0;
        int eligibleCount = 0;
        int eligibleMedCount = 0;
        int ineligibleCount = 0;

        int rowNum = 1;
        for (AttendanceScenarioResult r : results) {
            String status = r.getEligibilityStatus();
            if ("ELIGIBLE".equalsIgnoreCase(status)) eligibleCount++;
            else if (status.contains("MEDICAL")) eligibleMedCount++;
            else ineligibleCount++;
            total++;

            // Filtering
            if (eligibilityFilter != null && !eligibilityFilter.startsWith("All")) {
                if (eligibilityFilter.contains("Passed") && !"ELIGIBLE".equalsIgnoreCase(status)) continue;
                if (eligibilityFilter.contains("Concession") && !status.contains("MEDICAL")) continue;
                if (eligibilityFilter.contains("Barred") && !status.contains("INELIGIBLE")) continue;
            }

            if (!search.isEmpty()) {
                String index = r.getStudent().getIndexNo().toLowerCase();
                String name = r.getStudent().getFullName().toLowerCase();
                if (!index.contains(search) && !name.contains(search)) continue;
            }

            summaryTableModel.addRow(new Object[]{
                    rowNum++,
                    r.getStudent().getIndexNo(),
                    r.getStudent().getFullName(),
                    r.getAttendedCount() + " / " + r.getTotalConducted(),
                    String.format("%.1f%%", r.getRawPercentage()),
                    r.getApprovedMedicals(),
                    String.format("%.1f%%", r.getAdjustedPercentage()),
                    r.getScenarioCode() + ": " + r.getScenarioDescription().substring(0, Math.min(25, r.getScenarioDescription().length())) + "...",
                    r.getEligibilityStatus(),
                    r.getScenarioDescription()
            });
        }

        sumTotalLabel.setText("Total: " + total);
        sumEligibleLabel.setText("Eligible (>80%): " + eligibleCount);
        sumEligibleMedLabel.setText("Eligible (Med): " + eligibleMedCount);
        sumIneligibleLabel.setText("Barred (<80%): " + ineligibleCount);
    }

    // =========================================================================
    // TAB 3: Individual Student Attendance Profile & History (FR-ATT-07)
    // =========================================================================
    private JPanel createTab3StudentHistory() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(lightBackground);

        // Search Controls Card
        JPanel searchCard = createCardPanel();
        searchCard.setLayout(new BorderLayout(10, 8));

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchControls.setOpaque(false);

        studentOfferingCombo = new JComboBox<>();
        styleComboBox(studentOfferingCombo);

        studentSearchField = new JTextField("TG/2022/891", 12);
        styleTextField(studentSearchField);

        JButton searchBtn = createStyledButton("Search Student Profile", primaryBlue, Color.WHITE);
        searchBtn.addActionListener(e -> loadStudentProfileHistory());

        searchControls.add(new JLabel("Course Offering:"));
        searchControls.add(studentOfferingCombo);
        searchControls.add(new JLabel("Student Index No:"));
        searchControls.add(studentSearchField);
        searchControls.add(searchBtn);

        searchCard.add(searchControls, BorderLayout.CENTER);

        // Center Profile Card + Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);

        // Profile summary info
        JPanel profileCard = createCardPanel();
        profileCard.setLayout(new GridLayout(2, 2, 10, 6));

        studentProfileNameLabel = new JLabel("Student Name: -");
        studentProfileNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        studentProfileNameLabel.setForeground(textDark);

        studentProfileIndexLabel = new JLabel("Index No: -");
        studentProfileIndexLabel.setFont(new Font("Arial", Font.BOLD, 13));
        studentProfileIndexLabel.setForeground(textDark);

        studentProfileBatchLabel = new JLabel("Batch / Dept: -");
        studentProfileBatchLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        studentProfileBatchLabel.setForeground(textMuted);

        studentProfileStatsLabel = new JLabel("Attendance: -");
        studentProfileStatsLabel.setFont(new Font("Arial", Font.BOLD, 13));
        studentProfileStatsLabel.setForeground(primaryBlue);

        profileCard.add(studentProfileNameLabel);
        profileCard.add(studentProfileIndexLabel);
        profileCard.add(studentProfileBatchLabel);
        profileCard.add(studentProfileStatsLabel);

        // Warning banner
        studentWarningBanner = new JLabel("  Status: Awaiting student search.");
        studentWarningBanner.setFont(new Font("Arial", Font.BOLD, 12));
        studentWarningBanner.setOpaque(true);
        studentWarningBanner.setBackground(new Color(236, 242, 248));
        studentWarningBanner.setForeground(darkNavy);
        studentWarningBanner.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel profileHeader = new JPanel(new BorderLayout(0, 6));
        profileHeader.setOpaque(false);
        profileHeader.add(profileCard, BorderLayout.NORTH);
        profileHeader.add(studentWarningBanner, BorderLayout.SOUTH);

        // History Table
        JPanel historyCard = createCardPanel();
        historyCard.setLayout(new BorderLayout(0, 8));

        JLabel historyTitle = new JLabel("Session-by-Session Historical Audit Log (Sessions 1 - 15)");
        historyTitle.setFont(new Font("Arial", Font.BOLD, 13));
        historyTitle.setForeground(darkNavy);

        String[] cols = {"Session #", "Date", "Track Type", "Status", "Remarks", "Medical Concession Applied"};
        studentHistoryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        studentHistoryTable = new JTable(studentHistoryModel);
        studentHistoryTable.setRowHeight(30);
        studentHistoryTable.setFont(new Font("Arial", Font.PLAIN, 12));
        setupTableHeader(studentHistoryTable);
        studentHistoryTable.getColumnModel().getColumn(3).setCellRenderer(new AttendanceStatusRenderer());
        studentHistoryTable.setDefaultRenderer(Object.class, new AlternateRowRenderer());

        JScrollPane historyScroll = new JScrollPane(studentHistoryTable);
        historyScroll.setBorder(new LineBorder(borderColor, 1));
        historyScroll.getViewport().setBackground(Color.WHITE);

        historyCard.add(historyTitle, BorderLayout.NORTH);
        historyCard.add(historyScroll, BorderLayout.CENTER);

        centerPanel.add(profileHeader, BorderLayout.NORTH);
        centerPanel.add(historyCard, BorderLayout.CENTER);

        panel.add(searchCard, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void loadStudentProfileHistory() {
        String index = studentSearchField.getText().trim();
        if (index.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a student index number (e.g. TG/2022/891).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CourseOffering offering = (CourseOffering) studentOfferingCombo.getSelectedItem();
        int offeringId = offering != null ? offering.getOfferingId() : 1;

        Undergraduate student = controller.findStudentByIndex(index);
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Student not found with Index No: " + index, "Search Result", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        studentProfileNameLabel.setText("Student Name: " + student.getFullName());
        studentProfileIndexLabel.setText("Index No: " + student.getIndexNo());
        studentProfileBatchLabel.setText("Batch: " + student.getBatch() + " | Department: " + student.getDepartment());

        List<AttendanceRecord> history = controller.getStudentHistory(offeringId, index);
        studentHistoryModel.setRowCount(0);

        int attended = 0;
        int medicals = 0;
        int total = history.size() > 0 ? history.size() : 15;

        for (AttendanceRecord r : history) {
            if ("Present".equalsIgnoreCase(r.getStatus())) attended++;
            else if ("Medical".equalsIgnoreCase(r.getStatus())) medicals++;

            studentHistoryModel.addRow(new Object[]{
                    "Session " + r.getSessionNumber(),
                    r.getSessionDate() != null ? dateFormat.format(r.getSessionDate()) : "N/A",
                    r.getSessionType() != null ? r.getSessionType() : "Practical",
                    r.getStatus(),
                    r.getRemarks() != null ? r.getRemarks() : "",
                    "Medical".equalsIgnoreCase(r.getStatus()) ? "Yes (Approved Certificate Evaluated)" : "No"
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

        List<CourseOffering> offerings = controller.getCourseOfferings();
        for (CourseOffering o : offerings) {
            courseOfferingCombo.addItem(o);
            summaryOfferingCombo.addItem(o);
            studentOfferingCombo.addItem(o);
        }
    }

    private void autoLoadEnrolledBatch() {
        CourseOffering selected = (CourseOffering) courseOfferingCombo.getSelectedItem();
        if (selected == null) return;

        List<Undergraduate> students = controller.getStudentsForOffering(selected.getOfferingId());
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
        tableModel.addRow(new Object[]{String.valueOf(nextIndex), "", "", "", "Present", ""});
        int lastRow = tableModel.getRowCount() - 1;
        attendanceTable.setRowSelectionInterval(lastRow, lastRow);
        attendanceTable.scrollRectToVisible(attendanceTable.getCellRect(lastRow, 0, true));
    }

    private void removeSelectedRosterRow() {
        int count = tableModel.getRowCount();
        if (count == 0) return;

        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        int selected = attendanceTable.getSelectedRow();
        if (selected >= 0 && selected < count) {
            tableModel.removeRow(selected);
        } else {
            tableModel.removeRow(count - 1);
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(String.valueOf(i + 1), i, 0);
        }
        updateLiveCounters();
    }

    private void updateLiveCounters() {
        int total = tableModel.getRowCount();
        int present = 0;
        int absent = 0;
        int medical = 0;

        for (int i = 0; i < total; i++) {
            Object statusObj = tableModel.getValueAt(i, 4);
            String status = statusObj != null ? statusObj.toString() : "";
            if ("Present".equalsIgnoreCase(status)) present++;
            else if ("Absent".equalsIgnoreCase(status)) absent++;
            else if ("Medical".equalsIgnoreCase(status)) medical++;
        }

        totalStudentsLabel.setText("Total Students: " + total);
        presentCountLabel.setText("Present: " + present);
        absentCountLabel.setText("Absent: " + absent);
        medicalCountLabel.setText("Medical: " + medical);
    }

    private void markAllPresent() {
        int count = tableModel.getRowCount();
        if (count == 0) return;

        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        for (int i = 0; i < count; i++) {
            tableModel.setValueAt("Present", i, 4);
        }
        updateLiveCounters();
    }

    private void clearSessionForm() {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }
        tableModel.setRowCount(0);
        sessionDateField.setText("");
        sessionNumberField.setText("");
        durationField.setText("");
        if (courseOfferingCombo.getItemCount() > 0) courseOfferingCombo.setSelectedIndex(0);
        if (sessionTypeCombo.getItemCount() > 0) sessionTypeCombo.setSelectedIndex(0);
        updateLiveCounters();
    }

    private void saveAttendanceSession() {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        CourseOffering offering = (CourseOffering) courseOfferingCombo.getSelectedItem();
        if (offering == null) {
            JOptionPane.showMessageDialog(this, "Please select a Course Offering.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String type = (String) sessionTypeCombo.getSelectedItem();
        if (type == null || type.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Please select a valid Session Type.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = sessionDateField.getText().trim();
        String sessionNoStr = sessionNumberField.getText().trim();
        String durationStr = durationField.getText().trim();

        if (dateStr.isEmpty() || sessionNoStr.isEmpty() || durationStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all session configuration fields (Date, Session No, Duration).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sessionNo;
        double duration;
        Date sessionDate;

        try {
            sessionNo = Integer.parseInt(sessionNoStr);
            if (sessionNo < 1 || sessionNo > 15) {
                JOptionPane.showMessageDialog(this, "Session Number must be between 1 and 15 (Standard curriculum bounds per FR-CRS-02).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
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

        boolean success = controller.recordSessionAttendance(session, records);
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

    // =========================================================================
    // UI Styling & Custom Renderers
    // =========================================================================
    private void setupTableHeader(JTable table) {
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 12));
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 34));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel headerLabel = new JLabel(value != null ? value.toString() : "");
                headerLabel.setOpaque(true);
                headerLabel.setBackground(darkNavy);
                headerLabel.setForeground(Color.WHITE);
                headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
                headerLabel.setHorizontalAlignment(column == 2 || column == 3 ? SwingConstants.LEFT : SwingConstants.CENTER);
                headerLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(45, 90, 135)),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
                return headerLabel;
            }
        });
    }

    private void setColumnWidth(JTable table, int colIndex, int preferredWidth, int maxWidth) {
        TableColumn col = table.getColumnModel().getColumn(colIndex);
        col.setPreferredWidth(preferredWidth);
        if (maxWidth > 0 && colIndex == 0) {
            col.setMaxWidth(maxWidth);
            col.setMinWidth(preferredWidth);
        }
    }

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
                new EmptyBorder(10, 14, 10, 14)
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
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg = getBackground();
                boolean isWhite = Color.WHITE.equals(currentBg) || new Color(245, 247, 250).equals(currentBg);

                if (getModel().isPressed()) {
                    g2.setColor(isWhite ? new Color(225, 230, 238) : currentBg.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(isWhite ? new Color(242, 245, 248) : currentBg.darker());
                } else {
                    g2.setColor(currentBg);
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
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 14, 6, 14));
        return button;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Arial", Font.PLAIN, 13));
        textField.setForeground(textDark);
        textField.setBackground(Color.WHITE);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width > 120 ? textField.getPreferredSize().width : 160, 32));
        textField.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        comboBox.setForeground(textDark);
        comboBox.setBackground(Color.WHITE);
        comboBox.setPreferredSize(new Dimension(280, 32));
    }

    // Status column colored renderer
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

    // Eligibility badge renderer for batch summary table
    private class EligibilityBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));

            String val = value != null ? value.toString() : "";
            if ("ELIGIBLE".equalsIgnoreCase(val)) {
                label.setForeground(new Color(15, 125, 110));
                label.setBackground(isSelected ? new Color(205, 235, 230) : new Color(230, 247, 244));
            } else if (val.contains("MEDICAL")) {
                label.setForeground(pendingPurple);
                label.setBackground(isSelected ? new Color(235, 225, 250) : new Color(245, 238, 255));
            } else {
                label.setForeground(new Color(190, 40, 40));
                label.setBackground(isSelected ? new Color(250, 215, 215) : new Color(254, 237, 237));
            }
            label.setBorder(new EmptyBorder(2, 6, 2, 6));
            return label;
        }
    }

    // Zebra striping renderer
    private class AlternateRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setForeground(textDark);
                c.setBackground(row % 2 == 0 ? Color.WHITE : tableStripe);
            } else {
                c.setForeground(darkNavy);
                c.setBackground(new Color(225, 237, 248));
            }

            if (column == 0 || column == 1 || column == 4 || column == 5 || column == 6) {
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
