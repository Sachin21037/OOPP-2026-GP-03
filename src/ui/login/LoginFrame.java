package ui.login;

import dao.StudentDAO;
import database.DBConnection;
import model.Student;
import ui.admin.AdminDashboard;
import ui.admin.NoticeViewerFrame;
import ui.admin.TimetableViewerFrame;
import ui.lecturer.LecturerDashboardUI;
import ui.student.StudentDashboard;
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

/**
 * Modern University Login Interface for Faculty Academic Management System (FAMS).
 * Reproduces 100% the layout, structure, and aesthetic of the reference LMS interface:
 * - Full-bleed photographic & artistic background (university campus, desk, laptop, books, green curves).
 * - Left side: Academic Emblem, "FAMS" wordmark, "Faculty Academic Management System", and "Learn • Access • Excel" divider.
 * - Right side: Floating white rounded card with drop shadow, "Welcome Back!", "Sign in to continue",
 *   icon-adorned username and password fields with vector eye visibility toggle,
 *   administration contact notice, "Login →" deep sage pill button, and graduation cap footer divider.
 */
public class LoginFrame extends JFrame {

    // Palette harmonized with Soft Sage Green theme and Reference Image
    private static final Color SAGE_DEEP        = new Color(47, 82, 68);    // #2F5244 Deep Academic Sage (Emblem, FAMS)
    private static final Color SAGE_ACCENT      = new Color(59, 91, 76);    // #3B5B4C Login Button Base Color
    private static final Color SAGE_HOVER       = new Color(45, 73, 60);    // #2D493C Login Button Hover Color
    private static final Color SAGE_PRESSED     = new Color(32, 53, 44);    // #20352C Login Button Pressed Color
    private static final Color GOLD_ARCH        = new Color(205, 166, 92);  // #CDA65C Emblem Gold Arch
    private static final Color TEXT_DARK        = new Color(31, 56, 47);    // #1F382F "Welcome Back!" heading
    private static final Color TEXT_MUTED       = new Color(100, 116, 139); // #64748B Subtitle, placeholders, footers
    private static final Color ICON_MUTED       = new Color(148, 163, 184); // #94A3B8 Input field icons
    private static final Color BORDER_INPUT     = new Color(209, 213, 219); // #D1D5DB Input border
    private static final Color BORDER_FOCUS     = new Color(42, 119, 102);  // #2A7766 Focus border

    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton eyeButton;
    private JButton loginButton;

    private boolean isPasswordVisible = false;
    private char defaultEchoChar = '•';
    private BufferedImage backgroundImage;

    private final StudentDAO studentDAO = new StudentDAO();

    public LoginFrame() {
        setTitle("Faculty Academic Management System - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 600));
        setMinimumSize(new Dimension(960, 560));
        setSize(1024, 600);
        setLocationRelativeTo(null);

        loadBackgroundImage();
        initUI();
    }

    /**
     * Loads the high-resolution clean background image from resources or local directories.
     */
    private void loadBackgroundImage() {
        try {
            String[] resourcePaths = {
                    "/ui/login/login_bg.jpg",
                    "/ui/login/login_illustration.jpg",
                    "login_bg.jpg",
                    "login_illustration.jpg"
            };
            for (String path : resourcePaths) {
                URL imgUrl = getClass().getResource(path);
                if (imgUrl != null) {
                    backgroundImage = ImageIO.read(imgUrl);
                    if (backgroundImage != null) return;
                }
            }

            String[] filePaths = {
                    "src/ui/login/login_bg.jpg",
                    "src/ui/login/login_illustration.jpg",
                    "out/production/OOPP-2026-GP-03/ui/login/login_bg.jpg",
                    "out/production/OOPP-2026-GP-03/ui/login/login_illustration.jpg",
                    "bin/ui/login/login_bg.jpg",
                    "login_illustration.jpg"
            };
            for (String path : filePaths) {
                File file = new File(path);
                if (file.exists()) {
                    backgroundImage = ImageIO.read(file);
                    if (backgroundImage != null) return;
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load background image: " + e.getMessage());
        }
    }

    private void initUI() {
        JPanel rootPanel = buildRootPanel();
        setContentPane(rootPanel);

        // Default enter key triggers login
        if (loginButton != null) {
            getRootPane().setDefaultButton(loginButton);
        }
    }

    public JPanel buildRootPanel() {
        // Root panel that paints the full-bleed background image
        JPanel rootPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, w, h, null);
                } else {
                    // Fallback Soft Sage gradient
                    GradientPaint gp = new GradientPaint(0, 0, new Color(239, 245, 242), w, h, new Color(220, 236, 230));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, w, h);
                }
                g2.dispose();
            }
        };
        rootPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        // 1. Left Column (52% width) - Academic Branding (Emblem, FAMS, Title, Tagline divider)
        JPanel leftPanel = createLeftBrandingPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.52;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(leftPanel, gbc);

        // 2. Right Column (48% width) - Floating White Login Card
        JPanel rightPanel = createRightCardPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.48;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(rightPanel, gbc);

        return rootPanel;
    }

    /**
     * Creates the Left Branding Section matching 100% the reference structure:
     * - Vector Academic Emblem with gold arc and tassel
     * - Bold "FAMS" uppercase wordmark
     * - "Faculty Academic Management System" subtitle
     * - Elegant "Learn • Access • Excel" divider rule
     */
    private JPanel createLeftBrandingPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(22, 60, 20, 20));

        JPanel brandingBox = new JPanel();
        brandingBox.setOpaque(false);
        brandingBox.setLayout(new BoxLayout(brandingBox, BoxLayout.Y_AXIS));

        // Row 1: Academic Emblem + FAMS Title
        JPanel rowHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        rowHeader.setOpaque(false);
        rowHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        AcademicEmblem emblem = new AcademicEmblem();
        rowHeader.add(emblem);

        JLabel lblFAMS = new JLabel("FAMS");
        lblFAMS.setFont(new Font("SansSerif", Font.BOLD, 46));
        lblFAMS.setForeground(SAGE_DEEP);
        rowHeader.add(lblFAMS);

        brandingBox.add(rowHeader);
        brandingBox.add(Box.createVerticalStrut(2));

        // Row 2: Full System Name
        JLabel lblFullTitle = new JLabel("Faculty Academic Management System");
        lblFullTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblFullTitle.setForeground(SAGE_DEEP);
        lblFullTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandingBox.add(lblFullTitle);
        brandingBox.add(Box.createVerticalStrut(8));

        // Row 3: Tagline Divider rule
        LearnDivider divider = new LearnDivider();
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandingBox.add(divider);

        leftPanel.add(brandingBox, BorderLayout.NORTH);
        return leftPanel;
    }

    /**
     * Creates the Right Column containing the centered floating white card.
     */
    private JPanel createRightCardPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(20, 10, 20, 60));

        // Floating White Card
        FloatingCardPanel card = new FloatingCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 30, 24, 30));
        card.setPreferredSize(new Dimension(380, 480));
        card.setMaximumSize(new Dimension(410, 500));

        // 1. Heading: Welcome Back!
        JLabel lblHeading = new JLabel("Welcome Back!");
        lblHeading.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblHeading.setForeground(TEXT_DARK);
        lblHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Subtitle: Sign in to continue (as explicitly instructed by user)
        JLabel lblSubtitle = new JLabel("Sign in to continue");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Username Input Container with Person Icon (👤)
        ModernInputFieldContainer usernameContainer = new ModernInputFieldContainer();
        usernameContainer.setLayout(new BorderLayout());
        usernameContainer.setPreferredSize(new Dimension(320, 42));
        usernameContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Person icon component
        JComponent personIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.setColor(ICON_MUTED);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Head circle
                g2.drawOval(cx - 5, cy - 8, 10, 10);
                // Shoulders arc
                g2.drawArc(cx - 9, cy + 3, 18, 12, 35, 110);

                g2.dispose();
            }
        };
        personIcon.setPreferredSize(new Dimension(38, 42));

        usernameField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(156, 163, 175));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("Username", getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        usernameField.setForeground(TEXT_DARK);
        usernameField.setCaretColor(BORDER_FOCUS);
        usernameField.setOpaque(false);
        usernameField.setBorder(new EmptyBorder(0, 0, 0, 8));

        usernameField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { usernameContainer.setFocused(true); }
            @Override public void focusLost(FocusEvent e) { usernameContainer.setFocused(false); }
        });
        usernameField.addActionListener(e -> handleLogin());

        usernameContainer.add(personIcon, BorderLayout.WEST);
        usernameContainer.add(usernameField, BorderLayout.CENTER);

        // 4. Password Input Container with Lock Icon (🔒) and Eye toggle
        ModernInputFieldContainer passwordContainer = new ModernInputFieldContainer();
        passwordContainer.setLayout(new BorderLayout());
        passwordContainer.setPreferredSize(new Dimension(320, 42));
        passwordContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Lock icon component
        JComponent lockIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.setColor(ICON_MUTED);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Shackle
                g2.drawArc(cx - 5, cy - 8, 10, 10, 0, 180);
                // Body
                g2.drawRoundRect(cx - 7, cy - 2, 14, 11, 3, 3);
                // Keyhole
                g2.fillOval(cx - 1, cy + 2, 2, 2);
                g2.drawLine(cx, cy + 4, cx, cy + 6);

                g2.dispose();
            }
        };
        lockIcon.setPreferredSize(new Dimension(38, 42));

        passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(156, 163, 175));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("Password", getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordField.setForeground(TEXT_DARK);
        passwordField.setCaretColor(BORDER_FOCUS);
        passwordField.setOpaque(false);
        passwordField.setBorder(new EmptyBorder(0, 0, 0, 4));
        defaultEchoChar = passwordField.getEchoChar();

        passwordField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { passwordContainer.setFocused(true); }
            @Override public void focusLost(FocusEvent e) { passwordContainer.setFocused(false); }
        });
        passwordField.addActionListener(e -> handleLogin());

        // Eye Button painted with Java2D
        eyeButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                Color iconColor = getModel().isRollover() ? BORDER_FOCUS : ICON_MUTED;
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Eye contour
                Path2D.Double eyeContour = new Path2D.Double();
                eyeContour.moveTo(cx - 8, cy);
                eyeContour.curveTo(cx - 4, cy - 5, cx + 4, cy - 5, cx + 8, cy);
                eyeContour.curveTo(cx + 4, cy + 5, cx - 4, cy + 5, cx - 8, cy);
                eyeContour.closePath();
                g2.draw(eyeContour);

                // Pupil
                g2.fillOval(cx - 3, cy - 3, 6, 6);

                if (!isPasswordVisible) {
                    // Diagonal slash when password hidden
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx - 7, cy - 6, cx + 7, cy + 6);
                }

                g2.dispose();
            }
        };
        eyeButton.setContentAreaFilled(false);
        eyeButton.setBorderPainted(false);
        eyeButton.setFocusPainted(false);
        eyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeButton.setPreferredSize(new Dimension(36, 42));
        eyeButton.setToolTipText("Show / Hide password");
        eyeButton.addActionListener(e -> togglePasswordVisibility());

        passwordContainer.add(lockIcon, BorderLayout.WEST);
        passwordContainer.add(passwordField, BorderLayout.CENTER);
        passwordContainer.add(eyeButton, BorderLayout.EAST);

        // 5. Administration Notice (Replaces Remember Me & Forgot Password link as requested)
        JLabel forgotNotice = new JLabel("<html><center style='color:#64748B; font-family:sans-serif; font-size:11px; line-height:1.2;'>" +
                "If you forgot your password,<br>please contact the administration." +
                "</center></html>");
        forgotNotice.setHorizontalAlignment(SwingConstants.CENTER);
        forgotNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 6. Modern Login Button with Arrow (Login →)
        loginButton = new ModernLoginButton("Login  →");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        // 7. Divider with Graduation Cap Icon
        CardCapDivider capDivider = new CardCapDivider();
        capDivider.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 8. Card Footer Tagline
        JLabel lblTagline = new JLabel("Education Beyond Boundaries");
        lblTagline.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblTagline.setForeground(TEXT_MUTED);
        lblTagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble Form Components with balanced layout
        card.add(Box.createVerticalGlue());
        card.add(lblHeading);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(24));

        card.add(usernameContainer);
        card.add(Box.createVerticalStrut(14));

        card.add(passwordContainer);
        card.add(Box.createVerticalStrut(14));

        card.add(forgotNotice);
        card.add(Box.createVerticalStrut(16));

        card.add(loginButton);
        card.add(Box.createVerticalStrut(20));

        card.add(capDivider);
        card.add(Box.createVerticalStrut(8));

        card.add(lblTagline);
        card.add(Box.createVerticalGlue());

        rightPanel.add(card, new GridBagConstraints());
        return rightPanel;
    }

    /**
     * Toggles password visibility between masked and plain text.
     */
    private void togglePasswordVisibility() {
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
     * Handles the database-authenticated login action.
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

        // 2. Query database for user credentials
        String sql = "SELECT user_id, username, password, role, first_name, last_name, email, phone, address, status "
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
                    String userId = rs.getString("user_id");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");
                    String address = rs.getString("address");

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
                        } else if ("UNDERGRADUATE".equalsIgnoreCase(role)) {
                            Student student = studentDAO.getStudentProfile(userId);
                            if (student == null) {
                                student = new Student(
                                        userId, dbUsername, role, firstName, lastName,
                                        email, phone, address, status,
                                        "TG/2024/2001", "2024/2025", "Level II",
                                        "Information and Communication Technology", "REGULAR"
                                );
                            }
                            final Student s = student;
                            SwingUtilities.invokeLater(() -> {
                                dispose();
                                new StudentDashboard(s).setVisible(true);
                            });
                        } else if ("LECTURER".equalsIgnoreCase(role)) {
                            dispose();
                            SwingUtilities.invokeLater(() -> {
                                new LecturerDashboardUI(userId).setVisible(true);
                            });
                        } else if ("TECHNICAL_OFFICER".equalsIgnoreCase(role)) {
                            final String finalOfficerId = (userId != null && !userId.isBlank()) ? userId : "TO001";
                            final String finalOfficerName = displayName;
                            SwingUtilities.invokeLater(() -> {
                                TechnicalOfficerDashboard toDash = new TechnicalOfficerDashboard(finalOfficerId, finalOfficerName);
                                toDash.setVisible(true);
                            });
                            dispose();
                        } else {
                            String roleName = formatRoleName(role);
                            Object[] options = {"View Academic Timetable", "View Notices", "Close"};
                            int choice = JOptionPane.showOptionDialog(
                                    this,
                                    "<html><body style='font-family: sans-serif; width: 320px;'>" +
                                    "<h4 style='color: #10284A; margin-bottom: 6px;'>Welcome, " + displayName + "!</h4>" +
                                    "<p style='color: #4A5568; line-height: 1.4;'>" +
                                    "You have successfully authenticated as <b>" + roleName + "</b>.<br><br>" +
                                    "You can view your published academic timetable schedules and faculty notices below." +
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
                                    TimetableViewerFrame viewer = new TimetableViewerFrame(role, displayName);
                                    viewer.setVisible(true);
                                });
                            } else if (choice == 1) {
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

    // =========================================================================
    // CUSTOM VECTOR GRAPHICS & FLOATING CARD COMPONENTS
    // =========================================================================

    /**
     * Vector Academic Emblem:
     * Golden Arch with tassel + Deep Sage Mortarboard cap + Open Book pages.
     */
    public static class AcademicEmblem extends JComponent {
        public AcademicEmblem() {
            setPreferredSize(new Dimension(68, 68));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 1. Golden Arch
            g2.setColor(GOLD_ARCH);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(6, 6, 52, 48, 15, 150);

            // Tassel hanging on the right side of the arch
            g2.drawLine(53, 24, 53, 34);
            g2.fillOval(51, 34, 4, 5);

            // 2. Deep Sage Green Mortarboard Cap
            g2.setColor(SAGE_DEEP);
            Path2D.Double cap = new Path2D.Double();
            cap.moveTo(32, 16);
            cap.lineTo(54, 25);
            cap.lineTo(32, 34);
            cap.lineTo(10, 25);
            cap.closePath();
            g2.fill(cap);

            // Cap skull under the mortarboard
            Path2D.Double skull = new Path2D.Double();
            skull.moveTo(21, 30);
            skull.lineTo(21, 37);
            skull.curveTo(21, 42, 43, 42, 43, 37);
            skull.lineTo(43, 30);
            skull.closePath();
            g2.fill(skull);

            // 3. Open Book beneath cap
            Path2D.Double bookLeft = new Path2D.Double();
            bookLeft.moveTo(31, 46);
            bookLeft.curveTo(25, 43, 17, 43, 11, 45);
            bookLeft.lineTo(11, 55);
            bookLeft.curveTo(17, 53, 25, 54, 31, 58);
            bookLeft.closePath();
            g2.fill(bookLeft);

            Path2D.Double bookRight = new Path2D.Double();
            bookRight.moveTo(33, 46);
            bookRight.curveTo(39, 43, 47, 43, 53, 45);
            bookRight.lineTo(53, 55);
            bookRight.curveTo(47, 53, 39, 54, 33, 58);
            bookRight.closePath();
            g2.fill(bookRight);

            g2.dispose();
        }
    }

    /**
     * Elegant "Learn • Access • Excel" divider rule.
     */
    public static class LearnDivider extends JComponent {
        public LearnDivider() {
            setPreferredSize(new Dimension(320, 20));
            setMaximumSize(new Dimension(320, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String text = "Learn  •  Access  •  Excel";
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(text);
            int cy = getHeight() / 2;

            int totalW = 320;
            int textX = (totalW - textW) / 2;

            g2.setColor(new Color(93, 122, 114));
            g2.drawString(text, textX, cy + fm.getAscent() / 2 - 1);

            // Left line
            g2.setColor(new Color(178, 195, 188));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(8, cy, textX - 12, cy);

            // Right line
            g2.drawLine(textX + textW + 12, cy, totalW - 8, cy);

            g2.dispose();
        }
    }

    /**
     * Floating White Card with soft ambient drop shadow and rounded corners.
     */
    public static class FloatingCardPanel extends JPanel {
        private final int radius = 26;

        public FloatingCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Multi-layered soft drop shadow
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(2, 6, w - 4, h - 6, radius, radius);
            g2.setColor(new Color(0, 0, 0, 16));
            g2.fillRoundRect(3, 8, w - 6, h - 8, radius, radius);
            g2.setColor(new Color(0, 0, 0, 24));
            g2.fillRoundRect(4, 12, w - 8, h - 12, radius, radius);

            // Crisp white background (translucent 98%)
            g2.setColor(new Color(255, 255, 255, 252));
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            // Subtle border
            g2.setColor(new Color(226, 232, 240, 220));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Rounded input container with animated border glow on focus.
     */
    public static class ModernInputFieldContainer extends JPanel {
        private final int radius = 12;
        private boolean focused = false;

        public ModernInputFieldContainer() {
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
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));

            if (focused) {
                // Focus glow ring
                g2.setColor(new Color(BORDER_FOCUS.getRed(), BORDER_FOCUS.getGreen(), BORDER_FOCUS.getBlue(), 40));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, radius, radius));

                // Inner border
                g2.setColor(BORDER_FOCUS);
                g2.setStroke(new BasicStroke(1.3f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));
            } else {
                // Standard border
                g2.setColor(BORDER_INPUT);
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Deep Sage rounded pill button with arrow (Login →).
     */
    public static class ModernLoginButton extends JButton {
        public ModernLoginButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(320, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int radius = h; // pill shape (22px)

            if (getModel().isPressed()) {
                g2.setColor(SAGE_PRESSED);
            } else if (getModel().isRollover()) {
                g2.setColor(SAGE_HOVER);
            } else {
                g2.setColor(SAGE_ACCENT);
            }

            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            // Centered high-contrast text with arrow
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent() + (getModel().isPressed() ? 1 : 0);
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    /**
     * Subtle horizontal rule with a graduation cap icon in the center.
     */
    public static class CardCapDivider extends JComponent {
        public CardCapDivider() {
            setPreferredSize(new Dimension(300, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int cy = getHeight() / 2;
            int cx = w / 2;

            // Subtle divider lines
            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(20, cy, cx - 16, cy);
            g2.drawLine(cx + 16, cy, w - 20, cy);

            // Graduation Cap Icon in Center
            g2.setColor(new Color(93, 122, 114));

            // Mortarboard diamond
            Path2D.Double cap = new Path2D.Double();
            cap.moveTo(cx, cy - 6);
            cap.lineTo(cx + 11, cy - 2);
            cap.lineTo(cx, cy + 2);
            cap.lineTo(cx - 11, cy - 2);
            cap.closePath();
            g2.fill(cap);

            // Cap skull
            Path2D.Double neck = new Path2D.Double();
            neck.moveTo(cx - 6, cy);
            neck.lineTo(cx - 6, cy + 4);
            neck.curveTo(cx - 6, cy + 6, cx + 6, cy + 6, cx + 6, cy + 4);
            neck.lineTo(cx + 6, cy);
            neck.closePath();
            g2.fill(neck);

            // Tassel
            g2.drawLine(cx + 11, cy - 2, cx + 11, cy + 3);
            g2.fillOval(cx + 10, cy + 3, 2, 2);

            g2.dispose();
        }
    }

    /**
     * Main method for standalone testing of the modern LoginFrame.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
