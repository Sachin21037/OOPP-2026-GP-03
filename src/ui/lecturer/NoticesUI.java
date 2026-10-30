package ui.lecturer;

import dao.NoticesDAO;
import dao.NoticesDAO.NoticeItem;
import dao.NoticesDAO.NoticesSummary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * NoticesUI - Presentation layer for Faculty Notices.
 * Matches the official FAMS portal design and provides full DB-backed operations.
 */
public class NoticesUI extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color BUTTON_BLUE = new Color(29, 78, 216); // Royal Blue #1D4ED8
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color GREEN = LecturerTheme.SUCCESS_GREEN;
    private static final Color RED = LecturerTheme.DANGER_RED;

    protected String lecturerId;

    private JTable noticesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JComboBox<String> statusFilter;
    private JTextField searchField;

    private JLabel activeNoticesValue;
    private JLabel recentNoticesValue;
    private JLabel totalNoticesValue;

    private JLabel subtitleLabel;
    private JLabel tableCountLabel;

    private final List<NoticeItem> loadedNotices = new ArrayList<>();
    private final List<NoticeItem> displayedNotices = new ArrayList<>();

    public NoticesUI() {
        this(resolveActiveLecturerId());
    }

    public NoticesUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : resolveActiveLecturerId();

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Notice Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadNotices();
    }

    private static String resolveActiveLecturerId() {
        String active = LecturerDashboardUI.getActiveLecturerId();
        if (active != null && !active.trim().isEmpty()) {
            return active.trim();
        }
        String sysProp = System.getProperty("fams.lecturer.id");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return "LEC001";
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Notices", lecturerId, this);
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Top Section: Title Bar + Summary Cards
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // 1. Page Title Bar: Left Title + Subtitle | Right: LECTURER PORTAL badge
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel mainTitle = new JLabel("Notice Board");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 26));
        mainTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("View academic announcements and faculty notices.");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);

        titles.add(mainTitle);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // Right Badge
        JPanel portalBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        portalBadge.setBackground(WHITE);
        portalBadge.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        JLabel portalLbl = new JLabel("LECTURER PORTAL");
        portalLbl.setFont(new Font("Arial", Font.BOLD, 11));
        portalLbl.setForeground(TEXT);
        portalBadge.add(portalLbl);

        titleBar.add(titles, BorderLayout.WEST);
        titleBar.add(portalBadge, BorderLayout.EAST);
        topSection.add(titleBar);
        topSection.add(Box.createVerticalStrut(16));

        // 2. Summary Cards (3 in horizontal row)
        topSection.add(createSummarySection());
        content.add(topSection, BorderLayout.NORTH);

        // 3. Main Table Container Card
        content.add(createTablePanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummarySection() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(0, 92));

        activeNoticesValue = new JLabel("0");
        recentNoticesValue = new JLabel("0");
        totalNoticesValue = new JLabel("0");

        summaryPanel.add(createSummaryCard("ACTIVE NOTICES", activeNoticesValue, TEAL, "Currently active faculty notices"));
        summaryPanel.add(createSummaryCard("RECENT (30 DAYS)", recentNoticesValue, NAVY, "Published in the last 30 days"));
        summaryPanel.add(createSummaryCard("ALL NOTICES", totalNoticesValue, PURPLE, "Total published announcements"));

        return summaryPanel;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color stripeColor, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(226, 232, 240));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                // 3px top horizontal stripe
                g2.setColor(stripeColor);
                g2.fillRect(0, 0, getWidth(), 3);
                // 4px left vertical stripe
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 11));
        titleLbl.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 26));
        value.setForeground(TEXT);

        JLabel capLbl = new JLabel(caption);
        capLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        capLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(capLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout(0, 12));
        tableContainer.setBackground(WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 14, 18)
        ));

        // Header Row: Published Notices | X NOTICES FOUND
        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);

        JLabel listTitle = new JLabel("Published Notices");
        listTitle.setFont(new Font("Arial", Font.BOLD, 16));
        listTitle.setForeground(TEXT);

        tableCountLabel = new JLabel("0 NOTICES FOUND");
        tableCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        tableCountLabel.setForeground(MUTED);

        listHeader.add(listTitle, BorderLayout.WEST);
        listHeader.add(tableCountLabel, BorderLayout.EAST);
        tableContainer.add(listHeader, BorderLayout.NORTH);

        // Table (5 columns)
        String[] cols = {
                "Notice ID",
                "Notice Title",
                "Date",
                "Posted By",
                "Status"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        rowSorter = new TableRowSorter<>(tableModel);
        noticesTable = new JTable(tableModel);
        noticesTable.setRowSorter(rowSorter);
        noticesTable.setRowHeight(34);
        noticesTable.setFont(LecturerTheme.FONT_PLAIN_13);
        noticesTable.setGridColor(new Color(241, 245, 249));
        noticesTable.setSelectionBackground(new Color(239, 246, 255));
        noticesTable.setSelectionForeground(NAVY);
        noticesTable.setShowGrid(true);

        JTableHeader th = noticesTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(new Color(51, 65, 85));
        th.setPreferredSize(new Dimension(0, 34));

        noticesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        noticesTable.getColumnModel().getColumn(1).setPreferredWidth(350);
        noticesTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        noticesTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        noticesTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        noticesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 12, 0, 0));
        noticesTable.getColumnModel().getColumn(1).setCellRenderer(leftIndentRenderer);
        noticesTable.getColumnModel().getColumn(3).setCellRenderer(leftIndentRenderer);

        noticesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer());
        noticesTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());

        noticesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewNoticeDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(noticesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom Toolbar: Left Filter/Search | Right Action Buttons
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Left Controls: Search [  ] Filter [ Active Notices ▾ ]
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftControls.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        searchLbl.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(LecturerTheme.FONT_PLAIN_13);
        searchField.setPreferredSize(new Dimension(150, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterNotices(); }
            public void removeUpdate(DocumentEvent e) { filterNotices(); }
            public void changedUpdate(DocumentEvent e) { filterNotices(); }
        });

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        filterLbl.setForeground(TEXT);

        statusFilter = new JComboBox<>(new String[]{"Active Notices", "All Notices", "Recent (30 Days)", "Inactive Notices"});
        statusFilter.setFont(LecturerTheme.FONT_PLAIN_13);
        statusFilter.setPreferredSize(new Dimension(130, 32));
        statusFilter.addActionListener(e -> filterNotices());

        leftControls.add(searchLbl);
        leftControls.add(searchField);
        leftControls.add(Box.createHorizontalStrut(6));
        leftControls.add(filterLbl);
        leftControls.add(statusFilter);

        // Right Buttons: [ Refresh ] [ Read Notice ]
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightButtons.setOpaque(false);

        JButton refreshBtn = createStyledButton("Refresh", WHITE, TEXT, 85);
        refreshBtn.addActionListener(e -> loadNotices());

        JButton readBtn = createStyledButton("Read Notice", BUTTON_BLUE, Color.WHITE, 120);
        readBtn.addActionListener(e -> viewNoticeDetails());

        rightButtons.add(refreshBtn);
        rightButtons.add(readBtn);

        bottomBar.add(leftControls, BorderLayout.WEST);
        bottomBar.add(rightButtons, BorderLayout.EAST);
        tableContainer.add(bottomBar, BorderLayout.SOUTH);

        return tableContainer;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String s = value != null ? value.toString() : "";

                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                    l.setFont(LecturerTheme.FONT_BOLD_12);
                    return l;
                }

                if ("ACTIVE".equalsIgnoreCase(s)) {
                    l.setBackground(WHITE);
                    l.setForeground(new Color(22, 163, 74)); // Green
                    l.setFont(LecturerTheme.FONT_BOLD_12);
                } else {
                    l.setBackground(WHITE);
                    l.setForeground(new Color(220, 38, 38)); // Red
                    l.setFont(LecturerTheme.FONT_BOLD_12);
                }
                l.setOpaque(true);
                return l;
            }
        };
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int minWidth) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(WHITE.equals(bg) ? new Color(243, 244, 246) : bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (WHITE.equals(bg)) {
                    g2.setColor(new Color(209, 213, 219));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }

                g2.setFont(getFont());
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(LecturerTheme.FONT_BOLD_12);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Math.max(minWidth, 80), 34));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        return btn;
    }

    public void loadNotices() {
        loadedNotices.clear();
        displayedNotices.clear();
        tableModel.setRowCount(0);

        try {
            List<NoticeItem> list = NoticesDAO.loadNotices();
            loadedNotices.addAll(list);

            if (subtitleLabel != null) {
                subtitleLabel.setText("View academic announcements and faculty notices.");
            }
        } catch (SQLException e) {
            System.err.println("[NoticesUI] DB Error: " + e.getMessage());
            if (subtitleLabel != null) subtitleLabel.setText("Database error: " + e.getMessage());
        }

        filterNotices();
    }

    public void filterNotices() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        displayedNotices.clear();

        String status = statusFilter != null ? (String) statusFilter.getSelectedItem() : "Active Notices";
        String search = searchField != null ? searchField.getText().trim() : "";

        List<NoticeItem> filtered = NoticesDAO.filterNotices(loadedNotices, status, search);

        for (NoticeItem item : filtered) {
            displayedNotices.add(item);
            tableModel.addRow(item.toTableRow());
        }

        if (tableCountLabel != null) {
            tableCountLabel.setText(filtered.size() + " NOTICES FOUND");
        }

        NoticesSummary summary = NoticesDAO.calculateSummary(loadedNotices);
        if (activeNoticesValue != null) activeNoticesValue.setText(String.valueOf(summary.activeCount));
        if (recentNoticesValue != null) recentNoticesValue.setText(String.valueOf(summary.recentCount));
        if (totalNoticesValue != null) totalNoticesValue.setText(String.valueOf(summary.totalCount));
    }

    private void viewNoticeDetails() {
        int row = noticesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a notice from the table to read.",
                    "Read Notice",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = noticesTable.convertRowIndexToModel(row);
        NoticeItem item = displayedNotices.get(modelRow);

        JDialog dialog = new JDialog(this, "Notice Details - " + item.noticeId, true);
        dialog.setSize(540, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(WHITE);

        JPanel header = new JPanel(new GridLayout(4, 2, 8, 8));
        header.setBackground(WHITE);
        header.setBorder(new EmptyBorder(18, 22, 10, 22));

        header.add(makeFieldLabel("Notice ID:"));
        header.add(makeFieldValue(item.noticeId));

        header.add(makeFieldLabel("Title / Subject:"));
        header.add(makeFieldValue(item.title));

        header.add(makeFieldLabel("Published Date:"));
        header.add(makeFieldValue(item.noticeDate));

        header.add(makeFieldLabel("Posted By:"));
        header.add(makeFieldValue(item.creatorName.isEmpty() ? item.createdBy : item.creatorName + " (" + item.createdBy + ")"));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setBackground(WHITE);
        centerPanel.setBorder(new EmptyBorder(0, 22, 10, 22));

        JLabel contentLbl = makeFieldLabel("Notice Content:");
        JTextArea contentArea = new JTextArea(item.description);
        contentArea.setFont(LecturerTheme.FONT_PLAIN_13);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(new Color(248, 250, 252));
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JScrollPane areaScroll = new JScrollPane(contentArea);
        areaScroll.setBorder(null);

        centerPanel.add(contentLbl, BorderLayout.NORTH);
        centerPanel.add(areaScroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(WHITE);
        btnPanel.setBorder(new EmptyBorder(0, 0, 10, 20));

        JButton closeBtn = createStyledButton("Close", BUTTON_BLUE, Color.WHITE, 90);
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LecturerTheme.FONT_BOLD_12);
        l.setForeground(MUTED);
        return l;
    }

    private JLabel makeFieldValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LecturerTheme.FONT_PLAIN_13);
        l.setForeground(TEXT);
        return l;
    }

    public static void main(String[] args) {
        String testId = (args != null && args.length > 0) ? args[0] : null;
        if (testId != null) {
            LecturerDashboardUI.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new NoticesUI(testId).setVisible(true);
        });
    }
}
