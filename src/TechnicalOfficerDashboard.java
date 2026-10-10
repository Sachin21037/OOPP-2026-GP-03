import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TechnicalOfficerDashboard extends JFrame {

    // theme colors
    Color darkBlue = new Color(27, 67, 103);
    Color lightBackground = new Color(245, 247, 250);
    Color sidebarColor = Color.WHITE;
    Color textColor = new Color(35, 45, 55);
    Color activeMenuColor = new Color(235, 242, 250);

    // views container
    private CardLayout cardLayout;
    private JPanel contentContainer;
    private AttendanceManagementPage attendancePage;
    private Map<String, JButton> menuButtons = new HashMap<>();
    private String currentView = "Dashboard";

    public TechnicalOfficerDashboard() {
        setTitle("Technical Officer Dashboard - Faculty Academic Management System");
        setSize(1150, 720);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // main window layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // top blue navigation header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(1150, 65));

        JLabel systemTitle = new JLabel("  FACULTY ACADEMIC MANAGEMENT SYSTEM");
        systemTitle.setForeground(Color.WHITE);
        systemTitle.setFont(new Font("Arial", Font.BOLD, 19));

        JLabel userType = new JLabel("TECHNICAL OFFICER  ");
        userType.setForeground(new Color(180, 240, 220));
        userType.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(systemTitle, BorderLayout.WEST);
        header.add(userType, BorderLayout.EAST);

        // left sidebar menu
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(200, 655));

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("  TECHNICAL OFFICER MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 11));
        menuTitle.setForeground(new Color(110, 120, 130));
        menuTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 15, 0));

        menuPanel.add(menuTitle);

        // sidebar items
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Medical Certificates");
        addMenuButton(menuPanel, "Lab Maintenance");
        addMenuButton(menuPanel, "Notices");
        addMenuButton(menuPanel, "My Profile");

        sidebar.add(menuPanel, BorderLayout.NORTH);

        // logout button at bottom of sidebar
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setBackground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 13));
        logoutButton.setForeground(new Color(210, 60, 60));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        // central container switching between views
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(lightBackground);

        // dashboard overview view
        JPanel dashboardOverview = createDashboardOverview();
        contentContainer.add(dashboardOverview, "Dashboard");

        // attendance management view (embedded in same window)
        attendancePage = new AttendanceManagementPage();
        contentContainer.add(attendancePage, "Attendance");

        // assemble main frame
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentContainer, BorderLayout.CENTER);

        // set initial active button state
        highlightActiveMenu("Dashboard");

        add(mainPanel);
        setVisible(true);
    }

    // create the default dashboard overview view
    private JPanel createDashboardOverview() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(lightBackground);

        // welcome greeting
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(lightBackground);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25));

        JLabel welcome = new JLabel("Welcome, Technical Officer");
        welcome.setFont(new Font("Arial", Font.BOLD, 28));
        welcome.setForeground(textColor);

        JLabel subtitle = new JLabel("Manage lab attendance, medical records, and equipment status.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 110, 120));

        welcomePanel.add(welcome);
        welcomePanel.add(Box.createVerticalStrut(5));
        welcomePanel.add(subtitle);

        contentPanel.add(welcomePanel, BorderLayout.NORTH);

        // center cards and recent activity list
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(lightBackground);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

        // quick stats cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsPanel.setBackground(lightBackground);

        cardsPanel.add(createCard("Assigned Labs", "0", new Color(45, 100, 160)));
        cardsPanel.add(createCard("Today's Sessions", "0", new Color(20, 165, 145)));
        cardsPanel.add(createCard("Pending Medicals", "0", new Color(115, 75, 180)));
        cardsPanel.add(createCard("Equipment Faults", "0", new Color(210, 60, 60)));

        // recent activity log
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 228, 232)),
                        BorderFactory.createEmptyBorder(18, 18, 18, 18)
                )
        );

        JLabel activityTitle = new JLabel("Technical Officer Activities");
        activityTitle.setFont(new Font("Arial", Font.BOLD, 16));
        activityTitle.setForeground(textColor);

        JPanel activityList = new JPanel();
        activityList.setBackground(Color.WHITE);
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));

        activityPanel.add(activityTitle, BorderLayout.NORTH);
        activityPanel.add(activityList, BorderLayout.CENTER);

        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(activityPanel, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    // helper to add menu buttons and bind view switching
    private void addMenuButton(JPanel panel, String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setForeground(textColor);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        button.addActionListener(e -> {
            if ("Dashboard".equalsIgnoreCase(text) || "Attendance".equalsIgnoreCase(text)) {
                cardLayout.show(contentContainer, text);
                highlightActiveMenu(text);
            }
        });

        menuButtons.put(text, button);
        panel.add(button);
    }

    // change background color of selected menu item
    private void highlightActiveMenu(String activeText) {
        currentView = activeText;
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(activeText)) {
                entry.getValue().setBackground(activeMenuColor);
                entry.getValue().setFont(new Font("Arial", Font.BOLD, 13));
            } else {
                entry.getValue().setBackground(Color.WHITE);
                entry.getValue().setFont(new Font("Arial", Font.PLAIN, 13));
            }
        }
    }

    // dashboard stat card component
    private JPanel createCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 228, 232)),
                        BorderFactory.createEmptyBorder(18, 18, 18, 18)
                )
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 110, 120));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 25));
        valueLabel.setForeground(valueColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    public static void main(String[] args) {
        // use system look and feel if available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new TechnicalOfficerDashboard());
    }
}