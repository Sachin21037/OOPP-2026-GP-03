package ui.admin;

import model.Notice;
import service.NoticeService;
import service.NoticeService.OperationResult;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Notice Management & Viewer Panel for FAMS.
 * Supports dual-mode operation:
 * - ADMIN: Full management (Create, Read, Update, Delete, Search, Filter, Metrics).
 * - LECTURER / TECHNICAL_OFFICER / UNDERGRADUATE: Read-only notice reader with Search and Refresh.
 */
public class NoticeManagementPanel extends JPanel {

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
    private final Runnable onNoticeChangedCallback;

    private final NoticeService noticeService;

    // Header & Summary Card Labels
    private JLabel lblTotalNotices;
    private JLabel lblActiveNotices;
    private JLabel lblInactiveNotices;
    private JLabel lblLatestDate;
    private JLabel lblHeaderCount;
    private JLabel lblTableStatus;

    // Search and Filter Components
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JButton btnRefresh;

    // Table & Model
    private JTable noticeTable;
    private NoticeTableModel tableModel;
    private TableRowSorter<NoticeTableModel> sorter;

    // Admin Form Components
    private JLabel lblFormMode;
    private JTextField txtNoticeId;
    private JTextField txtTitle;
    private JTextField txtNoticeDate;
    private JComboBox<String> cmbStatus;
    private JTextArea txtDescription;
    private JLabel lblCharCount;
    private JLabel lblCreatorInfo;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Reader Mode Components (for non-admins)
    private JLabel lblReaderTitle;
    private JLabel lblReaderMeta;
    private JLabel lblReaderStatus;
    private JTextArea txtReaderBody;

    // Currently selected notice
    private Notice selectedNotice = null;

    public NoticeManagementPanel() {
        this("ADMIN", "ADM001", null);
    }

    public NoticeManagementPanel(Runnable onNoticeChangedCallback) {
        this("ADMIN", "ADM001", onNoticeChangedCallback);
    }

    public NoticeManagementPanel(String currentUserRole, String currentUserId, Runnable onNoticeChangedCallback) {
        this.currentUserRole = (currentUserRole != null && !currentUserRole.isBlank()) ? currentUserRole.trim().toUpperCase() : "ADMIN";
        this.currentUserId = (currentUserId != null && !currentUserId.isBlank()) ? currentUserId.trim() : "ADM001";
        this.isAdmin = "ADMIN".equalsIgnoreCase(this.currentUserRole);
        this.onNoticeChangedCallback = onNoticeChangedCallback;
        this.noticeService = new NoticeService();

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Top Section: Header + (Summary Cards if Admin)
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        topContainer.add(createHeaderPanel());
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

        JPanel rightCard = isAdmin ? createAdminFormCard() : createNoticeReaderCard();
        rightCard.setPreferredSize(new Dimension(isAdmin ? 370 : 400, 500));
        centerPanel.add(rightCard, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        // Setup custom column widths and renderers
        setupColumnRenderers();

        // Initial async data load
        loadNoticesAsync();
    }

    // SECTION A: PAGE HEADER

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel(isAdmin ? "NOTICE MANAGEMENT" : "FACULTY NOTICES");
        title.setFont(AdminDashboard.getModernFont(Font.BOLD, 20));
        title.setForeground(PRIMARY_NAVY);
        titlePanel.add(title);

        lblHeaderCount = new JLabel("0 Notices");
        lblHeaderCount.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        lblHeaderCount.setForeground(SECONDARY_BLUE);
        lblHeaderCount.setOpaque(true);
        lblHeaderCount.setBackground(LIGHT_BLUE);
        lblHeaderCount.setBorder(new EmptyBorder(3, 8, 3, 8));
        titlePanel.add(lblHeaderCount);

        JPanel leftContainer = new JPanel();
        leftContainer.setOpaque(false);
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.add(titlePanel);

        JLabel subtitle = new JLabel(isAdmin
                ? "Manage official faculty announcements, exam notices, and academic circulars."
                : "Official announcements, notices, and academic updates published by Faculty Administration.");
        subtitle.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(4, 10, 0, 0));
        leftContainer.add(subtitle);

        header.add(leftContainer, BorderLayout.WEST);

        // Right Actions (Refresh button)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnRefresh = new JButton("↻ Refresh");
        btnRefresh.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btnRefresh.setForeground(PRIMARY_NAVY);
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            loadNoticesAsync();
            if (onNoticeChangedCallback != null) {
                onNoticeChangedCallback.run();
            }
        });
        actionPanel.add(btnRefresh);

        header.add(actionPanel, BorderLayout.EAST);
        return header;
    }


    // SECTION B: SUMMARY CARDS ROW (ADMIN)

    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        lblTotalNotices = new JLabel("--");
        lblActiveNotices = new JLabel("--");
        lblInactiveNotices = new JLabel("--");
        lblLatestDate = new JLabel("--");

        row.add(createMetricCard("Total Notices", lblTotalNotices, "All Published", PRIMARY_NAVY, LIGHT_BLUE));
        row.add(createMetricCard("Active Notices", lblActiveNotices, "Visible to Users", SUCCESS_COLOR, new Color(220, 252, 231)));
        row.add(createMetricCard("Inactive / Archived", lblInactiveNotices, "Hidden from Users", WARNING_COLOR, new Color(254, 243, 199)));
        row.add(createMetricCard("Latest Published", lblLatestDate, "Most Recent Date", PURPLE_COLOR, new Color(243, 232, 255)));

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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
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

    // SECTION C: NOTICE TABLE CARD (MASTER VIEW)

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
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 1. Toolbar: Search + Filter + Status
        JPanel toolbar = createToolbar();
        card.add(toolbar, BorderLayout.NORTH);

        // 2. Table
        tableModel = new NoticeTableModel();
        sorter = new TableRowSorter<>(tableModel);
        noticeTable = new JTable(tableModel);
        noticeTable.setRowSorter(sorter);
        noticeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noticeTable.setRowHeight(38);
        noticeTable.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        noticeTable.getTableHeader().setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        noticeTable.getTableHeader().setBackground(new Color(248, 250, 252));
        noticeTable.getTableHeader().setForeground(TEXT_PRIMARY);
        noticeTable.getTableHeader().setPreferredSize(new Dimension(0, 34));
        noticeTable.getTableHeader().setReorderingAllowed(false);
        noticeTable.setShowGrid(false);
        noticeTable.setIntercellSpacing(new Dimension(0, 1));

        noticeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(noticeTable);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // 3. Table Footer Status
        lblTableStatus = new JLabel("Loading notices...");
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

        // Search Input
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(230, 34));
        txtSearch.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search title or notice content...");
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterNotices(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterNotices(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterNotices(); }
        });
        leftPanel.add(txtSearch);

        // Status Filter Dropdown (Only for Admin)
        if (isAdmin) {
            String[] statuses = {"All Status", "ACTIVE", "INACTIVE"};
            cmbStatusFilter = new JComboBox<>(statuses);
            cmbStatusFilter.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
            cmbStatusFilter.setBackground(Color.WHITE);
            cmbStatusFilter.setPreferredSize(new Dimension(115, 34));
            cmbStatusFilter.addActionListener(e -> filterNotices());
            leftPanel.add(cmbStatusFilter);
        }

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

    private void setupColumnRenderers() {
        // Col 0: Notice ID Pill Badge
        noticeTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        noticeTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
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

        // Col 1: Title
        noticeTable.getColumnModel().getColumn(1).setPreferredWidth(210);

        // Col 2: Date
        noticeTable.getColumnModel().getColumn(2).setPreferredWidth(95);

        // Col 3: Published By (Author)
        noticeTable.getColumnModel().getColumn(3).setPreferredWidth(130);

        // Col 4: Status Pill Badge
        noticeTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        noticeTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String status = (value != null) ? value.toString() : "ACTIVE";
                boolean active = "ACTIVE".equalsIgnoreCase(status);

                JLabel pill = new JLabel("  " + status + "  ");
                pill.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
                pill.setOpaque(true);
                pill.setBorder(new EmptyBorder(2, 8, 2, 8));

                if (active) {
                    pill.setBackground(new Color(220, 252, 231));
                    pill.setForeground(SUCCESS_COLOR);
                } else {
                    pill.setBackground(new Color(241, 245, 249));
                    pill.setForeground(TEXT_SECONDARY);
                }

                p.add(pill);
                return p;
            }
        });
    }

    // SECTION D: ADMIN FORM CARD (PUBLISH / EDIT / DELETE)

    private JPanel createAdminFormCard() {
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

        lblFormMode = new JLabel("PUBLISH NEW NOTICE");
        lblFormMode.setFont(AdminDashboard.getModernFont(Font.BOLD, 13));
        lblFormMode.setForeground(PRIMARY_NAVY);
        formHeader.add(lblFormMode, BorderLayout.WEST);

        btnClear = new JButton("Clear");
        btnClear.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        btnClear.setForeground(TEXT_SECONDARY);
        btnClear.setBackground(Color.WHITE);
        btnClear.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(4, 10, 4, 10)));
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearForm());
        formHeader.add(btnClear, BorderLayout.EAST);

        card.add(formHeader, BorderLayout.NORTH);

        // Form Fields (Scrollable)
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        // Field 1: Notice ID
        txtNoticeId = new JTextField();
        txtNoticeId.setEditable(false);
        txtNoticeId.setBackground(new Color(248, 250, 252));
        fieldsPanel.add(createFormField("Notice ID", txtNoticeId, "Auto-generated identifier (e.g. NOT004)"));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 2: Notice Title
        txtTitle = new JTextField();
        fieldsPanel.add(createFormField("Notice Title *", txtTitle, "Clear announcement title (max 200 chars)"));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 3: Notice Date & Status Row
        JPanel dateStatusRow = new JPanel(new GridLayout(1, 2, 8, 0));
        dateStatusRow.setOpaque(false);

        txtNoticeDate = new JTextField(LocalDate.now().toString());
        dateStatusRow.add(createSubFormField("Publish Date (YYYY-MM-DD) *", txtNoticeDate));

        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        cmbStatus.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        cmbStatus.setBackground(Color.WHITE);
        dateStatusRow.add(createSubFormField("Status", cmbStatus));

        fieldsPanel.add(dateStatusRow);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Field 4: Description / Body
        txtDescription = new JTextArea(7, 20);
        txtDescription.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(6, 8, 6, 8));

        lblCharCount = new JLabel("0 / 5000 characters");
        lblCharCount.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        lblCharCount.setForeground(TEXT_SECONDARY);

        txtDescription.getDocument().addDocumentListener(new DocumentListener() {
            private void updateCount() {
                lblCharCount.setText(txtDescription.getText().length() + " characters");
            }
            @Override public void insertUpdate(DocumentEvent e) { updateCount(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCount(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCount(); }
        });

        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        descScroll.setPreferredSize(new Dimension(300, 130));

        JPanel descField = new JPanel(new BorderLayout(0, 4));
        descField.setOpaque(false);
        JLabel lblDescTitle = new JLabel("Notice Content / Announcement Body *");
        lblDescTitle.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        lblDescTitle.setForeground(TEXT_PRIMARY);

        JPanel descBottom = new JPanel(new BorderLayout());
        descBottom.setOpaque(false);
        descBottom.add(lblCharCount, BorderLayout.EAST);

        descField.add(lblDescTitle, BorderLayout.NORTH);
        descField.add(descScroll, BorderLayout.CENTER);
        descField.add(descBottom, BorderLayout.SOUTH);
        fieldsPanel.add(descField);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Creator info banner
        lblCreatorInfo = new JLabel("Author: Administrator (" + currentUserId + ")");
        lblCreatorInfo.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        lblCreatorInfo.setForeground(TEXT_SECONDARY);
        fieldsPanel.add(lblCreatorInfo);

        JScrollPane formScroll = new JScrollPane(fieldsPanel);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        card.add(formScroll, BorderLayout.CENTER);

        // Action Buttons Row (Add, Update, Delete)
        JPanel actionRow = new JPanel(new GridLayout(1, 3, 8, 0));
        actionRow.setOpaque(false);

        btnAdd = new JButton("Publish");
        styleActionButton(btnAdd, SECONDARY_BLUE, Color.WHITE);
        btnAdd.addActionListener(e -> performAddNotice());
        actionRow.add(btnAdd);

        btnUpdate = new JButton("Update");
        styleActionButton(btnUpdate, SUCCESS_COLOR, Color.WHITE);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> performUpdateNotice());
        actionRow.add(btnUpdate);

        btnDelete = new JButton("Delete");
        styleActionButton(btnDelete, DANGER_COLOR, Color.WHITE);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> performDeleteNotice());
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

    // SECTION E: NOTICE READER CARD (FOR LECTURER, TECH OFFICER, UNDERGRADUATE)
    private JPanel createNoticeReaderCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
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
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Reader Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        lblReaderTitle = new JLabel("Select a Notice to View");
        lblReaderTitle.setFont(AdminDashboard.getModernFont(Font.BOLD, 15));
        lblReaderTitle.setForeground(PRIMARY_NAVY);
        lblReaderTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(lblReaderTitle);

        headerPanel.add(Box.createVerticalStrut(6));

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblReaderMeta = new JLabel("Published: -- | Author: --");
        lblReaderMeta.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        lblReaderMeta.setForeground(TEXT_SECONDARY);
        metaRow.add(lblReaderMeta);

        lblReaderStatus = new JLabel("  ACTIVE  ");
        lblReaderStatus.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
        lblReaderStatus.setOpaque(true);
        lblReaderStatus.setBackground(new Color(220, 252, 231));
        lblReaderStatus.setForeground(SUCCESS_COLOR);
        lblReaderStatus.setBorder(new EmptyBorder(2, 6, 2, 6));
        lblReaderStatus.setVisible(false);
        metaRow.add(lblReaderStatus);

        headerPanel.add(metaRow);
        headerPanel.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(sep);

        card.add(headerPanel, BorderLayout.NORTH);

        // Notice Body Area
        txtReaderBody = new JTextArea("Please select an academic notice from the list on the left to read its complete content.");
        txtReaderBody.setFont(AdminDashboard.getModernFont(Font.PLAIN, 13));
        txtReaderBody.setForeground(TEXT_PRIMARY);
        txtReaderBody.setLineWrap(true);
        txtReaderBody.setWrapStyleWord(true);
        txtReaderBody.setEditable(false);
        txtReaderBody.setOpaque(false);
        txtReaderBody.setBorder(new EmptyBorder(8, 4, 8, 4));

        JScrollPane readerScroll = new JScrollPane(txtReaderBody);
        readerScroll.setBorder(null);
        readerScroll.setOpaque(false);
        readerScroll.getViewport().setOpaque(false);
        card.add(readerScroll, BorderLayout.CENTER);

        // Footer info
        JLabel lblReaderFooter = new JLabel("Official Notice • University of Ruhuna");
        lblReaderFooter.setFont(AdminDashboard.getModernFont(Font.ITALIC, 10));
        lblReaderFooter.setForeground(new Color(148, 163, 184));
        card.add(lblReaderFooter, BorderLayout.SOUTH);

        return card;
    }

    // SECTION F: ASYNC DATA LOADING & REFRESH
    public void loadNoticesAsync() {
        btnRefresh.setEnabled(false);
        lblTableStatus.setText("Loading notices from database...");

        SwingWorker<List<Notice>, Void> worker = new SwingWorker<>() {
            private Map<String, Object> summaryCounts;

            @Override
            protected List<Notice> doInBackground() {
                if (isAdmin) {
                    summaryCounts = noticeService.getNoticeSummaryCounts();
                    return noticeService.getAllNotices();
                } else {
                    return noticeService.getActiveNotices();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Notice> notices = get();
                    tableModel.setNotices(notices);
                    filterNotices();

                    if (isAdmin && summaryCounts != null) {
                        updateSummaryCards(summaryCounts);
                    }

                    lblHeaderCount.setText(notices.size() + " Notices");
                    lblTableStatus.setText("Showing " + notices.size() + " published notices.");

                    if (isAdmin && selectedNotice == null) {
                        clearForm();
                    }
                } catch (Exception e) {
                    lblTableStatus.setText("Error loading notices: " + e.getMessage());
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
    public void loadNoticesSync() {
        List<Notice> notices = isAdmin ? noticeService.getAllNotices() : noticeService.getActiveNotices();
        tableModel.setNotices(notices);
        filterNotices();
        if (isAdmin) {
            updateSummaryCards(noticeService.getNoticeSummaryCounts());
            if (selectedNotice == null) {
                clearForm();
            }
        }
        lblHeaderCount.setText(notices.size() + " Notices");
        lblTableStatus.setText("Showing " + notices.size() + " published notices.");
    }

    private void updateSummaryCards(Map<String, Object> counts) {
        if (counts == null) return;
        lblTotalNotices.setText(String.valueOf(counts.getOrDefault("total", 0)));
        lblActiveNotices.setText(String.valueOf(counts.getOrDefault("active", 0)));
        lblInactiveNotices.setText(String.valueOf(counts.getOrDefault("inactive", 0)));
        lblLatestDate.setText(String.valueOf(counts.getOrDefault("latest_date", "None")));
    }

    private void filterNotices() {
        String keyword = txtSearch.getText().trim();
        String statusFilter = (cmbStatusFilter != null) ? (String) cmbStatusFilter.getSelectedItem() : null;

        List<RowFilter<NoticeTableModel, Integer>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword)));
        }

        if (isAdmin && statusFilter != null && !statusFilter.equalsIgnoreCase("All Status")) {
            filters.add(RowFilter.regexFilter("^" + statusFilter + "$", 4));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }

        int visible = noticeTable.getRowCount();
        int total = tableModel.getRowCount();
        lblTableStatus.setText("Showing " + visible + " of " + total + " notices.");
    }

    private void resetFilters() {
        txtSearch.setText("");
        if (cmbStatusFilter != null) {
            cmbStatusFilter.setSelectedIndex(0);
        }
        filterNotices();
    }

    private void onTableRowSelected() {
        int selectedRow = noticeTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int modelIdx = noticeTable.convertRowIndexToModel(selectedRow);
        selectedNotice = tableModel.getNoticeAt(modelIdx);
        if (selectedNotice == null) return;

        if (isAdmin) {
            // Populate Admin Form
            lblFormMode.setText("EDITING NOTICE [" + selectedNotice.getNoticeId() + "]");
            txtNoticeId.setText(selectedNotice.getNoticeId());
            txtTitle.setText(selectedNotice.getTitle());
            txtNoticeDate.setText(selectedNotice.getNoticeDate() != null ? selectedNotice.getNoticeDate().toString() : "");
            cmbStatus.setSelectedItem(selectedNotice.getStatus());
            txtDescription.setText(selectedNotice.getDescription());
            lblCreatorInfo.setText("Author: " + selectedNotice.getCreatorName() + " (" + selectedNotice.getCreatedBy() + ")");

            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
        } else {
            // Populate Reader View
            lblReaderTitle.setText("<html><b>" + selectedNotice.getTitle() + "</b></html>");
            lblReaderMeta.setText("Published: " + selectedNotice.getFormattedDate() + " | By: " + selectedNotice.getCreatorName());
            lblReaderStatus.setText("  " + selectedNotice.getStatus() + "  ");
            lblReaderStatus.setVisible(true);
            txtReaderBody.setText(selectedNotice.getDescription());
            txtReaderBody.setCaretPosition(0);
        }
    }

    private void clearForm() {
        selectedNotice = null;
        if (noticeTable.getSelectedRow() != -1) {
            noticeTable.clearSelection();
        }

        if (isAdmin) {
            lblFormMode.setText("PUBLISH NEW NOTICE");
            txtNoticeId.setText(noticeService.getNextSuggestedNoticeId());
            txtTitle.setText("");
            txtNoticeDate.setText(LocalDate.now().toString());
            cmbStatus.setSelectedItem("ACTIVE");
            txtDescription.setText("");
            lblCreatorInfo.setText("Author: Administrator (" + currentUserId + ")");

            btnAdd.setEnabled(true);
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
            txtTitle.requestFocusInWindow();
        } else {
            lblReaderTitle.setText("Select a Notice to View");
            lblReaderMeta.setText("Published: -- | Author: --");
            lblReaderStatus.setVisible(false);
            txtReaderBody.setText("Please select an academic notice from the list on the left to read its complete content.");
        }
    }

    // SECTION G: CRUD ACTION HANDLERS (ADMIN ONLY)

    private void performAddNotice() {
        if (!isAdmin) {
            showError("Permission Denied: Only administrators can publish notices.");
            return;
        }

        String id = txtNoticeId.getText().trim();
        String title = txtTitle.getText().trim();
        String dateStr = txtNoticeDate.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        String desc = txtDescription.getText().trim();

        Date noticeDate;
        try {
            noticeDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            showError("Invalid date format. Please use YYYY-MM-DD (e.g. 2026-03-01).");
            txtNoticeDate.requestFocusInWindow();
            return;
        }

        Notice notice = new Notice(id, title, desc, noticeDate, currentUserId, status);
        OperationResult res = noticeService.addNotice(notice, currentUserRole);

        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Notice Published", JOptionPane.INFORMATION_MESSAGE);
            loadNoticesAsync();
            if (onNoticeChangedCallback != null) {
                onNoticeChangedCallback.run();
            }
        } else {
            showError(res.getMessage());
        }
    }

    private void performUpdateNotice() {
        if (!isAdmin) {
            showError("Permission Denied: Only administrators can edit notices.");
            return;
        }

        if (selectedNotice == null) {
            showError("Please select a notice to update.");
            return;
        }

        String title = txtTitle.getText().trim();
        String dateStr = txtNoticeDate.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        String desc = txtDescription.getText().trim();

        Date noticeDate;
        try {
            noticeDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            showError("Invalid date format. Please use YYYY-MM-DD.");
            txtNoticeDate.requestFocusInWindow();
            return;
        }

        selectedNotice.setTitle(title);
        selectedNotice.setNoticeDate(noticeDate);
        selectedNotice.setStatus(status);
        selectedNotice.setDescription(desc);

        OperationResult res = noticeService.updateNotice(selectedNotice, currentUserRole);

        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Notice Updated", JOptionPane.INFORMATION_MESSAGE);
            loadNoticesAsync();
            if (onNoticeChangedCallback != null) {
                onNoticeChangedCallback.run();
            }
        } else {
            showError(res.getMessage());
        }
    }

    private void performDeleteNotice() {
        if (!isAdmin) {
            showError("Permission Denied: Only administrators can delete notices.");
            return;
        }

        if (selectedNotice == null) {
            showError("Please select a notice to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "<html>Are you sure you want to delete notice <b>" + selectedNotice.getNoticeId() + "</b>?<br>" +
                        "<i>\"" + selectedNotice.getTitle() + "\"</i><br><br>" +
                        "<font color='#DC2626'>This action cannot be undone.</font></html>",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            OperationResult res = noticeService.deleteNotice(selectedNotice.getNoticeId(), currentUserRole);
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Notice Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadNoticesAsync();
                if (onNoticeChangedCallback != null) {
                    onNoticeChangedCallback.run();
                }
            } else {
                showError(res.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice Management Error", JOptionPane.ERROR_MESSAGE);
    }

    // TABLE MODEL CLASS
    public static class NoticeTableModel extends AbstractTableModel {
        private final String[] columns = {"Notice ID", "Title", "Date", "Published By", "Status"};
        private List<Notice> notices = new ArrayList<>();

        public void setNotices(List<Notice> notices) {
            this.notices = (notices != null) ? notices : new ArrayList<>();
            fireTableDataChanged();
        }

        public Notice getNoticeAt(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < notices.size()) {
                return notices.get(rowIndex);
            }
            return null;
        }

        @Override public int getRowCount() { return notices.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Notice n = notices.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> n.getNoticeId();
                case 1 -> n.getTitle();
                case 2 -> n.getFormattedDate();
                case 3 -> n.getCreatorName();
                case 4 -> n.getStatus();
                default -> "";
            };
        }
    }
}
