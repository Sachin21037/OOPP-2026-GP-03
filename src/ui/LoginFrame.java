package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class LoginFrame extends JFrame {
    
    // COLORS

    private static final Color NAVY =
            new Color(15, 39, 68);

    private static final Color NAVY_LIGHT =
            new Color(24, 57, 91);

    private static final Color TEAL =
            new Color(20, 151, 145);

    private static final Color TEAL_DARK =
            new Color(15, 125, 121);

    private static final Color BACKGROUND =
            new Color(245, 248, 252);

    private static final Color WHITE =
            Color.WHITE;

    private static final Color TEXT_DARK =
            new Color(37, 50, 65);

    private static final Color TEXT_MUTED =
            new Color(130, 143, 158);

    private static final Color BORDER =
            new Color(220, 228, 236);

    private static final Color FIELD_BACKGROUND =
            new Color(249, 251, 254);

    private static final Color SUCCESS =
            new Color(30, 155, 100);

    private static final Color ERROR =
            new Color(210, 65, 65);

    // COMPONENTS

    private RoundedTextField usernameField;

    private RoundedPasswordField passwordField;

    private JCheckBox rememberMeCheckBox;

    private RoundedButton loginButton;

    private JLabel statusLabel;

    private EyeButton eyeButton;


    // CONSTRUCTOR


    public LoginFrame() {

        setTitle("FAMS - Faculty Academic Management System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1100, 680);

        setMinimumSize(new Dimension(1000, 620));

        setLocationRelativeTo(null);

        setResizable(false);

        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND);

        // Left branding panel
        JPanel brandingPanel = createBrandingPanel();

        // Right login panel
        JPanel loginPanel = createLoginPanel();

        add(brandingPanel, BorderLayout.WEST);

        add(loginPanel, BorderLayout.CENTER);
    }


    // BRANDING PANEL
    private JPanel createBrandingPanel() {

        RoundedPanel panel = new RoundedPanel(0);

        panel.setBackground(NAVY);

        panel.setPreferredSize(new Dimension(520, 680));

        panel.setLayout(new BorderLayout());

        panel.setBorder(
                new EmptyBorder(35, 45, 30, 45)
        );


        // BRANDING HEADER

        JPanel brandingHeader = new JPanel();

        brandingHeader.setOpaque(false);

        brandingHeader.setLayout(
                new BoxLayout(
                        brandingHeader,
                        BoxLayout.Y_AXIS
                )
        );


        // FAMS logo
        JLabel logoLabel = createLogoLabel();

        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandingHeader.add(logoLabel);

        brandingHeader.add(Box.createVerticalStrut(12));


        // Main title
        JLabel titleLabel = new JLabel(
                "<html>"
                        + "<span style='font-size:27px;"
                        + "font-weight:bold;'>"
                        + "Faculty Academic"
                        + "</span><br>"
                        + "<span style='font-size:27px;"
                        + "font-weight:bold;'>"
                        + "Management System"
                        + "</span>"
                        + "</html>"
        );

        titleLabel.setFont(
                new Font("SansSerif", Font.BOLD, 27)
        );

        titleLabel.setForeground(WHITE);

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandingHeader.add(titleLabel);

        brandingHeader.add(Box.createVerticalStrut(14));


        // Subtitle
        JLabel subtitleLabel = new JLabel(
                "<html>"
                        + "<div style='width:350px;"
                        + "line-height:1.5;'>"
                        + "A unified platform for managing "
                        + "academic activities, resources, "
                        + "and student information."
                        + "</div>"
                        + "</html>"
        );

        subtitleLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        subtitleLabel.setForeground(
                new Color(197, 212, 228)
        );

        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandingHeader.add(subtitleLabel);


        panel.add(
                brandingHeader,
                BorderLayout.NORTH
        );


        // CENTER ILLUSTRATION

        JPanel illustrationContainer = new JPanel(
                new GridBagLayout()
        );

        illustrationContainer.setOpaque(false);

        UniversityIllustration illustration =
                new UniversityIllustration();

        illustration.setPreferredSize(
                new Dimension(400, 270)
        );

        illustrationContainer.add(illustration);

        panel.add(
                illustrationContainer,
                BorderLayout.CENTER
        );


        // FOOTER

        JPanel footerPanel = new JPanel();

        footerPanel.setOpaque(false);

        footerPanel.setLayout(
                new BoxLayout(
                        footerPanel,
                        BoxLayout.Y_AXIS
                )
        );


        JLabel footerTitle = new JLabel(
                "SMARTER ACADEMIC MANAGEMENT"
        );

        footerTitle.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        footerTitle.setForeground(
                new Color(125, 218, 210)
        );

        footerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);


        JLabel footerDescription = new JLabel(
                "Simplifying the academic experience."
        );

        footerDescription.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        footerDescription.setForeground(
                new Color(183, 200, 217)
        );

        footerDescription.setAlignmentX(Component.LEFT_ALIGNMENT);


        footerPanel.add(footerTitle);

        footerPanel.add(
                Box.createVerticalStrut(8)
        );

        footerPanel.add(footerDescription);


        panel.add(
                footerPanel,
                BorderLayout.SOUTH
        );


        return panel;
    }


    // LOAD FAMS LOGO

    private JLabel createLogoLabel() {

        URL logoURL = LoginFrame.class.getResource(
                "/images/fams-logo-transparent.png"
        );


        // Fallback if the image cannot be found
        if (logoURL == null) {

            System.err.println(
                    "FAMS logo not found: "
                            + "/images/fams-logo-transparent.png"
            );

            JLabel fallbackLabel = new JLabel("FAMS");

            fallbackLabel.setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            42
                    )
            );

            fallbackLabel.setForeground(WHITE);

            return fallbackLabel;
        }


        // Load original image
        ImageIcon originalIcon = new ImageIcon(logoURL);

        int originalWidth = originalIcon.getIconWidth();

        int originalHeight = originalIcon.getIconHeight();


        if (originalWidth <= 0 || originalHeight <= 0) {

            JLabel fallbackLabel = new JLabel("FAMS");

            fallbackLabel.setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            42
                    )
            );

            fallbackLabel.setForeground(WHITE);

            return fallbackLabel;
        }


        // Maintain original image proportions
        int targetWidth = 175;

        int targetHeight = 110;

        double scale = Math.min(
                (double) targetWidth / originalWidth,
                (double) targetHeight / originalHeight
        );


        int scaledWidth = Math.max(
                1,
                (int) (originalWidth * scale)
        );

        int scaledHeight = Math.max(
                1,
                (int) (originalHeight * scale)
        );


        Image scaledImage = originalIcon
                .getImage()
                .getScaledInstance(
                        scaledWidth,
                        scaledHeight,
                        Image.SCALE_SMOOTH
                );


        JLabel logoLabel = new JLabel(
                new ImageIcon(scaledImage)
        );

        logoLabel.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        logoLabel.setVerticalAlignment(
                SwingConstants.CENTER
        );

        logoLabel.setToolTipText(
                "Faculty Academic Management System"
        );


        return logoLabel;
    }


    // LOGIN PANEL

    private JPanel createLoginPanel() {

        JPanel mainPanel = new JPanel(
                new GridBagLayout()
        );

        mainPanel.setBackground(BACKGROUND);

        mainPanel.setBorder(
                new EmptyBorder(25, 35, 25, 35)
        );


        // LOGIN CARD

        RoundedPanel loginCard = new RoundedPanel(28);

        loginCard.setBackground(WHITE);

        loginCard.setLayout(
                new BoxLayout(
                        loginCard,
                        BoxLayout.Y_AXIS
                )
        );

        loginCard.setBorder(
                new EmptyBorder(35, 38, 30, 38)
        );

        loginCard.setPreferredSize(
                new Dimension(425, 535)
        );

        // WELCOME TITLE

        JLabel welcomeLabel = new JLabel(
                "Welcome Back!"
        );

        welcomeLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        29
                )
        );

        welcomeLabel.setForeground(NAVY);

        welcomeLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        // DESCRIPTION

        JLabel descriptionLabel = new JLabel(
                "Sign in to access your academic dashboard."
        );

        descriptionLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        descriptionLabel.setForeground(TEXT_MUTED);

        descriptionLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        // USERNAME LABEL

        JLabel usernameLabel = createFieldLabel(
                "Username"
        );


        // USERNAME FIELD

        usernameField = new RoundedTextField(
                "Enter your username"
        );

        styleTextField(usernameField);

        // PASSWORD LABEL

        JLabel passwordLabel = createFieldLabel(
                "Password"
        );

        // PASSWORD FIELD

        passwordField = new RoundedPasswordField(
                "Enter your password"
        );

        stylePasswordField(passwordField);

        // PASSWORD FIELD WITH EYE BUTTON

        JPanel passwordContainer = new JPanel(
                new BorderLayout(5, 0)
        );

        passwordContainer.setOpaque(false);

        passwordContainer.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        passwordContainer.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        48
                )
        );

        passwordContainer.setPreferredSize(
                new Dimension(340, 48)
        );


        eyeButton = new EyeButton();

        eyeButton.setToolTipText(
                "Show or hide password"
        );

        eyeButton.addActionListener(e -> togglePasswordVisibility());


        passwordContainer.add(
                passwordField,
                BorderLayout.CENTER
        );

        passwordContainer.add(
                eyeButton,
                BorderLayout.EAST
        );

        // REMEMBER ME

        rememberMeCheckBox = new JCheckBox(
                "Remember me"
        );

        rememberMeCheckBox.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        rememberMeCheckBox.setForeground(
                TEXT_MUTED
        );

        rememberMeCheckBox.setOpaque(false);

        rememberMeCheckBox.setFocusPainted(false);

        rememberMeCheckBox.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        rememberMeCheckBox.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        // LOGIN BUTTON
        loginButton = new RoundedButton(
                "Sign In"
        );

        loginButton.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        loginButton.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        49
                )
        );

        loginButton.setPreferredSize(
                new Dimension(340, 49)
        );

        loginButton.addActionListener(
                e -> handleLogin()
        );


        // STATUS LABEL

        statusLabel = new JLabel(
                " "
        );

        statusLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        statusLabel.setForeground(ERROR);

        statusLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        // KEYBOARD SUPPORT

        usernameField.addActionListener(
                e -> passwordField.requestFocusInWindow()
        );

        passwordField.addActionListener(
                e -> handleLogin()
        );

        // ADD COMPONENTS TO LOGIN CARD


        loginCard.add(welcomeLabel);

        loginCard.add(
                Box.createVerticalStrut(8)
        );

        loginCard.add(descriptionLabel);

        loginCard.add(
                Box.createVerticalStrut(30)
        );


        loginCard.add(usernameLabel);

        loginCard.add(
                Box.createVerticalStrut(9)
        );

        loginCard.add(usernameField);

        loginCard.add(
                Box.createVerticalStrut(22)
        );


        loginCard.add(passwordLabel);

        loginCard.add(
                Box.createVerticalStrut(9)
        );

        loginCard.add(passwordContainer);

        loginCard.add(
                Box.createVerticalStrut(17)
        );


        loginCard.add(rememberMeCheckBox);

        loginCard.add(
                Box.createVerticalStrut(24)
        );


        loginCard.add(loginButton);

        loginCard.add(
                Box.createVerticalStrut(10)
        );

        loginCard.add(statusLabel);

        loginCard.add(
                Box.createVerticalGlue()
        );


        // FOOTER

        JLabel footerLabel = new JLabel(
                "Faculty Academic Management System"
        );

        footerLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        11
                )
        );

        footerLabel.setForeground(TEXT_MUTED);

        footerLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        // MAIN PANEL LAYOUT
        JPanel cardWrapper = new JPanel();

        cardWrapper.setOpaque(false);

        cardWrapper.setLayout(
                new BoxLayout(
                        cardWrapper,
                        BoxLayout.Y_AXIS
                )
        );

        loginCard.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        cardWrapper.add(loginCard);

        cardWrapper.add(
                Box.createVerticalStrut(18)
        );

        cardWrapper.add(footerLabel);


        mainPanel.add(cardWrapper);


        return mainPanel;
    }


    // FIELD LABEL


    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        label.setForeground(TEXT_DARK);

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        return label;
    }

    // STYLE USERNAME FIELD


    private void styleTextField(
            RoundedTextField field
    ) {

        field.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        field.setForeground(TEXT_DARK);

        field.setBackground(FIELD_BACKGROUND);

        field.setCaretColor(TEAL);

        field.setPreferredSize(
                new Dimension(340, 48)
        );

        field.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        48
                )
        );


        field.addFocusListener(
                new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {

                        field.setFocused(true);
                    }

                    @Override
                    public void focusLost(FocusEvent e) {

                        field.setFocused(false);
                    }
                }
        );
    }

    // STYLE PASSWORD FIELD

    private void stylePasswordField(
            RoundedPasswordField field
    ) {

        field.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        field.setForeground(TEXT_DARK);

        field.setBackground(FIELD_BACKGROUND);

        field.setCaretColor(TEAL);

        field.setPreferredSize(
                new Dimension(280, 48)
        );


        field.addFocusListener(
                new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {

                        field.setFocused(true);
                    }

                    @Override
                    public void focusLost(FocusEvent e) {

                        field.setFocused(false);
                    }
                }
        );
    }

    // PASSWORD VISIBILITY

    private void togglePasswordVisibility() {

        if (passwordField == null) {
            return;
        }


        if (passwordField.isPasswordVisible()) {

            passwordField.setPasswordVisible(false);

            eyeButton.setVisiblePassword(false);

        } else {

            passwordField.setPasswordVisible(true);

            eyeButton.setVisiblePassword(true);
        }


        passwordField.requestFocusInWindow();
    }


    // LOGIN VALIDATION

    private void handleLogin() {

        String username = usernameField.getText().trim();

        char[] passwordChars =
                passwordField.getPassword();

        String password = new String(passwordChars);


        // Clear temporary password array
        java.util.Arrays.fill(
                passwordChars,
                '\0'
        );

        // EMPTY FIELD VALIDATION

        if (username.isEmpty()
                || username.equals("Enter your username")) {

            showError(
                    "Please enter your username."
            );

            usernameField.requestFocusInWindow();

            return;
        }


        if (password.isEmpty()
                || password.equals("Enter your password")) {

            showError(
                    "Please enter your password."
            );

            passwordField.requestFocusInWindow();

            return;
        }

        // TEMPORARY LOGIN

        if (username.equals("admin")
                && password.equals("1234")) {

            showSuccess(
                    "Login successful!"
            );


            JOptionPane.showMessageDialog(
                    this,
                    "Welcome to FAMS, " + username + "!",
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            showError(
                    "Invalid username or password."
            );

            passwordField.setText("");

            passwordField.requestFocusInWindow();
        }
    }

    // SHOW ERROR

    private void showError(String message) {

        statusLabel.setForeground(ERROR);

        statusLabel.setText(message);
    }

    // SHOW SUCCESS

    private void showSuccess(String message) {

        statusLabel.setForeground(SUCCESS);

        statusLabel.setText(message);
    }

    // ROUNDED PANEL


    static class RoundedPanel extends JPanel {

        private final int cornerRadius;


        public RoundedPanel(int radius) {

            this.cornerRadius = radius;

            setOpaque(false);
        }


        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            g2.setColor(getBackground());


            if (cornerRadius <= 0) {

                g2.fillRect(
                        0,
                        0,
                        getWidth(),
                        getHeight()
                );

            } else {

                g2.fill(
                        new RoundRectangle2D.Float(
                                0,
                                0,
                                getWidth(),
                                getHeight(),
                                cornerRadius,
                                cornerRadius
                        )
                );
            }


            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ROUNDED USERNAME FIELD

    static class RoundedTextField extends JTextField {

        private boolean focused = false;

        private boolean showingPlaceholder = true;

        private final String placeholder;


        public RoundedTextField(String placeholder) {

            this.placeholder = placeholder;

            setText(placeholder);

            setOpaque(false);

            setBorder(
                    new EmptyBorder(0, 15, 0, 15)
            );


            addFocusListener(
                    new FocusAdapter() {

                        @Override
                        public void focusGained(FocusEvent e) {

                            if (showingPlaceholder) {

                                setText("");

                                showingPlaceholder = false;
                            }

                            repaint();
                        }


                        @Override
                        public void focusLost(FocusEvent e) {

                            if (getText().trim().isEmpty()) {

                                setText(placeholder);

                                showingPlaceholder = true;
                            }

                            repaint();
                        }
                    }
            );
        }


        public void setFocused(boolean focused) {

            this.focused = focused;

            repaint();
        }


        @Override
        public String getText() {

            if (showingPlaceholder) {

                return "";
            }

            return super.getText();
        }


        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            g2.setColor(getBackground());

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    14,
                    14
            );


            g2.setColor(
                    focused ? TEAL : BORDER
            );

            g2.setStroke(
                    new BasicStroke(
                            focused ? 1.8f : 1.0f
                    )
            );

            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    14,
                    14
            );


            g2.dispose();


            Color previousForeground = getForeground();


            if (showingPlaceholder) {

                setForeground(TEXT_MUTED);
            }


            super.paintComponent(g);


            if (showingPlaceholder) {

                setForeground(TEXT_MUTED);

            } else {

                setForeground(previousForeground);
            }
        }
    }


    // ROUNDED PASSWORD FIELD
    static class RoundedPasswordField
            extends JPasswordField {

        private boolean focused = false;

        private boolean showingPlaceholder = true;

        private final String placeholder;

        private boolean passwordVisible = false;


        public RoundedPasswordField(String placeholder) {

            this.placeholder = placeholder;

            setText(placeholder);

            setOpaque(false);

            setBorder(
                    new EmptyBorder(0, 15, 0, 15)
            );

            setEchoChar((char) 0);


            addFocusListener(
                    new FocusAdapter() {

                        @Override
                        public void focusGained(FocusEvent e) {

                            if (showingPlaceholder) {

                                setText("");

                                showingPlaceholder = false;

                                if (!passwordVisible) {

                                    setEchoChar('\u2022');
                                }
                            }

                            repaint();
                        }


                        @Override
                        public void focusLost(FocusEvent e) {

                            if (getPassword().length == 0) {

                                setText(placeholder);

                                showingPlaceholder = true;

                                setEchoChar((char) 0);
                            }

                            repaint();
                        }
                    }
            );
        }


        public void setFocused(boolean focused) {

            this.focused = focused;

            repaint();
        }


        public boolean isPasswordVisible() {

            return passwordVisible;
        }


        public void setPasswordVisible(boolean visible) {

            passwordVisible = visible;


            if (showingPlaceholder) {

                setEchoChar((char) 0);

            } else if (visible) {

                setEchoChar((char) 0);

            } else {

                setEchoChar('\u2022');
            }


            repaint();
        }


        @Override
        public char[] getPassword() {

            if (showingPlaceholder) {

                return new char[0];
            }

            return super.getPassword();
        }


        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            g2.setColor(getBackground());

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    14,
                    14
            );


            g2.setColor(
                    focused ? TEAL : BORDER
            );

            g2.setStroke(
                    new BasicStroke(
                            focused ? 1.8f : 1.0f
                    )
            );

            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    14,
                    14
            );


            g2.dispose();


            Color previousForeground = getForeground();


            if (showingPlaceholder) {

                setForeground(TEXT_MUTED);
            }


            super.paintComponent(g);


            if (showingPlaceholder) {

                setForeground(TEXT_MUTED);

            } else {

                setForeground(previousForeground);
            }
        }
    }

    // ROUNDED LOGIN BUTTON

    static class RoundedButton extends JButton {

        private boolean hovered = false;


        public RoundedButton(String text) {

            super(text);

            setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            15
                    )
            );

            setForeground(WHITE);

            setBackground(TEAL);

            setFocusPainted(false);

            setBorderPainted(false);

            setContentAreaFilled(false);

            setOpaque(false);

            setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );

            setBorder(
                    new EmptyBorder(12, 20, 12, 20)
            );


            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {

                            hovered = true;

                            repaint();
                        }


                        @Override
                        public void mouseExited(MouseEvent e) {

                            hovered = false;

                            repaint();
                        }
                    }
            );
        }


        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            Color buttonColor;


            if (!isEnabled()) {

                buttonColor = TEXT_MUTED;

            } else if (getModel().isPressed()) {

                buttonColor = TEAL_DARK;

            } else if (hovered) {

                buttonColor = TEAL_DARK;

            } else {

                buttonColor = TEAL;
            }


            g2.setColor(buttonColor);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    15,
                    15
            );


            g2.dispose();


            super.paintComponent(g);
        }
    }

    // PASSWORD EYE BUTTON

    static class EyeButton extends JButton {

        private boolean visiblePassword = false;


        public EyeButton() {

            setPreferredSize(
                    new Dimension(45, 48)
            );

            setMinimumSize(
                    new Dimension(45, 48)
            );

            setMaximumSize(
                    new Dimension(45, 48)
            );

            setFocusPainted(false);

            setBorderPainted(false);

            setContentAreaFilled(false);

            setOpaque(false);

            setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );
        }


        public void setVisiblePassword(boolean visible) {

            visiblePassword = visible;

            repaint();
        }


        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            int centerX = getWidth() / 2;

            int centerY = getHeight() / 2;


            // Eye outline
            g2.setColor(
                    new Color(110, 126, 145)
            );

            g2.setStroke(
                    new BasicStroke(
                            1.7f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );


            g2.drawOval(
                    centerX - 11,
                    centerY - 6,
                    22,
                    13
            );


            // Eye pupil
            g2.fillOval(
                    centerX - 3,
                    centerY - 3,
                    7,
                    7
            );


            // Slash when password is hidden
            if (!visiblePassword) {

                g2.setStroke(
                        new BasicStroke(
                                1.8f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                g2.drawLine(
                        centerX - 12,
                        centerY + 10,
                        centerX + 12,
                        centerY - 10
                );
            }


            g2.dispose();
        }
    }

    // UNIVERSITY ILLUSTRATION

    static class UniversityIllustration extends JPanel {

        public UniversityIllustration() {

            setOpaque(false);
        }


        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            int centerX = getWidth() / 2;

            // BACKGROUND CIRCLE

            g2.setColor(
                    new Color(255, 255, 255, 12)
            );

            g2.fillOval(
                    centerX - 145,
                    10,
                    290,
                    250
            );


            // Decorative circles
            g2.setColor(
                    new Color(92, 195, 187, 110)
            );

            g2.fillOval(
                    centerX + 115,
                    42,
                    12,
                    12
            );

            g2.fillOval(
                    centerX - 130,
                    80,
                    8,
                    8
            );

            // UNIVERSITY BUILDING

            int buildingX = centerX - 105;

            int buildingY = 105;

            int buildingWidth = 210;

            int buildingHeight = 110;


            // Building shadow
            g2.setColor(
                    new Color(0, 0, 0, 35)
            );

            g2.fillRoundRect(
                    buildingX + 7,
                    buildingY + 7,
                    buildingWidth,
                    buildingHeight,
                    8,
                    8
            );


            // Main building
            g2.setColor(
                    new Color(238, 245, 250)
            );

            g2.fillRect(
                    buildingX,
                    buildingY + 25,
                    buildingWidth,
                    buildingHeight - 25
            );


            // Roof
            int[] roofX = {
                    buildingX - 12,
                    centerX,
                    buildingX + buildingWidth + 12
            };

            int[] roofY = {
                    buildingY + 28,
                    buildingY - 12,
                    buildingY + 28
            };


            g2.setColor(
                    new Color(116, 173, 190)
            );

            g2.fillPolygon(
                    roofX,
                    roofY,
                    3
            );


            // Roof outline
            g2.setColor(
                    new Color(206, 231, 237)
            );

            g2.setStroke(
                    new BasicStroke(2f)
            );

            g2.drawPolygon(
                    roofX,
                    roofY,
                    3
            );

            // WINDOWS

            g2.setColor(NAVY_LIGHT);


            int windowWidth = 20;

            int windowHeight = 27;

            int windowY = buildingY + 42;


            for (int i = 0; i < 6; i++) {

                int windowX =
                        buildingX + 14 + i * 34;

                g2.fillRoundRect(
                        windowX,
                        windowY,
                        windowWidth,
                        windowHeight,
                        4,
                        4
                );


                g2.setColor(
                        new Color(115, 205, 197)
                );

                g2.fillRect(
                        windowX + 8,
                        windowY + 2,
                        3,
                        windowHeight - 4
                );


                g2.setColor(NAVY_LIGHT);
            }

            // BUILDING DOOR
            g2.setColor(
                    new Color(25, 62, 88)
            );

            g2.fillRoundRect(
                    centerX - 17,
                    buildingY + 72,
                    34,
                    43,
                    4,
                    4
            );


            // Door detail
            g2.setColor(
                    new Color(115, 205, 197)
            );

            g2.fillOval(
                    centerX + 8,
                    buildingY + 91,
                    4,
                    4
            );


            // FLAG

            g2.setColor(
                    new Color(220, 235, 245)
            );

            g2.fillRect(
                    centerX - 2,
                    buildingY - 38,
                    4,
                    28
            );


            g2.setColor(TEAL);

            int[] flagX = {
                    centerX + 2,
                    centerX + 31,
                    centerX + 2
            };

            int[] flagY = {
                    buildingY - 37,
                    buildingY - 27,
                    buildingY - 17
            };


            g2.fillPolygon(
                    flagX,
                    flagY,
                    3
            );
            // GROUND

            g2.setColor(
                    new Color(104, 191, 180)
            );

            g2.fillRoundRect(
                    centerX - 150,
                    buildingY + 112,
                    300,
                    7,
                    7,
                    7
            );


            // Ground details
            g2.setColor(
                    new Color(255, 255, 255, 90)
            );

            g2.fillRoundRect(
                    centerX - 120,
                    buildingY + 125,
                    70,
                    3,
                    3,
                    3
            );

            g2.fillRoundRect(
                    centerX + 55,
                    buildingY + 125,
                    70,
                    3,
                    3,
                    3
            );

            // DECORATIVE BOOK

            int bookX = centerX - 125;

            int bookY = 35;


            g2.setColor(
                    new Color(93, 193, 184)
            );

            g2.fillRoundRect(
                    bookX,
                    bookY,
                    40,
                    28,
                    4,
                    4
            );


            g2.setColor(WHITE);

            g2.setStroke(
                    new BasicStroke(1.5f)
            );

            g2.drawLine(
                    bookX + 20,
                    bookY + 4,
                    bookX + 20,
                    bookY + 24
            );

            g2.drawLine(
                    bookX + 7,
                    bookY + 9,
                    bookX + 15,
                    bookY + 9
            );

            g2.drawLine(
                    bookX + 25,
                    bookY + 9,
                    bookX + 33,
                    bookY + 9
            );
            // DECORATIVE GRADUATION CAP

            int capX = centerX + 91;

            int capY = 38;


            int[] capXPoints = {
                    capX - 22,
                    capX,
                    capX + 22,
                    capX
            };

            int[] capYPoints = {
                    capY + 7,
                    capY - 3,
                    capY + 7,
                    capY + 17
            };


            g2.setColor(
                    new Color(105, 207, 195)
            );

            g2.fillPolygon(
                    capXPoints,
                    capYPoints,
                    4
            );


            g2.setColor(
                    new Color(180, 231, 222)
            );

            g2.setStroke(
                    new BasicStroke(2f)
            );

            g2.drawLine(
                    capX + 17,
                    capY + 8,
                    capX + 23,
                    capY + 25
            );


            g2.fillOval(
                    capX + 20,
                    capY + 23,
                    6,
                    6
            );


            g2.dispose();
        }
    }
    // MAIN METHOD
    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    try {

                        UIManager.setLookAndFeel(
                                UIManager.getSystemLookAndFeelClassName()
                        );

                    } catch (Exception e) {

                        System.err.println(
                                "Unable to set system look and feel: "
                                        + e.getMessage()
                        );
                    }


                    LoginFrame loginFrame =
                            new LoginFrame();

                    loginFrame.setVisible(true);
                }
        );
    }
}