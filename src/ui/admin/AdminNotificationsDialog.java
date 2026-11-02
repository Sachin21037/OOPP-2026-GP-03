package ui.admin;

import dao.NoticeDAO;
import model.Notice;
import ui.util.UIAnimationHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Modern Notifications & Notices Dialog for FAMS Admin Dashboard.
 * Connected directly to the top header bell icon, providing instant access
 * to live notices from the database with direct navigation to Notice Management.
 */
public class AdminNotificationsDialog extends JDialog {

    private static final Color PRIMARY_NAVY    = new Color(26, 83, 71);    // #1A5347 Velvety Deep Sage Pine
    private static final Color SECONDARY_BLUE  = new Color(42, 119, 102);  // #2A7766 Soft Sage Green Accent
    private static final Color BG_MAIN         = new Color(239, 245, 242); // #EFF5F2 Soft Sage Canvas
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color TEXT_PRIMARY    = new Color(26, 56, 48);    // #1A3830 Deep Sage Slate
    private static final Color TEXT_SECONDARY  = new Color(93, 122, 114);  // #5D7A72 Muted Sage Slate
    private static final Color BORDER_COLOR    = new Color(210, 228, 220); // #D2E4DC Soft Sage Border
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A

    private final Runnable onOpenNoticeManagement;
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private JPanel noticeListContainer;
    private JLabel lblNoticeCount;

    public AdminNotificationsDialog(Window parent, Runnable onOpenNoticeManagement) {
        super(parent, "Academic Notices & Announcements", ModalityType.APPLICATION_MODAL);
        this.onOpenNoticeManagement = onOpenNoticeManagement;

        initUI();
        loadNoticesAsync();
    }

    private void initUI() {
        setSize(720, 680);
        setMinimumSize(new Dimension(640, 550));
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        // 1. Header Banner
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // 2. Scrollable Notice Items Container
        noticeListContainer = new JPanel();
        noticeListContainer.setOpaque(false);
        noticeListContainer.setLayout(new BoxLayout(noticeListContainer, BoxLayout.Y_AXIS));
        noticeListContainer.setBorder(new EmptyBorder(16, 20, 16, 20));

        JScrollPane scrollPane = new JScrollPane(noticeListContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Footer Bar
        JPanel footer = createFooter();
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_NAVY, getWidth(), getHeight(), SECONDARY_BLUE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Left Bell Icon Circle
        JPanel bellCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 26));
                g2.fillOval(0, 0, 48, 48);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawOval(0, 0, 47, 47);

                // Bell icon
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(16, 13, 16, 15, 0, 180);
                g2.drawLine(16, 21, 12, 28);
                g2.drawLine(32, 21, 36, 28);
                g2.drawLine(12, 28, 36, 28);
                g2.fillOval(21, 29, 6, 4);

                g2.dispose();
            }
        };
        bellCircle.setOpaque(false);
        bellCircle.setPreferredSize(new Dimension(48, 48));
        header.add(bellCircle, BorderLayout.WEST);

        // Center Title & Count
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Academic Notices & Announcements");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);

        lblNoticeCount = new JLabel("Loading published notices from database...");
        lblNoticeCount.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        lblNoticeCount.setForeground(new Color(203, 213, 225));

        titlePanel.add(titleLbl);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblNoticeCount);

        header.add(titlePanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 22, 12, 22)
        ));

        JButton btnManage = UIAnimationHelper.createAnimatedPillButton("📢 Open Notice Management", SECONDARY_BLUE, 20, e -> {
            dispose();
            if (onOpenNoticeManagement != null) {
                onOpenNoticeManagement.run();
            }
        });
        footer.add(btnManage, BorderLayout.WEST);

        JButton btnClose = UIAnimationHelper.createAnimatedSecondaryButton("Close", TEXT_SECONDARY, BORDER_COLOR, 20, e -> dispose());
        footer.add(btnClose, BorderLayout.EAST);

        return footer;
    }

    private void loadNoticesAsync() {
        SwingWorker<List<Notice>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Notice> doInBackground() {
                return noticeDAO.getAllNotices();
            }

            @Override
            protected void done() {
                try {
                    List<Notice> list = get();
                    populateNoticeCards(list);
                } catch (Exception ex) {
                    System.err.println("[AdminNotificationsDialog] Error loading notices: " + ex.getMessage());
                    lblNoticeCount.setText("Error loading notices from database.");
                }
            }
        };
        worker.execute();
    }

    private void populateNoticeCards(List<Notice> notices) {
        noticeListContainer.removeAll();

        if (notices == null || notices.isEmpty()) {
            lblNoticeCount.setText("No published academic notices found.");
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
            emptyPanel.add(new JLabel("No active notices currently published."));
            noticeListContainer.add(emptyPanel);
        } else {
            lblNoticeCount.setText(notices.size() + " active notices published for faculty members and students");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Notice n : notices) {
                JPanel card = createNoticeCard(n, sdf);
                noticeListContainer.add(card);
                noticeListContainer.add(Box.createVerticalStrut(12));
            }
        }

        noticeListContainer.revalidate();
        noticeListContainer.repaint();
    }

    private JPanel createNoticeCard(Notice n, SimpleDateFormat sdf) {
        JPanel card = new JPanel(new BorderLayout(12, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                // Subtle left accent bar
                g2.setColor(SECONDARY_BLUE);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Top Row: Title + Date & Status Badges
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLbl = new JLabel(n.getTitle());
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 13));
        titleLbl.setForeground(TEXT_PRIMARY);
        topRow.add(titleLbl, BorderLayout.WEST);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);

        String dateStr = (n.getNoticeDate() != null) ? sdf.format(n.getNoticeDate()) : "Recent";
        JLabel dateBadge = new JLabel("📅 " + dateStr);
        dateBadge.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        dateBadge.setForeground(TEXT_SECONDARY);

        JLabel statusPill = new JLabel("  " + (n.getStatus() != null ? n.getStatus() : "ACTIVE") + "  ");
        statusPill.setFont(AdminDashboard.getModernFont(Font.BOLD, 9));
        statusPill.setForeground(SUCCESS_COLOR);
        statusPill.setOpaque(true);
        statusPill.setBackground(new Color(220, 252, 231));
        statusPill.setBorder(new EmptyBorder(2, 6, 2, 6));

        badges.add(dateBadge);
        badges.add(statusPill);
        topRow.add(badges, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Center: Description
        String desc = n.getDescription();
        if (desc == null) desc = "";
        String shortDesc = (desc.length() > 180) ? desc.substring(0, 180) + "..." : desc;

        JLabel descLbl = new JLabel("<html><body style='width: 540px; font-size: 11px; color: #4A5568; line-height: 1.4;'>" +
                shortDesc + "</body></html>");
        card.add(descLbl, BorderLayout.CENTER);

        // Bottom: Author & Read Details Button
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        String creator = (n.getCreatorName() != null && !n.getCreatorName().isBlank())
                ? n.getCreatorName()
                : (n.getCreatedBy() != null ? n.getCreatedBy() : "Administrator");
        JLabel authorLbl = new JLabel("Posted by: " + creator);
        authorLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        authorLbl.setForeground(new Color(148, 163, 184));
        bottomRow.add(authorLbl, BorderLayout.WEST);

        JButton btnRead = new JButton("Read Full Notice →");
        btnRead.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
        btnRead.setForeground(SECONDARY_BLUE);
        btnRead.setContentAreaFilled(false);
        btnRead.setBorderPainted(false);
        btnRead.setFocusPainted(false);
        btnRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRead.addActionListener(e -> showNoticeDetailModal(n, dateStr, creator));
        bottomRow.add(btnRead, BorderLayout.EAST);

        card.add(bottomRow, BorderLayout.SOUTH);
        return card;
    }

    private void showNoticeDetailModal(Notice n, String dateStr, String creator) {
        JDialog detailDialog = new JDialog(this, "Notice Details", ModalityType.APPLICATION_MODAL);
        detailDialog.setSize(560, 420);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.getContentPane().setBackground(Color.WHITE);
        detailDialog.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(22, 24, 20, 24));

        JLabel title = new JLabel(n.getTitle());
        title.setFont(AdminDashboard.getModernFont(Font.BOLD, 15));
        title.setForeground(PRIMARY_NAVY);

        JLabel meta = new JLabel("Published on " + dateStr + " • Author: " + creator);
        meta.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        meta.setForeground(TEXT_SECONDARY);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JTextArea body = new JTextArea(n.getDescription());
        body.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        body.setForeground(TEXT_PRIMARY);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setBackground(new Color(248, 250, 252));
        body.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(meta);
        p.add(Box.createVerticalStrut(10));
        p.add(sep);
        p.add(Box.createVerticalStrut(12));
        p.add(new JScrollPane(body));

        detailDialog.add(p, BorderLayout.CENTER);

        JPanel closeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        closeBar.setBackground(Color.WHITE);
        closeBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnClose = UIAnimationHelper.createAnimatedSecondaryButton("Close", TEXT_SECONDARY, BORDER_COLOR, 18, ev -> detailDialog.dispose());
        closeBar.add(btnClose);
        detailDialog.add(closeBar, BorderLayout.SOUTH);

        detailDialog.setVisible(true);
    }
}

