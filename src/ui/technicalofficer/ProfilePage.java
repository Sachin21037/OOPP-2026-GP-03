package ui.technicalofficer;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Technical Officer Profile Page in FAMS.
 * Implements profile viewing and updating except username and password,
 * matching the official role specification.
 */
public class ProfilePage extends JPanel {

    // Theme color palette matching TechnicalOfficerDashboard
    private final Color darkNavy = new Color(27, 67, 103);
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color cardWhite = Color.WHITE;
    private final Color successGreen = new Color(20, 165, 145);
    private final Color borderColor = new Color(225, 228, 232);
    private final Color textDark = new Color(30, 41, 59);
    private final Color textMuted = new Color(100, 116, 139);

    private final TechnicalOfficerService service = new TechnicalOfficerService();
    private final String currentOfficerId = "TO001";
    private TechnicalOfficer currentOfficer;

    // Hero Profile Labels
    private JLabel avatarLabel;
    private JLabel heroNameLabel;
    private JLabel heroRoleLabel;
    private JLabel heroDeptLabel;
    private JLabel heroContactLabel;
    private JLabel heroStatusBadge;
    private JLabel staffPillLabel;
    private JLabel sysIdPillLabel;

    // Tab Navigation
    private CardLayout tabCardLayout;
    private JPanel tabContentContainer;
    private JButton tab1Btn;
    private JButton tab2Btn;
    private JButton tab3Btn;

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

    // Tab 2 (Security / Account) Labels
    private JLabel secUsernameLabel;
    private JLabel secRoleLabel;

    // Tab 3 (Audit) Table
    private JTable auditTable;
    private DefaultTableModel auditTableModel;

    // Cross-module navigation callback
    private Runnable onNavigateToTimetable;

    public void setOnNavigateToTimetable(Runnable onNavigateToTimetable) {
        this.onNavigateToTimetable = onNavigateToTimetable;
    }

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

        // 1. Top Section (Header + Hero Card + Tabs)
        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);

        topSection.add(createPageHeader(), BorderLayout.NORTH);

        JPanel heroAndTabPanel = new JPanel(new BorderLayout(0, 14));
        heroAndTabPanel.setOpaque(false);
        heroAndTabPanel.add(createHeroCard(), BorderLayout.NORTH);
        heroAndTabPanel.add(createNavTabBar(), BorderLayout.SOUTH);

        topSection.add(heroAndTabPanel, BorderLayout.CENTER);

        // 2. Tab Content Container
        tabCardLayout = new CardLayout();
        tabContentContainer = new JPanel(tabCardLayout);
        tabContentContainer.setOpaque(false);

        tabContentContainer.add(createPersonalDetailsTab(), "TAB_DETAILS");
        tabContentContainer.add(createSecurityTab(), "TAB_SECURITY");
        tabContentContainer.add(createAuditTrailTab(), "TAB_AUDIT");

        mainContainer.add(topSection, BorderLayout.NORTH);
        mainContainer.add(tabContentContainer, BorderLayout.CENTER);

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

        JLabel title = new JLabel("Technical Officer Profile & Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Manage personal information, review departmental authorizations, and view activity history.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton editBtn = createStyledButton("Edit Profile", primaryBlue, Color.WHITE);
        editBtn.addActionListener(e -> showEditProfileDialog());

        JButton refreshBtn = createStyledButton("Refresh", Color.WHITE, Color.BLACK);
        refreshBtn.addActionListener(e -> refreshProfile());

        actions.add(editBtn);
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
        heroStatusBadge = createPillLabel("ACTIVE", successGreen, new Color(230, 247, 244));

        nameAndBadgesRow.add(heroNameLabel);
        nameAndBadgesRow.add(staffPillLabel);
        nameAndBadgesRow.add(sysIdPillLabel);
        nameAndBadgesRow.add(heroStatusBadge);

        heroRoleLabel = new JLabel("Technical Officer (Grade II) - Faculty of Technology");
        heroRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heroRoleLabel.setForeground(textMuted);

        heroDeptLabel = new JLabel("Department of Information and Communication Technology");
        heroDeptLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heroDeptLabel.setForeground(primaryBlue);

        heroContactLabel = new JLabel("Email: dinesh.to@uor.lk   |   Phone: 0710000007   |   Office: Room A-204");
        heroContactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heroContactLabel.setForeground(textMuted);

        centerBox.add(nameAndBadgesRow);
        centerBox.add(Box.createVerticalStrut(4));
        centerBox.add(heroRoleLabel);
        centerBox.add(Box.createVerticalStrut(2));
        centerBox.add(heroDeptLabel);
        centerBox.add(Box.createVerticalStrut(4));
        centerBox.add(heroContactLabel);

        card.add(avatarPanel, BorderLayout.WEST);
        card.add(centerBox, BorderLayout.CENTER);

        return card;
    }

    private JPanel createNavTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        tab1Btn = createTabButton("Personal & Employment Details", true);
        tab1Btn.addActionListener(e -> switchTab(1));

        tab2Btn = createTabButton("Account Credentials & Policy", false);
        tab2Btn.addActionListener(e -> switchTab(2));

        tab3Btn = createTabButton("Activity & System Audit Trail", false);
        tab3Btn.addActionListener(e -> switchTab(3));

        bar.add(tab1Btn);
        bar.add(tab2Btn);
        bar.add(tab3Btn);

        return bar;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(230, 36));
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
            tabCardLayout.show(tabContentContainer, "TAB_DETAILS");
        } else if (tabIndex == 2) {
            tabCardLayout.show(tabContentContainer, "TAB_SECURITY");
        } else if (tabIndex == 3) {
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

        // Left Card: Employment Overview
        JPanel employmentCard = createCardPanel();
        employmentCard.setLayout(new BorderLayout(0, 12));
        employmentCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JLabel empHeader = new JLabel("Official University Appointment Details");
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
        detAppointedLabel = new JLabel("2026-10-07");

        addDetailRow(empFields, "Staff Member No:", detStaffNoLabel);
        addDetailRow(empFields, "System User ID:", detSysIdLabel);
        addDetailRow(empFields, "Full Legal Name:", detFullNameLabel);
        addDetailRow(empFields, "Academic Designation:", detDesignationLabel);
        addDetailRow(empFields, "Faculty Affiliation:", detFacultyLabel);
        addDetailRow(empFields, "Assigned Department:", detDepartmentLabel);
        addDetailRow(empFields, "Appointment Status:", detStatusLabel);
        addDetailRow(empFields, "Date of Appointment:", detAppointedLabel);

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

        panel.add(grid, BorderLayout.CENTER);

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

    // --- Tab 2: Account Credentials & Policy ---

    private JPanel createSecurityTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel secOverviewCard = createCardPanel();
        secOverviewCard.setLayout(new BorderLayout(0, 16));
        secOverviewCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(20, 24, 20, 24)));

        JLabel secTitle = new JLabel("Account Credentials & Access Authorization");
        secTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        secTitle.setForeground(darkNavy);

        JPanel secGrid = new JPanel(new GridLayout(4, 2, 12, 14));
        secGrid.setOpaque(false);

        secUsernameLabel = new JLabel("to001");
        secRoleLabel = new JLabel("TECHNICAL_OFFICER (Privileged Staff)");
        JLabel secStatus = new JLabel("ACTIVE");
        secStatus.setForeground(successGreen);
        secStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel secTerminal = new JLabel("FAMS Technical Officer Terminal (Localhost)");

        addDetailRow(secGrid, "Login Username:", secUsernameLabel);
        addDetailRow(secGrid, "Access Role Level:", secRoleLabel);
        addDetailRow(secGrid, "Account Status:", secStatus);
        addDetailRow(secGrid, "Terminal Access:", secTerminal);

        JPanel policyBox = new JPanel(new BorderLayout());
        policyBox.setBackground(new Color(239, 246, 255));
        policyBox.setBorder(new CompoundBorder(new LineBorder(new Color(191, 219, 254), 1), new EmptyBorder(14, 16, 14, 16)));

        JLabel policyText = new JLabel(
                "<html><b>FAMS Policy Specification:</b> Technical Officers can update their profile information " +
                "(name, contact details, assigned department, address) except username and password. " +
                "Username and password credentials are managed directly by Faculty System Administrators.</html>");
        policyText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        policyText.setForeground(darkNavy);
        policyBox.add(policyText, BorderLayout.CENTER);

        secOverviewCard.add(secTitle, BorderLayout.NORTH);
        secOverviewCard.add(secGrid, BorderLayout.CENTER);
        secOverviewCard.add(policyBox, BorderLayout.SOUTH);

        panel.add(secOverviewCard, BorderLayout.NORTH);

        return panel;
    }

    // --- Tab 3: Activity & System Audit Trail ---

    private JPanel createAuditTrailTab() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(16, 18, 16, 18)));

        JPanel headRow = new JPanel(new BorderLayout());
        headRow.setOpaque(false);

        JLabel auditTitle = new JLabel("Technical Officer Activity & Audit Log");
        auditTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        auditTitle.setForeground(darkNavy);

        JLabel auditSub = new JLabel("Real-time chronological log of attendance recordings, medical validations, and profile updates.");
        auditSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        auditSub.setForeground(textMuted);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        titleBox.add(auditTitle);
        titleBox.add(auditSub);

        headRow.add(titleBox, BorderLayout.WEST);

        String[] cols = { "Timestamp", "Activity Category", "Description / Action Taken", "Outcome" };
        auditTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        auditTable = new JTable(auditTableModel);
        styleTable(auditTable);

        auditTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(450);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(auditTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(headRow, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void loadAuditTable() {
        if (auditTableModel == null) return;
        auditTableModel.setRowCount(0);

        List<String[]> logs = service.getAuditLogs();
        for (String[] entry : logs) {
            auditTableModel.addRow(entry);
        }
    }

    // --- Edit Profile Dialog (Profile details except username and password) ---

    private void showEditProfileDialog() {
        if (currentOfficer == null) {
            currentOfficer = service.getProfile(currentOfficerId);
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Technical Officer Profile - " + currentOfficer.getTechnicalOfficerId(), true);
        dialog.setSize(520, 480);
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
        form.add(createLabeledField("Official Email:", emailField), gbc);
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
        if (currentOfficer == null) return;

        // Update Avatar initials
        String initials = "";
        if (currentOfficer.getFirstName() != null && !currentOfficer.getFirstName().isEmpty()) {
            initials += currentOfficer.getFirstName().substring(0, 1).toUpperCase();
        }
        if (currentOfficer.getLastName() != null && !currentOfficer.getLastName().isEmpty()) {
            initials += currentOfficer.getLastName().substring(0, 1).toUpperCase();
        }
        if (initials.isEmpty()) initials = "TO";
        avatarLabel.setText(initials);

        // Update Hero Card
        heroNameLabel.setText(currentOfficer.getFullName());
        staffPillLabel.setText("STAFF: " + currentOfficer.getStaffNo());
        sysIdPillLabel.setText("ID: " + currentOfficer.getTechnicalOfficerId());
        heroRoleLabel.setText(currentOfficer.getDesignation() + " - " + currentOfficer.getFaculty());
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

        // Update Tab 2 Security
        secUsernameLabel.setText(currentOfficer.getUsername() != null ? currentOfficer.getUsername() : "to001");

        loadAuditTable();
    }

    // --- UI Helpers & Styling ---

    private JLabel createPillLabel(String text, Color fg, Color bg) {
        JLabel pill = new JLabel(text, SwingConstants.CENTER);
        pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pill.setForeground(fg);
        pill.setOpaque(true);
        pill.setBackground(bg);
        pill.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(2, 8, 2, 8)));
        return pill;
    }

    private JPanel createCardPanel() {
        JPanel p = new JPanel();
        p.setBackground(cardWhite);
        return p;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(7, 16, 7, 16)));
        return btn;
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

    private void styleTextField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setPreferredSize(new Dimension(300, 32));
        f.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(4, 8, 4, 8)));
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(borderColor);
        table.setSelectionBackground(new Color(237, 243, 248));
        table.setSelectionForeground(darkNavy);
        table.setDefaultRenderer(Object.class, new AlternateRowRenderer());

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
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setOpaque(true);
                label.setBackground(darkNavy);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(45, 90, 135)),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
                return label;
            }
        });
    }

    private static class AlternateRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252));
            } else {
                c.setBackground(new Color(237, 243, 248));
            }

            if (c instanceof JLabel) {
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
            }

            return c;
        }
    }
}
