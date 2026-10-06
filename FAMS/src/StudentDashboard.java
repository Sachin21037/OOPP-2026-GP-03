import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StudentDashboard extends JFrame {

    // dash board colors
    Color darkBlue = new Color(27, 67, 103);
    Color lightBackground = new Color(245, 247, 250);
    Color sidebarColor = Color.WHITE;
    Color textColor = new Color(35, 45, 55);

    JPanel contentPanel;

    public StudentDashboard() {

        setTitle("Student Dashboard - Faculty Academic Management System");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // header

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(1050, 65));

        JLabel systemTitle = new JLabel(
                "  FACULTY ACADEMIC MANAGEMENT SYSTEM"
        );

        systemTitle.setForeground(Color.WHITE);
        systemTitle.setFont(new Font("Arial", Font.BOLD, 19));

        JLabel userType = new JLabel("STUDENT  ");
        userType.setForeground(new Color(180, 240, 220));
        userType.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(systemTitle, BorderLayout.WEST);
        header.add(userType, BorderLayout.EAST);

        // left side bar


        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(190, 585));
        sidebar.setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("  STUDENT MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 12));
        menuTitle.setForeground(new Color(110, 120, 130));

        menuTitle.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 15, 0)
        );

        menuPanel.add(menuTitle);

        // Menu buttons
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "My Courses");
        addMenuButton(menuPanel, "My Results");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Assignments");
        addMenuButton(menuPanel, "Course Materials");
        addMenuButton(menuPanel, "My Profile");

        sidebar.add(menuPanel, BorderLayout.NORTH);

        // Logout
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setBackground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 13));
        logoutButton.setForeground(new Color(210, 60, 60));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "You have been logged out.",
                    "Logout",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        // content area

        contentPanel = new JPanel();
        contentPanel.setBackground(lightBackground);
        contentPanel.setLayout(new BorderLayout());

        // Welcome section
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(lightBackground);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

        welcomePanel.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 15, 25)
        );

        JLabel welcome = new JLabel("Welcome, Student");
        welcome.setFont(new Font("Arial", Font.BOLD, 28));
        welcome.setForeground(textColor);

        JLabel subtitle = new JLabel(
                "Manage your courses, results, attendance and academic activities."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 110, 120));

        welcomePanel.add(welcome);
        welcomePanel.add(Box.createVerticalStrut(5));
        welcomePanel.add(subtitle);

        contentPanel.add(welcomePanel, BorderLayout.NORTH);


        // dashboard center

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(lightBackground);
        centerPanel.setLayout(new BorderLayout());

        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(0, 25, 20, 25)
        );

        // cards
        JPanel cardsPanel = new JPanel(
                new GridLayout(1, 4, 12, 0)
        );

        cardsPanel.setBackground(lightBackground);

        cardsPanel.add(
                createCard(
                        "My Courses",
                        "6",
                        new Color(45, 100, 160)
                )
        );

        cardsPanel.add(
                createCard(
                        "Attendance",
                        "87%",
                        new Color(20, 165, 145)
                )
        );

        cardsPanel.add(
                createCard(
                        "Assignments",
                        "5",
                        new Color(45, 75, 100)
                )
        );

        cardsPanel.add(
                createCard(
                        "Current GPA",
                        "3.42",
                        new Color(115, 75, 180)
                )
        );

        // activities
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(Color.WHITE);

        activityPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 228, 232)
                        ),
                        BorderFactory.createEmptyBorder(
                                18, 18, 18, 18
                        )
                )
        );

        JLabel activityTitle = new JLabel("Student Activities");
        activityTitle.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        JPanel activityList = new JPanel();
        activityList.setBackground(Color.WHITE);
        activityList.setLayout(
                new BoxLayout(activityList, BoxLayout.Y_AXIS)
        );

        addActivity(
                activityList,
                "Attendance marked for ICT2132"
        );

        addActivity(
                activityList,
                "New assignment available"
        );

        addActivity(
                activityList,
                "Programming marks released"
        );

        addActivity(
                activityList,
                "New course material uploaded"
        );

        activityPanel.add(
                activityTitle,
                BorderLayout.NORTH
        );

        activityPanel.add(
                activityList,
                BorderLayout.CENTER
        );

        centerPanel.add(
                cardsPanel,
                BorderLayout.NORTH
        );

        centerPanel.add(
                activityPanel,
                BorderLayout.CENTER
        );

        contentPanel.add(
                centerPanel,
                BorderLayout.CENTER
        );

        // add everything

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        setVisible(true);
    }

    // crate menu button

    private void addMenuButton(
            JPanel panel,
            String text
    ) {

        JButton button = new JButton(text);

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        button.setForeground(textColor);
        button.setBackground(Color.WHITE);

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        12, 15, 12, 10
                )
        );

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        // hover effect
        button.addMouseListener(
                new MouseAdapter() {

                    public void mouseEntered(
                            MouseEvent e
                    ) {
                        button.setBackground(
                                new Color(235, 242, 248)
                        );
                    }

                    public void mouseExited(
                            MouseEvent e
                    ) {
                        button.setBackground(
                                Color.WHITE
                        );
                    }
                }
        );

        // button action
        button.addActionListener(e -> {

            String selected = button.getText();

            JOptionPane.showMessageDialog(
                    this,
                    selected + " page selected.",
                    selected,
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        panel.add(button);
    }

    // create dashboard card


    private JPanel createCard(
            String title,
            String value,
            Color valueColor
    ) {

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 228, 232)
                        ),
                        BorderFactory.createEmptyBorder(
                                18, 18, 18, 18
                        )
                )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        titleLabel.setForeground(
                new Color(100, 110, 120)
        );

        JLabel valueLabel = new JLabel(value);

        valueLabel.setFont(
                new Font("Arial", Font.BOLD, 25)
        );

        valueLabel.setForeground(valueColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    // add activity

    private void addActivity(
            JPanel panel,
            String text
    ) {

        JLabel activity = new JLabel(
                "• " + text
        );

        activity.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        activity.setForeground(
                new Color(70, 80, 90)
        );

        activity.setBorder(
                BorderFactory.createEmptyBorder(
                        6, 0, 6, 0
                )
        );

        panel.add(activity);
    }

    // main method

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new StudentDashboard();
        });
    }
}