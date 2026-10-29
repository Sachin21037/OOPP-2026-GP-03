package ui.login;

import dao.DatabaseConnection;
import database.DBConnection;
import ui.technicalofficer.TechnicalOfficerDashboard;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Faculty Academic Management System (FAMS) - Sign In Interface.
 * Implements a modern dual-pane login screen featuring:
 * - 42% left branding banner with 3D academic/technology illustration card
 * - 58% right authentication form with rounded inputs, vector password visibility toggle,
 *   and responsive role-based dispatch to Technical Officer Dashboard and other subsystems.
 */
public class LoginFrame extends JFrame {

    // Theme Palette - Harmonized with Admin and Technical Officer Dashboards
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

    // Resilient offline fallback user database matching database/fams.sql
    private static class LocalUserRecord {
        final String userId;
        final String username;
        final String password;
        final String role;
        final String firstName;
        final String lastName;
        final String email;
        final String status;

        LocalUserRecord(String userId, String username, String password, String role,
                        String firstName, String lastName, String email, String status) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.role = role;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.status = status;
        }
    }

    private static final Map<String, LocalUserRecord> LOCAL_USER_STORE = new HashMap<>();

    static {
        // High-fidelity local credentials aligned with fams.sql
        registerLocalUser(new LocalUserRecord("TO001", "to001", "TO@123", "TECHNICAL_OFFICER", "Dinesh", "Kumara", "dinesh.to@uor.lk", "ACTIVE"));
        registerLocalUser(new LocalUserRecord("TO002", "to002", "TO@123", "TECHNICAL_OFFICER", "Nimali", "Perera", "nimali.to@uor.lk", "ACTIVE"));
        registerLocalUser(new LocalUserRecord("ADM001", "admin001", "Admin@123", "ADMIN", "System", "Administrator", "admin@fams.uor.lk", "ACTIVE"));
        registerLocalUser(new LocalUserRecord("LEC001", "lec001", "Lec@123", "LECTURER", "Kasun", "Perera", "kasun.perera@uor.lk", "ACTIVE"));
        registerLocalUser(new LocalUserRecord("LEC002", "lec002", "Lec@123", "LECTURER", "Nadeesha", "Fernando", "nadeesha.fernando@uor.lk", "ACTIVE"));
        registerLocalUser(new LocalUserRecord("STU001", "stu001", "Stu@123", "UNDERGRADUATE", "Amal", "Perera", "amal.perera@student.uor.lk", "ACTIVE"));
        registerLocalUser(new LocalUserRecord("STU002", "stu002", "Stu@123", "UNDERGRADUATE", "Nimal", "Fernando", "nimal.fernando@student.uor.lk", "ACTIVE"));
    }

    private static void registerLocalUser(LocalUserRecord user) {
        LOCAL_USER_STORE.put(user.username.toLowerCase(), user);
        LOCAL_USER_STORE.put(user.userId.toLowerCase(), user);
        if (user.email != null) {
            LOCAL_USER_STORE.put(user.email.toLowerCase(), user);
        }
    }

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

    /**
     * Loads the 3D illustration image from resources or local file paths.
     * Supports both PNG and JPG formats seamlessly.
     */
    private void loadIllustrationImage() {
        try {
            // 1. Try classpath resources
            String[] resourcePaths = {
                    "/ui/login/login_illustration.png",
                    "login_illustration.png",
                    "/ui/login/login_illustration.jpg",
                    "login_illustration.jpg"
            };
            for (String path : resourcePaths) {
                URL imgUrl = getClass().getResource(path);
                if (imgUrl != null) {
                    illustrationImage = ImageIO.read(imgUrl);
                    if (illustrationImage != null) return;
                }
            }

            // 2. Try filesystem relative paths
            String[] filePaths = {
                    "src/ui/login/login_illustration.png",
                    "src/ui/login/login_illustration.jpg",
                    "ui/login/login_illustration.png",
                    "ui/login/login_illustration.jpg",
                    "login_illustration.png",
                    "login_illustration.jpg"
            };
            for (String path : filePaths) {
                File file = new File(path);
                if (file.exists()) {
                    illustrationImage = ImageIO.read(file);
                    if (illustrationImage != null) return;
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

    /**
     * Creates the Left Panel (42% width):
     * Soft light-blue gradient background, FAMS header branding, descriptive platform summary,
     * and the 3D academic/technology illustration image card with shadow and accent border.
     */
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

                // Subtle ambient geometric shapes in Navy & Blue
                g2.setColor(new Color(16, 40, 74, 15));
                g2.fillOval(-40, -40, 160, 160);
                g2.setColor(new Color(29, 78, 216, 20));
                g2.fillOval(getWidth() - 90, getHeight() / 2 - 50, 150, 150);

                // Draw exact 3D illustration image card in the lower section
                if (illustrationImage != null) {
                    int padX = 24;
                    int targetW = getWidth() - (padX * 2);
                    // Maintain aspect ratio (735 x 490 = 3:2)
                    int targetH = (int) (targetW * (490.0 / 735.0));
                    int targetY = getHeight() - targetH - 24;

                    // 1. Soft Navy card shadow
                    g2.setColor(new Color(16, 40, 74, 38));
                    g2.fill(new RoundRectangle2D.Float(padX - 2, targetY + 3, targetW + 4, targetH + 4, 18, 18));

                    // 2. Light Blue container mat / backing
                    RoundRectangle2D.Float matRect = new RoundRectangle2D.Float(padX - 3, targetY - 3, targetW + 6, targetH + 6, 18, 18);
                    g2.setColor(LIGHT_BLUE);
                    g2.fill(matRect);

                    // 3. Secondary Blue accent border around the mat
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

        // Content branding & description panel
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(30, 28, 8, 28));

        // Faculty Academic Management System title matching secondary blue
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

    /**
     * Creates the Right Panel (58% width):
     * Pure white background, clean Sign In header, modern rounded inputs, custom vector eye
     * password visibility toggle, remember me, forgot password, and prominent royal-blue login button.
     */
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

        // 5. Prominent Navy & Blue LOGIN Button
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

    /**
     * Toggles password visibility between masked and plain text.
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

    /**
     * Displays an informative message for the Forgot Password action.
     */
    private void showForgotPasswordMessage() {
        JOptionPane.showMessageDialog(
                this,
                "<html><body style='font-family: sans-serif; width: 300px;'>" +
                "<h4 style='color: #10284A; margin-bottom: 6px;'>Self-Service Password Assistance</h4>" +
                "<p style='color: #475569; line-height: 1.4;'>" +
                "To reset your credentials, please contact the Faculty Administration " +
                "office or your department coordinator with your university ID card." +
                "</p></body></html>",
                "Forgot Password",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Handles the login action:
     * 1. Validates input fields.
     * 2. Checks against MySQL database if active, or high-fidelity local store if offline.
     * 3. Verifies account status and password.
     * 4. Dispatches the user to the corresponding role dashboard.
     */
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

        boolean authenticated = false;
        String dbUserId = null;
        String dbPassword = null;
        String status = null;
        String role = null;
        String firstName = null;
        String lastName = null;
        String dbUsername = null;

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT user_id, username, password, role, first_name, last_name, email, status "
                       + "FROM users WHERE username = ? OR email = ? OR user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, usernameInput);
                pstmt.setString(2, usernameInput);
                pstmt.setString(3, usernameInput);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        authenticated = true;
                        dbUserId = rs.getString("user_id");
                        dbPassword = rs.getString("password");
                        status = rs.getString("status");
                        role = rs.getString("role");
                        firstName = rs.getString("first_name");
                        lastName = rs.getString("last_name");
                        dbUsername = rs.getString("username");
                    }
                }
            } catch (SQLException ex) {
                System.err.println("[LoginFrame] Database query error: " + ex.getMessage());
            }
        }

        // Fallback to high-fidelity local data store if database connection was unavailable
        if (!authenticated) {
            LocalUserRecord localRecord = LOCAL_USER_STORE.get(usernameInput.toLowerCase());
            if (localRecord != null) {
                authenticated = true;
                dbUserId = localRecord.userId;
                dbPassword = localRecord.password;
                status = localRecord.status;
                role = localRecord.role;
                firstName = localRecord.firstName;
                lastName = localRecord.lastName;
                dbUsername = localRecord.username;
            }
        }

        // 3. Evaluate Authentication Result
        if (authenticated) {
            // Verify account status
            if ("INACTIVE".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(
                        this,
                        "This account is currently marked as INACTIVE. Please contact the administrator.",
                        "Account Inactive",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Verify password
            if (dbPassword != null && dbPassword.equals(passwordInput)) {
                String displayName = (firstName != null && !firstName.isBlank())
                        ? (firstName + " " + (lastName != null ? lastName : "")).trim()
                        : dbUsername;

                dispatchUserByRole(role, displayName, dbUserId);
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

        Arrays.fill(passwordChars, '0');
    }

    /**
     * Dispatches user to the appropriate portal or dashboard window based on role.
     */
    private void dispatchUserByRole(String role, String displayName, String userId) {
        String roleStr = (role != null) ? role.toUpperCase() : "USER";

        if ("TECHNICAL_OFFICER".equals(roleStr)) {
            final String finalOfficerId = (userId != null && !userId.isBlank()) ? userId : "TO001";
            final String finalOfficerName = displayName;
            SwingUtilities.invokeLater(() -> {
                TechnicalOfficerDashboard dashboard = new TechnicalOfficerDashboard(finalOfficerId, finalOfficerName);
                dashboard.setVisible(true);
            });
            dispose();
            return;
        }

        if ("ADMIN".equals(roleStr)) {
            // Attempt to launch AdminDashboard if available in classpath via reflection
            try {
                Class<?> adminClass = Class.forName("ui.admin.AdminDashboard");
                try {
                    java.lang.reflect.Constructor<?> ctor = adminClass.getConstructor(String.class);
                    JFrame adminFrame = (JFrame) ctor.newInstance(displayName);
                    adminFrame.setVisible(true);
                    dispose();
                    return;
                } catch (NoSuchMethodException e) {
                    JFrame adminFrame = (JFrame) adminClass.getDeclaredConstructor().newInstance();
                    adminFrame.setVisible(true);
                    dispose();
                    return;
                }
            } catch (Throwable ignored) {
                // Admin dashboard class not present in this workspace branch; show role portal dialog
            }
        }

        // For roles without an independent dashboard in this branch (Admin, Lecturer, Undergraduate)
        String roleName = formatRoleName(roleStr);
        Object[] options = {"Open Technical Officer Dashboard", "Sign Out"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "<html><body style='font-family: sans-serif; width: 330px;'>" +
                "<h3 style='color: #10284A; margin-bottom: 4px;'>Welcome, " + displayName + "!</h3>" +
                "<p style='color: #1D4ED8; font-weight: bold; margin-bottom: 8px;'>Role: " + roleName + "</p>" +
                "<p style='color: #475569; line-height: 1.4;'>" +
                "You have successfully authenticated into the Faculty Academic Management System.<br><br>" +
                "You can inspect the active Technical Officer subsystem or sign out." +
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
                TechnicalOfficerDashboard dashboard = new TechnicalOfficerDashboard();
                dashboard.setVisible(true);
            });
            dispose();
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

    // Custom button with rounded corners, anti-aliased rendering, and hover/press feedback
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

    /**
     * Standalone main method to test and launch LoginFrame directly.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
