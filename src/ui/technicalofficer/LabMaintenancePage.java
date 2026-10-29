package ui.technicalofficer;

import model.LabFacility;
import model.LabMaintenanceRecord;
import model.LabScheduleTask;
import service.LabMaintenanceService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Lab maintenance dashboard for Technical Officers.
 * Handles workstation status tracking, maintenance tickets, and preventive schedules.
 */
public class LabMaintenancePage extends JPanel {

    // Theme colors
    private final Color darkNavy = new Color(27, 67, 103);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color cardWhite = Color.WHITE;
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color successGreen = new Color(20, 165, 145);
    private final Color alertRed = new Color(210, 60, 60);
    private final Color pendingPurple = new Color(115, 75, 180);
    private final Color warningAmber = new Color(217, 119, 6);
    private final Color borderColor = new Color(225, 228, 232);
    private final Color textDark = new Color(35, 45, 55);
    private final Color textMuted = new Color(110, 120, 130);
    private final Color tableStripe = new Color(240, 244, 249);
    private final Color tableGridColor = new Color(218, 224, 233);
    private final Color tableSelectionBg = new Color(193, 218, 243);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    private final LabMaintenanceService service = new LabMaintenanceService();

    // Cross-module navigation callback
    private Runnable onNavigateToTimetable;

    public void setOnNavigateToTimetable(Runnable onNavigateToTimetable) {
        this.onNavigateToTimetable = onNavigateToTimetable;
    }

    // KPI labels
    private JLabel totalLabsValueLabel;
    private JLabel workstationHealthValueLabel;
    private JLabel activeIssuesValueLabel;
    private JLabel scheduledTasksValueLabel;

    // Tab buttons & container
    private CardLayout tabCardLayout;
    private JPanel tabContentContainer;
    private JButton tab1Btn;
    private JButton tab2Btn;
    private JButton tab3Btn;

    // Facilities tab components
    private JPanel facilityCardsGrid;
    private JTable facilitiesTable;
    private DefaultTableModel facilitiesTableModel;

    // Maintenance tickets tab components
    private JTextField searchField;
    private JComboBox<String> labFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> priorityFilterCombo;
    private JTable maintenanceTable;
    private DefaultTableModel maintenanceTableModel;
    private TableRowSorter<DefaultTableModel> maintenanceRowSorter;

    // Summary footer counters
    private JLabel totalRecordsLabel;
    private JLabel pendingRecordsLabel;
    private JLabel inProgressRecordsLabel;
    private JLabel resolvedRecordsLabel;
    private JLabel totalCostLabel;

    // Preventive schedules tab components
    private JTable schedulesTable;
    private DefaultTableModel schedulesTableModel;

    public LabMaintenancePage() {
        setLayout(new BorderLayout());
        setBackground(lightBackground);

        initComponents();
        refreshAllData();
    }

    private void initComponents() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 12));
        mainContainer.setBackground(lightBackground);
        mainContainer.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top section with title and stats
        JPanel headerSection = new JPanel(new BorderLayout(0, 12));
        headerSection.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Laboratory Maintenance & Status Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Monitor lab workstations, report hardware/network faults, track repairs, and manage preventive routines.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        headerSection.add(titlePanel, BorderLayout.NORTH);
        headerSection.add(createKpiCardsPanel(), BorderLayout.CENTER);

        // Navigation tab bar
        JPanel navTabBar = createTabNavigationBar();
        headerSection.add(navTabBar, BorderLayout.SOUTH);

        // Tab views container
        tabCardLayout = new CardLayout();
        tabContentContainer = new JPanel(tabCardLayout);
        tabContentContainer.setOpaque(false);

        tabContentContainer.add(createFacilitiesTabPanel(), "TAB_FACILITIES");
        tabContentContainer.add(createMaintenanceIssuesTabPanel(), "TAB_MAINTENANCE");
        tabContentContainer.add(createPreventiveSchedulesTabPanel(), "TAB_SCHEDULES");

        mainContainer.add(headerSection, BorderLayout.NORTH);
        mainContainer.add(tabContentContainer, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);
    }

    // Top summary cards showing quick metrics
    private JPanel createKpiCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 90));

        totalLabsValueLabel = new JLabel("4");
        workstationHealthValueLabel = new JLabel("95%");
        activeIssuesValueLabel = new JLabel("3");
        scheduledTasksValueLabel = new JLabel("4");

        panel.add(createSummaryCard("ASSIGNED LABS", totalLabsValueLabel, "4 active computer & hardware labs", darkNavy));
        panel.add(createSummaryCard("WORKSTATION HEALTH", workstationHealthValueLabel, "Operational machines online", successGreen));
        panel.add(createSummaryCard("ACTIVE FAULTS", activeIssuesValueLabel, "Breakdowns awaiting resolution", alertRed));
        panel.add(createSummaryCard("PREVENTIVE TASKS", scheduledTasksValueLabel, "Scheduled servicing routines", primaryBlue));

        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(8, 2));
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(10, 14, 10, 14)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(textMuted);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(0, 0, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(8, 8));
        dot.setOpaque(false);

        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(dot, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(textDark);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(textMuted);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // Navigation bar for switching tabs
    private JPanel createTabNavigationBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 0, 4, 0));

        tab1Btn = createTabButton("1. Lab Facilities & Workstations", true);
        tab2Btn = createTabButton("2. Maintenance & Fault Register", false);
        tab3Btn = createTabButton("3. Preventive Maintenance Schedules", false);

        tab1Btn.addActionListener(e -> switchTab(1));
        tab2Btn.addActionListener(e -> switchTab(2));
        tab3Btn.addActionListener(e -> switchTab(3));

        JButton timetableBtn = createTabButton("🗓️ View Lab Timetable", false);
        timetableBtn.setPreferredSize(new Dimension(175, 34));
        timetableBtn.setToolTipText("Check Department Timetable & Lab Sessions");
        timetableBtn.addActionListener(e -> {
            if (onNavigateToTimetable != null) {
                onNavigateToTimetable.run();
            }
        });

        panel.add(tab1Btn);
        panel.add(tab2Btn);
        panel.add(tab3Btn);
        panel.add(timetableBtn);

        return panel;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(240, 34));
        updateTabButtonStyle(btn, active);
        return btn;
    }

    private void updateTabButtonStyle(JButton btn, boolean active) {
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(Color.BLACK);
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setBorder(new LineBorder(darkNavy, 2));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setBorder(new LineBorder(borderColor, 1));
        }
    }

    private void switchTab(int tabIndex) {
        updateTabButtonStyle(tab1Btn, tabIndex == 1);
        updateTabButtonStyle(tab2Btn, tabIndex == 2);
        updateTabButtonStyle(tab3Btn, tabIndex == 3);

        if (tabIndex == 1) {
            tabCardLayout.show(tabContentContainer, "TAB_FACILITIES");
        } else if (tabIndex == 2) {
            tabCardLayout.show(tabContentContainer, "TAB_MAINTENANCE");
        } else {
            tabCardLayout.show(tabContentContainer, "TAB_SCHEDULES");
        }
    }

    // --- Tab 1: Lab Facilities & Workstations ---

    private JPanel createFacilitiesTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Facility cards grid
        facilityCardsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        facilityCardsGrid.setOpaque(false);
        facilityCardsGrid.setPreferredSize(new Dimension(800, 240));

        // Facilities summary table
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Assigned Laboratory Facilities Register");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(darkNavy);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tableActions.setOpaque(false);

        JButton updateLabBtn = createStyledButton("Update Selected Lab", primaryBlue, Color.WHITE);
        updateLabBtn.addActionListener(e -> showUpdateSelectedLabDialog());

        JButton logFaultBtn = createStyledButton("+ Log Fault for Lab", warningAmber, Color.WHITE);
        logFaultBtn.addActionListener(e -> showLogIssueForSelectedLab());

        JButton refreshBtn = createStyledButton("Refresh", Color.WHITE, textDark);
        refreshBtn.addActionListener(e -> refreshAllData());

        tableActions.add(updateLabBtn);
        tableActions.add(logFaultBtn);
        tableActions.add(refreshBtn);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(tableActions, BorderLayout.EAST);

        String[] cols = {
                "Lab ID", "Lab Facility Name", "Location", "Department",
                "Total Workstations", "Operational", "Faulty", "Availability",
                "Status", "Officer In-Charge", "Key Equipment"
        };

        facilitiesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        facilitiesTable = new JTable(facilitiesTableModel);
        styleTable(facilitiesTable);

        facilitiesTable.getColumnModel().getColumn(8).setCellRenderer(new LabStatusCellRenderer());
        facilitiesTable.getColumnModel().getColumn(7).setCellRenderer(new AvailabilityCellRenderer());

        JScrollPane scrollPane = new JScrollPane(facilitiesTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        panel.add(facilityCardsGrid, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private void renderFacilityCards() {
        facilityCardsGrid.removeAll();
        List<LabFacility> labs = service.getAllLabs();

        for (LabFacility lab : labs) {
            JPanel card = createCardPanel();
            card.setLayout(new BorderLayout(0, 6));
            card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 14, 12, 14)));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            JLabel nameLbl = new JLabel(lab.getLabName());
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nameLbl.setForeground(darkNavy);

            JLabel statusPill = createPillBadge(lab.getStatus());
            topRow.add(nameLbl, BorderLayout.WEST);
            topRow.add(statusPill, BorderLayout.EAST);

            JPanel centerBox = new JPanel();
            centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
            centerBox.setOpaque(false);

            JLabel locLbl = new JLabel(lab.getLocation() + " • " + lab.getInChargeOfficerName());
            locLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            locLbl.setForeground(textMuted);

            double pct = lab.getOperationalPercentage();
            JLabel capacityLbl = new JLabel(String.format("Workstations: %d / %d operational (%.1f%%)",
                    lab.getOperationalWorkstations(), lab.getTotalWorkstations(), pct));
            capacityLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            capacityLbl.setForeground(textDark);

            JProgressBar meter = new JProgressBar(0, 100);
            meter.setValue((int) pct);
            meter.setPreferredSize(new Dimension(200, 7));
            meter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
            meter.setForeground(pct >= 90 ? successGreen : (pct >= 75 ? warningAmber : alertRed));
            meter.setBackground(new Color(235, 238, 242));
            meter.setBorderPainted(false);

            JLabel equipSummary = new JLabel("<html><font color='#64748b'>" + lab.getEquipmentSummary() + "</font></html>");
            equipSummary.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            centerBox.add(locLbl);
            centerBox.add(Box.createVerticalStrut(4));
            centerBox.add(capacityLbl);
            centerBox.add(Box.createVerticalStrut(4));
            centerBox.add(meter);
            centerBox.add(Box.createVerticalStrut(4));
            centerBox.add(equipSummary);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            btnRow.setOpaque(false);

            JButton updateBtn = createMiniButton("Quick Status", primaryBlue);
            updateBtn.addActionListener(e -> showQuickUpdateDialog(lab));

            JButton issueBtn = createMiniButton("+ Fault Ticket", warningAmber);
            issueBtn.addActionListener(e -> showLogIssueForLab(lab));

            btnRow.add(updateBtn);
            btnRow.add(issueBtn);

            card.add(topRow, BorderLayout.NORTH);
            card.add(centerBox, BorderLayout.CENTER);
            card.add(btnRow, BorderLayout.SOUTH);

            facilityCardsGrid.add(card);
        }

        facilityCardsGrid.revalidate();
        facilityCardsGrid.repaint();
    }

    // --- Tab 2: Maintenance Issues & Fault Register ---

    private JPanel createMaintenanceIssuesTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Filters and search bar
        JPanel filterCard = createCardPanel();
        filterCard.setLayout(new BorderLayout(0, 8));

        JLabel filterTitle = new JLabel("Filter & Search Maintenance Tickets");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterTitle.setForeground(darkNavy);

        JPanel filterGrid = new JPanel(new GridBagLayout());
        filterGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        searchField = new JTextField("");
        styleTextField(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        labFilterCombo = new JComboBox<>();
        labFilterCombo.addItem("All Labs");
        for (LabFacility f : service.getAllLabs()) {
            labFilterCombo.addItem(f.getLabName());
        }
        styleComboBox(labFilterCombo);
        labFilterCombo.addActionListener(e -> applyFilters());

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Pending", "In Progress", "Scheduled", "Resolved"});
        styleComboBox(statusFilterCombo);
        statusFilterCombo.addActionListener(e -> applyFilters());

        priorityFilterCombo = new JComboBox<>(new String[]{"All Priorities", "Critical", "High", "Medium", "Low"});
        styleComboBox(priorityFilterCombo);
        priorityFilterCombo.addActionListener(e -> applyFilters());

        JButton filterBtn = createStyledButton("Apply", primaryBlue, Color.WHITE);
        filterBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = createStyledButton("Reset", Color.WHITE, textDark);
        resetBtn.addActionListener(e -> resetFilters());

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnBox.setOpaque(false);
        btnBox.add(resetBtn);
        btnBox.add(filterBtn);

        gbc.gridx = 0; gbc.gridy = 0;
        filterGrid.add(createLabeledField("Search Keyword (ID / Workstation / Tech):", searchField), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        filterGrid.add(createLabeledField("Lab Facility:", labFilterCombo), gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        filterGrid.add(createLabeledField("Status:", statusFilterCombo), gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        filterGrid.add(createLabeledField("Priority:", priorityFilterCombo), gbc);

        gbc.gridx = 4; gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        filterGrid.add(btnBox, gbc);

        filterCard.add(filterTitle, BorderLayout.NORTH);
        filterCard.add(filterGrid, BorderLayout.CENTER);

        // Tickets table card
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Laboratory Equipment Maintenance & Breakdown Register");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(darkNavy);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tableActions.setOpaque(false);

        JButton addTicketBtn = createStyledButton("+ Log New Ticket", primaryBlue, Color.WHITE);
        addTicketBtn.addActionListener(e -> showAddMaintenanceDialog(null));

        JButton startBtn = createStyledButton("Start Repair", warningAmber, Color.WHITE);
        startBtn.addActionListener(e -> markSelectedInProgress());

        JButton resolveBtn = createStyledButton("Mark Resolved", successGreen, Color.WHITE);
        resolveBtn.addActionListener(e -> showResolveDialog());

        JButton editBtn = createStyledButton("Edit", Color.WHITE, textDark);
        editBtn.addActionListener(e -> showEditMaintenanceDialog());

        JButton deleteBtn = createStyledButton("Delete", Color.WHITE, alertRed);
        deleteBtn.addActionListener(e -> deleteSelectedMaintenance());

        tableActions.add(addTicketBtn);
        tableActions.add(startBtn);
        tableActions.add(resolveBtn);
        tableActions.add(editBtn);
        tableActions.add(deleteBtn);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(tableActions, BorderLayout.EAST);

        String[] cols = {
                "#", "Ticket ID", "Lab Facility", "Equipment Code", "Category",
                "Priority", "Status", "Reported Date", "Scheduled", "Resolved",
                "Technician / Officer", "Issue Details & Action Taken", "Est. Cost (LKR)"
        };

        maintenanceTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        maintenanceTable = new JTable(maintenanceTableModel);
        styleTable(maintenanceTable);

        maintenanceRowSorter = new TableRowSorter<>(maintenanceTableModel);
        maintenanceTable.setRowSorter(maintenanceRowSorter);

        maintenanceTable.getColumnModel().getColumn(5).setCellRenderer(new PriorityBadgeRenderer());
        maintenanceTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(maintenanceTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableCard.add(createMaintenanceFooterPanel(), BorderLayout.SOUTH);

        panel.add(filterCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMaintenanceFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 4, 4, 4));

        JPanel badgesBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        badgesBox.setOpaque(false);

        totalRecordsLabel = createFooterBadge("Total: 0", textDark, new Color(240, 243, 246));
        pendingRecordsLabel = createFooterBadge("Pending: 0", pendingPurple, new Color(243, 238, 255));
        inProgressRecordsLabel = createFooterBadge("In Progress: 0", warningAmber, new Color(254, 243, 199));
        resolvedRecordsLabel = createFooterBadge("Resolved: 0", successGreen, new Color(230, 247, 244));
        totalCostLabel = createFooterBadge("Total Spend: LKR 0.00", darkNavy, new Color(237, 243, 248));

        badgesBox.add(totalRecordsLabel);
        badgesBox.add(pendingRecordsLabel);
        badgesBox.add(inProgressRecordsLabel);
        badgesBox.add(resolvedRecordsLabel);
        badgesBox.add(totalCostLabel);

        footer.add(badgesBox, BorderLayout.WEST);
        return footer;
    }

    // --- Tab 3: Preventive Maintenance Schedules ---

    private JPanel createPreventiveSchedulesTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Guidelines banner
        JPanel sopCard = createCardPanel();
        sopCard.setLayout(new BorderLayout(0, 6));
        sopCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 16, 12, 16)));

        JLabel sopTitle = new JLabel("Faculty IT Infrastructure Preventive Maintenance Guidelines");
        sopTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sopTitle.setForeground(darkNavy);

        JLabel sopText = new JLabel("<html>All Technical Officers must perform regular hardware checks, network integrity tests, " +
                "and operating system maintenance routines to ensure zero downtime during practical classes and exam sessions. " +
                "Click <b>'Mark Completed Today'</b> upon finishing a scheduled routine to automatically update the next due cycle.</html>");
        sopText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sopText.setForeground(textDark);

        sopCard.add(sopTitle, BorderLayout.NORTH);
        sopCard.add(sopText, BorderLayout.CENTER);

        // Schedules table card
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Routine Preventive Maintenance Schedules");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(darkNavy);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tableActions.setOpaque(false);

        JButton markDoneBtn = createStyledButton("Mark Completed Today", successGreen, Color.WHITE);
        markDoneBtn.addActionListener(e -> markSelectedTaskCompleted());

        JButton addScheduleBtn = createStyledButton("+ Add New Routine", primaryBlue, Color.WHITE);
        addScheduleBtn.addActionListener(e -> showAddScheduleDialog());

        JButton deleteScheduleBtn = createStyledButton("Delete Routine", Color.WHITE, alertRed);
        deleteScheduleBtn.addActionListener(e -> deleteSelectedScheduleTask());

        JButton refreshBtn = createStyledButton("Refresh", Color.WHITE, textDark);
        refreshBtn.addActionListener(e -> refreshAllData());

        tableActions.add(markDoneBtn);
        tableActions.add(addScheduleBtn);
        tableActions.add(deleteScheduleBtn);
        tableActions.add(refreshBtn);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(tableActions, BorderLayout.EAST);

        String[] cols = {
                "Task ID", "Lab Facility", "Routine Maintenance Task", "Frequency",
                "Last Completed", "Next Due Date", "Officer", "Status", "Checklist & Inspection Details"
        };

        schedulesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        schedulesTable = new JTable(schedulesTableModel);
        styleTable(schedulesTable);

        schedulesTable.getColumnModel().getColumn(7).setCellRenderer(new TaskStatusRenderer());

        JScrollPane scrollPane = new JScrollPane(schedulesTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        panel.add(sopCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // --- Data Loading & Refresh ---

    public void refreshAllData() {
        totalLabsValueLabel.setText(String.valueOf(service.getTotalLabs()));
        workstationHealthValueLabel.setText(String.format("%.0f%%", service.getWorkstationHealthPercentage()));
        activeIssuesValueLabel.setText(String.valueOf(service.getActiveIssuesCount()));
        scheduledTasksValueLabel.setText(String.valueOf(service.getAllScheduleTasks().size()));

        renderFacilityCards();
        loadFacilitiesTable();
        applyFilters();
        loadSchedulesTable();
    }

    private void loadFacilitiesTable() {
        facilitiesTableModel.setRowCount(0);
        List<LabFacility> list = service.getAllLabs();
        for (LabFacility f : list) {
            facilitiesTableModel.addRow(new Object[]{
                    f.getLabId(),
                    f.getLabName(),
                    f.getLocation(),
                    f.getDepartment(),
                    f.getTotalWorkstations(),
                    f.getOperationalWorkstations(),
                    f.getFaultyWorkstations(),
                    String.format("%.1f%%", f.getOperationalPercentage()),
                    f.getStatus(),
                    f.getInChargeOfficerName(),
                    f.getEquipmentSummary()
            });
        }
    }

    private void applyFilters() {
        String kw = searchField.getText();
        String lab = (String) labFilterCombo.getSelectedItem();
        String status = (String) statusFilterCombo.getSelectedItem();
        String priority = (String) priorityFilterCombo.getSelectedItem();

        List<LabMaintenanceRecord> list = service.filterMaintenanceRecords(lab, status, priority, kw);
        maintenanceTableModel.setRowCount(0);

        int count = 1;
        int pending = 0;
        int inProg = 0;
        int resolved = 0;
        double totalCost = 0.0;

        for (LabMaintenanceRecord r : list) {
            maintenanceTableModel.addRow(new Object[]{
                    count++,
                    r.getMaintenanceId(),
                    r.getLabName(),
                    r.getEquipmentCode(),
                    r.getEquipmentType(),
                    r.getPriority(),
                    r.getStatus(),
                    r.getFormattedReportedDate(),
                    r.getFormattedScheduledDate(),
                    r.getFormattedResolvedDate(),
                    r.getTechnicianInCharge(),
                    r.getIssueDescription() + (r.getActionTaken() != null && !r.getActionTaken().isEmpty() ? " [Action: " + r.getActionTaken() + "]" : ""),
                    currencyFormat.format(r.getEstimatedCost())
            });

            if ("Pending".equalsIgnoreCase(r.getStatus())) pending++;
            else if ("In Progress".equalsIgnoreCase(r.getStatus())) inProg++;
            else if ("Resolved".equalsIgnoreCase(r.getStatus())) resolved++;

            totalCost += r.getEstimatedCost();
        }

        totalRecordsLabel.setText("Total: " + list.size());
        pendingRecordsLabel.setText("Pending: " + pending);
        inProgressRecordsLabel.setText("In Progress: " + inProg);
        resolvedRecordsLabel.setText("Resolved: " + resolved);
        totalCostLabel.setText("Total Spend: LKR " + currencyFormat.format(totalCost));
    }

    private void resetFilters() {
        searchField.setText("");
        labFilterCombo.setSelectedIndex(0);
        statusFilterCombo.setSelectedIndex(0);
        priorityFilterCombo.setSelectedIndex(0);
        applyFilters();
    }

    private void loadSchedulesTable() {
        schedulesTableModel.setRowCount(0);
        List<LabScheduleTask> tasks = service.getAllScheduleTasks();
        for (LabScheduleTask t : tasks) {
            schedulesTableModel.addRow(new Object[]{
                    t.getTaskId(),
                    t.getLabName(),
                    t.getTaskName(),
                    t.getFrequency(),
                    t.getFormattedLastCompletedDate(),
                    t.getFormattedNextDueDate(),
                    t.getAssignedOfficer(),
                    t.getStatus(),
                    t.getChecklistDetails()
            });
        }
    }

    // --- Action Handlers & Dialogs ---

    private void showAddMaintenanceDialog(LabFacility preselectedLab) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Log New Equipment Maintenance Ticket", true);
        dialog.setSize(550, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JComboBox<LabFacility> labCombo = new JComboBox<>();
        List<LabFacility> labs = service.getAllLabs();
        for (LabFacility f : labs) {
            labCombo.addItem(f);
            if (preselectedLab != null && f.getLabId().equalsIgnoreCase(preselectedLab.getLabId())) {
                labCombo.setSelectedItem(f);
            }
        }
        styleComboBox(labCombo);

        JTextField equipCodeField = new JTextField("WS-01-");
        styleTextField(equipCodeField);

        JComboBox<String> equipTypeCombo = new JComboBox<>(new String[]{
                "Workstation / PC", "Network Equipment", "Multimedia / Projector",
                "Power / UPS", "Software / OS", "Air Conditioning / Facility", "Other"
        });
        styleComboBox(equipTypeCombo);

        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"Medium", "Critical", "High", "Low"});
        styleComboBox(priorityCombo);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Hardware Repair", "Preventive Servicing", "Software Upgrade",
                "Emergency Replacement", "Routine Inspection"
        });
        styleComboBox(typeCombo);

        JTextField techField = new JTextField("TO001 - Ruwan Jayasinghe");
        styleTextField(techField);

        JTextField costField = new JTextField("0.00");
        styleTextField(costField);

        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(borderColor, 1));

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Target Lab Facility:", labCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Equipment Code / Workstation ID:", equipCodeField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Equipment Category:", equipTypeCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Priority Level:", priorityCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Maintenance Type:", typeCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Assigned Officer / Technician:", techField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Estimated Repair Cost (LKR):", costField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createLabeledField("Issue Description & Fault Symptoms:", descScroll), gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnRow.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, textDark);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = createStyledButton("Submit Ticket", primaryBlue, Color.WHITE);
        submitBtn.addActionListener(e -> {
            String equip = equipCodeField.getText().trim();
            String desc = descArea.getText().trim();
            if (equip.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter Equipment Code and Issue Description.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double cost = 0.0;
            try {
                cost = Double.parseDouble(costField.getText().trim());
            } catch (Exception ignored) {
            }

            LabFacility selLab = (LabFacility) labCombo.getSelectedItem();
            LabMaintenanceRecord rec = new LabMaintenanceRecord();
            rec.setLabId(selLab.getLabId());
            rec.setLabName(selLab.getLabName());
            rec.setEquipmentCode(equip);
            rec.setEquipmentType((String) equipTypeCombo.getSelectedItem());
            rec.setPriority((String) priorityCombo.getSelectedItem());
            rec.setMaintenanceType((String) typeCombo.getSelectedItem());
            rec.setTechnicianInCharge(techField.getText().trim());
            rec.setEstimatedCost(cost);
            rec.setIssueDescription(desc);
            rec.setReportedDate(new Date());
            rec.setStatus("Pending");

            boolean ok = service.addMaintenanceRecord(rec);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Ticket logged successfully with ID: " + rec.getMaintenanceId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Could not save ticket to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(submitBtn);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void markSelectedInProgress() {
        int row = maintenanceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance ticket from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = maintenanceTable.convertRowIndexToModel(row);
        String ticketId = (String) maintenanceTableModel.getValueAt(modelRow, 1);
        String currentStatus = (String) maintenanceTableModel.getValueAt(modelRow, 6);

        if ("Resolved".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This ticket is already marked as Resolved.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Start repair work for ticket " + ticketId + " and set status to 'In Progress'?",
                "Confirm Start Repair", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            service.startMaintenance(ticketId, "TO001 - Ruwan Jayasinghe");
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Ticket " + ticketId + " is now In Progress.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showResolveDialog() {
        int row = maintenanceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance ticket to resolve.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = maintenanceTable.convertRowIndexToModel(row);
        String ticketId = (String) maintenanceTableModel.getValueAt(modelRow, 1);
        String labName = (String) maintenanceTableModel.getValueAt(modelRow, 2);
        String equipCode = (String) maintenanceTableModel.getValueAt(modelRow, 3);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Resolve Maintenance Ticket: " + ticketId, true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel infoLbl = new JLabel("<html><b>Lab:</b> " + labName + "<br><b>Equipment:</b> " + equipCode + "</html>");
        infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTextArea actionArea = new JTextArea(3, 20);
        actionArea.setLineWrap(true);
        actionArea.setWrapStyleWord(true);
        actionArea.setText("Parts replaced and tested OK. Workstation returned to operational pool.");
        JScrollPane actionScroll = new JScrollPane(actionArea);
        actionScroll.setBorder(new LineBorder(borderColor, 1));

        JTextField techField = new JTextField("TO001 - Ruwan Jayasinghe");
        styleTextField(techField);

        JTextField costField = new JTextField("0.00");
        styleTextField(costField);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; form.add(infoLbl, gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Action Taken & Resolution Notes:", actionScroll), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Technician In Charge:", techField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Final Incurred Cost (LKR):", costField), gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnRow.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, textDark);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton confirmBtn = createStyledButton("Confirm & Mark Resolved", successGreen, Color.WHITE);
        confirmBtn.addActionListener(e -> {
            double finalCost = 0.0;
            try {
                finalCost = Double.parseDouble(costField.getText().trim());
            } catch (Exception ignored) {
            }

            service.resolveMaintenance(ticketId, actionArea.getText().trim(), techField.getText().trim(), finalCost);
            dialog.dispose();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Ticket " + ticketId + " has been marked as Resolved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditMaintenanceDialog() {
        int row = maintenanceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance ticket to edit.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = maintenanceTable.convertRowIndexToModel(row);
        String ticketId = (String) maintenanceTableModel.getValueAt(modelRow, 1);

        LabMaintenanceRecord rec = null;
        for (LabMaintenanceRecord r : service.getAllMaintenanceRecords()) {
            if (r.getMaintenanceId().equalsIgnoreCase(ticketId)) {
                rec = r;
                break;
            }
        }
        if (rec == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Ticket: " + ticketId, true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField equipField = new JTextField(rec.getEquipmentCode());
        styleTextField(equipField);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "In Progress", "Scheduled", "Resolved"});
        statusCombo.setSelectedItem(rec.getStatus());
        styleComboBox(statusCombo);

        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Low"});
        priorityCombo.setSelectedItem(rec.getPriority());
        styleComboBox(priorityCombo);

        JTextField techField = new JTextField(rec.getTechnicianInCharge());
        styleTextField(techField);

        JTextField costField = new JTextField(String.valueOf(rec.getEstimatedCost()));
        styleTextField(costField);

        JTextArea descArea = new JTextArea(rec.getIssueDescription(), 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(borderColor, 1));

        JTextArea actionArea = new JTextArea(rec.getActionTaken() != null ? rec.getActionTaken() : "", 3, 20);
        actionArea.setLineWrap(true);
        actionArea.setWrapStyleWord(true);
        JScrollPane actionScroll = new JScrollPane(actionArea);
        actionScroll.setBorder(new LineBorder(borderColor, 1));

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Equipment Code:", equipField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Status:", statusCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Priority:", priorityCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Technician In Charge:", techField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Cost (LKR):", costField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Issue Description:", descScroll), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Action Taken / Resolution Notes:", actionScroll), gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnRow.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, textDark);
        cancelBtn.addActionListener(e -> dialog.dispose());

        LabMaintenanceRecord targetRecord = rec;
        JButton saveBtn = createStyledButton("Save Changes", primaryBlue, Color.WHITE);
        saveBtn.addActionListener(e -> {
            targetRecord.setEquipmentCode(equipField.getText().trim());
            targetRecord.setStatus((String) statusCombo.getSelectedItem());
            targetRecord.setPriority((String) priorityCombo.getSelectedItem());
            targetRecord.setTechnicianInCharge(techField.getText().trim());
            targetRecord.setIssueDescription(descArea.getText().trim());
            targetRecord.setActionTaken(actionArea.getText().trim());
            try {
                targetRecord.setEstimatedCost(Double.parseDouble(costField.getText().trim()));
            } catch (Exception ignored) {
            }

            service.updateMaintenanceRecord(targetRecord);
            dialog.dispose();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Ticket updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedMaintenance() {
        int row = maintenanceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = maintenanceTable.convertRowIndexToModel(row);
        String ticketId = (String) maintenanceTableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete maintenance ticket " + ticketId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteMaintenanceRecord(ticketId);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Ticket " + ticketId + " removed.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showQuickUpdateDialog(LabFacility lab) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quick Update: " + lab.getLabName(), true);
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Operational", "Maintenance Required", "Under Maintenance", "Occupied"});
        statusCombo.setSelectedItem(lab.getStatus());
        styleComboBox(statusCombo);

        JSpinner operationalSpinner = new JSpinner(new SpinnerNumberModel(lab.getOperationalWorkstations(), 0, lab.getTotalWorkstations(), 1));

        JTextField officerField = new JTextField(lab.getInChargeOfficerName());
        styleTextField(officerField);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Facility Status:", statusCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Operational Workstations (out of " + lab.getTotalWorkstations() + "):", operationalSpinner), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Technical Officer in Charge:", officerField), gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnRow.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, textDark);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Update Status", primaryBlue, Color.WHITE);
        saveBtn.addActionListener(e -> {
            lab.setStatus((String) statusCombo.getSelectedItem());
            lab.setOperationalWorkstations((Integer) operationalSpinner.getValue());
            lab.setInChargeOfficerName(officerField.getText().trim());
            service.updateLab(lab);
            dialog.dispose();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Lab facility updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showUpdateSelectedLabDialog() {
        int row = facilitiesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a lab facility from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String labId = (String) facilitiesTableModel.getValueAt(row, 0);
        LabFacility lab = service.getLabById(labId);
        if (lab != null) {
            showQuickUpdateDialog(lab);
        }
    }

    private void showLogIssueForSelectedLab() {
        int row = facilitiesTable.getSelectedRow();
        LabFacility lab = null;
        if (row >= 0) {
            String labId = (String) facilitiesTableModel.getValueAt(row, 0);
            lab = service.getLabById(labId);
        }
        showAddMaintenanceDialog(lab);
    }

    private void showLogIssueForLab(LabFacility lab) {
        showAddMaintenanceDialog(lab);
    }

    private void markSelectedTaskCompleted() {
        int row = schedulesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a routine maintenance schedule from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String taskId = (String) schedulesTableModel.getValueAt(row, 0);
        String taskName = (String) schedulesTableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm completion of task: " + taskName + " (" + taskId + ") today?",
                "Mark Routine Completed", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            service.markTaskCompleted(taskId);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Task marked as completed today. Next due cycle automatically recalculated.", "Task Completed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAddScheduleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Routine Preventive Maintenance Task", true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JComboBox<LabFacility> labCombo = new JComboBox<>();
        for (LabFacility f : service.getAllLabs()) {
            labCombo.addItem(f);
        }
        styleComboBox(labCombo);

        JTextField taskNameField = new JTextField("");
        styleTextField(taskNameField);

        JComboBox<String> freqCombo = new JComboBox<>(new String[]{"Weekly", "Bi-Weekly", "Monthly", "Quarterly", "Semester-End"});
        styleComboBox(freqCombo);

        JTextField officerField = new JTextField("TO001");
        styleTextField(officerField);

        JTextArea checklistArea = new JTextArea(3, 20);
        checklistArea.setLineWrap(true);
        checklistArea.setWrapStyleWord(true);
        JScrollPane checkScroll = new JScrollPane(checklistArea);
        checkScroll.setBorder(new LineBorder(borderColor, 1));

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Target Lab:", labCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Routine Task Name:", taskNameField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Frequency Interval:", freqCombo), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Assigned Officer ID:", officerField), gbc);
        gbc.gridx = 0; gbc.gridy = y++; form.add(createLabeledField("Checklist & SOP Notes:", checkScroll), gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnRow.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, textDark);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton addBtn = createStyledButton("Add Schedule", primaryBlue, Color.WHITE);
        addBtn.addActionListener(e -> {
            String name = taskNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a task name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LabFacility selLab = (LabFacility) labCombo.getSelectedItem();
            LabScheduleTask task = new LabScheduleTask();
            task.setLabId(selLab.getLabId());
            task.setLabName(selLab.getLabName());
            task.setTaskName(name);
            task.setFrequency((String) freqCombo.getSelectedItem());
            task.setAssignedOfficer(officerField.getText().trim());
            task.setChecklistDetails(checklistArea.getText().trim());
            task.setStatus("Due Soon");
            task.setLastCompletedDate(null);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 7);
            task.setNextDueDate(cal.getTime());

            service.addScheduleTask(task);
            dialog.dispose();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Preventive maintenance task scheduled.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnRow.add(cancelBtn);
        btnRow.add(addBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedScheduleTask() {
        int row = schedulesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a routine task to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String taskId = (String) schedulesTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete preventive maintenance task " + taskId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteScheduleTask(taskId);
            refreshAllData();
        }
    }

    // --- UI Helpers & Component Factories ---

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(cardWhite);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 16, 12, 16)));
        return card;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(textDark);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setForeground(textDark);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 8, 6, 8)));
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(Color.WHITE);
        cb.setForeground(textDark);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(6, 12, 6, 12)));
        return btn;
    }

    private JButton createMiniButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setBackground(Color.WHITE);
        btn.setForeground(color);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(color, 1), new EmptyBorder(3, 8, 3, 8)));
        return btn;
    }

    private JLabel createFooterBadge(String text, Color fg, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(fg);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(4, 10, 4, 10)));
        return lbl;
    }

    private JLabel createPillBadge(String status) {
        JLabel pill = new JLabel(status, SwingConstants.CENTER);
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setOpaque(true);

        if ("Operational".equalsIgnoreCase(status)) {
            pill.setBackground(new Color(230, 247, 244));
            pill.setForeground(successGreen);
        } else if ("Under Maintenance".equalsIgnoreCase(status) || "Maintenance Required".equalsIgnoreCase(status)) {
            pill.setBackground(new Color(254, 243, 199));
            pill.setForeground(warningAmber);
        } else {
            pill.setBackground(new Color(237, 243, 248));
            pill.setForeground(primaryBlue);
        }

        pill.setBorder(new CompoundBorder(new LineBorder(pill.getForeground(), 1), new EmptyBorder(3, 8, 3, 8)));
        return pill;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(textDark);
        table.setGridColor(tableGridColor);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setSelectionBackground(tableSelectionBg);
        table.setSelectionForeground(darkNavy);
        table.setDefaultRenderer(Object.class, new AlternateRowRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
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

                if (table.getRowSorter() != null) {
                    java.util.List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
                    if (!sortKeys.isEmpty() && sortKeys.get(0).getColumn() == table.convertColumnIndexToModel(column)) {
                        SortOrder order = sortKeys.get(0).getSortOrder();
                        if (order == SortOrder.ASCENDING) {
                            label.setText(label.getText() + " ▲");
                        } else if (order == SortOrder.DESCENDING) {
                            label.setText(label.getText() + " ▼");
                        }
                    }
                }
                return label;
            }
        });
    }

    // --- Table Cell Renderers ---

    private class AlternateRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
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
                c.setBackground(tableSelectionBg);
            }

            if (c instanceof JLabel) {
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
            }

            return c;
        }
    }

    private class LabStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String st = value != null ? value.toString() : "";
            setHorizontalAlignment(CENTER);

            if (!isSelected) {
                if ("Operational".equalsIgnoreCase(st)) {
                    setForeground(new Color(13, 125, 110));
                    setBackground(new Color(220, 246, 240));
                } else {
                    setForeground(new Color(180, 83, 9));
                    setBackground(new Color(254, 238, 204));
                }
            } else {
                setForeground(darkNavy);
                setBackground(tableSelectionBg);
            }
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            return c;
        }
    }

    private class AvailabilityCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (!isSelected) {
                setForeground(darkNavy);
                setBackground(row % 2 == 0 ? Color.WHITE : tableStripe);
            } else {
                setForeground(darkNavy);
                setBackground(tableSelectionBg);
            }
            return c;
        }
    }

    private class PriorityBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String p = value != null ? value.toString() : "";
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));

            if (!isSelected) {
                if ("Critical".equalsIgnoreCase(p)) {
                    setForeground(new Color(185, 28, 28));
                    setBackground(new Color(254, 226, 226));
                } else if ("High".equalsIgnoreCase(p)) {
                    setForeground(new Color(180, 83, 9));
                    setBackground(new Color(254, 235, 190));
                } else if ("Medium".equalsIgnoreCase(p)) {
                    setForeground(new Color(29, 78, 137));
                    setBackground(new Color(224, 238, 255));
                } else {
                    setForeground(new Color(51, 65, 85));
                    setBackground(new Color(226, 232, 240));
                }
            } else {
                setForeground(darkNavy);
                setBackground(tableSelectionBg);
            }
            return c;
        }
    }

    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String s = value != null ? value.toString() : "";
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));

            if (!isSelected) {
                if ("Pending".equalsIgnoreCase(s)) {
                    setForeground(new Color(109, 40, 217));
                    setBackground(new Color(237, 228, 255));
                } else if ("In Progress".equalsIgnoreCase(s)) {
                    setForeground(new Color(180, 83, 9));
                    setBackground(new Color(254, 235, 190));
                } else if ("Resolved".equalsIgnoreCase(s)) {
                    setForeground(new Color(13, 125, 110));
                    setBackground(new Color(220, 246, 240));
                } else {
                    setForeground(darkNavy);
                    setBackground(new Color(224, 235, 245));
                }
            } else {
                setForeground(darkNavy);
                setBackground(tableSelectionBg);
            }
            return c;
        }
    }

    private class TaskStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String s = value != null ? value.toString() : "";
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));

            if (!isSelected) {
                if ("Completed".equalsIgnoreCase(s)) {
                    setForeground(new Color(13, 125, 110));
                    setBackground(new Color(220, 246, 240));
                } else if ("Due Soon".equalsIgnoreCase(s)) {
                    setForeground(new Color(180, 83, 9));
                    setBackground(new Color(254, 235, 190));
                } else {
                    setForeground(new Color(185, 28, 28));
                    setBackground(new Color(254, 226, 226));
                }
            } else {
                setForeground(darkNavy);
                setBackground(tableSelectionBg);
            }
            return c;
        }
    }
}
