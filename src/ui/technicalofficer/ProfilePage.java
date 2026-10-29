package ui.technicalofficer;

import model.LabFacility;
import model.TechnicalOfficer;
import service.TechnicalOfficerService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Technical Officer profile and account management view.
 * Displays employment details, assigned labs, security settings, and activity logs.
 */
public class ProfilePage extends JPanel {

    // Theme color palette matching TechnicalOfficerDashboard
    private final Color darkNavy = new Color(27, 67, 103);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color cardWhite = Color.WHITE;
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color successGreen = new Color(20, 165, 145);
    private final Color alertRed = new Color(210, 60, 60);
    private final Color warningAmber = new Color(217, 119, 6);
    private final Color borderColor = new Color(225, 228, 232);
    private final Color textDark = new Color(35, 45, 55);
    private final Color textMuted = new Color(110, 120, 130);
    private final Color tableStripe = new Color(240, 244, 249);
    private final Color tableGridColor = new Color(218, 224, 233);
    private final Color tableSelectionBg = new Color(193, 218, 243);

    // Business service
    private final TechnicalOfficerService service = new TechnicalOfficerService();
    private TechnicalOfficer currentOfficer;
    private final String currentOfficerId = "TO001";

    // Hero Profile Labels
    private JLabel avatarLabel;
    private JLabel heroNameLabel;
    private JLabel heroRoleLabel;
    private JLabel heroDeptLabel;
    private JLabel heroContactLabel;
    private JLabel heroStatusBadge;
    private JLabel staffPillLabel;
    private JLabel sysIdPillLabel;

    // Cross-module navigation callback
    private Runnable onNavigateToTimetable;

    public void setOnNavigateToTimetable(Runnable onNavigateToTimetable) {
        this.onNavigateToTimetable = onNavigateToTimetable;
    }

    // Top KPI Labels
    private JLabel kpiLabsValueLabel;
    private JLabel kpiWorkstationsValueLabel;
    private JLabel kpiUptimeValueLabel;
    private JLabel kpiTicketsValueLabel;

    // Tab Navigation
    private CardLayout tabCardLayout;
    private JPanel tabContentContainer;
    private JButton tab1Btn;
    private JButton tab2Btn;
    private JButton tab3Btn;
    private JButton tab4Btn;

    // Tab 1 (Details) Labels
    private JLabel detStaffNoLabel;
    private JLabel detSysIdLabel;
    private JLabel detFullNameLabel;
    private JLabel detDesignationLabel;
    private JLabel detFacultyLabel;
    private JLabel detDepartmentLabel;
    private JLabel detStatusLabel;
    private JLabel detAppointedLabel;

    private JLabel detEmailLabel;
    private JLabel detPhoneLabel;
    private JLabel detOfficeRoomLabel;
    private JLabel detAddressLabel;

    // Tab 2 (Labs) Table
    private JTable assignedLabsTable;
    private DefaultTableModel assignedLabsTableModel;

    // Tab 3 (Security) Fields
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel secUsernameLabel;
    private JLabel secRoleLabel;

    // Tab 4 (Audit) Table
    private JTable auditTable;
    private DefaultTableModel auditTableModel;

    public ProfilePage() {
        setLayout(new BorderLayout());
        setBackground(lightBackground);

        initComponents();
        refreshProfile();
    }

    private void initComponents() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 16));
        mainContainer.setBackground(lightBackground);
        mainContainer.setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Top Section (Header + Hero Card + KPI ribbon + Tabs)
        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);

        topSection.add(createPageHeader(), BorderLayout.NORTH);

        JPanel heroAndKpiPanel = new JPanel(new BorderLayout(0, 12));
        heroAndKpiPanel.setOpaque(false);
        heroAndKpiPanel.add(createHeroCard(), BorderLayout.NORTH);
        heroAndKpiPanel.add(createKpiCardsPanel(), BorderLayout.CENTER);
        heroAndKpiPanel.add(createNavTabBar(), BorderLayout.SOUTH);

        topSection.add(heroAndKpiPanel, BorderLayout.CENTER);

        // 2. Tab Content Container
        tabCardLayout = new CardLayout();
        tabContentContainer = new JPanel(tabCardLayout);
        tabContentContainer.setOpaque(false);

        tabContentContainer.add(createPersonalDetailsTab(), "TAB_DETAILS");
        tabContentContainer.add(createAssignedLabsTab(), "TAB_LABS");
        tabContentContainer.add(createSecurityTab(), "TAB_SECURITY");
        tabContentContainer.add(createAuditTrailTab(), "TAB_AUDIT");

        mainContainer.add(topSection, BorderLayout.NORTH);
        mainContainer.add(tabContentContainer, BorderLayout.CENTER);

        // Wrap in smooth JScrollPane for seamless scrolling on smaller displays
        JScrollPane rootScroll = new JScrollPane(mainContainer);
        rootScroll.setBorder(null);
        rootScroll.getVerticalScrollBar().setUnitIncrement(16);
        rootScroll.getViewport().setBackground(lightBackground);

        add(rootScroll, BorderLayout.CENTER);
    }

    // --- Header & Profile Card ---

    private JPanel createPageHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Technical Officer Profile & Account Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel(
                "View employment records, manage assigned laboratory infrastructure, update security credentials, and review activity logs.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton editBtn = createStyledButton("Edit Profile", primaryBlue, Color.WHITE);
        editBtn.addActionListener(e -> showEditProfileDialog());

        JButton changePassBtn = createStyledButton("Change Password", Color.WHITE, textDark);
        changePassBtn.addActionListener(e -> switchTab(3));

        JButton refreshBtn = createStyledButton("Refresh", Color.WHITE, textDark);
        refreshBtn.addActionListener(e -> refreshProfile());

        actions.add(editBtn);
        actions.add(changePassBtn);
        actions.add(refreshBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel createHeroCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(18, 0));
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 20, 16, 20)));

        // Avatar Column
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setOpaque(false);

        avatarLabel = new JLabel("DK", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(darkNavy);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);

                // Online indicator dot
                g2.setColor(successGreen);
                g2.fillOval(getWidth() - 20, getHeight() - 20, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(getWidth() - 20, getHeight() - 20, 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setPreferredSize(new Dimension(74, 74));
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);

        // Center Identity Details
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        JPanel nameAndBadgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nameAndBadgesRow.setOpaque(false);

        heroNameLabel = new JLabel("Dinesh Kumara");
        heroNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heroNameLabel.setForeground(textDark);

        staffPillLabel = createPillLabel("STAFF: TO-STF-001", primaryBlue, new Color(239, 246, 255));
        sysIdPillLabel = createPillLabel("ID: TO001", textDark, new Color(241, 245, 249));
        heroStatusBadge = createPillLabel("● ACTIVE", successGreen, new Color(230, 247, 244));

        nameAndBadgesRow.add(heroNameLabel);
        nameAndBadgesRow.add(staffPillLabel);
        nameAndBadgesRow.add(sysIdPillLabel);
        nameAndBadgesRow.add(heroStatusBadge);

        heroRoleLabel = new JLabel("Technical Officer (Grade II) • Faculty of Technology");
        heroRoleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heroRoleLabel.setForeground(primaryBlue);
        heroRoleLabel.setBorder(new EmptyBorder(3, 4, 3, 0));

        heroDeptLabel = new JLabel("Department of Information and Communication Technology");
        heroDeptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heroDeptLabel.setForeground(textDark);
        heroDeptLabel.setBorder(new EmptyBorder(0, 4, 3, 0));

        heroContactLabel = new JLabel(
                "Email: dinesh.to@uor.lk   |   Phone: 0710000007   |   Office: Room A-204, IT Technical Office");
        heroContactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heroContactLabel.setForeground(textMuted);
        heroContactLabel.setBorder(new EmptyBorder(0, 4, 0, 0));

        centerBox.add(nameAndBadgesRow);
        centerBox.add(heroRoleLabel);
        centerBox.add(heroDeptLabel);
        centerBox.add(heroContactLabel);

        card.add(avatarPanel, BorderLayout.WEST);
        card.add(centerBox, BorderLayout.CENTER);

        return card;
    }

    private JPanel createKpiCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 85));

        kpiLabsValueLabel = new JLabel("4 Labs");
        kpiWorkstationsValueLabel = new JLabel("170 PCs");
        kpiUptimeValueLabel = new JLabel("97.1%");
        kpiTicketsValueLabel = new JLabel("12 Handled");

        panel.add(createSummaryCard("ASSIGNED LABS", kpiLabsValueLabel, "Under primary officer supervision", darkNavy));
        panel.add(createSummaryCard("WORKSTATIONS MANAGED", kpiWorkstationsValueLabel,
                "Active computer fleet across labs", primaryBlue));
        panel.add(createSummaryCard("LAB FLEET HEALTH", kpiUptimeValueLabel, "Average operational equipment uptime",
                successGreen));
        panel.add(createSummaryCard("MAINTENANCE TICKETS", kpiTicketsValueLabel, "Service & fault tickets processed",
                warningAmber));

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

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(textDark);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(textMuted);

        JPanel contentBox = new JPanel(new GridLayout(2, 1, 0, 1));
        contentBox.setOpaque(false);
        contentBox.add(valueLabel);
        contentBox.add(subLbl);

        card.add(topRow, BorderLayout.NORTH);
        card.add(contentBox, BorderLayout.CENTER);

        return card;
    }

    private JPanel createNavTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        tab1Btn = createTabButton("Personal & Employment Details", true);
        tab1Btn.addActionListener(e -> switchTab(1));

        tab2Btn = createTabButton("Assigned Laboratories & Fleet", false);
        tab2Btn.addActionListener(e -> switchTab(2));

        tab3Btn = createTabButton("Account Security & Password", false);
        tab3Btn.addActionListener(e -> switchTab(3));

        tab4Btn = createTabButton("Activity & System Audit Trail", false);
        tab4Btn.addActionListener(e -> switchTab(4));

        bar.add(tab1Btn);
        bar.add(tab2Btn);
        bar.add(tab3Btn);
        bar.add(tab4Btn);

        return bar;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(210, 36));
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
        updateTabButtonStyle(tab4Btn, tabIndex == 4);

        if (tabIndex == 1) {
            tabCardLayout.show(tabContentContainer, "TAB_DETAILS");
        } else if (tabIndex == 2) {
            tabCardLayout.show(tabContentContainer, "TAB_LABS");
            loadAssignedLabsTable();
        } else if (tabIndex == 3) {
            tabCardLayout.show(tabContentContainer, "TAB_SECURITY");
        } else {
            tabCardLayout.show(tabContentContainer, "TAB_AUDIT");
            loadAuditTable();
        }
    }

    // --- Tab 1: Personal & Employment Details ---

    private JPanel createPersonalDetailsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 0));
        grid.setOpaque(false);

        // Left Card: Employment & University Data
        JPanel employmentCard = createCardPanel();
        employmentCard.setLayout(new BorderLayout(0, 12));
        employmentCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JLabel empHeader = new JLabel("Employment & Academic Appointment");
        empHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        empHeader.setForeground(darkNavy);

        JPanel empFields = new JPanel(new GridLayout(8, 2, 8, 10));
        empFields.setOpaque(false);

        detStaffNoLabel = new JLabel("TO-STF-001");
        detSysIdLabel = new JLabel("TO001");
        detFullNameLabel = new JLabel("Dinesh Kumara");
        detDesignationLabel = new JLabel("Technical Officer (Grade II)");
        detFacultyLabel = new JLabel("Faculty of Technology");
        detDepartmentLabel = new JLabel("Information and Communication Technology");
        detStatusLabel = new JLabel("Permanent Staff (ACTIVE)");
        detAppointedLabel = new JLabel("2026-10-07 14:51:10");

        addDetailRow(empFields, "Staff Registration No:", detStaffNoLabel);
        addDetailRow(empFields, "System Officer ID:", detSysIdLabel);
        addDetailRow(empFields, "Full Legal Name:", detFullNameLabel);
        addDetailRow(empFields, "Designation / Grade:", detDesignationLabel);
        addDetailRow(empFields, "Faculty:", detFacultyLabel);
        addDetailRow(empFields, "Academic Department:", detDepartmentLabel);
        addDetailRow(empFields, "Employment Status:", detStatusLabel);
        addDetailRow(empFields, "Appointment Date:", detAppointedLabel);

        employmentCard.add(empHeader, BorderLayout.NORTH);
        employmentCard.add(empFields, BorderLayout.CENTER);

        // Right Card: Contact & Office Location
        JPanel contactCard = createCardPanel();
        contactCard.setLayout(new BorderLayout(0, 12));
        contactCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JLabel contactHeader = new JLabel("Campus Office & Contact Channels");
        contactHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contactHeader.setForeground(darkNavy);

        JPanel contactFields = new JPanel(new GridLayout(6, 2, 8, 12));
        contactFields.setOpaque(false);

        detEmailLabel = new JLabel("dinesh.to@uor.lk");
        detPhoneLabel = new JLabel("0710000007");
        detOfficeRoomLabel = new JLabel("Room A-204, IT Technical Office");
        detAddressLabel = new JLabel("Faculty of Technology, University of Ruhuna, Kamburupitiya");
        JLabel hoursLabel = new JLabel("Mon - Fri: 08:00 AM - 04:30 PM");
        JLabel emergencyExt = new JLabel("Intercom Ext: 4410");

        addDetailRow(contactFields, "University Email:", detEmailLabel);
        addDetailRow(contactFields, "Mobile Phone:", detPhoneLabel);
        addDetailRow(contactFields, "Campus Office Room:", detOfficeRoomLabel);
        addDetailRow(contactFields, "Residential Address:", detAddressLabel);
        addDetailRow(contactFields, "Official Working Hours:", hoursLabel);
        addDetailRow(contactFields, "Emergency Extension:", emergencyExt);

        contactCard.add(contactHeader, BorderLayout.NORTH);
        contactCard.add(contactFields, BorderLayout.CENTER);

        grid.add(employmentCard);
        grid.add(contactCard);

        // Bottom Card: Technical Specializations & Duties
        JPanel dutiesCard = createCardPanel();
        dutiesCard.setLayout(new BorderLayout(0, 10));
        dutiesCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(14, 18, 14, 18)));

        JLabel dutiesTitle = new JLabel("Assigned Operational Roles & System Authorizations");
        dutiesTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dutiesTitle.setForeground(darkNavy);

        JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        badgesRow.setOpaque(false);

        badgesRow.add(createSpecializationBadge("Network & Server Maintenance (Cisco / VLANs / DHCP)", primaryBlue));
        badgesRow.add(createSpecializationBadge("Workstation OS Imaging & Lab Deployment", darkNavy));
        badgesRow.add(createSpecializationBadge("Practical Attendance Logging & 80% Threshold Audits", successGreen));
        badgesRow.add(createSpecializationBadge("Medical Certificate Authenticity Verification", warningAmber));
        badgesRow.add(createSpecializationBadge("Preventive Servicing & Equipment Replacement", alertRed));

        dutiesCard.add(dutiesTitle, BorderLayout.NORTH);
        dutiesCard.add(badgesRow, BorderLayout.CENTER);

        panel.add(grid, BorderLayout.CENTER);
        panel.add(dutiesCard, BorderLayout.SOUTH);

        return panel;
    }

    private void addDetailRow(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(textMuted);

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueLabel.setForeground(textDark);

        panel.add(l);
        panel.add(valueLabel);
    }

    private JPanel createSpecializationBadge(String text, Color color) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        badge.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
        badge.setBorder(new CompoundBorder(new LineBorder(color, 1), new EmptyBorder(2, 6, 2, 6)));

        JLabel lbl = new JLabel("✓  " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(color);

        badge.add(lbl);
        return badge;
    }

    // --- Tab 2: Assigned Laboratories & Fleet ---

    private JPanel createAssignedLabsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Header info banner
        JPanel noteCard = createCardPanel();
        noteCard.setLayout(new BorderLayout());
        noteCard.setBorder(
                new CompoundBorder(new LineBorder(new Color(191, 219, 254), 1), new EmptyBorder(12, 16, 12, 16)));
        noteCard.setBackground(new Color(239, 246, 255));

        JLabel noteText = new JLabel(
                "<html><b>Primary Laboratory Fleet Assignment:</b> Officer <b>TO001 (Dinesh Kumara)</b> is designated as the officer-in-charge "
                        +
                        "for all academic computing, networking, and hardware facilities under the Department of Information and Communication Technology. "
                        +
                        "Direct all equipment maintenance tickets and practical timetable setups through this module.</html>");
        noteText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteText.setForeground(darkNavy);
        noteCard.add(noteText, BorderLayout.CENTER);

        JButton viewTimetableBtn = createStyledButton("🗓️ View Timetables", darkNavy, Color.WHITE);
        viewTimetableBtn.setToolTipText("View Academic Timetable & Lab Sessions");
        viewTimetableBtn.addActionListener(e -> {
            if (onNavigateToTimetable != null) {
                onNavigateToTimetable.run();
            }
        });
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnWrapper.setOpaque(false);
        btnWrapper.add(viewTimetableBtn);
        noteCard.add(btnWrapper, BorderLayout.EAST);

        // Labs Table Card
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JLabel tableTitle = new JLabel("Laboratories Under Supervision");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(darkNavy);

        String[] cols = {
                "Lab ID", "Lab Name", "Campus Location", "Department",
                "Total Stations", "Operational", "Availability", "Status", "Equipment Highlights"
        };

        assignedLabsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        assignedLabsTable = new JTable(assignedLabsTableModel);
        styleTable(assignedLabsTable);

        JScrollPane scrollPane = new JScrollPane(assignedLabsTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        panel.add(noteCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private void loadAssignedLabsTable() {
        if (assignedLabsTableModel == null)
            return;
        assignedLabsTableModel.setRowCount(0);

        List<LabFacility> labs = service.getAssignedLabs();
        for (LabFacility lab : labs) {
            assignedLabsTableModel.addRow(new Object[] {
                    lab.getLabId(),
                    lab.getLabName(),
                    lab.getLocation(),
                    lab.getDepartment(),
                    lab.getTotalWorkstations() + " units",
                    lab.getOperationalWorkstations() + " operational",
                    String.format("%.1f%%", lab.getOperationalPercentage()),
                    lab.getStatus(),
                    lab.getEquipmentSummary()
            });
        }
    }

    // --- Tab 3: Account Security & Credentials ---

    private JPanel createSecurityTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 0));
        panel.setOpaque(false);

        // Left Card: Authentication & Security Overview
        JPanel secOverviewCard = createCardPanel();
        secOverviewCard.setLayout(new BorderLayout(0, 14));
        secOverviewCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JLabel secTitle = new JLabel("Security & Session Authentication");
        secTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        secTitle.setForeground(darkNavy);

        JPanel secGrid = new JPanel(new GridLayout(6, 2, 8, 12));
        secGrid.setOpaque(false);

        secUsernameLabel = new JLabel("to001");
        secRoleLabel = new JLabel("TECHNICAL_OFFICER (Privileged Staff)");
        JLabel sec2fa = new JLabel("Enabled (SMS / Institutional Mail)");
        sec2fa.setForeground(successGreen);
        JLabel secSession = new JLabel("Secured Local Session (127.0.0.1)");
        JLabel secPolicy = new JLabel("Strict 8-char mix policy");
        JLabel secLastLogin = new JLabel("Today, System Launch Time");

        addDetailRow(secGrid, "Login Username:", secUsernameLabel);
        addDetailRow(secGrid, "Access Role Level:", secRoleLabel);
        addDetailRow(secGrid, "Two-Factor Auth:", sec2fa);
        addDetailRow(secGrid, "Current Terminal Session:", secSession);
        addDetailRow(secGrid, "Password Security Policy:", secPolicy);
        addDetailRow(secGrid, "Last System Login:", secLastLogin);

        JPanel tipBox = new JPanel(new BorderLayout());
        tipBox.setBackground(new Color(254, 243, 199));
        tipBox.setBorder(
                new CompoundBorder(new LineBorder(new Color(245, 158, 11), 1), new EmptyBorder(10, 12, 10, 12)));

        JLabel tipText = new JLabel(
                "<html><b>Security Notice:</b> Technical Officer credentials grant direct access to " +
                        "examination attendance records, lab workstation settings, and medical certificate verifications. "
                        +
                        "Never share your password or leave your terminal unlocked.</html>");
        tipText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tipText.setForeground(new Color(146, 64, 14));
        tipBox.add(tipText, BorderLayout.CENTER);

        secOverviewCard.add(secTitle, BorderLayout.NORTH);
        secOverviewCard.add(secGrid, BorderLayout.CENTER);
        secOverviewCard.add(tipBox, BorderLayout.SOUTH);

        // Right Card: Change Password Form
        JPanel changePassCard = createCardPanel();
        changePassCard.setLayout(new BorderLayout(0, 12));
        changePassCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JLabel changeTitle = new JLabel("Change Account Password");
        changeTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        changeTitle.setForeground(darkNavy);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);

        currentPasswordField = new JPasswordField();
        stylePasswordField(currentPasswordField);

        newPasswordField = new JPasswordField();
        stylePasswordField(newPasswordField);

        confirmPasswordField = new JPasswordField();
        stylePasswordField(confirmPasswordField);

        int y = 0;
        gbc.gridx = 0;
        gbc.gridy = y++;
        formGrid.add(createLabeledField("Current System Password:", currentPasswordField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        formGrid.add(createLabeledField("New Secure Password (min 6 characters):", newPasswordField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        formGrid.add(createLabeledField("Confirm New Password:", confirmPasswordField), gbc);

        JLabel pwdHint = new JLabel(
                "<html>• Must be at least 6 characters long<br>• Cannot match your current password<br>• Stored securely with immediate synchronization</html>");
        pwdHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pwdHint.setForeground(textMuted);
        gbc.gridx = 0;
        gbc.gridy = y++;
        formGrid.add(pwdHint, gbc);

        JButton savePassBtn = createStyledButton("Update Account Password", darkNavy, Color.WHITE);
        savePassBtn.addActionListener(e -> handleChangePassword());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnRow.setOpaque(false);
        btnRow.add(savePassBtn);

        changePassCard.add(changeTitle, BorderLayout.NORTH);
        changePassCard.add(formGrid, BorderLayout.CENTER);
        changePassCard.add(btnRow, BorderLayout.SOUTH);

        panel.add(secOverviewCard);
        panel.add(changePassCard);

        return panel;
    }

    private void handleChangePassword() {
        String currentPass = new String(currentPasswordField.getPassword()).trim();
        String newPass = new String(newPasswordField.getPassword()).trim();
        String confirmPass = new String(confirmPasswordField.getPassword()).trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all three password fields.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "New password must be at least 6 characters long.",
                    "Security Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.equals(currentPass)) {
            JOptionPane.showMessageDialog(this,
                    "New password cannot be identical to the current password.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                    "The new password and confirmation password do not match.",
                    "Mismatch Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = service.changePassword(currentOfficerId, currentPass, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Password updated successfully! Your account credentials have been synchronized.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            loadAuditTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "The current password you entered is incorrect. Please verify and try again.",
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Tab 4: Activity & System Audit Trail ---

    private JPanel createAuditTrailTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JLabel title = new JLabel("Audit Trail & Administrative Activity History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(darkNavy);

        String[] cols = { "Timestamp", "Action Category", "Activity Description", "Result Status" };
        auditTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        auditTable = new JTable(auditTableModel);
        styleTable(auditTable);

        // Center align category and status
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(450);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(auditTable);
        scroll.setBorder(new LineBorder(borderColor, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        tableCard.add(title, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private void loadAuditTable() {
        if (auditTableModel == null)
            return;
        auditTableModel.setRowCount(0);

        List<String[]> logs = service.getAuditLogs();
        for (String[] entry : logs) {
            auditTableModel.addRow(entry);
        }
    }

    // --- Edit Profile Dialog ---

    private void showEditProfileDialog() {
        if (currentOfficer == null) {
            currentOfficer = service.getProfile(currentOfficerId);
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Technical Officer Profile - " + currentOfficer.getTechnicalOfficerId(), true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 22, 18, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);

        JTextField fnField = new JTextField(currentOfficer.getFirstName());
        styleTextField(fnField);

        JTextField lnField = new JTextField(currentOfficer.getLastName());
        styleTextField(lnField);

        JTextField emailField = new JTextField(currentOfficer.getEmail());
        styleTextField(emailField);

        JTextField phoneField = new JTextField(currentOfficer.getPhone());
        styleTextField(phoneField);

        JTextField deptField = new JTextField(currentOfficer.getDepartment());
        styleTextField(deptField);

        JTextField roomField = new JTextField(currentOfficer.getOfficeRoom());
        styleTextField(roomField);

        JTextField addressField = new JTextField(currentOfficer.getAddress());
        styleTextField(addressField);

        int y = 0;
        gbc.gridx = 0;
        gbc.gridy = y++;
        form.add(createLabeledField("First Name:", fnField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        form.add(createLabeledField("Last Name:", lnField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        form.add(createLabeledField("University Email Address:", emailField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        form.add(createLabeledField("Official Contact Phone:", phoneField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        form.add(createLabeledField("Assigned Department:", deptField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        form.add(createLabeledField("Campus Office / Room:", roomField), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;
        form.add(createLabeledField("Residential Address:", addressField), gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnRow.setBackground(new Color(245, 247, 250));
        btnRow.setBorder(new LineBorder(borderColor, 1));

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, textDark);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Save Changes", successGreen, Color.WHITE);
        saveBtn.addActionListener(e -> {
            String fn = fnField.getText().trim();
            String ln = lnField.getText().trim();
            String em = emailField.getText().trim();
            String ph = phoneField.getText().trim();

            if (fn.isEmpty() || ln.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "First Name and Last Name are required.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (em.isEmpty() || !em.contains("@")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            currentOfficer.setFirstName(fn);
            currentOfficer.setLastName(ln);
            currentOfficer.setEmail(em);
            currentOfficer.setPhone(ph);
            currentOfficer.setDepartment(deptField.getText().trim());
            currentOfficer.setOfficeRoom(roomField.getText().trim());
            currentOfficer.setAddress(addressField.getText().trim());

            boolean ok = service.updateProfile(currentOfficer);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Profile details updated successfully.", "Saved",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshProfile();
            } else {
                JOptionPane.showMessageDialog(dialog, "Could not save profile details.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // --- Refresh Data & State Binding ---

    public void refreshProfile() {
        currentOfficer = service.getProfile(currentOfficerId);
        if (currentOfficer == null)
            return;

        // Update Avatar initials
        String initials = "";
        if (currentOfficer.getFirstName() != null && !currentOfficer.getFirstName().isEmpty()) {
            initials += currentOfficer.getFirstName().substring(0, 1).toUpperCase();
        }
        if (currentOfficer.getLastName() != null && !currentOfficer.getLastName().isEmpty()) {
            initials += currentOfficer.getLastName().substring(0, 1).toUpperCase();
        }
        if (initials.isEmpty())
            initials = "TO";
        avatarLabel.setText(initials);

        // Update Hero Card
        heroNameLabel.setText(currentOfficer.getFullName());
        staffPillLabel.setText("STAFF: " + currentOfficer.getStaffNo());
        sysIdPillLabel.setText("ID: " + currentOfficer.getTechnicalOfficerId());
        heroRoleLabel.setText(currentOfficer.getDesignation() + " • " + currentOfficer.getFaculty());
        heroDeptLabel.setText(currentOfficer.getDepartment());
        heroContactLabel.setText("Email: " + currentOfficer.getEmail() + "   |   Phone: " + currentOfficer.getPhone()
                + "   |   Office: " + currentOfficer.getOfficeRoom());

        // Update Tab 1 Details
        detStaffNoLabel.setText(currentOfficer.getStaffNo());
        detSysIdLabel.setText(currentOfficer.getTechnicalOfficerId());
        detFullNameLabel.setText(currentOfficer.getFullName());
        detDesignationLabel.setText(currentOfficer.getDesignation());
        detFacultyLabel.setText(currentOfficer.getFaculty());
        detDepartmentLabel.setText(currentOfficer.getDepartment());
        detStatusLabel.setText("Permanent Staff (" + currentOfficer.getStatus() + ")");
        detAppointedLabel.setText(currentOfficer.getCreatedDate());

        detEmailLabel.setText(currentOfficer.getEmail());
        detPhoneLabel.setText(currentOfficer.getPhone());
        detOfficeRoomLabel.setText(currentOfficer.getOfficeRoom());
        detAddressLabel.setText(currentOfficer.getAddress());

        // Update Tab 3 Security
        secUsernameLabel.setText(currentOfficer.getUsername() != null ? currentOfficer.getUsername() : "to001");

        // Update Top KPI ribbon
        Map<String, Object> stats = service.getProfileStats();
        kpiLabsValueLabel.setText(stats.get("totalLabs") + " Labs");
        kpiWorkstationsValueLabel.setText(stats.get("totalWorkstations") + " PCs");
        kpiUptimeValueLabel.setText((String) stats.get("uptimePercentage"));
        kpiTicketsValueLabel.setText(stats.get("maintenanceTickets") + " Handled");

        // Reload active table
        loadAssignedLabsTable();
        loadAuditTable();
    }

    // --- UI Helpers & Styling ---

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(cardWhite);
        return card;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBackground(Color.WHITE);
        tf.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 8, 6, 8)));
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pf.setBackground(Color.WHITE);
        pf.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 8, 6, 8)));
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(6, 14, 6, 14)));
        return btn;
    }

    private JLabel createPillLabel(String text, Color fg, Color bg) {
        JLabel pill = new JLabel(text, SwingConstants.CENTER);
        pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pill.setForeground(fg);
        pill.setOpaque(true);
        pill.setBackground(bg);
        pill.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(2, 8, 2, 8)));
        return pill;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(textDark);

        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
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

    // Striped Row Renderer
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
}
