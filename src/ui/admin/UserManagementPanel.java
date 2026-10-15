package ui.admin;

import model.UserAccount;
import service.UserService;
import service.UserService.OperationResult;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern User Management CRUD Panel for FAMS Admin Dashboard.
 * Manages system login accounts ('users' table) and displays linked profiles:
 * - Undergraduates (Index Number)
 * - Lecturers (Staff ID)
 * - Technical Officers (Staff ID)
 * - Administrators (Admin ID)
 */
public class UserManagementPanel extends JPanel {

    // Palette harmonized with AdminDashboard
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color PURPLE_ACCENT   = new Color(99, 102, 241);  // #6366F1
    private static final Color WARNING_COLOR   = new Color(217, 119, 6);   // #D97706
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A
    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // #DC2626
    private static final Color MAIN_BG         = new Color(245, 247, 251); // #F5F7FB
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_SECONDARY  = new Color(100, 116, 139); // #64748B
    private static final Color BORDER_COLOR    = new Color(226, 232, 240); // #E2E8F0
    private static final Color LIGHT_BLUE      = new Color(238, 242, 255); // #EEF2FF

    // Service reference
    private final UserService userService;
    private final Runnable dataChangedCallback;

    // Table & Models
    private JTable usersTable;
    private UserTableModel tableModel;
    private TableRowSorter<UserTableModel> sorter;

    // Summary Metric Labels
    private JLabel lblTotalUsers;
    private JLabel lblActiveUsers;
    private JLabel lblInactiveUsers;
    private JLabel lblAdminUsers;

    // Filters & Search
    private JTextField txtSearch;
    private JComboBox<String> cmbRoleFilter;
    private JComboBox<String> cmbStatusFilter;
    private JLabel lblTableStatus;

    // Account Details Form Controls
    private JTextField txtUserId;
    private JTextField txtUsername;
    private JComboBox<String> cmbRole;
    private JTextField txtLinkedProfile;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JComboBox<String> cmbStatus;
    private JLabel lblFormMode;

    // Action Buttons
    private JButton btnSave;
    private JButton btnToggleStatus;
    private JButton btnNewAdmin;
    private JButton btnClear;
    private JButton btnRefresh;

    private boolean isCreatingNewAdmin = false;
    private UserAccount selectedAccount = null;

    public UserManagementPanel() {
        this(null);
    }

    public UserManagementPanel(Runnable dataChangedCallback) {
        this.userService = UserService.getInstance();
        this.dataChangedCallback = dataChangedCallback;
        initUI();
        loadUsersAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(16, 24, 20, 24));

        // 1. Top Section: Page Header & Summary Cards
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        topContainer.add(createPageHeader());
        topContainer.add(Box.createVerticalStrut(12));
        topContainer.add(createSummaryCards());
        topContainer.add(Box.createVerticalStrut(14));

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: Master-Detail (Left: Users Table, Right: Account Details Form)
        JPanel masterDetailPanel = createMasterDetailSection();
        add(masterDetailPanel, BorderLayout.CENTER);
    }

    // SECTION A: PAGE HEADER

    private JPanel createPageHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("USER MANAGEMENT");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 20));
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel("Manage System Login Accounts and Portal Access Permissions");
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        subLbl.setForeground(TEXT_SECONDARY);

        titles.add(titleLbl);
        titles.add(Box.createVerticalStrut(3));
        titles.add(subLbl);
        header.add(titles, BorderLayout.WEST);

        // Right side: Quick Refresh & Export/Info
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightHeader.setOpaque(false);

        btnRefresh = createIconButton("↻", "Refresh User Accounts", e -> loadUsersAsync());
        rightHeader.add(btnRefresh);

        header.add(rightHeader, BorderLayout.EAST);
        return header;
    }

    // SECTION B: SUMMARY CARDS

    private JPanel createSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row.setPreferredSize(new Dimension(1000, 82));

        lblTotalUsers    = new JLabel("--");
        lblActiveUsers   = new JLabel("--");
        lblInactiveUsers = new JLabel("--");
        lblAdminUsers    = new JLabel("--");

        row.add(createMetricCard("Total Users", lblTotalUsers, PRIMARY_NAVY, new Color(241, 245, 249), "All System Accounts"));
        row.add(createMetricCard("Active Users", lblActiveUsers, SUCCESS_COLOR, new Color(220, 252, 231), "Enabled Login Access"));
        row.add(createMetricCard("Inactive Users", lblInactiveUsers, WARNING_COLOR, new Color(254, 243, 199), "Suspended Accounts"));
        row.add(createMetricCard("Administrators", lblAdminUsers, PURPLE_ACCENT, LIGHT_BLUE, "Privileged Access"));

        return row;
    }

    private JPanel createMetricCard(String title, JLabel valLabel, Color accent, Color bgPill, String subtext) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        tLbl.setForeground(TEXT_SECONDARY);

        valLabel.setFont(AdminDashboard.getModernFont(Font.BOLD, 20));
        valLabel.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtext);
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        subLbl.setForeground(new Color(148, 163, 184));

        text.add(tLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(valLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(subLbl);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // SECTION C & D & E: MASTER-DETAIL WORKSPACE

    private JPanel createMasterDetailSection() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Panel: Users Table (63% width)
        gbc.gridx = 0;
        gbc.weightx = 0.63;
        gbc.insets = new Insets(0, 0, 0, 12);
        container.add(createTableCard(), gbc);

        // Right Panel: Account Details Form (37% width)
        gbc.gridx = 1;
        gbc.weightx = 0.37;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(createFormCard(), gbc);

        return container;
    }

    // SECTION D: USERS TABLE CARD

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 14, 12, 14));

        // 1. Search & Filter Toolbar
        JPanel toolbar = createToolbar();
        card.add(toolbar, BorderLayout.NORTH);

        // 2. JTable
        tableModel = new UserTableModel();
        sorter = new TableRowSorter<>(tableModel);

        usersTable = new JTable(tableModel);
        usersTable.setRowSorter(sorter);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setRowHeight(38);
        usersTable.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        usersTable.setForeground(TEXT_PRIMARY);
        usersTable.setBackground(Color.WHITE);
        usersTable.setShowGrid(false);
        usersTable.setIntercellSpacing(new Dimension(0, 0));
        usersTable.setFillsViewportHeight(true);

        // Table Header Styling
        JTableHeader header = usersTable.getTableHeader();
        header.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        header.setForeground(TEXT_SECONDARY);
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);

        // Custom Column Renderers
        setupColumnRenderers();

        // Selection Listener -> Populates Account Details Form
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // 3. Footer Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 4, 0, 4));

        lblTableStatus = new JLabel("Loading user accounts...");
        lblTableStatus.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        lblTableStatus.setForeground(TEXT_SECONDARY);
        footer.add(lblTableStatus, BorderLayout.WEST);

        JLabel tipLbl = new JLabel("Tip: Select any row to populate and edit account details.");
        tipLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        tipLbl.setForeground(new Color(148, 163, 184));
        footer.add(tipLbl, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        // Left Controls: Search Bar + Role Filter + Status Filter
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        // Search Input
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(210, 34));
        txtSearch.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search username, name, email, or ID...");
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterUsers(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterUsers(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterUsers(); }
        });
        leftPanel.add(txtSearch);

        // Role Filter Dropdown
        String[] roles = {"All Roles", "ADMIN", "LECTURER", "TECHNICAL_OFFICER", "UNDERGRADUATE"};
        cmbRoleFilter = new JComboBox<>(roles);
        styleFilterCombo(cmbRoleFilter, 130);
        cmbRoleFilter.addActionListener(e -> filterUsers());
        leftPanel.add(cmbRoleFilter);

        // Status Filter Dropdown
        String[] statuses = {"All Status", "ACTIVE", "INACTIVE"};
        cmbStatusFilter = new JComboBox<>(statuses);
        styleFilterCombo(cmbStatusFilter, 105);
        cmbStatusFilter.addActionListener(e -> filterUsers());
        leftPanel.add(cmbStatusFilter);

        // Reset Button
        JButton btnReset = new JButton("Reset");
        btnReset.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        btnReset.setForeground(TEXT_SECONDARY);
        btnReset.setBackground(Color.WHITE);
        btnReset.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(6, 10, 6, 10)));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetFilters());
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
        // Striped row base renderer
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(new Color(224, 237, 255));
                    c.setForeground(PRIMARY_NAVY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < usersTable.getColumnCount(); i++) {
            usersTable.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
        }

        // Column 0: User ID
        usersTable.getColumnModel().getColumn(0).setPreferredWidth(75);

        // Column 1: Username
        usersTable.getColumnModel().getColumn(1).setPreferredWidth(95);

        // Column 2: Role Badge Renderer
        usersTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        usersTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String role = (value != null) ? value.toString() : "";
                JLabel badge = new JLabel(" " + role + " ");
                badge.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
                badge.setOpaque(true);
                badge.setBorder(new EmptyBorder(2, 6, 2, 6));

                if ("ADMIN".equalsIgnoreCase(role)) {
                    badge.setBackground(new Color(238, 242, 255));
                    badge.setForeground(PURPLE_ACCENT);
                } else if ("LECTURER".equalsIgnoreCase(role)) {
                    badge.setBackground(new Color(224, 231, 255));
                    badge.setForeground(new Color(67, 56, 202));
                } else if ("TECHNICAL_OFFICER".equalsIgnoreCase(role)) {
                    badge.setBackground(new Color(254, 243, 199));
                    badge.setForeground(new Color(180, 83, 9));
                } else {
                    badge.setBackground(new Color(219, 234, 254));
                    badge.setForeground(SECONDARY_BLUE);
                }

                p.add(badge);
                return p;
            }
        });

        // Column 3: Linked Profile
        usersTable.getColumnModel().getColumn(3).setPreferredWidth(140);

        // Column 4: Full Name
        usersTable.getColumnModel().getColumn(4).setPreferredWidth(130);

        // Column 5: Email
        usersTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        // Column 6: Account Status Pill Renderer
        usersTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        usersTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String status = (value != null) ? value.toString() : "INACTIVE";
                boolean active = "ACTIVE".equalsIgnoreCase(status);

                JLabel pill = new JLabel("  " + status + "  ");
                pill.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
                pill.setOpaque(true);
                pill.setBorder(new EmptyBorder(2, 8, 2, 8));

                if (active) {
                    pill.setBackground(new Color(220, 252, 231));
                    pill.setForeground(SUCCESS_COLOR);
                } else {
                    pill.setBackground(new Color(254, 226, 226));
                    pill.setForeground(DANGER_COLOR);
                }

                p.add(pill);
                return p;
            }
        });
    }
    // SECTION E: ACCOUNT DETAILS FORM CARD
    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Form Header
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Account Details");
        formTitle.setFont(AdminDashboard.getModernFont(Font.BOLD, 14));
        formTitle.setForeground(TEXT_PRIMARY);

        lblFormMode = new JLabel("SELECT AN ACCOUNT");
        lblFormMode.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
        lblFormMode.setForeground(TEXT_SECONDARY);
        lblFormMode.setOpaque(true);
        lblFormMode.setBackground(new Color(241, 245, 249));
        lblFormMode.setBorder(new EmptyBorder(3, 8, 3, 8));

        formHeader.add(formTitle, BorderLayout.WEST);
        formHeader.add(lblFormMode, BorderLayout.EAST);
        card.add(formHeader, BorderLayout.NORTH);

        // Scrollable Form Fields Panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        // Field 1: User ID & Role (Grid 1x2)
        JPanel rowIdRole = new JPanel(new GridLayout(1, 2, 10, 0));
        rowIdRole.setOpaque(false);

        txtUserId = createStyledTextField();
        txtUserId.setEditable(false);
        txtUserId.setBackground(new Color(248, 250, 252));
        rowIdRole.add(createFormField("User ID", txtUserId));

        String[] formRoles = {"ADMIN", "LECTURER", "TECHNICAL_OFFICER", "UNDERGRADUATE"};
        cmbRole = new JComboBox<>(formRoles);
        styleFilterCombo(cmbRole, 120);
        cmbRole.setEnabled(false); // Locked by default to preserve child table FKs
        rowIdRole.add(createFormField("Role", cmbRole));
        fieldsPanel.add(rowIdRole);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 2: Username
        txtUsername = createStyledTextField();
        fieldsPanel.add(createFormField("Username *", txtUsername));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 3: Linked Profile
        txtLinkedProfile = createStyledTextField();
        txtLinkedProfile.setEditable(false);
        txtLinkedProfile.setBackground(new Color(248, 250, 252));
        fieldsPanel.add(createFormField("Linked Profile / ID", txtLinkedProfile));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 4: First Name & Last Name (Grid 1x2)
        JPanel rowNames = new JPanel(new GridLayout(1, 2, 10, 0));
        rowNames.setOpaque(false);

        txtFirstName = createStyledTextField();
        rowNames.add(createFormField("First Name *", txtFirstName));

        txtLastName = createStyledTextField();
        rowNames.add(createFormField("Last Name *", txtLastName));
        fieldsPanel.add(rowNames);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 5: Email & Phone (Grid 1x2)
        JPanel rowContact = new JPanel(new GridLayout(1, 2, 10, 0));
        rowContact.setOpaque(false);

        txtEmail = createStyledTextField();
        rowContact.add(createFormField("Email Address", txtEmail));

        txtPhone = createStyledTextField();
        rowContact.add(createFormField("Phone Number", txtPhone));
        fieldsPanel.add(rowContact);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 6: Account Status
        String[] statusOptions = {"ACTIVE", "INACTIVE"};
        cmbStatus = new JComboBox<>(statusOptions);
        styleFilterCombo(cmbStatus, 140);
        fieldsPanel.add(createFormField("Account Status", cmbStatus));
        fieldsPanel.add(Box.createVerticalStrut(12));

        // Informational Notice Banner
        JPanel noticeCard = new JPanel(new BorderLayout(8, 0));
        noticeCard.setBackground(new Color(248, 250, 252));
        noticeCard.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel noticeLbl = new JLabel("<html><body style='width: 250px; font-size: 10px; color: #64748B;'>" +
                "<b>Policy Notice:</b> Academic profile details (index numbers, departments, courses) " +
                "are governed by Student, Lecturer, and Technical Officer modules. User Management coordinates " +
                "credentials, status, and Administrator accounts." +
                "</body></html>");
        noticeCard.add(noticeLbl, BorderLayout.CENTER);
        fieldsPanel.add(noticeCard);

        JScrollPane formScroll = new JScrollPane(fieldsPanel);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        card.add(formScroll, BorderLayout.CENTER);

        // Form Action Buttons (Save, Activate/Deactivate, New Admin, Clear)
        JPanel actionsPanel = new JPanel();
        actionsPanel.setOpaque(false);
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));

        // Top button row: Save + Status Toggle
        JPanel rowBtns1 = new JPanel(new GridLayout(1, 2, 8, 0));
        rowBtns1.setOpaque(false);

        btnSave = createPrimaryButton("Save Changes", SECONDARY_BLUE, e -> onSaveAccount());
        btnSave.setEnabled(false);
        rowBtns1.add(btnSave);

        btnToggleStatus = createPrimaryButton("Deactivate", WARNING_COLOR, e -> onToggleStatus());
        btnToggleStatus.setEnabled(false);
        rowBtns1.add(btnToggleStatus);

        actionsPanel.add(rowBtns1);
        actionsPanel.add(Box.createVerticalStrut(6));

        // Bottom button row: + New Admin + Clear Form
        JPanel rowBtns2 = new JPanel(new GridLayout(1, 2, 8, 0));
        rowBtns2.setOpaque(false);

        btnNewAdmin = createOutlineButton("+ New Admin", PURPLE_ACCENT, e -> onNewAdminMode());
        rowBtns2.add(btnNewAdmin);

        btnClear = createOutlineButton("Clear Form", TEXT_SECONDARY, e -> clearForm());
        rowBtns2.add(btnClear);

        actionsPanel.add(rowBtns2);
        card.add(actionsPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFormField(String label, JComponent comp) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        field.add(l);
        field.add(Box.createVerticalStrut(3));
        field.add(comp);
        return field;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        tf.setPreferredSize(new Dimension(100, 34));
        return tf;
    }

    private JButton createPrimaryButton(String text, Color bg, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text);
        btn.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        btn.addActionListener(l);
        return btn;
    }

    private JButton createOutlineButton(String text, Color color, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text);
        btn.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        btn.setForeground(color);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(color, 1, true), new EmptyBorder(6, 10, 6, 10)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 34));
        btn.addActionListener(l);
        return btn;
    }

    private JButton createIconButton(String iconText, String tooltip, java.awt.event.ActionListener l) {
        JButton btn = new JButton(iconText);
        btn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        btn.setToolTipText(tooltip);
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(4, 10, 4, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(l);
        return btn;
    }

    // DATA OPERATIONS & HANDLERS

    /**
     * Loads user accounts from the database asynchronously.
     */
    public void loadUsersAsync() {
        lblTableStatus.setText("Refreshing accounts from database...");
        btnRefresh.setEnabled(false);

        SwingWorker<List<UserAccount>, Void> worker = new SwingWorker<>() {
            private Map<String, Integer> summaryCounts;

            @Override
            protected List<UserAccount> doInBackground() {
                summaryCounts = userService.getUserSummaryCounts();
                return userService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<UserAccount> users = get();
                    tableModel.setUsers(users);
                    filterUsers();
                    updateSummaryCards(summaryCounts);
                    lblTableStatus.setText("Showing " + users.size() + " user accounts.");
                } catch (Exception e) {
                    lblTableStatus.setText("Error loading user records: " + e.getMessage());
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
    public void loadUsersSync() {
        List<UserAccount> users = userService.getAllUsers();
        tableModel.setUsers(users);
        filterUsers();
        updateSummaryCards(userService.getUserSummaryCounts());
    }

    private void updateSummaryCards(Map<String, Integer> counts) {
        if (counts == null) return;
        lblTotalUsers.setText(String.valueOf(counts.getOrDefault("total", 0)));
        lblActiveUsers.setText(String.valueOf(counts.getOrDefault("active", 0)));
        lblInactiveUsers.setText(String.valueOf(counts.getOrDefault("inactive", 0)));
        lblAdminUsers.setText(String.valueOf(counts.getOrDefault("admin", 0)));
    }

    private void filterUsers() {
        String keyword = txtSearch.getText().trim();
        String roleFilter = (String) cmbRoleFilter.getSelectedItem();
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();

        List<RowFilter<UserTableModel, Integer>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + PatternQuote(keyword)));
        }

        if (roleFilter != null && !roleFilter.equalsIgnoreCase("All Roles")) {
            filters.add(RowFilter.regexFilter("^" + roleFilter + "$", 2));
        }

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Status")) {
            filters.add(RowFilter.regexFilter("^" + statusFilter + "$", 6));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }

        int visible = usersTable.getRowCount();
        int total = tableModel.getRowCount();
        lblTableStatus.setText("Showing " + visible + " of " + total + " user accounts.");
    }

    private String PatternQuote(String s) {
        return java.util.regex.Pattern.quote(s);
    }

    private void resetFilters() {
        txtSearch.setText("");
        cmbRoleFilter.setSelectedIndex(0);
        cmbStatusFilter.setSelectedIndex(0);
        filterUsers();
    }

    private void onTableRowSelected() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int modelIdx = usersTable.convertRowIndexToModel(selectedRow);
        selectedAccount = tableModel.getUserAt(modelIdx);
        if (selectedAccount == null) return;

        isCreatingNewAdmin = false;
        lblFormMode.setText("EDITING ACCOUNT");
        lblFormMode.setBackground(new Color(224, 237, 255));
        lblFormMode.setForeground(PRIMARY_NAVY);

        // Populate fields
        txtUserId.setText(selectedAccount.getUserId());
        txtUsername.setText(selectedAccount.getUsername());
        cmbRole.setSelectedItem(selectedAccount.getRole());
        cmbRole.setEnabled(false); // Locked for existing profile integrity
        txtLinkedProfile.setText(selectedAccount.getLinkedProfileIdentifier() + " (" + selectedAccount.getDepartment() + ")");
        txtFirstName.setText(selectedAccount.getFirstName());
        txtLastName.setText(selectedAccount.getLastName());
        txtEmail.setText(selectedAccount.getEmail() != null ? selectedAccount.getEmail() : "");
        txtPhone.setText(selectedAccount.getPhone() != null ? selectedAccount.getPhone() : "");
        cmbStatus.setSelectedItem(selectedAccount.getStatus());

        // Buttons
        btnSave.setText("Save Changes");
        btnSave.setEnabled(true);

        boolean active = selectedAccount.isActive();
        btnToggleStatus.setText(active ? "Deactivate" : "Activate");
        btnToggleStatus.setBackground(active ? WARNING_COLOR : SUCCESS_COLOR);
        btnToggleStatus.setEnabled(true);
    }

    private void onNewAdminMode() {
        usersTable.clearSelection();
        selectedAccount = null;
        isCreatingNewAdmin = true;

        lblFormMode.setText("NEW ADMIN ACCOUNT");
        lblFormMode.setBackground(LIGHT_BLUE);
        lblFormMode.setForeground(PURPLE_ACCENT);

        String nextAdminId = userService.getNextSuggestedAdminId();
        txtUserId.setText(nextAdminId);
        txtUsername.setText("");
        cmbRole.setSelectedItem("ADMIN");
        cmbRole.setEnabled(false);
        txtLinkedProfile.setText("Admin ID: " + nextAdminId + " (Faculty of Technology)");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        cmbStatus.setSelectedItem("ACTIVE");

        btnSave.setText("Create Admin");
        btnSave.setEnabled(true);
        btnToggleStatus.setEnabled(false);
        txtUsername.requestFocusInWindow();
    }

    public void clearForm() {
        usersTable.clearSelection();
        selectedAccount = null;
        isCreatingNewAdmin = false;

        lblFormMode.setText("SELECT AN ACCOUNT");
        lblFormMode.setBackground(new Color(241, 245, 249));
        lblFormMode.setForeground(TEXT_SECONDARY);

        txtUserId.setText("");
        txtUsername.setText("");
        cmbRole.setSelectedIndex(0);
        cmbRole.setEnabled(false);
        txtLinkedProfile.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        cmbStatus.setSelectedIndex(0);

        btnSave.setText("Save Changes");
        btnSave.setEnabled(false);
        btnToggleStatus.setText("Deactivate");
        btnToggleStatus.setEnabled(false);
    }

    private void onSaveAccount() {
        if (isCreatingNewAdmin) {
            handleCreateAdmin();
        } else {
            handleUpdateAccount();
        }
    }

    private void handleUpdateAccount() {
        if (selectedAccount == null) return;

        UserAccount toUpdate = new UserAccount();
        toUpdate.setUserId(txtUserId.getText().trim());
        toUpdate.setUsername(txtUsername.getText().trim());
        toUpdate.setRole((String) cmbRole.getSelectedItem());
        toUpdate.setFirstName(txtFirstName.getText().trim());
        toUpdate.setLastName(txtLastName.getText().trim());
        toUpdate.setEmail(txtEmail.getText().trim());
        toUpdate.setPhone(txtPhone.getText().trim());
        toUpdate.setStatus((String) cmbStatus.getSelectedItem());

        OperationResult result = userService.updateUserAccount(toUpdate);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Account Updated", JOptionPane.INFORMATION_MESSAGE);
            loadUsersAsync();
            if (dataChangedCallback != null) dataChangedCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Validation / Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCreateAdmin() {
        UserAccount newAdmin = new UserAccount();
        newAdmin.setUserId(txtUserId.getText().trim());
        newAdmin.setUsername(txtUsername.getText().trim());
        newAdmin.setRole("ADMIN");
        newAdmin.setFirstName(txtFirstName.getText().trim());
        newAdmin.setLastName(txtLastName.getText().trim());
        newAdmin.setEmail(txtEmail.getText().trim());
        newAdmin.setPhone(txtPhone.getText().trim());
        newAdmin.setStatus("ACTIVE");
        newAdmin.setDepartment("Faculty of Technology");

        OperationResult result = userService.createAdminAccount(newAdmin);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 280px; font-family: sans-serif;'>" +
                    "<h4 style='color: #10284A; margin-bottom: 4px;'>Administrator Created!</h4>" +
                    "<p style='color: #4A5568; line-height: 1.4;'>" +
                    "Account <b>" + newAdmin.getUsername() + "</b> (" + newAdmin.getUserId() + ") was created successfully.<br><br>" +
                    "<small style='color: #718096;'>Initial standard credential assigned: <code>Admin@123</code></small>" +
                    "</p></body></html>",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadUsersAsync();
            if (dataChangedCallback != null) dataChangedCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Error Creating Administrator", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onToggleStatus() {
        if (selectedAccount == null) return;

        String id = selectedAccount.getUserId();
        String currentStatus = selectedAccount.getStatus();
        boolean isActive = "ACTIVE".equalsIgnoreCase(currentStatus);
        String targetStatus = isActive ? "INACTIVE" : "ACTIVE";
        String actionVerb = isActive ? "deactivate" : "activate";

        // Prevent admin deactivating their own currently logged-in account
        if (isActive && "ADM001".equalsIgnoreCase(id)) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Warning: Deactivating primary administrator account ADM001 may restrict system administration.\nAre you sure you want to proceed?",
                    "Confirm Primary Admin Deactivation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + actionVerb + " user account '" + selectedAccount.getUsername() + "' (" + id + ")?",
                "Confirm Account Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        OperationResult result = isActive ? userService.deactivateUser(id) : userService.activateUser(id);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            loadUsersAsync();
            if (dataChangedCallback != null) dataChangedCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getters for programmatic verification & automated testing
    public JTable getUsersTable() {
        return usersTable;
    }

    public JTextField getTxtSearch() {
        return txtSearch;
    }

    public JComboBox<String> getCmbRoleFilter() {
        return cmbRoleFilter;
    }

    public JComboBox<String> getCmbStatusFilter() {
        return cmbStatusFilter;
    }

    public void selectRow(int viewRowIdx) {
        if (viewRowIdx >= 0 && viewRowIdx < usersTable.getRowCount()) {
            usersTable.setRowSelectionInterval(viewRowIdx, viewRowIdx);
            onTableRowSelected();
        }
    }


    // INNER CLASS: TABLE MODEL
    private static class UserTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {
                "User ID", "Username", "Role", "Linked Profile", "Full Name", "Email Address", "Status"
        };
        private List<UserAccount> users = new ArrayList<>();

        public void setUsers(List<UserAccount> users) {
            this.users = (users != null) ? new ArrayList<>(users) : new ArrayList<>();
            fireTableDataChanged();
        }

        public UserAccount getUserAt(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < users.size()) {
                return users.get(rowIndex);
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return users.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            UserAccount u = users.get(rowIndex);
            switch (columnIndex) {
                case 0: return u.getUserId();
                case 1: return u.getUsername();
                case 2: return u.getRole();
                case 3: return u.getLinkedProfileIdentifier();
                case 4: return u.getFullName();
                case 5: return u.getEmail() != null ? u.getEmail() : "-";
                case 6: return u.getStatus();
                default: return "";
            }
        }
    }
}
