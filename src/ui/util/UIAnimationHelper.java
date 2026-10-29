package ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UIAnimationHelper provides reusable animation helpers and ultra-modern
 * rounded UI components (pills, animated buttons, search boxes, and dropdown cards).
 */
public class UIAnimationHelper {

    // Color Interpolation for Smooth 60fps Micro-Animations
    public static Color interpolateColor(Color c1, Color c2, float fraction) {
        fraction = Math.max(0f, Math.min(1f, fraction));
        int r = (int) (c1.getRed() + fraction * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + fraction * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + fraction * (c2.getBlue() - c1.getBlue()));
        int a = (int) (c1.getAlpha() + fraction * (c2.getAlpha() - c1.getAlpha()));
        return new Color(r, g, b, a);
    }

    public static Color lighten(Color c, float factor) {
        int r = Math.min(255, (int) (c.getRed() * factor));
        int g = Math.min(255, (int) (c.getGreen() * factor));
        int b = Math.min(255, (int) (c.getBlue() * factor));
        return new Color(r, g, b, c.getAlpha());
    }

    public static Color darken(Color c, float factor) {
        int r = Math.max(0, (int) (c.getRed() * factor));
        int g = Math.max(0, (int) (c.getGreen() * factor));
        int b = Math.max(0, (int) (c.getBlue() * factor));
        return new Color(r, g, b, c.getAlpha());
    }

    /**
     * Creates an animated pill-shaped primary action button with smooth hover lift and brightness transition.
     */
    public static JButton createAnimatedPillButton(String text, Color baseColor, int radius, ActionListener al) {
        final float[] animProgress = {0f};
        final boolean[] isHovered = {false};
        final boolean[] isPressed = {false};
        final Timer[] animTimer = {null};

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int yOffset = isPressed[0] ? 1 : (int) (-animProgress[0] * 1.5f);

                Color hoverColor = lighten(baseColor, 1.15f);
                Color currentColor = interpolateColor(baseColor, hoverColor, animProgress[0]);
                if (isPressed[0]) {
                    currentColor = darken(baseColor, 0.88f);
                }

                // Subtle soft shadow when hovering
                if (animProgress[0] > 0.05f && !isPressed[0]) {
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (40 * animProgress[0])));
                    g2.fillRoundRect(0, yOffset + 2, w, h - 2, radius, radius);
                }

                g2.setColor(currentColor);
                g2.fillRoundRect(0, Math.max(0, yOffset), w, h - Math.abs(yOffset), radius, radius);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                startAnimation(btn, animProgress, animTimer, true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                isPressed[0] = false;
                startAnimation(btn, animProgress, animTimer, false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed[0] = true;
                btn.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed[0] = false;
                btn.repaint();
            }
        });

        if (al != null) btn.addActionListener(al);
        return btn;
    }

    /**
     * Creates an animated pill-shaped secondary action button with soft border and smooth hover fill.
     */
    public static JButton createAnimatedSecondaryButton(String text, Color textColor, Color borderColor, int radius, ActionListener al) {
        final float[] animProgress = {0f};
        final boolean[] isHovered = {false};
        final boolean[] isPressed = {false};
        final Timer[] animTimer = {null};

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int yOffset = isPressed[0] ? 1 : 0;

                // Background fill animation
                Color bgNormal = Color.WHITE;
                Color bgHover = new Color(241, 245, 249);
                Color currentBg = interpolateColor(bgNormal, bgHover, animProgress[0]);

                g2.setColor(currentBg);
                g2.fillRoundRect(0, yOffset, w, h - yOffset, radius, radius);

                // Border animation
                Color borderHover = textColor;
                Color currentBorder = interpolateColor(borderColor, borderHover, animProgress[0]);
                g2.setColor(currentBorder);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, yOffset, w - 1, h - yOffset - 1, radius, radius);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(textColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                startAnimation(btn, animProgress, animTimer, true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                isPressed[0] = false;
                startAnimation(btn, animProgress, animTimer, false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed[0] = true;
                btn.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed[0] = false;
                btn.repaint();
            }
        });

        if (al != null) btn.addActionListener(al);
        return btn;
    }

    /**
     * Creates an animated icon button with circular/squircle pill shape.
     */
    public static JButton createAnimatedIconButton(String iconText, String tooltip, Color baseFg, Color hoverBg, int radius, ActionListener al) {
        final float[] animProgress = {0f};
        final boolean[] isHovered = {false};
        final Timer[] animTimer = {null};

        JButton btn = new JButton(iconText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color bg = interpolateColor(Color.WHITE, hoverBg, animProgress[0]);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, radius, radius);

                g2.setColor(interpolateColor(new Color(226, 232, 240), baseFg, animProgress[0] * 0.5f));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(baseFg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setToolTipText(tooltip);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                startAnimation(btn, animProgress, animTimer, true);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                startAnimation(btn, animProgress, animTimer, false);
            }
        });

        if (al != null) btn.addActionListener(al);
        return btn;
    }

    /**
     * Creates a modern pill search input box with rounded corners and focus ring animation.
     */
    public static JPanel createPillSearchBox(JTextField txtField, int width, int height, int radius) {
        final boolean[] isFocused = {false};
        final float[] focusAnim = {0f};
        final Timer[] timer = {null};

        JPanel box = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, radius, radius);

                // Focus glow / border
                Color normalBorder = new Color(226, 232, 240);
                Color activeBorder = new Color(59, 130, 246);
                Color currentBorder = interpolateColor(normalBorder, activeBorder, focusAnim[0]);

                g2.setColor(currentBorder);
                g2.setStroke(new BasicStroke(focusAnim[0] > 0.05f ? 1.6f : 1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

                g2.dispose();
            }
        };

        box.setOpaque(false);
        box.setPreferredSize(new Dimension(width, height));
        box.setBorder(new EmptyBorder(4, 12, 4, 12));

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        icon.setForeground(new Color(148, 163, 184));
        box.add(icon, BorderLayout.WEST);

        txtField.setOpaque(false);
        txtField.setBorder(null);
        txtField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtField.setForeground(new Color(15, 23, 42));

        txtField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                isFocused[0] = true;
                startAnimation(box, focusAnim, timer, true);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                isFocused[0] = false;
                startAnimation(box, focusAnim, timer, false);
            }
        });

        box.add(txtField, BorderLayout.CENTER);
        return box;
    }

    private static void startAnimation(JComponent comp, float[] progress, Timer[] timer, boolean targetIn) {
        if (timer[0] != null && timer[0].isRunning()) {
            timer[0].stop();
        }

        final float step = 0.15f;
        timer[0] = new Timer(16, e -> {
            if (targetIn) {
                progress[0] += step;
                if (progress[0] >= 1.0f) {
                    progress[0] = 1.0f;
                    timer[0].stop();
                }
            } else {
                progress[0] -= step;
                if (progress[0] <= 0.0f) {
                    progress[0] = 0.0f;
                    timer[0].stop();
                }
            }
            comp.repaint();
        });
        timer[0].start();
    }

    /**
     * Creates an ultra-modern pill-shaped text field with rounded corners, antialiasing, and focus highlight.
     */
    public static JTextField createPillTextField(int radius) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color borderCol = hasFocus() ? new Color(59, 130, 246) : new Color(226, 232, 240);
                g2.setColor(borderCol);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.6f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                g2.dispose();
            }
        };
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(6, 12, 6, 12));
        return tf;
    }

    /**
     * Builds a modern floating profile dropdown popup menu.
     */
    public static JPopupMenu createProfileDropdown(
            String displayName,
            String role,
            String username,
            String email,
            Runnable onUserManagement,
            Runnable onLogout
    ) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.setBackground(new Color(0, 0, 0, 0));
        popup.setOpaque(false);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(2, 4, w - 4, h - 4, 22, 22);

                // Main card background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 3, 20, 20);

                // Modern subtle border
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 2, h - 4, 20, 20);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(280, 290));

        // 1. Header with large avatar and gradient ring
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        header.setOpaque(false);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient Ring (Pink/Rose to Violet, matching reference image)
                GradientPaint ring = new GradientPaint(0, 0, new Color(244, 63, 94), 52, 52, new Color(139, 92, 246));
                g2.setPaint(ring);
                g2.fillOval(0, 0, 52, 52);

                // Inner avatar circle
                g2.setColor(new Color(30, 41, 59));
                g2.fillOval(3, 3, 46, 46);

                // Initials
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                String init = "SA";
                int tx = (52 - fm.stringWidth(init)) / 2;
                int ty = ((52 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(init, tx, ty);

                // Green Online Dot
                g2.setColor(new Color(16, 185, 129));
                g2.fillOval(38, 38, 11, 11);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(38, 38, 11, 11);

                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(52, 52));
        header.add(avatar);
        card.add(header);

        card.add(Box.createVerticalStrut(6));

        // Name & Role
        JLabel nameLbl = new JLabel(displayName, SwingConstants.CENTER);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLbl.setForeground(new Color(15, 23, 42));
        card.add(nameLbl);

        card.add(Box.createVerticalStrut(2));

        // Role pill badge
        JPanel roleBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(238, 242, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        roleBadge.setOpaque(false);
        roleBadge.setBorder(new EmptyBorder(2, 10, 2, 10));
        roleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleText = new JLabel("● Active  •  " + role);
        roleText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleText.setForeground(new Color(79, 70, 229));
        roleBadge.add(roleText);
        card.add(roleBadge);

        card.add(Box.createVerticalStrut(12));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep);

        card.add(Box.createVerticalStrut(10));

        // Info Rows
        card.add(createInfoRow("Username", username != null ? username : "admin001"));
        card.add(Box.createVerticalStrut(4));
        card.add(createInfoRow("Email", email != null ? email : "admin@fams.uor.lk"));
        card.add(Box.createVerticalStrut(4));
        card.add(createInfoRow("System", "FAMS Central Admin"));

        card.add(Box.createVerticalStrut(14));

        // Action Buttons Row (User Management & Logout)
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton btnManage = createAnimatedPillButton("Users", new Color(79, 70, 229), 16, e -> {
            popup.setVisible(false);
            if (onUserManagement != null) onUserManagement.run();
        });
        btnManage.setFont(new Font("Segoe UI", Font.BOLD, 11));

        JButton btnLogout = createAnimatedSecondaryButton("Log Out", new Color(220, 38, 38), new Color(254, 202, 202), 16, e -> {
            popup.setVisible(false);
            if (onLogout != null) onLogout.run();
        });
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));

        btnRow.add(btnManage);
        btnRow.add(btnLogout);
        card.add(btnRow);

        popup.add(card);
        return popup;
    }

    private static JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(100, 116, 139));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 11));
        val.setForeground(new Color(30, 41, 59));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }
}
