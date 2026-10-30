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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Faculty & Department Notices Page for Technical Officers.
 * Displays academic notices directly from the MySQL database schema (notices table).
 * View-only: allows reading announcements, searching, and filtering by status.
 */
public class NoticesPage extends JPanel {

    // Theme palette harmonized with FAMS Technical Officer Dashboard
    private final Color darkNavy = new Color(27, 67, 103);       // #1B4367 exact navy
    private final Color lightBg = new Color(245, 247, 250);       // #F5F7FA
    private final Color borderColor = new Color(226, 232, 240);   // #E2E8F0
    private final Color textDark = new Color(30, 41, 59);         // #1E293B
    private final Color textMuted = new Color(100, 116, 139);     // #64748B
    private final Color activeGreen = new Color(22, 163, 74);     // #16A34A
    private final Color inactiveGray = new Color(156, 163, 175);  // #9CA3AF

    private final NoticeService noticeService = new NoticeService();
    private String officerDepartment = "Information and Communication Technology";

    // UI Filter Controls
    private JTextField searchField;
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
    private JLabel inactiveNoticesBadge;

    // Navigation callback
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

    public void setOfficerDepartment(String dept) {
        if (dept != null && !dept.isBlank()) {
            this.officerDepartment = dept;
            refreshNotices();
        }
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 16));
        mainContainer.setBackground(lightBg);
        mainContainer.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header Panel
        JPanel headerPanel = createHeaderPanel();

        // 2. Filter Bar
        JPanel filterCard = createFilterBar();

        // 3. Central Views Container (Table vs Empty State)
        viewsContainer = new JPanel();
        viewCards = new CardLayout();
        viewsContainer.setLayout(viewCards);
        viewsContainer.setBackground(lightBg);

        JPanel tableCard = createTableCard();
        emptyStatePanel = createEmptyStatePanel();

        viewsContainer.add(tableCard, "TABLE");
        viewsContainer.add(emptyStatePanel, "EMPTY");

        // Assemble Layout
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

        // Top Row: Title + Action Buttons
        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Faculty & Department Notices");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Official department notices, examination circulars, and administrative directives");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitle);

        // Right Action Buttons (Black text on buttons)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);

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
        viewTimetableBtn.setBackground(Color.WHITE);
        viewTimetableBtn.setFocusPainted(false);
        viewTimetableBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(7, 14, 7, 14)));
        viewTimetableBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewTimetableBtn.addActionListener(e -> {
            if (onNavigateToTimetable != null) {
                onNavigateToTimetable.run();
            }
        });

        actionsPanel.add(refreshBtn);
        actionsPanel.add(viewTimetableBtn);

        topRow.add(titleBox, BorderLayout.WEST);
        topRow.add(actionsPanel, BorderLayout.EAST);

        // Bottom Row: Summary Badges
        JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        badgesRow.setOpaque(false);

        totalNoticesBadge = createPillBadge("Total: 0", textDark, new Color(241, 245, 249));
        activeNoticesBadge = createPillBadge("Active: 0", activeGreen, new Color(220, 252, 231));
        inactiveNoticesBadge = createPillBadge("Inactive: 0", inactiveGray, new Color(243, 244, 246));

        badgesRow.add(totalNoticesBadge);
        badgesRow.add(activeNoticesBadge);
        badgesRow.add(inactiveNoticesBadge);

        header.add(topRow, BorderLayout.NORTH);
        header.add(badgesRow, BorderLayout.SOUTH);

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

    private JPanel createFilterBar() {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(12, 16, 12, 16)));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilters.setOpaque(false);

        // Status Filter
        leftFilters.add(createFilterLabel("Status:"));
        statusFilter = new JComboBox<>(new String[] { "Active Notices", "Inactive Notices", "All Notices" });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setPreferredSize(new Dimension(160, 32));
        statusFilter.addActionListener(e -> applyFilters());
        leftFilters.add(statusFilter);

        // Search Field
        leftFilters.add(createFilterLabel("Search:"));
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search notices by ID, title, or publisher...");
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

        // Columns strictly reflecting MySQL 'notices' table schema
        String[] columns = { "#", "Notice ID", "Notice Title", "Notice Date", "Published By", "Status" };
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
        noticeTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // #
        noticeTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Notice ID
        noticeTable.getColumnModel().getColumn(2).setPreferredWidth(450);  // Notice Title
        noticeTable.getColumnModel().getColumn(3).setPreferredWidth(130);  // Notice Date
        noticeTable.getColumnModel().getColumn(4).setPreferredWidth(180);  // Published By
        noticeTable.getColumnModel().getColumn(5).setPreferredWidth(110);  // Status

        // Column 0 Center (#)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        noticeTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Column 1 Center (Notice ID)
        noticeTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(darkNavy);
                return lbl;
            }
        });

        // Column 5 Renderer: Status
        noticeTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String st = (value != null) ? value.toString() : "ACTIVE";
                if ("ACTIVE".equalsIgnoreCase(st)) {
                    lbl.setForeground(activeGreen);
                } else {
                    lbl.setForeground(inactiveGray);
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

        JLabel tip = new JLabel("Tip: Double-click any notice row to open and read the full announcement.");
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

        JLabel titleLabel = new JLabel("No Notices Found", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textDark);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(
                "<html><center>There are currently no published notices matching your search or status criteria.<br>You can switch the filter to 'All Notices' to view inactive notices.</center></html>",
                SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(textMuted);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resetBtn = new JButton("Reset Filters");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(8, 16, 8, 16)));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> {
            if (searchField != null) searchField.setText("");
            if (statusFilter != null) statusFilter.setSelectedIndex(0);
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

    public void refreshNotices() {
        applyFilters();
    }

    private void applyFilters() {
        if (viewsContainer == null) return;

        String kw = (searchField != null) ? searchField.getText().trim() : "";
        String selectedStatus = (statusFilter != null && statusFilter.getSelectedItem() != null)
                ? statusFilter.getSelectedItem().toString()
                : "Active Notices";

        String statusMode;
        if (selectedStatus.startsWith("Active")) {
            statusMode = "ACTIVE";
        } else if (selectedStatus.startsWith("Inactive")) {
            statusMode = "INACTIVE";
        } else {
            statusMode = "ALL";
        }

        List<Notice> notices = noticeService.searchNotices(kw, null, statusMode);

        // Update Stat Badges
        List<Notice> all = noticeService.getAllNotices();
        int total = all.size();
        int active = 0;
        int inactive = 0;
        for (Notice n : all) {
            if ("ACTIVE".equalsIgnoreCase(n.getStatus())) {
                active++;
            } else {
                inactive++;
            }
        }

        if (totalNoticesBadge != null) totalNoticesBadge.setText("Total: " + total);
        if (activeNoticesBadge != null) activeNoticesBadge.setText("Active: " + active);
        if (inactiveNoticesBadge != null) inactiveNoticesBadge.setText("Inactive: " + inactive);

        if (notices.isEmpty()) {
            viewCards.show(viewsContainer, "EMPTY");
            return;
        }

        tableModel.setRowCount(0);
        int index = 1;
        for (Notice n : notices) {
            tableModel.addRow(new Object[] {
                    index++,
                    n.getNoticeId(),
                    n.getTitle(),
                    n.getFormattedDate(),
                    n.getCreatorName(),
                    n.getStatus()
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
        if (row < 0) return;

        String noticeId = tableModel.getValueAt(row, 1).toString();
        openNoticeById(noticeId);
    }

    /**
     * Modal dialog displaying the full notice details strictly from the database schema.
     */
    private void showNoticeModal(Notice notice) {
        noticeService.markNoticeAsRead(notice.getNoticeId());

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Notice Announcement - " + notice.getNoticeId(), true);
        dialog.setSize(580, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(24, 26, 24, 26));

        // Header Panel: Notice ID + Title + Status Pill
        JPanel modalHeader = new JPanel(new BorderLayout(12, 0));
        modalHeader.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel idLabel = new JLabel(notice.getNoticeId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        idLabel.setForeground(darkNavy);

        JLabel titleLabel = new JLabel("<html><body style='width: 380px;'>" + notice.getTitle() + "</body></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(textDark);

        titleBox.add(idLabel);
        titleBox.add(titleLabel);

        JLabel statusBadge = new JLabel(notice.getStatus(), SwingConstants.CENTER);
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground("ACTIVE".equalsIgnoreCase(notice.getStatus()) ? activeGreen : inactiveGray);
        statusBadge.setOpaque(true);
        statusBadge.setBackground("ACTIVE".equalsIgnoreCase(notice.getStatus()) ? new Color(220, 252, 231) : new Color(243, 244, 246));
        statusBadge.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(4, 12, 4, 12)));

        modalHeader.add(titleBox, BorderLayout.CENTER);
        modalHeader.add(statusBadge, BorderLayout.EAST);

        // Metadata Card (Published Date, Published By)
        JPanel metaCard = new JPanel(new GridLayout(1, 2, 16, 0));
        metaCard.setBackground(new Color(248, 250, 252));
        metaCard.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(10, 14, 10, 14)));

        metaCard.add(createMetaLabel("Notice Date:", notice.getFormattedDate()));
        metaCard.add(createMetaLabel("Published By:", notice.getCreatorName()));

        // Content Area: Full notice description from database
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 8));
        bodyPanel.setOpaque(false);

        JLabel contentTitle = new JLabel("Notice Content:");
        contentTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contentTitle.setForeground(textDark);

        JTextArea descArea = new JTextArea(notice.getDescription());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setForeground(textDark);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(borderColor, 1));

        bodyPanel.add(contentTitle, BorderLayout.NORTH);
        bodyPanel.add(descScroll, BorderLayout.CENTER);

        // Center container combining Metadata + Description
        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);
        centerContainer.add(metaCard, BorderLayout.NORTH);
        centerContainer.add(bodyPanel, BorderLayout.CENTER);

        // Footer with Close Button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setForeground(Color.BLACK);
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(8, 22, 8, 22)));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        footer.add(closeBtn);

        root.add(modalHeader, BorderLayout.NORTH);
        root.add(centerContainer, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private JPanel createMetaLabel(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(textMuted);

        JLabel v = new JLabel(value != null ? value : "N/A");
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(textDark);

        p.add(l);
        p.add(v);
        return p;
    }

    /**
     * Standard dark navy header matching the application theme.
     */
    private void setupTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(darkNavy);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setOpaque(true);

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
