package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    // Colors
    private static final Color NAVY = new Color(20, 45, 75);
    private static final Color BLUE = new Color(37, 99, 166);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color LIGHT_BG = new Color(247, 249, 252);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_GRAY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(220, 226, 234);

    // Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton showPasswordButton;
    private JCheckBox rememberMe;
    private JLabel messageLabel;

    public LoginFrame() {

        setTitle("FAMS - Faculty Academic Management System");
        setSize(1000, 620);
        setMinimumSize(new Dimension(900, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT_BG);
        setContentPane(mainPanel);

        //left panel

        JPanel leftPanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int width = getWidth();
                int height = getHeight();

                // Background
                g2.setColor(NAVY);
                g2.fillRect(0, 0, width, height);

                // Decorative circles
                g2.setColor(new Color(37, 99, 166, 80));
                g2.fillOval(-100, height - 180, 300, 300);

                g2.setColor(new Color(20, 184, 166, 55));
                g2.fillOval(width - 160, -100, 260, 260);

                // Decorative small circles
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillOval(45, 90, 12, 12);
                g2.fillOval(width - 70, 250, 8, 8);
                g2.fillOval(80, 410, 7, 7);

                g2.dispose();
            }
        };

        leftPanel.setPreferredSize(new Dimension(500, 620));
        leftPanel.setLayout(new BorderLayout());

        // Branding content
        JPanel brandingPanel = new JPanel();
        brandingPanel.setOpaque(false);
        brandingPanel.setLayout(new BoxLayout(brandingPanel, BoxLayout.Y_AXIS));
        brandingPanel.setBorder(new EmptyBorder(45, 55, 30, 55));

        JLabel logoLabel = new JLabel("FAMS");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 38));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Faculty Academic Management System");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(210, 225, 240));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandingPanel.add(logoLabel);
        brandingPanel.add(Box.createVerticalStrut(5));
        brandingPanel.add(subtitleLabel);

        // Illustration
        UniversityIllustration illustration = new UniversityIllustration();
        illustration.setOpaque(false);
        illustration.setPreferredSize(new Dimension(390, 260));
        illustration.setMaximumSize(new Dimension(390, 260));
        illustration.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandingPanel.add(Box.createVerticalStrut(20));
        brandingPanel.add(illustration);
        brandingPanel.add(Box.createVerticalGlue());

        // Description
        JLabel descriptionLabel = new JLabel(
                "<html><div style='width:360px; text-align:center;'>" +
                        "Manage academic activities, connect faculty members, " +
                        "and access everything you need from one place." +
                        "</div></html>"
        );

        descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descriptionLabel.setForeground(new Color(210, 225, 240));
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandingPanel.add(descriptionLabel);
        brandingPanel.add(Box.createVerticalStrut(25));

        leftPanel.add(brandingPanel, BorderLayout.CENTER);

       //login form

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(LIGHT_BG);

        JPanel loginCard = new RoundedPanel(25, Color.WHITE);
        loginCard.setPreferredSize(new Dimension(400, 500));
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(new EmptyBorder(40, 45, 35, 45));

        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        welcomeLabel.setForeground(TEXT_DARK);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel signInLabel = new JLabel("Sign in to continue to FAMS");
        signInLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        signInLabel.setForeground(TEXT_GRAY);
        signInLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginCard.add(welcomeLabel);
        loginCard.add(Box.createVerticalStrut(7));
        loginCard.add(signInLabel);
        loginCard.add(Box.createVerticalStrut(35));

        // Username
        JLabel usernameLabel = createFieldLabel("Username");
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        styleTextField(usernameField);
        usernameField.setToolTipText("Enter your username");

        loginCard.add(usernameLabel);
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(usernameField);
        loginCard.add(Box.createVerticalStrut(20));

        // Password
        JLabel passwordLabel = createFieldLabel("Password");
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setOpaque(false);
        passwordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        passwordPanel.setPreferredSize(new Dimension(310, 48));
        passwordPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setEchoChar('•');
        passwordField.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 45));

        showPasswordButton = new JButton("●");
        showPasswordButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        showPasswordButton.setForeground(TEXT_GRAY);
        showPasswordButton.setBackground(Color.WHITE);
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder());
        showPasswordButton.setFocusPainted(false);
        showPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordButton.setToolTipText("Show / Hide password");

        showPasswordButton.addActionListener(e -> togglePassword());

        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordButton, BorderLayout.EAST);

        loginCard.add(passwordLabel);
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(passwordPanel);
        loginCard.add(Box.createVerticalStrut(15));

        // Remember me
        rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rememberMe.setForeground(TEXT_GRAY);
        rememberMe.setBackground(Color.WHITE);
        rememberMe.setFocusPainted(false);
        rememberMe.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginCard.add(rememberMe);
        loginCard.add(Box.createVerticalStrut(18));

        // Message label
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(220, 70, 70));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginCard.add(messageLabel);
        loginCard.add(Box.createVerticalStrut(5));

        // Login button
        loginButton = new JButton("SIGN IN");
        styleLoginButton();

        loginCard.add(loginButton);
        loginCard.add(Box.createVerticalStrut(20));

        // Footer
        JLabel footerLabel = new JLabel("Faculty Academic Management System");
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(150, 160, 175));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginCard.add(footerLabel);

        // Login action
        loginButton.addActionListener(e -> handleLogin());

        // Enter key login
        getRootPane().setDefaultButton(loginButton);

        rightPanel.add(loginCard);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Focus username field when application starts
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    //field lable

    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);

        return label;
    }


    private void styleTextField(JTextField field) {

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT_DARK);
        field.setBackground(Color.WHITE);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1),
                        BorderFactory.createEmptyBorder(0, 14, 0, 14)
                )
        );

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setPreferredSize(new Dimension(310, 48));

        // Focus effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {

                field.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BLUE, 2),
                                BorderFactory.createEmptyBorder(0, 13, 0, 13)
                        )
                );
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {

                field.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER, 1),
                                BorderFactory.createEmptyBorder(0, 14, 0, 14)
                        )
                );
            }
        });
    }


    // LOGIN BUTTON STYLE


    private void styleLoginButton() {

        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(BLUE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginButton.setPreferredSize(new Dimension(310, 48));

        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {

                loginButton.setBackground(new Color(30, 82, 145));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {

                loginButton.setBackground(BLUE);
            }
        });
    }

    // PASSWORD SHOW / HIDE


    private void togglePassword() {

        if (passwordField.getEchoChar() == (char) 0) {

            passwordField.setEchoChar('•');
            showPasswordButton.setText("●");

        } else {

            passwordField.setEchoChar((char) 0);
            showPasswordButton.setText("○");
        }
    }


    // LOGIN FUNCTION


    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        messageLabel.setText(" ");

        if (username.isEmpty()) {

            messageLabel.setText("Please enter your username.");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {

            messageLabel.setText("Please enter your password.");
            passwordField.requestFocus();
            return;
        }


         // TEMPORARY LOGIN TEST


        if (username.equals("admin") && password.equals("1234")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Login successful!",
                    "FAMS",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            messageLabel.setText("Invalid username or password.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    // ROUNDED PANEL


    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color backgroundColor) {

            this.radius = radius;
            this.backgroundColor = backgroundColor;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);

            g2.fill(
                    new RoundRectangle2D.Double(
                            0,
                            0,
                            getWidth(),
                            getHeight(),
                            radius,
                            radius
                    )
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // UNIVERSITY ILLUSTRATION


    private static class UniversityIllustration extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            // Illustration ground
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillRoundRect(
                    20,
                    205,
                    width - 40,
                    4,
                    4,
                    4
            );

            // University building
            int buildingX = 80;
            int buildingY = 75;
            int buildingWidth = 230;
            int buildingHeight = 125;

            // Building body
            g2.setColor(new Color(245, 248, 252));
            g2.fillRoundRect(
                    buildingX,
                    buildingY,
                    buildingWidth,
                    buildingHeight,
                    8,
                    8
            );

            // Roof
            Polygon roof = new Polygon();

            roof.addPoint(buildingX - 15, buildingY);
            roof.addPoint(
                    buildingX + buildingWidth / 2,
                    buildingY - 55
            );
            roof.addPoint(
                    buildingX + buildingWidth + 15,
                    buildingY
            );

            g2.setColor(new Color(220, 232, 244));
            g2.fillPolygon(roof);

            // Roof outline
            g2.setColor(new Color(180, 200, 220));
            g2.drawPolygon(roof);

            // Columns
            g2.setColor(NAVY);

            int[] columns = {
                    buildingX + 20,
                    buildingX + 65,
                    buildingX + 110,
                    buildingX + 155,
                    buildingX + 200
            };

            for (int x : columns) {

                g2.fillRoundRect(
                        x,
                        buildingY + 45,
                        12,
                        80,
                        5,
                        5
                );
            }

            // Door
            g2.setColor(TEAL);

            g2.fillRoundRect(
                    buildingX + 100,
                    buildingY + 75,
                    30,
                    50,
                    5,
                    5
            );

            // University symbol
            g2.setColor(BLUE);
            g2.fillOval(
                    buildingX + 103,
                    buildingY + 20,
                    24,
                    24
            );

            // Book icon
            g2.setColor(new Color(255, 255, 255, 220));

            g2.fillRoundRect(
                    125,
                    225,
                    75,
                    18,
                    4,
                    4
            );

            g2.setColor(TEAL);

            g2.fillRect(
                    160,
                    225,
                    3,
                    18
            );

            // Student circles
            drawStudent(g2, 55, 235, BLUE);
            drawStudent(g2, 260, 235, TEAL);
            drawStudent(g2, 320, 225, new Color(100, 150, 210));

            g2.dispose();
        }

        private void drawStudent(
                Graphics2D g2,
                int x,
                int y,
                Color color
        ) {

            // Head
            g2.setColor(new Color(245, 190, 150));
            g2.fillOval(x, y, 18, 18);

            // Body
            g2.setColor(color);
            g2.fillRoundRect(
                    x - 5,
                    y + 17,
                    28,
                    35,
                    10,
                    10
            );

            // Book
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    x + 22,
                    y + 25,
                    18,
                    13,
                    2,
                    2
            );
        }
    }

    // MAIN METHOD
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );

            } catch (Exception e) {

                e.printStackTrace();
            }

            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}