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
import java.util.List;

/**
 * Notices & Announcements Page for Technical Officer.
 * Allows Technical Officers to view faculty notices, official announcements,
 * exam notices, and search through published communications.
 */
public class NoticesPage extends JPanel {

    // Theme palette harmonized with Technical Officer Dashboard
    private final Color darkNavy = new Color(27, 66, 104);       // #1B4268
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color lightBg = new Color(248, 250, 252);       // #F8FAFC
    private final Color borderColor = new Color(226, 232, 240);   // #E2E8F0
    private final Color textDark = new Color(30, 41, 59);         // #1E293B
    private final Color textMuted = new Color(100, 116, 139);     // #64748B
    private final Color activeGreen = new Color(22, 163, 74);     // #16A34A

    private final NoticeService noticeService = new NoticeService();

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JTable noticeTable;
    private DefaultTableModel tableModel;
    private JLabel totalNoticesLabel;
    private JLabel activeNoticesLabel;
    private JButton viewNoticeBtn;

    public NoticesPage() {
        setLayout(new BorderLayout());
        setBackground(lightBg);

        initUI();
        refreshNotices();
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 16));
        mainContainer.setBackground(lightBg);
        mainContainer.setBorder(new EmptyBorder(22, 28, 24, 28));

        // 1. Top Header Banner
        JPanel headerPanel = createHeaderPanel();

        // 2. Center Card Container (Filter Bar + Table + Footer Actions)
        JPanel contentCard = createContentCard();

        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentCard, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Faculty Notices & Announcements");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Official administrative circulars, examination schedules, and faculty academic updates.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitle);

        // Stats summary chips
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        statsPanel.setOpaque(false);

        totalNoticesLabel = createStatBadge("Total: 0", new Color(241, 245, 249), textDark);
        activeNoticesLabel = createStatBadge("Active: 0", new Color(220, 252, 231), activeGreen);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(primaryBlue);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 14, 6, 14)));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshNotices());

        statsPanel.add(totalNoticesLabel);
        statsPanel.add(activeNoticesLabel);
        statsPanel.add(refreshBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

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

    private JPanel createContentCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(18, 20, 18, 20)));

        // Filter Bar (Search + Status Filter + Read Button)
        JPanel filterBar = new JPanel(new BorderLayout(14, 0));
        filterBar.setOpaque(false);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilter.setOpaque(false);

        searchField = new JTextField(22);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search notices by title or content...");
        searchField.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 10, 6, 10)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "ACTIVE", "INACTIVE"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> applyFilters());

        leftFilter.add(new JLabel("Search:"));
        leftFilter.add(searchField);
        leftFilter.add(Box.createHorizontalStrut(8));
        leftFilter.add(new JLabel("Status:"));
        leftFilter.add(statusFilter);

        viewNoticeBtn = new JButton("View Full Notice");
        viewNoticeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewNoticeBtn.setForeground(Color.WHITE);
        viewNoticeBtn.setBackground(primaryBlue);
        viewNoticeBtn.setFocusPainted(false);
        viewNoticeBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        viewNoticeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewNoticeBtn.setEnabled(false);
        viewNoticeBtn.addActionListener(e -> openSelectedNotice());

        filterBar.add(leftFilter, BorderLayout.WEST);
        filterBar.add(viewNoticeBtn, BorderLayout.EAST);

        // Notices Table
        String[] columns = {"Notice ID", "Title", "Publish Date", "Published By", "Status", "Summary Preview"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        noticeTable = new JTable(tableModel);
        noticeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noticeTable.setRowHeight(36);
        noticeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noticeTable.setShowVerticalLines(false);
        noticeTable.setGridColor(new Color(241, 245, 249));
        noticeTable.setSelectionBackground(new Color(234, 242, 255));
        noticeTable.setSelectionForeground(textDark);

        // Header Styling
        JTableHeader th = noticeTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(textDark);
        th.setPreferredSize(new Dimension(0, 38));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

        // Column widths
        noticeTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        noticeTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        noticeTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        noticeTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        noticeTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        noticeTable.getColumnModel().getColumn(5).setPreferredWidth(320);

        // Status column renderer
        noticeTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String st = (value != null) ? value.toString() : "ACTIVE";
                if ("ACTIVE".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(22, 163, 74));
                } else {
                    lbl.setForeground(new Color(156, 163, 175));
                }
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return lbl;
            }
        });

        // Double-click or selection listener
        noticeTable.getSelectionModel().addListSelectionListener(e -> {
            viewNoticeBtn.setEnabled(noticeTable.getSelectedRow() >= 0);
        });

        noticeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && noticeTable.getSelectedRow() >= 0) {
                    openSelectedNotice();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(noticeTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(filterBar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    public void refreshNotices() {
        applyFilters();
    }

    private void applyFilters() {
        String kw = (searchField != null) ? searchField.getText().trim() : "";
        String stFilter = (statusFilter != null && statusFilter.getSelectedItem() != null)
                ? statusFilter.getSelectedItem().toString()
                : "All Statuses";

        List<Notice> notices = (kw.isEmpty())
                ? noticeService.getAllNotices()
                : noticeService.searchNotices(kw);

        tableModel.setRowCount(0);
        int total = 0;
        int active = 0;

        for (Notice n : notices) {
            if ("ACTIVE".equalsIgnoreCase(stFilter) && !n.isActive()) continue;
            if ("INACTIVE".equalsIgnoreCase(stFilter) && n.isActive()) continue;

            total++;
            if (n.isActive()) active++;

            tableModel.addRow(new Object[]{
                    n.getNoticeId(),
                    n.getTitle(),
                    n.getFormattedDate(),
                    n.getCreatorName(),
                    n.getStatus(),
                    n.getShortDescription(80)
            });
        }

        if (totalNoticesLabel != null) {
            totalNoticesLabel.setText("Total: " + total);
        }
        if (activeNoticesLabel != null) {
            activeNoticesLabel.setText("Active: " + active);
        }
    }

    private void openSelectedNotice() {
        int row = noticeTable.getSelectedRow();
        if (row < 0) return;

        String noticeId = (String) tableModel.getValueAt(row, 0);
        Notice notice = noticeService.getNoticeById(noticeId);
        if (notice == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Faculty Notice - " + notice.getNoticeId(), true);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(22, 24, 20, 24));

        // Header info
        JPanel top = new JPanel(new GridLayout(3, 1, 0, 4));
        top.setOpaque(false);

        JLabel titleLbl = new JLabel(notice.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(textDark);

        JLabel metaLbl = new JLabel("Published: " + notice.getFormattedDate() + " • By: " + notice.getCreatorName() + " (" + notice.getCreatedBy() + ")");
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaLbl.setForeground(textMuted);

        JLabel statusLbl = new JLabel("Status: " + notice.getStatus());
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLbl.setForeground(notice.isActive() ? activeGreen : textMuted);

        top.add(titleLbl);
        top.add(metaLbl);
        top.add(statusLbl);

        // Content
        JTextArea descArea = new JTextArea(notice.getDescription());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setForeground(new Color(51, 65, 85));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setMargin(new Insets(12, 12, 12, 12));
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(new LineBorder(borderColor, 1));

        JScrollPane scroll = new JScrollPane(descArea);
        scroll.setBorder(null);

        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setForeground(textDark);
        closeBtn.setBackground(new Color(241, 245, 249));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);

        p.add(top, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(p);
        dialog.setVisible(true);
    }
}
