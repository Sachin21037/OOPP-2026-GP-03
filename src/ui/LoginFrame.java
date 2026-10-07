package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {


    // COLORS


    private static final Color NAVY = new Color(15, 39, 68);
    private static final Color NAVY_LIGHT = new Color(24, 57, 94);

    private static final Color BLUE = new Color(37, 99, 166);
    private static final Color BLUE_HOVER = new Color(28, 78, 135);

    private static final Color TEAL = new Color(20, 184, 166);

    private static final Color LIGHT_BG = new Color(246, 248, 252);

    private static final Color TEXT_DARK = new Color(25, 35, 50);
    private static final Color TEXT_GRAY = new Color(100, 116, 139);

    private static final Color BORDER = new Color(214, 222, 232);
    private static final Color FOCUS_BORDER = new Color(37, 99, 166);


    // COMPONENTS


    private JTextField usernameField;
    private JPasswordField passwordField;

    private RoundedButton loginButton;
    private EyeButton showPasswordButton;

    private JCheckBox rememberMe;

    private JLabel messageLabel;


    // CONSTRUCTOR


    public LoginFrame() {

        setTitle("FAMS - Faculty Academic Management System");

        setSize(1100, 680);
        setMinimumSize(new Dimension(1000, 620));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setResizable(false);



        // MAIN PANEL


        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(LIGHT_BG);

        setContentPane(mainPanel);



        // LEFT PANEL


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


                // Main gradient background
                GradientPaint gradient = new GradientPaint(
                        0,
                        0,
                        NAVY,
                        width,
                        height,
                        NAVY_LIGHT
                );

                g2.setPaint(gradient);

                g2.fillRect(
                        0,
                        0,
                        width,
                        height
                );


                // Decorative blue circle
                g2.setColor(
                        new Color(
                                37,
                                99,
                                166,
                                55
                        )
                );

                g2.fillOval(
                        -170,
                        height - 250,
                        400,
                        400
                );


                // Decorative teal circle
                g2.setColor(
                        new Color(
                                20,
                                184,
                                166,
                                35
                        )
                );

                g2.fillOval(
                        width - 180,
                        -120,
                        300,
                        300
                );


                // Decorative dots
                g2.setColor(
                        new Color(
                                255,
                                255,
                                255,
                                40
                        )
                );

                g2.fillOval(55, 110, 8, 8);
                g2.fillOval(width - 80, 270, 7, 7);
                g2.fillOval(90, 480, 6, 6);

                g2.dispose();
            }
        };


        leftPanel.setPreferredSize(
                new Dimension(570, 680)
        );

        leftPanel.setLayout(
                new BorderLayout()
        );


        // BRANDING PANEL


        JPanel brandingPanel = new JPanel();

        brandingPanel.setOpaque(false);

        brandingPanel.setLayout(
                new BoxLayout(
                        brandingPanel,
                        BoxLayout.Y_AXIS
                )
        );

        brandingPanel.setBorder(
                new EmptyBorder(
                        45,
                        60,
                        35,
                        60
                )
        );



        // FAMS BRAND

        JLabel logoLabel = new JLabel("FAMS");

        logoLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        56
                )
        );

        logoLabel.setForeground(Color.WHITE);

        logoLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        JLabel subtitleLabel = new JLabel(
                "FACULTY ACADEMIC MANAGEMENT SYSTEM"
        );

        subtitleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );

        subtitleLabel.setForeground(
                new Color(
                        190,
                        211,
                        232
                )
        );

        subtitleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        brandingPanel.add(
                logoLabel
        );

        brandingPanel.add(
                Box.createVerticalStrut(1)
        );

        brandingPanel.add(
                subtitleLabel
        );



        // DIVIDER


        JPanel divider = new JPanel();

        divider.setBackground(
                new Color(
                        255,
                        255,
                        255,
                        60
                )
        );

        divider.setMaximumSize(
                new Dimension(
                        110,
                        1
                )
        );

        divider.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        brandingPanel.add(
                Box.createVerticalStrut(15)
        );

        brandingPanel.add(divider);



        // ILLUSTRATION


        UniversityIllustration illustration =
                new UniversityIllustration();

        illustration.setOpaque(false);

        illustration.setPreferredSize(
                new Dimension(
                        450,
                        310
                )
        );

        illustration.setMaximumSize(
                new Dimension(
                        450,
                        310
                )
        );

        illustration.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        brandingPanel.add(
                Box.createVerticalStrut(15)
        );

        brandingPanel.add(
                illustration
        );


        brandingPanel.add(
                Box.createVerticalGlue()
        );


        // DESCRIPTION


        JLabel descriptionLabel = new JLabel(
                "<html>" +
                        "<div style='text-align:center;'>" +
                        "A smarter way to manage academic activities,<br>" +
                        "faculty information and university resources." +
                        "</div>" +
                        "</html>"
        );

        descriptionLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        descriptionLabel.setForeground(
                new Color(
                        205,
                        220,
                        236
                )
        );

        descriptionLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        descriptionLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        brandingPanel.add(
                descriptionLabel
        );

        brandingPanel.add(
                Box.createVerticalStrut(10)
        );


        leftPanel.add(
                brandingPanel,
                BorderLayout.CENTER
        );



        // RIGHT PANEL


        JPanel rightPanel =
                new JPanel(
                        new GridBagLayout()
                );

        rightPanel.setBackground(
                LIGHT_BG
        );



        // LOGIN CARD


        RoundedPanel loginCard =
                new RoundedPanel(
                        24,
                        Color.WHITE
                );


        loginCard.setPreferredSize(
                new Dimension(
                        420,
                        520
                )
        );


        loginCard.setLayout(
                new BoxLayout(
                        loginCard,
                        BoxLayout.Y_AXIS
                )
        );


        loginCard.setBorder(
                new EmptyBorder(
                        42,
                        45,
                        35,
                        45
                )
        );



        // WELCOME


        JLabel welcomeLabel =
                new JLabel(
                        "Welcome Back"
                );

        welcomeLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        30
                )
        );

        welcomeLabel.setForeground(
                TEXT_DARK
        );

        welcomeLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        JLabel signInLabel =
                new JLabel(
                        "Sign in to continue to FAMS"
                );

        signInLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        signInLabel.setForeground(
                TEXT_GRAY
        );

        signInLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        loginCard.add(
                welcomeLabel
        );

        loginCard.add(
                Box.createVerticalStrut(6)
        );

        loginCard.add(
                signInLabel
        );

        loginCard.add(
                Box.createVerticalStrut(32)
        );



        // USERNAME


        JLabel usernameLabel =
                createFieldLabel(
                        "Username"
                );

        usernameLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        usernameField =
                new RoundedTextField(
                        14
                );

        styleTextField(
                usernameField
        );

        usernameField.setToolTipText(
                "Enter your username"
        );


        loginCard.add(
                usernameLabel
        );

        loginCard.add(
                Box.createVerticalStrut(8)
        );

        loginCard.add(
                usernameField
        );


        loginCard.add(
                Box.createVerticalStrut(19)
        );


        // PASSWORD


        JLabel passwordLabel =
                createFieldLabel(
                        "Password"
                );

        passwordLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        JPanel passwordPanel =
                new JPanel(
                        new BorderLayout()
                );

        passwordPanel.setOpaque(false);

        passwordPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        50
                )
        );

        passwordPanel.setPreferredSize(
                new Dimension(
                        330,
                        50
                )
        );

        passwordPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        // IMPORTANT:
        // Password must use RoundedPasswordField
        passwordField =
                new RoundedPasswordField(
                        14
                );

        styleTextField(
                passwordField
        );

        passwordField.setEchoChar('•');


        // Remove normal Swing border
        // because RoundedPasswordField
        // draws its own border.
        passwordField.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        14,
                        0,
                        45
                )
        );


        showPasswordButton =
                new EyeButton();


        showPasswordButton.addActionListener(
                e -> togglePassword()
        );


        passwordPanel.add(
                passwordField,
                BorderLayout.CENTER
        );

        passwordPanel.add(
                showPasswordButton,
                BorderLayout.EAST
        );


        loginCard.add(
                passwordLabel
        );

        loginCard.add(
                Box.createVerticalStrut(8)
        );

        loginCard.add(
                passwordPanel
        );


        loginCard.add(
                Box.createVerticalStrut(16)
        );



        // REMEMBER ME


        rememberMe =
                new JCheckBox(
                        "Remember me"
                );

        rememberMe.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        rememberMe.setForeground(
                TEXT_GRAY
        );

        rememberMe.setBackground(
                Color.WHITE
        );

        rememberMe.setFocusPainted(false);

        rememberMe.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        loginCard.add(
                rememberMe
        );


        loginCard.add(
                Box.createVerticalStrut(16)
        );



        // MESSAGE


        messageLabel =
                new JLabel(
                        " "
                );

        messageLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        messageLabel.setForeground(
                new Color(
                        220,
                        70,
                        70
                )
        );

        messageLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        loginCard.add(
                messageLabel
        );


        loginCard.add(
                Box.createVerticalStrut(7)
        );



        // SIGN IN BUTTON


        loginButton =
                new RoundedButton(
                        "SIGN IN",
                        14
                );

        styleLoginButton();


        loginCard.add(
                loginButton
        );


        loginCard.add(
                Box.createVerticalStrut(20)
        );


        // FOOTER


        JLabel footerLabel =
                new JLabel(
                        "Faculty Academic Management System"
                );

        footerLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        11
                )
        );

        footerLabel.setForeground(
                new Color(
                        150,
                        160,
                        175
                )
        );

        footerLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        loginCard.add(
                footerLabel
        );



        // LOGIN ACTION


        loginButton.addActionListener(
                e -> handleLogin()
        );


        // Enter key
        getRootPane().setDefaultButton(
                loginButton
        );


        rightPanel.add(
                loginCard
        );



        // ADD PANELS


        mainPanel.add(
                leftPanel,
                BorderLayout.WEST
        );

        mainPanel.add(
                rightPanel,
                BorderLayout.CENTER
        );



        // INITIAL FOCUS


        SwingUtilities.invokeLater(
                () -> usernameField.requestFocusInWindow()
        );
    }



    // FIELD LABEL


    private JLabel createFieldLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        label.setForeground(
                TEXT_DARK
        );

        return label;
    }


    // TEXT FIELD STYLE


    private void styleTextField(
            JTextField field
    ) {

        field.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        field.setForeground(
                TEXT_DARK
        );

        field.setBackground(
                Color.WHITE
        );

        field.setCaretColor(
                BLUE
        );


        field.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        50
                )
        );

        field.setPreferredSize(
                new Dimension(
                        330,
                        50
                )
        );



        // FOCUS EFFECT


        field.addFocusListener(
                new FocusAdapter() {

                    @Override
                    public void focusGained(
                            FocusEvent e
                    ) {

                        if (
                                field
                                        instanceof
                                        RoundedTextField
                        ) {

                            RoundedTextField roundedField =
                                    (RoundedTextField) field;

                            roundedField.setFocusState(
                                    true
                            );

                        } else if (
                                field
                                        instanceof
                                        RoundedPasswordField
                        ) {

                            RoundedPasswordField roundedField =
                                    (RoundedPasswordField) field;

                            roundedField.setFocusState(
                                    true
                            );
                        }
                    }


                    @Override
                    public void focusLost(
                            FocusEvent e
                    ) {

                        if (
                                field
                                        instanceof
                                        RoundedTextField
                        ) {

                            RoundedTextField roundedField =
                                    (RoundedTextField) field;

                            roundedField.setFocusState(
                                    false
                            );

                        } else if (
                                field
                                        instanceof
                                        RoundedPasswordField
                        ) {

                            RoundedPasswordField roundedField =
                                    (RoundedPasswordField) field;

                            roundedField.setFocusState(
                                    false
                            );
                        }
                    }
                }
        );
    }



    // LOGIN BUTTON STYLE


    private void styleLoginButton() {

        loginButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        15
                )
        );

        loginButton.setForeground(
                Color.WHITE
        );

        loginButton.setBackground(
                BLUE
        );

        loginButton.setFocusPainted(
                false
        );

        loginButton.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        loginButton.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        loginButton.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        50
                )
        );

        loginButton.setPreferredSize(
                new Dimension(
                        330,
                        50
                )
        );


        // HOVER EFFECT

        loginButton.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            MouseEvent e
                    ) {

                        loginButton.setBackground(
                                BLUE_HOVER
                        );
                    }


                    @Override
                    public void mouseExited(
                            MouseEvent e
                    ) {

                        loginButton.setBackground(
                                BLUE
                        );
                    }
                }
        );
    }



    // PASSWORD SHOW / HIDE


    private void togglePassword() {

        if (
                passwordField.getEchoChar()
                        == (char) 0
        ) {

            passwordField.setEchoChar(
                    '•'
            );

            showPasswordButton.setShowing(
                    false
            );

        } else {

            passwordField.setEchoChar(
                    (char) 0
            );

            showPasswordButton.setShowing(
                    true
            );
        }

        passwordField.requestFocusInWindow();
    }


    // LOGIN FUNCTION


    private void handleLogin() {

        String username =
                usernameField
                        .getText()
                        .trim();

        String password =
                new String(
                        passwordField.getPassword()
                );


        messageLabel.setText(
                " "
        );


        if (username.isEmpty()) {

            messageLabel.setText(
                    "Please enter your username."
            );

            usernameField.requestFocus();

            return;
        }


        if (password.isEmpty()) {

            messageLabel.setText(
                    "Please enter your password."
            );

            passwordField.requestFocus();

            return;
        }



        // TEMPORARY LOGIN TEST


        if (
                username.equals("admin")
                        &&
                        password.equals("1234")
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Login successful!",
                    "FAMS",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            messageLabel.setText(
                    "Invalid username or password."
            );

            passwordField.setText("");

            passwordField.requestFocus();
        }
    }



    // ROUNDED PANEL


    private static class RoundedPanel
            extends JPanel {

        private final int radius;
        private final Color backgroundColor;


        public RoundedPanel(
                int radius,
                Color backgroundColor
        ) {

            this.radius =
                    radius;

            this.backgroundColor =
                    backgroundColor;

            setOpaque(false);
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();


            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            g2.setColor(
                    backgroundColor
            );


            g2.fill(
                    new RoundRectangle2D.Double(
                            0,
                            0,
                            getWidth() - 1,
                            getHeight() - 1,
                            radius,
                            radius
                    )
            );


            g2.dispose();

            super.paintComponent(g);
        }
    }



    // ROUNDED TEXT FIELD


    private static class RoundedTextField
            extends JTextField {

        private final int radius;

        private boolean focused = false;


        public RoundedTextField(
                int radius
        ) {

            this.radius =
                    radius;

            setOpaque(false);

            setBorder(
                    BorderFactory.createEmptyBorder(
                            0,
                            14,
                            0,
                            14
                    )
            );
        }


        public void setFocusState(
                boolean focused
        ) {

            this.focused =
                    focused;

            repaint();
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();


            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // Background
            g2.setColor(
                    Color.WHITE
            );


            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );


            // Border
            g2.setColor(
                    focused
                            ? FOCUS_BORDER
                            : BORDER
            );


            g2.setStroke(
                    new BasicStroke(
                            focused
                                    ? 2f
                                    : 1f
                    )
            );


            g2.drawRoundRect(
                    focused ? 1 : 0,
                    focused ? 1 : 0,
                    getWidth()
                            -
                            (focused ? 2 : 1),
                    getHeight()
                            -
                            (focused ? 2 : 1),
                    radius,
                    radius
            );


            g2.dispose();


            super.paintComponent(g);
        }
    }



    // ROUNDED PASSWORD FIELD


    private static class RoundedPasswordField
            extends JPasswordField {

        private final int radius;

        private boolean focused = false;


        public RoundedPasswordField(
                int radius
        ) {

            this.radius =
                    radius;

            setOpaque(false);

            setBorder(
                    BorderFactory.createEmptyBorder(
                            0,
                            14,
                            0,
                            45
                    )
            );
        }


        public void setFocusState(
                boolean focused
        ) {

            this.focused =
                    focused;

            repaint();
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();


            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // Background
            g2.setColor(
                    Color.WHITE
            );


            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );


            // Border
            g2.setColor(
                    focused
                            ? FOCUS_BORDER
                            : BORDER
            );


            g2.setStroke(
                    new BasicStroke(
                            focused
                                    ? 2f
                                    : 1f
                    )
            );


            g2.drawRoundRect(
                    focused ? 1 : 0,
                    focused ? 1 : 0,
                    getWidth()
                            -
                            (focused ? 2 : 1),
                    getHeight()
                            -
                            (focused ? 2 : 1),
                    radius,
                    radius
            );


            g2.dispose();


            super.paintComponent(g);
        }
    }



    // ROUNDED BUTTON


    private static class RoundedButton
            extends JButton {

        private final int radius;


        public RoundedButton(
                String text,
                int radius
        ) {

            super(text);

            this.radius =
                    radius;

            setOpaque(false);

            setBorderPainted(false);

            setFocusPainted(false);

            setContentAreaFilled(false);
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();


            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            g2.setColor(
                    getBackground()
            );


            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );


            g2.dispose();


            super.paintComponent(g);
        }
    }



    // PASSWORD EYE BUTTON


    private static class EyeButton
            extends JButton {

        private boolean showing = false;


        public EyeButton() {

            setPreferredSize(
                    new Dimension(
                            42,
                            50
                    )
            );

            setOpaque(false);

            setContentAreaFilled(false);

            setBorderPainted(false);

            setFocusPainted(false);

            setCursor(
                    new Cursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }


        public void setShowing(
                boolean showing
        ) {

            this.showing =
                    showing;

            repaint();
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();


            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            int centerX =
                    getWidth() / 2;

            int centerY =
                    getHeight() / 2;


            // Eye outline
            g2.setColor(
                    TEXT_GRAY
            );

            g2.setStroke(
                    new BasicStroke(
                            1.7f
                    )
            );


            g2.drawOval(
                    centerX - 9,
                    centerY - 6,
                    18,
                    12
            );


            // Pupil
            g2.fillOval(
                    centerX - 3,
                    centerY - 3,
                    6,
                    6
            );


            // Slash
            if (!showing) {

                g2.setStroke(
                        new BasicStroke(
                                1.7f
                        )
                );

                g2.drawLine(
                        centerX - 9,
                        centerY + 8,
                        centerX + 9,
                        centerY - 8
                );
            }


            g2.dispose();
        }
    }



    // UNIVERSITY ILLUSTRATION


    private static class UniversityIllustration
            extends JPanel {


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);


            Graphics2D g2 =
                    (Graphics2D) g.create();


            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            int width =
                    getWidth();



            // FLOATING DECORATION


            g2.setColor(
                    new Color(
                            255,
                            255,
                            255,
                            20
                    )
            );


            g2.fillOval(
                    45,
                    25,
                    55,
                    55
            );


            g2.setColor(
                    new Color(
                            20,
                            184,
                            166,
                            45
                    )
            );


            g2.fillOval(
                    width - 90,
                    40,
                    70,
                    70
            );



            // GROUND


            g2.setColor(
                    new Color(
                            255,
                            255,
                            255,
                            35
                    )
            );


            g2.fillRoundRect(
                    40,
                    265,
                    width - 80,
                    3,
                    3,
                    3
            );



            // UNIVERSITY BUILDING


            int buildingX = 105;
            int buildingY = 105;

            int buildingWidth = 260;
            int buildingHeight = 135;


            // Shadow
            g2.setColor(
                    new Color(
                            0,
                            0,
                            0,
                            30
                    )
            );


            g2.fillRoundRect(
                    buildingX + 8,
                    buildingY + 8,
                    buildingWidth,
                    buildingHeight,
                    12,
                    12
            );


            // Building
            g2.setColor(
                    new Color(
                            246,
                            249,
                            252
                    )
            );


            g2.fillRoundRect(
                    buildingX,
                    buildingY,
                    buildingWidth,
                    buildingHeight,
                    12,
                    12
            );



            // ROOF


            Polygon roof =
                    new Polygon();


            roof.addPoint(
                    buildingX - 18,
                    buildingY
            );


            roof.addPoint(
                    buildingX +
                            buildingWidth / 2,
                    buildingY - 65
            );


            roof.addPoint(
                    buildingX +
                            buildingWidth + 18,
                    buildingY
            );


            g2.setColor(
                    new Color(
                            218,
                            231,
                            244
                    )
            );


            g2.fillPolygon(
                    roof
            );


            g2.setColor(
                    new Color(
                            160,
                            184,
                            208
                    )
            );


            g2.drawPolygon(
                    roof
            );



            // FLAG


            g2.setColor(
                    TEAL
            );


            g2.fillRect(
                    buildingX +
                            buildingWidth / 2 - 2,
                    buildingY - 95,
                    4,
                    35
            );


            Polygon flag =
                    new Polygon();


            flag.addPoint(
                    buildingX +
                            buildingWidth / 2 + 2,
                    buildingY - 94
            );


            flag.addPoint(
                    buildingX +
                            buildingWidth / 2 + 28,
                    buildingY - 85
            );


            flag.addPoint(
                    buildingX +
                            buildingWidth / 2 + 2,
                    buildingY - 76
            );


            g2.fillPolygon(
                    flag
            );



            // COLUMNS


            g2.setColor(
                    NAVY
            );


            int[] columns = {
                    buildingX + 25,
                    buildingX + 75,
                    buildingX + 125,
                    buildingX + 175,
                    buildingX + 225
            };


            for (int x : columns) {

                g2.fillRoundRect(
                        x,
                        buildingY + 48,
                        15,
                        85,
                        6,
                        6
                );
            }



            // DOOR


            g2.setColor(
                    TEAL
            );


            g2.fillRoundRect(
                    buildingX + 113,
                    buildingY + 83,
                    35,
                    50,
                    6,
                    6
            );


            // Door handle
            g2.setColor(
                    Color.WHITE
            );


            g2.fillOval(
                    buildingX + 140,
                    buildingY + 107,
                    4,
                    4
            );



            // FAMS SYMBOL


            g2.setColor(
                    BLUE
            );


            g2.fillOval(
                    buildingX + 117,
                    buildingY + 17,
                    27,
                    27
            );


            g2.setColor(
                    Color.WHITE
            );


            g2.setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            13
                    )
            );


            g2.drawString(
                    "F",
                    buildingX + 126,
                    buildingY + 36
            );



            // BOOK


            g2.setColor(
                    Color.WHITE
            );


            g2.fillRoundRect(
                    170,
                    265,
                    90,
                    22,
                    5,
                    5
            );


            g2.setColor(
                    TEAL
            );


            g2.fillRect(
                    213,
                    265,
                    3,
                    22
            );



            // STUDENTS

            drawStudent(
                    g2,
                    75,
                    270,
                    BLUE
            );


            drawStudent(
                    g2,
                    300,
                    268,
                    TEAL
            );


            g2.dispose();
        }


        private void drawStudent(
                Graphics2D g2,
                int x,
                int y,
                Color color
        ) {

            // Head
            g2.setColor(
                    new Color(
                            239,
                            184,
                            145
                    )
            );


            g2.fillOval(
                    x,
                    y,
                    20,
                    20
            );


            // Body
            g2.setColor(
                    color
            );


            g2.fillRoundRect(
                    x - 7,
                    y + 18,
                    34,
                    40,
                    12,
                    12
            );


            // Book
            g2.setColor(
                    Color.WHITE
            );


            g2.fillRoundRect(
                    x + 24,
                    y + 26,
                    24,
                    16,
                    3,
                    3
            );
        }
    }


    
    // MAIN METHOD


    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                () -> {

                    try {

                        UIManager.setLookAndFeel(
                                UIManager
                                        .getSystemLookAndFeelClassName()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                    LoginFrame frame =
                            new LoginFrame();


                    frame.setVisible(true);
                }
        );
    }
}