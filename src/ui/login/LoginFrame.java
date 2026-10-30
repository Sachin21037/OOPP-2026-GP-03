package ui.login;

import database.DBConnection;
import ui.lecturer.LecturerDashboardUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Faculty Academic Management System (FAMS) - Sign In Frame.
 */
public class LoginFrame extends JFrame {

    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);
    private static final Color LIGHT_BLUE      = new Color(234, 242, 255);
    private static final Color BG_LEFT_TOP     = new Color(245, 248, 254);
    private static final Color BG_LEFT_BOTTOM  = new Color(228, 238, 252);
    private static final Color PRIMARY_HOVER   = new Color(30, 58, 138);
    private static final Color PRIMARY_PRESSED = new Color(10, 25, 47);

    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);
    private static final Color TEXT_SECONDARY  = new Color(71, 85, 105);
    private static final Color TEXT_MUTED      = new Color(100, 116, 139);
    private static final Color BORDER_COLOR    = new Color(203, 213, 225);
    private static final Color BG_INPUT        = new Color(248, 250, 252);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton eyeButton;
    private JCheckBox rememberMeCheckbox;
    private JButton loginButton;
    private JButton quickLecturerButton;

    private boolean isPasswordVisible = false;
    private final char defaultEchoChar;

    public LoginFrame() {
        setTitle("Faculty Academic Management System - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(920, 560));
        setMinimumSize(new Dimension(820, 500));
        setSize(920, 560);
        setLocationRelativeTo(null);

        JPasswordField tempPass = new JPasswordField();
        defaultEchoChar = tempPass.getEchoChar();

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        // Left Panel (42% width)
        JPanel leftPanel = createLeftPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.42;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(leftPanel, gbc);

        // Right Panel (58% width)
        JPanel rightPanel = createRightPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.58;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(rightPanel, gbc);

        setContentPane(rootPanel);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint bgGradient = new GradientPaint(
                        0, 0, BG_LEFT_TOP,
                        0, getHeight(), BG_LEFT_BOTTOM
                );
                g2.setPaint(bgGradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles
                g2.setColor(new Color(16, 40, 74, 15));
                g2.fillOval(-40, -40, 160, 160);
                g2.setColor(new Color(29, 78, 216, 20));
                g2.fillOval(getWidth() - 90, getHeight() / 2 - 50, 150, 150);

                // Faculty crest badge
                int badgeW = Math.min(220, getWidth() - 60);
                int badgeH = 120;
                int badgeX = (getWidth() - badgeW) / 2;
                int badgeY = getHeight() - badgeH - 40;

                g2.setColor(LIGHT_BLUE);
                g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 16, 16);
                g2.setColor(new Color(29, 78, 216, 80));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 16, 16);

                g2.setColor(PRIMARY_NAVY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String badgeTitle = "FAMS Portal";
                g2.drawString(badgeTitle, badgeX + (badgeW - fm.stringWidth(badgeTitle)) / 2, badgeY + 45);

                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                FontMetrics fm2 = g2.getFontMetrics();
                String badgeSub1 = "Faculty Academic";
                String badgeSub2 = "Management System";
                g2.drawString(badgeSub1, badgeX + (badgeW - fm2.stringWidth(badgeSub1)) / 2, badgeY + 70);
                g2.drawString(badgeSub2, badgeX + (badgeW - fm2.stringWidth(badgeSub2)) / 2, badgeY + 88);

                g2.dispose();
            }
        };

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(40, 36, 40, 36));

        // University Tag
        JLabel uniTag = new JLabel("UNIVERSITY FACULTY PORTAL");
        uniTag.setFont(new Font("SansSerif", Font.BOLD, 10));
        uniTag.setForeground(SECONDARY_BLUE);
        uniTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        // System Title
        JLabel titleMain = new JLabel("FAMS");
        titleMain.setFont(new Font("SansSerif", Font.BOLD, 34));
        titleMain.setForeground(PRIMARY_NAVY);
        titleMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Academic Management");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        subtitle.setForeground(TEXT_PRIMARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel famsDesc = new JLabel(
                "<html><body style='width: 260px; color: #475569; font-family: sans-serif; font-size: 11px; line-height: 1.4;'>" +
                "A unified digital platform for students, lecturers, technical officers, and administrators." +
                "</body></html>"
        );
        famsDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(uniTag);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(titleMain);
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalStrut(14));
        leftPanel.add(famsDesc);
        leftPanel.add(Box.createVerticalGlue());

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(36, 40, 36, 40));

        JLabel welcomeHeader = new JLabel("Sign In");
        welcomeHeader.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeHeader.setForeground(PRIMARY_NAVY);
        welcomeHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeSub = new JLabel("Enter your credentials to access your account");
        welcomeSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        welcomeSub.setForeground(TEXT_MUTED);
        welcomeSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username Label & Field
        JLabel usernameLabel = new JLabel("Username / User ID");
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        usernameLabel.setForeground(TEXT_PRIMARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        usernameField.setPreferredSize(new Dimension(320, 38));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setBackground(BG_INPUT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password Label & Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passwordLabel.setForeground(TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel passRow = new JPanel(new BorderLayout(6, 0));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordField.setBackground(BG_INPUT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        eyeButton = new JButton("Show");
        eyeButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        eyeButton.setFocusPainted(false);
        eyeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeButton.setBackground(LIGHT_BLUE);
        eyeButton.setForeground(SECONDARY_BLUE);
        eyeButton.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        eyeButton.addActionListener(e -> togglePasswordVisibility());

        passRow.add(passwordField, BorderLayout.CENTER);
        passRow.add(eyeButton, BorderLayout.EAST);

        // Remember Me & Forgot Password
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        optionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        optionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        rememberMeCheckbox = new JCheckBox("Remember me");
        rememberMeCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rememberMeCheckbox.setForeground(TEXT_SECONDARY);
        rememberMeCheckbox.setOpaque(false);
        rememberMeCheckbox.setFocusPainted(false);

        JLabel forgotLabel = new JLabel("Forgot password?");
        forgotLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotLabel.setForeground(SECONDARY_BLUE);
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "Please contact your Faculty Administrator to reset your password.",
                        "Password Reset",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        optionsRow.add(rememberMeCheckbox, BorderLayout.WEST);
        optionsRow.add(forgotLabel, BorderLayout.EAST);

        // Login Button
        loginButton = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_PRESSED);
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_HOVER);
                } else {
                    g2.setColor(PRIMARY_NAVY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setPreferredSize(new Dimension(320, 40));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        // Quick Launch Lecturer Dashboard
        quickLecturerButton = new JButton("Launch Lecturer Dashboard Directly") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(219, 234, 254));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(228, 238, 252));
                } else {
                    g2.setColor(LIGHT_BLUE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(29, 78, 216, 120));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        quickLecturerButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        quickLecturerButton.setForeground(SECONDARY_BLUE);
        quickLecturerButton.setFocusPainted(false);
        quickLecturerButton.setContentAreaFilled(false);
        quickLecturerButton.setBorderPainted(false);
        quickLecturerButton.setOpaque(false);
        quickLecturerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quickLecturerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        quickLecturerButton.setPreferredSize(new Dimension(320, 36));
        quickLecturerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickLecturerButton.addActionListener(e -> openLecturerDashboard(null));

        // Enter key action
        ActionListener submitAction = e -> handleLogin();
        usernameField.addActionListener(submitAction);
        passwordField.addActionListener(submitAction);

        rightPanel.add(welcomeHeader);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(welcomeSub);
        rightPanel.add(Box.createVerticalStrut(24));
        rightPanel.add(usernameLabel);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(usernameField);
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(passwordLabel);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(passRow);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(optionsRow);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(loginButton);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(quickLecturerButton);
        rightPanel.add(Box.createVerticalGlue());

        return rightPanel;
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordField.setEchoChar((char) 0);
            eyeButton.setText("Hide");
        } else {
            passwordField.setEchoChar(defaultEchoChar);
            eyeButton.setText("Show");
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        char[] pass = passwordField.getPassword();
        String password = new String(pass).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username.", "Validation", JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Validation", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocusInWindow();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT user_id, username, password, role, first_name, last_name, status " +
                         "FROM users WHERE username = ? OR email = ? OR user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, username);
                ps.setString(3, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String dbPass = rs.getString("password");
                        String role = rs.getString("role");
                        String userId = rs.getString("user_id");
                        String status = rs.getString("status");

                        if ("INACTIVE".equalsIgnoreCase(status)) {
                            JOptionPane.showMessageDialog(this, "Account is inactive. Please contact admin.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        if (dbPass != null && dbPass.equals(password)) {
                            openRoleDashboard(role, userId);
                        } else {
                            JOptionPane.showMessageDialog(this, "Incorrect password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                            passwordField.setText("");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "User not found in database.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            int opt = JOptionPane.showConfirmDialog(
                    this,
                    "Database connection unavailable (" + ex.getMessage() + ").\n" +
                    "Would you like to open the Lecturer Dashboard in demo/fallback mode?",
                    "Database Offline",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (opt == JOptionPane.YES_OPTION) {
                openLecturerDashboard(null);
            }
        } finally {
            Arrays.fill(pass, '0');
        }
    }

    private void openRoleDashboard(String role, String userId) {
        if ("LECTURER".equalsIgnoreCase(role)) {
            openLecturerDashboard(userId);
        } else {
            // For other roles or testing
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Authenticated successfully as: " + role + "\nOpen Lecturer Dashboard to view lecturer views?",
                    "Login Successful",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{"Open Lecturer Dashboard", "Close"},
                    "Open Lecturer Dashboard"
            );
            if (choice == 0) {
                openLecturerDashboard(userId);
            }
        }
    }

    private void openLecturerDashboard(String lecturerId) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new LecturerDashboardUI(lecturerId).setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginFrame().setVisible(true);
        });
    }
}

