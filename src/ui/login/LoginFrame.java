package ui.login;

import database.DBConnection;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.admin.AdminDashboard;
import ui.admin.NoticeViewerFrame;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class LoginFrame extends JFrame {

    // Theme Palette - Harmonized with Admin Dashboard
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color LIGHT_BLUE      = new Color(234, 242, 255); // #EAF2FF
    private static final Color BG_LEFT_TOP     = new Color(245, 248, 254);
    private static final Color BG_LEFT_BOTTOM  = new Color(228, 238, 252);
    private static final Color PRIMARY_BLUE    = new Color(29, 78, 216);   // #1D4ED8
    private static final Color PRIMARY_HOVER   = new Color(16, 40, 74);    // #10284A
    private static final Color PRIMARY_PRESSED = new Color(10, 25, 47);    // #0A192F

    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_SECONDARY  = new Color(71, 85, 105);
    private static final Color TEXT_MUTED      = new Color(100, 116, 139);
    private static final Color BORDER_COLOR    = new Color(203, 213, 225);
    private static final Color BG_INPUT        = new Color(248, 250, 252);

    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton eyeButton;
    private JCheckBox rememberMeCheckbox;
    private JLabel forgotPasswordLabel;
    private JButton loginButton;

    private boolean isPasswordVisible = false;
    private char defaultEchoChar = '•';
    private BufferedImage illustrationImage;

    public LoginFrame() {
        setTitle("Faculty Academic Management System - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 550));
        setMinimumSize(new Dimension(800, 500));
        setSize(900, 550);
        setLocationRelativeTo(null);

        loadIllustrationImage();
        initUI();
    }


     //Loads the exact 3D illustration image from resources or file path.

    private void loadIllustrationImage() {
        try {
            URL imgUrl = getClass().getResource("/ui/login/login_illustration.jpg");
            if (imgUrl == null) {
                imgUrl = getClass().getResource("login_illustration.jpg");
            }
            if (imgUrl != null) {
                illustrationImage = ImageIO.read(imgUrl);
            } else {
                File file = new File("src/ui/login/login_illustration.jpg");
                if (file.exists()) {
                    illustrationImage = ImageIO.read(file);
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load illustration image: " + e.getMessage());
        }
    }

    private void initUI() {
        // Main container with 42% / 58% split using GridBagLayout
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

        // Configure default Enter key behavior
        getRootPane().setDefaultButton(loginButton);
    }


     //Creates the Left Panel (42% width):
     //Soft light-blue gradient background, FAMS header branding, descriptive platform summary,
     // the exact 3D academic/technology illustration image card.

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Light blue gradient background
                GradientPaint bgGradient = new GradientPaint(
                        0, 0, BG_LEFT_TOP,
                        0, getHeight(), BG_LEFT_BOTTOM
                );
                g2.setPaint(bgGradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle ambient geometric shapes in Admin Navy & Blue
                g2.setColor(new Color(16, 40, 74, 15));
                g2.fillOval(-40, -40, 160, 160);
                g2.setColor(new Color(29, 78, 216, 20));
                g2.fillOval(getWidth() - 90, getHeight() / 2 - 50, 150, 150);

                // Draw exact 3D illustration image in the lower section with Admin Dashboard colors
                if (illustrationImage != null) {
                    int padX = 24;
                    int targetW = getWidth() - (padX * 2);
                    // Maintain aspect ratio (735 x 490 = 3:2)
                    int targetH = (int) (targetW * (490.0 / 735.0));
                    int targetY = getHeight() - targetH - 24;

                    // 1. Soft Navy card shadow
                    g2.setColor(new Color(16, 40, 74, 38));
                    g2.fill(new RoundRectangle2D.Float(padX - 2, targetY + 3, targetW + 4, targetH + 4, 18, 18));

                    // 2. Admin Light Blue container mat / backing
                    RoundRectangle2D.Float matRect = new RoundRectangle2D.Float(padX - 3, targetY - 3, targetW + 6, targetH + 6, 18, 18);
                    g2.setColor(LIGHT_BLUE);
                    g2.fill(matRect);

                    // 3. Admin Secondary Blue accent border around the mat
                    g2.setColor(new Color(29, 78, 216, 120));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(matRect);

                    // 4. Rounded card clip for the image
                    RoundRectangle2D.Float clipRect = new RoundRectangle2D.Float(padX, targetY, targetW, targetH, 14, 14);
                    Shape oldClip = g2.getClip();
                    g2.clip(clipRect);
                    g2.drawImage(illustrationImage, padX, targetY, targetW, targetH, null);
                    g2.setClip(oldClip);

                    // 5. Crisp inner border
                    g2.setColor(new Color(255, 255, 255, 230));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(clipRect);
                }

                g2.dispose();
            }
        };
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(378, 550));

        // Content branding & description panel (Logo removed, title sized up)
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(30, 28, 8, 28));

        // Sized-up Faculty Academic Management System title matching sign button color
        JLabel appCategoryLabel = new JLabel("Faculty Academic Management System");
        appCategoryLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        appCategoryLabel.setForeground(SECONDARY_BLUE);
        appCategoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Main Welcome Heading
        JLabel welcomeHeading = new JLabel("Welcome Back!");
        welcomeHeading.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcomeHeading.setForeground(TEXT_PRIMARY);
        welcomeHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        // FAMS Description Section
        JLabel famsHeadline = new JLabel("One Platform. Every Academic Need.");
        famsHeadline.setFont(new Font("SansSerif", Font.BOLD, 12));
        famsHeadline.setForeground(SECONDARY_BLUE);
        famsHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel famsDesc = new JLabel(
                "<html><body style='width: 315px; color: #475569; font-family: sans-serif; font-size: 10px; line-height: 1.4;'>" +
                "FAMS is a user-friendly Faculty Academic Management System designed to simplify " +
                "academic activities for students, lecturers, technical officers, and administrators." +
                "</body></html>"
        );
        famsDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(appCategoryLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(welcomeHeading);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(famsHeadline);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(famsDesc);

        leftPanel.add(contentPanel, BorderLayout.NORTH);

        return leftPanel;
    }

      //Creates the Right Panel (58% width):
    //Pure white background, clean Sign In header, modern rounded inputs, custom vector eye
     //password visibility toggle, remember me, forgot password, prominent royal-blue login button.
    private JPanel createRightPanel() {
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setBackground(Color.WHITE);
        rightWrapper.setPreferredSize(new Dimension(522, 550));

        // Inner form container with fixed comfortable width
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 48, 20, 48));
        formPanel.setPreferredSize(new Dimension(420, 480));
        formPanel.setMaximumSize(new Dimension(440, 500));

        // 1. Heading & Subtitle
        JLabel signInHeading = new JLabel("Sign In");
        signInHeading.setFont(new Font("SansSerif", Font.BOLD, 26));
        signInHeading.setForeground(TEXT_PRIMARY);
        signInHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel signInSubtitle = new JLabel("Access your account to continue");
        signInSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        signInSubtitle.setForeground(TEXT_MUTED);
        signInSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Username Label & Field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        usernameLabel.setForeground(TEXT_PRIMARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        usernameField.setForeground(TEXT_PRIMARY);
        usernameField.setOpaque(false);
        usernameField.setBorder(new EmptyBorder(8, 12, 8, 12));

        RoundedFieldContainer usernameContainer = new RoundedFieldContainer();
        usernameContainer.setLayout(new BorderLayout());
        usernameContainer.add(usernameField, BorderLayout.CENTER);
        usernameContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameContainer.setPreferredSize(new Dimension(320, 42));
        usernameContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                usernameContainer.setFocused(true);
            }
            @Override
            public void focusLost(FocusEvent e) {
                usernameContainer.setFocused(false);
            }
        });

        // 3. Password Label & Field with Custom Vector Eye Show/Hide toggle
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passwordLabel.setForeground(TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setOpaque(false);
        passwordField.setBorder(new EmptyBorder(8, 12, 8, 4));

        defaultEchoChar = passwordField.getEchoChar();

        // Eye Button painted directly with Java2D
        eyeButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;

                Color iconColor = getModel().isRollover() ? PRIMARY_BLUE : TEXT_MUTED;
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Eye contour
                Path2D.Double eyeContour = new Path2D.Double();
                eyeContour.moveTo(cx - 9, cy);
                eyeContour.curveTo(cx - 5, cy - 6, cx + 5, cy - 6, cx + 9, cy);
                eyeContour.curveTo(cx + 5, cy + 6, cx - 5, cy + 6, cx - 9, cy);
                eyeContour.closePath();
                g2.draw(eyeContour);

                // Pupil
                g2.fillOval(cx - 3, cy - 3, 6, 6);

                if (!isPasswordVisible) {
                    // Diagonal slash when masked/hidden
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx - 8, cy - 7, cx + 8, cy + 7);
                }

                g2.dispose();
            }
        };
        eyeButton.setContentAreaFilled(false);
        eyeButton.setBorderPainted(false);
        eyeButton.setFocusPainted(false);
        eyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeButton.setPreferredSize(new Dimension(36, 36));
        eyeButton.setToolTipText("Show password");
        eyeButton.addActionListener(e -> togglePasswordVisibility());

        RoundedFieldContainer passwordContainer = new RoundedFieldContainer();
        passwordContainer.setLayout(new BorderLayout());
        passwordContainer.add(passwordField, BorderLayout.CENTER);
        passwordContainer.add(eyeButton, BorderLayout.EAST);
        passwordContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordContainer.setPreferredSize(new Dimension(320, 42));
        passwordContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordContainer.setFocused(true);
            }
            @Override
            public void focusLost(FocusEvent e) {
                passwordContainer.setFocused(false);
            }
        });

        // 4. Remember Me & Forgot Password Row
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setBackground(Color.WHITE);
        optionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        optionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        rememberMeCheckbox = new JCheckBox("Remember Me");
        rememberMeCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rememberMeCheckbox.setForeground(TEXT_MUTED);
        rememberMeCheckbox.setBackground(Color.WHITE);
        rememberMeCheckbox.setFocusPainted(false);
        rememberMeCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        forgotPasswordLabel.setForeground(PRIMARY_BLUE);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordMessage();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPasswordLabel.setForeground(PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                forgotPasswordLabel.setForeground(PRIMARY_BLUE);
            }
        });

        optionsRow.add(rememberMeCheckbox, BorderLayout.WEST);
        optionsRow.add(forgotPasswordLabel, BorderLayout.EAST);

        // 5. Prominent Navy & Blue LOGIN Button (Admin Dashboard Theme)
        loginButton = new RoundedButton("LOGIN", PRIMARY_NAVY, SECONDARY_BLUE, PRIMARY_PRESSED, 8);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setPreferredSize(new Dimension(320, 42));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        // Pressing Enter in text fields triggers login
        usernameField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        // 6. Security Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        footerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent lockIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(TEXT_MUTED);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawArc(3, 1, 6, 6, 0, 180);
                g2.fillRoundRect(2, 4, 8, 7, 2, 2);

                g2.dispose();
            }
        };
        lockIcon.setPreferredSize(new Dimension(13, 13));

        JLabel footerLabel = new JLabel("Secure access to your academic portal.");
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(TEXT_MUTED);

        footerPanel.add(lockIcon);
        footerPanel.add(footerLabel);

        // Assemble Form Components with balanced spacing
        formPanel.add(Box.createVerticalGlue());
        formPanel.add(signInHeading);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(signInSubtitle);
        formPanel.add(Box.createVerticalStrut(24));

        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(usernameContainer);
        formPanel.add(Box.createVerticalStrut(14));

        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(passwordContainer);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(optionsRow);
        formPanel.add(Box.createVerticalStrut(20));

        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(22));

        formPanel.add(footerPanel);
        formPanel.add(Box.createVerticalGlue());

        rightWrapper.add(formPanel, new GridBagConstraints());
        return rightWrapper;
    }

    /*
      Toggles password visibility between masked and plain text.
     */
    private void togglePasswordVisibility() {
        if (passwordField != null && defaultEchoChar == '•') {
            char ec = passwordField.getEchoChar();
            if (ec != 0) {
                defaultEchoChar = ec;
            }
        }
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordField.setEchoChar((char) 0);
            eyeButton.setToolTipText("Hide password");
        } else {
            passwordField.setEchoChar(defaultEchoChar);
            eyeButton.setToolTipText("Show password");
        }
        eyeButton.repaint();
    }

    /*
     Displays a temporary placeholder notice for the Forgot Password action.
     */
    private void showForgotPasswordMessage() {
        JOptionPane.showMessageDialog(
                this,
                "The self-service password reset feature is currently being integrated.\n" +
                "Please contact your university administrator for assistance.",
                "Forgot Password",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


     //Handles the database-authenticated login action:

    private void handleLogin() {
        String usernameInput = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String passwordInput = new String(passwordChars).trim();

        // 1. Validate required fields
        if (usernameInput.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username cannot be empty. Please enter your username, email, or ID.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            usernameField.requestFocusInWindow();
            return;
        }

        if (passwordInput.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Password cannot be empty. Please enter your password.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            passwordField.requestFocusInWindow();
            return;
        }

        // 2. Query database for user credentials
        String sql = "SELECT user_id, username, password, role, first_name, last_name, email, status "
                   + "FROM users WHERE username = ? OR email = ? OR user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usernameInput);
            pstmt.setString(2, usernameInput);
            pstmt.setString(3, usernameInput);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    String status = rs.getString("status");
                    String role = rs.getString("role");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String dbUsername = rs.getString("username");

                    // 3. Verify account status
                    if ("INACTIVE".equalsIgnoreCase(status)) {
                        JOptionPane.showMessageDialog(
                                this,
                                "This account is currently marked as INACTIVE. Please contact the administrator.",
                                "Account Inactive",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    // 4. Verify password
                    if (dbPassword != null && dbPassword.equals(passwordInput)) {
                        String displayName = (firstName != null && !firstName.isBlank())
                                ? (firstName + " " + (lastName != null ? lastName : "")).trim()
                                : dbUsername;

                        // 5. Open appropriate dashboard based on authenticated role
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            SwingUtilities.invokeLater(() -> {
                                AdminDashboard dashboard = new AdminDashboard(displayName);
                                dashboard.setVisible(true);
                            });
                            dispose();
                        } else {
                            String roleName = formatRoleName(role);
                            Object[] options = {"View Academic Notices", "OK"};
                            int choice = JOptionPane.showOptionDialog(
                                    this,
                                    "<html><body style='font-family: sans-serif; width: 300px;'>" +
                                    "<h4 style='color: #10284A; margin-bottom: 6px;'>Welcome, " + displayName + "!</h4>" +
                                    "<p style='color: #4A5568; line-height: 1.4;'>" +
                                    "You have successfully authenticated as <b>" + roleName + "</b>.<br><br>" +
                                    "The full " + roleName + " portal is currently scheduled for upcoming phases.<br>" +
                                    "You can view published faculty academic notices now." +
                                    "</p></body></html>",
                                    "Login Successful - " + roleName,
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    options,
                                    options[0]
                            );
                            if (choice == 0) {
                                SwingUtilities.invokeLater(() -> {
                                    NoticeViewerFrame viewer = new NoticeViewerFrame(role, displayName);
                                    viewer.setVisible(true);
                                });
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Invalid username or password. Please try again.",
                                "Authentication Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid username or password. Please try again.",
                            "Authentication Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                    passwordField.setText("");
                    usernameField.requestFocusInWindow();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "<html><body style='font-family: sans-serif; width: 300px;'>" +
                    "<h4 style='color: #DC2626; margin-bottom: 6px;'>Database Connection Error</h4>" +
                    "<p style='color: #4A5568; line-height: 1.4;'>" +
                    "Unable to connect to the database.<br><br>" +
                    "• Please verify that MySQL is started in XAMPP.<br>" +
                    "• Ensure the database <b>fams</b> is active.<br><br>" +
                    "<small style='color: #718096;'>Details: " + ex.getMessage() + "</small>" +
                    "</p></body></html>",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            Arrays.fill(passwordChars, '0');
        }
    }

    private String formatRoleName(String role) {
        if (role == null) return "User";
        switch (role.toUpperCase()) {
            case "ADMIN": return "Administrator";
            case "LECTURER": return "Lecturer";
            case "TECHNICAL_OFFICER": return "Technical Officer";
            case "UNDERGRADUATE": return "Undergraduate";
            default: return role;
        }
    }


    // Custom container panel with smooth rounded borders and dynamic focus highlighting
    private static class RoundedFieldContainer extends JPanel {
        private final int radius = 8;
        private boolean focused = false;

        public RoundedFieldContainer() {
            setOpaque(false);
        }

        public void setFocused(boolean focused) {
            this.focused = focused;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background fill
            g2.setColor(BG_INPUT);
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));

            // Border stroke
            if (focused) {
                // Focus ring in primary blue with subtle glow
                g2.setColor(new Color(37, 99, 235, 45));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, radius, radius));

                g2.setColor(PRIMARY_BLUE);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));
            } else {
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }


     //Custom button with rounded corners, anti-aliased rendering, and hover/press feedback.

    private static class RoundedButton extends JButton {
        private final Color normalColor;
        private final Color hoverColor;
        private final Color pressedColor;
        private final int radius;

        public RoundedButton(String text, Color normalColor, Color hoverColor, Color pressedColor, int radius) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.pressedColor = pressedColor;
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                GradientPaint gp = new GradientPaint(0, 0, normalColor, getWidth(), 0, hoverColor);
                g2.setPaint(gp);
            }

            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();

            super.paintComponent(g);
        }
    }
}
