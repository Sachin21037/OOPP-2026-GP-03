package ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable Blue Header Banner Component for FAMS.
 * Reproduces the Admin Dashboard hero header design across all application pages.
 *
 * Visual Specifications:
 * - Gradient: Royal Navy (#10284A) to Secondary Blue (#1B559B)
 * - Corner Radius: 14px rounded rectangle
 * - Background Elements: Subtle geometric decorative arcs matching Admin Dashboard
 * - Typography: Bold 22pt white title, Plain 13pt soft blue (#E0EAFF) subtitle
 * - Padding: 20px top/bottom, 26px left/right
 * - Height: 105px (preferred and maximum constraint)
 * - Optional Badge: Translucent white pill for record counts
 * - Optional Action Button: Glassmorphic translucent action button (e.g. Refresh)
 */
public class PageHeaderBanner extends JPanel {

    private static final Color PRIMARY_NAVY   = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE = new Color(27, 85, 155);   // #1B559B
    private static final Color SUBTITLE_COLOR = new Color(224, 234, 254); // #E0EAFF

    private final JLabel lblTitle;
    private final JLabel lblSubtitle;
    private final JLabel lblBadge;
    private JButton btnAction;

    public PageHeaderBanner(String title, String subtitle) {
        this(title, subtitle, null, null, null);
    }

    public PageHeaderBanner(String title, String subtitle, String badgeText) {
        this(title, subtitle, badgeText, null, null);
    }

    public PageHeaderBanner(String title, String subtitle, String badgeText, String actionText, Runnable actionCallback) {
        setOpaque(false);
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(20, 26, 20, 26));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        setPreferredSize(new Dimension(1000, 105));

        // Left Container: Title + Badge + Subtitle
        JPanel leftContainer = new JPanel();
        leftContainer.setOpaque(false);
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTitle = new JLabel(title);
        lblTitle.setFont(AdminDashboard.getModernFont(Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        titleRow.add(lblTitle);

        titleRow.add(Box.createHorizontalStrut(10));

        lblBadge = new JLabel(badgeText != null ? badgeText : "") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setOpaque(false);
        lblBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        lblBadge.setVisible(badgeText != null && !badgeText.isBlank());
        titleRow.add(lblBadge);

        lblSubtitle = new JLabel(subtitle != null ? subtitle : "");
        lblSubtitle.setFont(AdminDashboard.getModernFont(Font.PLAIN, 13));
        lblSubtitle.setForeground(SUBTITLE_COLOR);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftContainer.add(Box.createVerticalGlue());
        leftContainer.add(titleRow);
        leftContainer.add(Box.createVerticalStrut(5));
        leftContainer.add(lblSubtitle);
        leftContainer.add(Box.createVerticalGlue());

        add(leftContainer, BorderLayout.CENTER);

        // Right Actions (e.g. Refresh button)
        if (actionText != null && actionCallback != null) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
            actionPanel.setOpaque(false);

            btnAction = new JButton(actionText) {
                private float hoverProgress = 0f;
                private Timer animTimer;
                {
                    addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            startAnim(true);
                        }
                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            startAnim(false);
                        }
                        private void startAnim(boolean in) {
                            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
                            animTimer = new Timer(16, evt -> {
                                hoverProgress += in ? 0.15f : -0.15f;
                                if (hoverProgress >= 1f) { hoverProgress = 1f; animTimer.stop(); }
                                else if (hoverProgress <= 0f) { hoverProgress = 0f; animTimer.stop(); }
                                repaint();
                            });
                            animTimer.start();
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int alphaBg = (int) (35 + 35 * hoverProgress);
                    int alphaBorder = (int) (90 + 70 * hoverProgress);
                    g2.setColor(new Color(255, 255, 255, alphaBg));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(255, 255, 255, alphaBorder));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnAction.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
            btnAction.setForeground(Color.WHITE);
            btnAction.setContentAreaFilled(false);
            btnAction.setBorderPainted(false);
            btnAction.setFocusPainted(false);
            btnAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAction.setBorder(new EmptyBorder(8, 18, 8, 18));
            btnAction.addActionListener(e -> actionCallback.run());
            actionPanel.add(btnAction);

            add(actionPanel, BorderLayout.EAST);
        }
    }

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    public void setBadgeText(String text) {
        if (text != null && !text.isBlank()) {
            lblBadge.setText(text);
            lblBadge.setVisible(true);
        } else {
            lblBadge.setVisible(false);
        }
    }

    public JButton getActionButton() {
        return btnAction;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(
                0, 0, PRIMARY_NAVY,
                getWidth(), getHeight(), SECONDARY_BLUE
        );
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

        // Subtle modern background arcs matching Dashboard reference
        g2.setColor(new Color(255, 255, 255, 14));
        g2.fillOval(getWidth() - 170, -40, 210, 210);
        g2.fillOval(getWidth() - 250, 15, 140, 140);
        g2.dispose();
    }
}
