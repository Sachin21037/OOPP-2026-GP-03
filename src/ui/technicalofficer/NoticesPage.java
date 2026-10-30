package ui.technicalofficer;

import model.Notice;
import service.NoticeService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Department Notices & Announcements Page for Technical Officers.
 * Allows Technical Officers to view notices published for their department,
 * inspect urgent alerts, download attachments, distinguish unread from read
 * notices,
 * and filter active versus archived notices.
 * Strictly view-only (no publish, edit, or delete operations).
 */
public class NoticesPage extends JPanel {

    // Theme palette harmonized with FAMS Technical Officer Dashboard
    private final Color darkNavy = new Color(27, 66, 104); // #1B4268
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color lightBg = new Color(245, 247, 250); // #F5F7FA
    private final Color borderColor = new Color(226, 232, 240); // #E2E8F0
    private final Color textDark = new Color(30, 41, 59); // #1E293B
    private final Color textMuted = new Color(100, 116, 139); // #64748B
    private final Color activeGreen = new Color(22, 163, 74); // #16A34A
    private final Color urgentRed = new Color(220, 38, 38); // #DC2626
    private final Color unreadBlue = new Color(37, 99, 235); // #2563EB

    private final NoticeService noticeService = new NoticeService();
    private String officerDepartment = "Information and Communication Technology";

    // UI Filter Controls
    private JTextField searchField;
    private JComboBox<String> deptFilter;
    private JComboBox<String> statusFilter;
    private JButton readNoticeBtn;

    // View Components
    private CardLayout viewCards;
    private JPanel viewsContainer;
    private JTable noticeTable;
    private DefaultTableModel tableModel;
    private JPanel emptyStatePanel;

    // Stats Badges
    private JLabel totalNoticesBadge;
    private JLabel activeNoticesBadge;
    private JLabel unreadNoticesBadge;

    // Cross-module navigation callback
    private Runnable onNavigateToTimetable;

    public void setOnNavigateToTimetable(Runnable onNavigateToTimetable) {
        this.onNavigateToTimetable = onNavigateToTimetable;
    }

    public NoticesPage() {
        this("Information and Communication Technology");
    }

    public NoticesPage(String department) {
        if (department != null && !department.isBlank()) {
            this.officerDepartment = department;
        }
        setLayout(new BorderLayout());
        setBackground(lightBg);

        initUI();
        refreshNotices();
    }

    public void setOfficerDepartment(String department) {
        if (department != null && !department.isBlank() && !department.equalsIgnoreCase(this.officerDepartment)) {
            this.officerDepartment = department;
            refreshNotices();
        }
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 16));
        mainContainer.setBackground(lightBg);
        mainContainer.setBorder(new EmptyBorder(22, 28, 24, 28));

        // 1. Top Header Banner
        JPanel headerPanel = createHeaderPanel();

        // 2. Filter Bar (Search + Status + Department + Action Buttons)
        JPanel filterCard = createFilterBar();

        // 3. Central Views: Table View vs Empty State
        viewsContainer = new JPanel();
        viewCards = new CardLayout();
        viewsContainer.setLayout(viewCards);
        viewsContainer.setBackground(lightBg);

        JPanel tableCard = createTableCard();
        emptyStatePanel = createEmptyStatePanel();

        viewsContainer.add(tableCard, "TABLE");
        viewsContainer.add(emptyStatePanel, "EMPTY");

        // Assemble Central Area
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setBackground(lightBg);
        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(viewsContainer, BorderLayout.CENTER);

        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(centerPanel, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        // Top Row: Title & Subtitle on LEFT, Actions on RIGHT
        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Faculty & Department Notices");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel(
                "Official department notices, examination circulars, and administrative directives for "
                        + officerDepartment);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitle);

        // Actions Row (Right) - Black letters
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);

        JButton markAllReadBtn = new JButton("Mark All Read");
        markAllReadBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        markAllReadBtn.setForeground(Color.BLACK);
        markAllReadBtn.setBackground(Color.WHITE);
        markAllReadBtn.setFocusPainted(false);
        markAllReadBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(7, 14, 7, 14)));
        markAllReadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markAllReadBtn.addActionListener(e -> markAllNoticesAsRead());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.BLACK);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(7, 14, 7, 14)));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshNotices());

        JButton viewTimetableBtn = new JButton("Academic Timetable");
        viewTimetableBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewTimetableBtn.setForeground(Color.BLACK);
        viewTimetableBtn.setBackground(new Color(230, 242, 255));
        viewTimetableBtn.setFocusPainted(false);
        viewTimetableBtn.setBorder(new CompoundBorder(new LineBorder(primaryBlue, 1), new EmptyBorder(7, 14, 7, 14)));
        viewTimetableBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewTimetableBtn.setToolTipText("View Department Timetable & Lab Sessions");
        viewTimetableBtn.addActionListener(e -> {
            if (onNavigateToTimetable != null) {
                onNavigateToTimetable.run();
            }
        });

        actionsPanel.add(markAllReadBtn);
        actionsPanel.add(refreshBtn);
        actionsPanel.add(viewTimetableBtn);

        topRow.add(titleBox, BorderLayout.WEST);
        topRow.add(actionsPanel, BorderLayout.EAST);

        // Bottom Row: Badges
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statsRow.setOpaque(false);

        totalNoticesBadge = createStatBadge("Total: 0", new Color(241, 245, 249), Color.BLACK);
        activeNoticesBadge = createStatBadge("Active: 0", new Color(220, 252, 231), Color.BLACK);
        unreadNoticesBadge = createStatBadge("Unread: 0", new Color(219, 234, 254), Color.BLACK);

        statsRow.add(totalNoticesBadge);
        statsRow.add(activeNoticesBadge);
        statsRow.add(unreadNoticesBadge);

        header.add(topRow, BorderLayout.NORTH);
        header.add(statsRow, BorderLayout.SOUTH);

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

    private JPanel createFilterBar() {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 16, 12, 16)));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        // 1. Department Scope Filter
        leftFilters.add(createFilterLabel("Scope:"));
        deptFilter = new JComboBox<>(new String[] { "My Department (" + officerDepartment + ")", "All Departments" });
        deptFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deptFilter.setBackground(Color.WHITE);
        deptFilter.setPreferredSize(new Dimension(240, 32));
        deptFilter.addActionListener(e -> applyFilters());
        leftFilters.add(deptFilter);

        // 2. Status / Expiry Filter (Hides expired by default)
        leftFilters.add(createFilterLabel("Status:"));
        statusFilter = new JComboBox<>(
                new String[] { "Active Notices (Hide Expired)", "Archived / Expired Notices", "All Notices" });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setPreferredSize(new Dimension(200, 32));
        statusFilter.addActionListener(e -> applyFilters());
        leftFilters.add(statusFilter);

        // 3. Search Box
        leftFilters.add(createFilterLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText",
                "Search notices by title, publisher, or document...");
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(4, 8, 4, 8)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });
        leftFilters.add(searchField);

        // Right Action: Read Full Notice Button
        readNoticeBtn = new JButton("Read Full Notice");
        readNoticeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        readNoticeBtn.setForeground(Color.WHITE);
        readNoticeBtn.setBackground(darkNavy);
        readNoticeBtn.setFocusPainted(false);
        readNoticeBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        readNoticeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        readNoticeBtn.setEnabled(false);
        readNoticeBtn.addActionListener(e -> openSelectedNotice());

        card.add(leftFilters, BorderLayout.WEST);
        card.add(readNoticeBtn, BorderLayout.EAST);

        return card;
    }

    private JLabel createFilterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(textMuted);
        return l;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(14, 16, 14, 16)));

        String[] columns = { "Indicator", "Notice ID", "Notice Title", "Department", "Published Date", "Expiry Date",
                "Published By", "Attachment", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        noticeTable = new JTable(tableModel);
        noticeTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noticeTable.setRowHeight(34);
        noticeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noticeTable.setShowGrid(true);
        noticeTable.setShowHorizontalLines(true);
        noticeTable.setShowVerticalLines(true);
        noticeTable.setGridColor(borderColor);
        noticeTable.setSelectionBackground(new Color(237, 243, 248));
        noticeTable.setSelectionForeground(darkNavy);
        noticeTable.setDefaultRenderer(Object.class, new AlternateRowRenderer());

        // Header Styling matching FAMS Dark Navy Blue standard
        setupTableHeader(noticeTable);

        // Column widths
        noticeTable.getColumnModel().getColumn(0).setPreferredWidth(95); // Indicator (NEW / URGENT)
        noticeTable.getColumnModel().getColumn(1).setPreferredWidth(80); // Notice ID
        noticeTable.getColumnModel().getColumn(2).setPreferredWidth(270); // Title
        noticeTable.getColumnModel().getColumn(3).setPreferredWidth(170); // Department
        noticeTable.getColumnModel().getColumn(4).setPreferredWidth(105); // Published Date
        noticeTable.getColumnModel().getColumn(5).setPreferredWidth(105); // Expiry Date
        noticeTable.getColumnModel().getColumn(6).setPreferredWidth(140); // Published By
        noticeTable.getColumnModel().getColumn(7).setPreferredWidth(130); // Attachment
        noticeTable.getColumnModel().getColumn(8).setPreferredWidth(90); // Status

        // Column 0 Renderer: Priority & Unread Badge
        noticeTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                String indicator = (value != null) ? value.toString() : "";
                JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
                Color rowBg = isSelected ? new Color(237, 243, 248) : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252));
                pillPanel.setBackground(rowBg);

                Color bg;
                Color fg;
                if (indicator.contains("URGENT")) {
                    bg = new Color(254, 226, 226); // Light red
                    fg = urgentRed;
                } else if (indicator.contains("NEW")) {
                    bg = new Color(219, 234, 254); // Light blue
                    fg = unreadBlue;
                } else {
                    bg = new Color(241, 245, 249); // Muted slate
                    fg = new Color(148, 163, 184);
                }

                JLabel badge = new JLabel(indicator, SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setForeground(fg);
                badge.setOpaque(false);
                badge.setBorder(new EmptyBorder(2, 6, 2, 6));
                pillPanel.add(badge);
                return pillPanel;
            }
        });

        // Column 2 Renderer: Bold Title for Unread Notices
        noticeTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                Color rowBg = isSelected ? new Color(237, 243, 248) : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252));
                lbl.setBackground(rowBg);
                String indicator = (String) table.getValueAt(row, 0);
                if (indicator != null && (indicator.contains("NEW") || indicator.contains("URGENT"))) {
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setForeground(textDark);
                } else {
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    lbl.setForeground(new Color(71, 85, 105));
                }
                lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        });

        // Column 7 Renderer: Attachment Icon & Name
        noticeTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                Color rowBg = isSelected ? new Color(237, 243, 248) : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252));
                lbl.setBackground(rowBg);
                String val = (value != null) ? value.toString() : "--";
                if (!val.equals("--")) {
                    lbl.setText(val);
                    lbl.setForeground(primaryBlue);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else {
                    lbl.setText("--");
                    lbl.setForeground(new Color(156, 163, 175));
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
                lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        });

        // Column 8 Renderer: Status (Active vs Expired)
        noticeTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                Color rowBg = isSelected ? new Color(237, 243, 248) : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252));
                lbl.setBackground(rowBg);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String st = (value != null) ? value.toString() : "ACTIVE";
                if ("ACTIVE".equalsIgnoreCase(st)) {
                    lbl.setForeground(activeGreen);
                } else {
                    lbl.setForeground(new Color(156, 163, 175));
                }
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return lbl;
            }
        });

        // Selection listener
        noticeTable.getSelectionModel().addListSelectionListener(e -> {
            readNoticeBtn.setEnabled(noticeTable.getSelectedRow() >= 0);
        });

        // Double-click to open
        noticeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && noticeTable.getSelectedRow() >= 0) {
                    openSelectedNotice();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(noticeTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Footer Action Panel
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel tip = new JLabel(
                "Tip: Double-click any notice row to open full content and download available attachments.");
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tip.setForeground(textMuted);

        footer.add(tip, BorderLayout.WEST);

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

        JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("No Department Notices Found", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textDark);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(
                "<html><center>There are currently no published notices matching your search or status criteria.<br>You can switch the filter to 'All Notices' to view archived announcements.</center></html>",
                SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(textMuted);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton showAllBtn = new JButton("Show All Notices");
        showAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        showAllBtn.setBackground(darkNavy);
        showAllBtn.setForeground(Color.WHITE);
        showAllBtn.setFocusPainted(false);
        showAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showAllBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        showAllBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        showAllBtn.addActionListener(e -> {
            if (statusFilter != null)
                statusFilter.setSelectedIndex(2);
            if (searchField != null)
                searchField.setText("");
            applyFilters();
        });

        inner.add(iconLabel);
        inner.add(Box.createVerticalStrut(14));
        inner.add(titleLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(descLabel);
        inner.add(Box.createVerticalStrut(18));
        inner.add(showAllBtn);

        emptyPanel.add(inner);
        return emptyPanel;
    }

    public void refreshNotices() {
        applyFilters();
    }

    private void markAllNoticesAsRead() {
        List<Notice> notices = noticeService.getAllNotices();
        for (Notice n : notices) {
            noticeService.markNoticeAsRead(n.getNoticeId());
        }
        applyFilters();
    }

    private void applyFilters() {
        if (viewsContainer == null)
            return;

        String kw = (searchField != null) ? searchField.getText().trim() : "";
        String selectedStatus = (statusFilter != null && statusFilter.getSelectedItem() != null)
                ? statusFilter.getSelectedItem().toString()
                : "Active Notices (Hide Expired)";

        String statusMode;
        if (selectedStatus.startsWith("Active")) {
            statusMode = "ACTIVE";
        } else if (selectedStatus.startsWith("Archived")) {
            statusMode = "ARCHIVED";
        } else {
            statusMode = "ALL";
        }

        String selectedScope = (deptFilter != null && deptFilter.getSelectedItem() != null)
                ? deptFilter.getSelectedItem().toString()
                : "My Department (" + officerDepartment + ")";

        String targetDept = selectedScope.startsWith("My Department") ? officerDepartment : null;

        List<Notice> notices = noticeService.searchNotices(kw, targetDept, statusMode);

        // Update Stat Badges
        int total = notices.size();
        int active = 0;
        int unread = 0;
        for (Notice n : notices) {
            if (n.isActive())
                active++;
            if (!n.isRead())
                unread++;
        }

        if (totalNoticesBadge != null)
            totalNoticesBadge.setText("Total: " + total);
        if (activeNoticesBadge != null)
            activeNoticesBadge.setText("Active: " + active);
        if (unreadNoticesBadge != null)
            unreadNoticesBadge.setText("Unread: " + unread);

        if (notices.isEmpty()) {
            viewCards.show(viewsContainer, "EMPTY");
            return;
        }

        tableModel.setRowCount(0);
        for (Notice n : notices) {
            String indicator;
            if (n.isUrgent()) {
                indicator = "URGENT";
            } else if (!n.isRead()) {
                indicator = "NEW";
            } else {
                indicator = "READ";
            }

            tableModel.addRow(new Object[] {
                    indicator,
                    n.getNoticeId(),
                    n.getTitle(),
                    n.getTargetDepartment(),
                    n.getFormattedDate(),
                    n.getFormattedExpiryDate(),
                    n.getCreatorName(),
                    n.hasAttachment() ? n.getAttachmentName() : "--",
                    n.isActive() ? "ACTIVE" : "EXPIRED"
            });
        }

        viewCards.show(viewsContainer, "TABLE");
    }

    public void openNoticeById(String noticeId) {
        Notice notice = noticeService.getNoticeById(noticeId);
        if (notice != null) {
            showNoticeModal(notice);
        }
    }

    private void openSelectedNotice() {
        int row = noticeTable.getSelectedRow();
        if (row < 0)
            return;

        String noticeId = (String) tableModel.getValueAt(row, 1);
        Notice notice = noticeService.getNoticeById(noticeId);
        if (notice != null) {
            showNoticeModal(notice);
        }
    }

    /**
     * Shows a modal reader dialog for the notice, marking it as read and
     * providing full document preview and attachment download capability.
     */
    private void showNoticeModal(Notice notice) {
        // Mark as read immediately
        noticeService.markNoticeAsRead(notice.getNoticeId());
        notice.setRead(true);
        applyFilters();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Notice - " + notice.getNoticeId(),
                true);
        dialog.setSize(620, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(24, 26, 22, 26));

        // Header Section
        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLbl = new JLabel("<html><b>" + notice.getTitle() + "</b></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(textDark);

        JPanel badgesBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgesBox.setOpaque(false);

        if (notice.isUrgent()) {
            JLabel urgBadge = new JLabel("URGENT", SwingConstants.CENTER);
            urgBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            urgBadge.setForeground(urgentRed);
            urgBadge.setBackground(new Color(254, 226, 226));
            urgBadge.setOpaque(true);
            urgBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
            badgesBox.add(urgBadge);
        }

        JLabel statusBadge = new JLabel(notice.isActive() ? "ACTIVE" : "EXPIRED", SwingConstants.CENTER);
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(notice.isActive() ? activeGreen : new Color(156, 163, 175));
        statusBadge.setBackground(notice.isActive() ? new Color(220, 252, 231) : new Color(241, 245, 249));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
        badgesBox.add(statusBadge);

        titleRow.add(titleLbl, BorderLayout.CENTER);
        titleRow.add(badgesBox, BorderLayout.EAST);

        // Metadata grid
        JPanel metaGrid = new JPanel(new GridLayout(2, 2, 12, 6));
        metaGrid.setOpaque(false);
        metaGrid.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(10, 12, 10, 12)));

        metaGrid.add(createMetaLabel("Published Date:", notice.getFormattedDate()));
        metaGrid.add(createMetaLabel("Expiry Date:", notice.getFormattedExpiryDate()));
        metaGrid.add(createMetaLabel("Publisher:", notice.getCreatorName() + " (" + notice.getCreatedBy() + ")"));
        metaGrid.add(createMetaLabel("Target Department:", notice.getTargetDepartment()));

        top.add(titleRow, BorderLayout.NORTH);
        top.add(metaGrid, BorderLayout.CENTER);

        // Content Body
        JTextArea descArea = new JTextArea(notice.getDescription());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setForeground(new Color(51, 65, 85));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setMargin(new Insets(14, 14, 14, 14));
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(new LineBorder(borderColor, 1));

        JScrollPane scroll = new JScrollPane(descArea);
        scroll.setBorder(null);

        // Attachment Download Card
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);
        centerContainer.add(scroll, BorderLayout.CENTER);

        if (notice.hasAttachment()) {
            JPanel attachCard = new JPanel(new BorderLayout(12, 0));
            attachCard.setBackground(new Color(239, 246, 255)); // Soft blue bg
            attachCard.setBorder(
                    new CompoundBorder(new LineBorder(new Color(191, 219, 254), 1), new EmptyBorder(10, 14, 10, 14)));

            JLabel attachIcon = new JLabel("[FILE]");
            attachIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
            attachIcon.setForeground(primaryBlue);

            JPanel attachInfo = new JPanel(new GridLayout(2, 1, 0, 2));
            attachInfo.setOpaque(false);
            JLabel attachName = new JLabel(notice.getAttachmentName());
            attachName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            attachName.setForeground(darkNavy);

            JLabel attachSize = new JLabel("Official Department Document • Ready for offline inspection");
            attachSize.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            attachSize.setForeground(textMuted);

            attachInfo.add(attachName);
            attachInfo.add(attachSize);

            JButton downloadBtn = new JButton("Download Document");
            downloadBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            downloadBtn.setBackground(darkNavy);
            downloadBtn.setForeground(Color.WHITE);
            downloadBtn.setFocusPainted(false);
            downloadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            downloadBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
            downloadBtn.addActionListener(e -> downloadAttachment(notice, dialog));

            attachCard.add(attachIcon, BorderLayout.WEST);
            attachCard.add(attachInfo, BorderLayout.CENTER);
            attachCard.add(downloadBtn, BorderLayout.EAST);

            centerContainer.add(attachCard, BorderLayout.SOUTH);
        }

        // Close & Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        if (notice.getTitle().toLowerCase().contains("timetable")
                || notice.getDescription().toLowerCase().contains("timetable")
                || (notice.getAttachmentName() != null
                        && notice.getAttachmentName().toLowerCase().contains("timetable"))) {
            JButton timetableJumpBtn = new JButton("Open Timetable View");
            timetableJumpBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            timetableJumpBtn.setBackground(darkNavy);
            timetableJumpBtn.setForeground(Color.WHITE);
            timetableJumpBtn.setFocusPainted(false);
            timetableJumpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            timetableJumpBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
            timetableJumpBtn.addActionListener(e -> {
                dialog.dispose();
                if (onNavigateToTimetable != null) {
                    onNavigateToTimetable.run();
                }
            });
            btnPanel.add(timetableJumpBtn);
        }

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(new Color(241, 245, 249));
        closeBtn.setForeground(textDark);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        closeBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(closeBtn);

        p.add(top, BorderLayout.NORTH);
        p.add(centerContainer, BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    private void downloadAttachment(Notice notice, JDialog parentDialog) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(notice.getAttachmentName()));
        int result = chooser.showSaveDialog(parentDialog);
        if (result == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(dest)) {
                writer.write("============================================================\n");
                writer.write("FACULTY ACADEMIC MANAGEMENT SYSTEM (FAMS)\n");
                writer.write("DEPARTMENT NOTICE ATTACHMENT\n");
                writer.write("============================================================\n\n");
                writer.write("Notice ID: " + notice.getNoticeId() + "\n");
                writer.write("Notice Title: " + notice.getTitle() + "\n");
                writer.write("Target Department: " + notice.getTargetDepartment() + "\n");
                writer.write("Publish Date: " + notice.getFormattedDate() + "\n");
                writer.write("Expiry Date: " + notice.getFormattedExpiryDate() + "\n");
                writer.write("Published By: " + notice.getCreatorName() + " (" + notice.getCreatedBy() + ")\n\n");
                writer.write("OFFICIAL NOTICE TEXT:\n");
                writer.write(notice.getDescription() + "\n\n");
                writer.write("============================================================\n");
                writer.write("Attached File: " + notice.getAttachmentName() + "\n");
                writer.write("Verification Hash: FAMS-" + Integer.toHexString(notice.hashCode()) + "\n");
                writer.write("Downloaded successfully for Technical Officer offline operations.\n");

                JOptionPane.showMessageDialog(parentDialog,
                        "Document '" + notice.getAttachmentName() + "' downloaded successfully to:\n"
                                + dest.getAbsolutePath(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentDialog,
                        "Failed to save attachment: " + ex.getMessage(),
                        "Download Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createMetaLabel(String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(textMuted);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        v.setForeground(textDark);

        p.add(l);
        p.add(v);
        return p;
    }

    private void setupTableHeader(JTable table) {
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
                if (column == 1 || column == 4 || column == 5 || column == 8) {
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
