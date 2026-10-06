package lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LecturerDashboard extends JFrame {

    // ================= COLORS =================

    private final Color HEADER_COLOR = new Color(25, 67, 103);
    private final Color BACKGROUND_COLOR = new Color(247, 249, 252);
    private final Color TEXT_COLOR = new Color(35, 45, 55);
    private final Color SUBTEXT_COLOR = new Color(105, 115, 125);

    // ================= CONSTRUCTOR =================

    public LecturerDashboard() {

        setTitle("Faculty Academic Management System - Lecturer");

        setSize(1100, 700);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Dashboard
        add(createDashboard(), BorderLayout.CENTER);
    }

    // =====================================================
    // HEADER
    // =====================================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(HEADER_COLOR);

        header.setPreferredSize(
                new Dimension(0, 65)
        );

        header.setBorder(
                new EmptyBorder(
                        0,
                        20,
                        0,
                        20
                )
        );

        // System title
        JLabel title =
                new JLabel(
                        "FACULTY ACADEMIC MANAGEMENT SYSTEM"
                );

        title.setForeground(Color.WHITE);

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        // Lecturer
        JLabel role =
                new JLabel("LECTURER");

        role.setForeground(Color.WHITE);

        role.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        13
                )
        );

        header.add(
                title,
                BorderLayout.WEST
        );

        header.add(
                role,
                BorderLayout.EAST
        );

        return header;
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private JPanel createSidebar() {

        JPanel sidebar =
                new JPanel(
                        new BorderLayout()
                );

        sidebar.setBackground(
                Color.WHITE
        );

        sidebar.setPreferredSize(
                new Dimension(
                        190,
                        0
                )
        );

        // ================= MENU PANEL =================

        JPanel menuPanel =
                new JPanel();

        menuPanel.setBackground(
                Color.WHITE
        );

        menuPanel.setLayout(
                new BoxLayout(
                        menuPanel,
                        BoxLayout.Y_AXIS
                )
        );

        menuPanel.setBorder(
                new EmptyBorder(
                        25,
                        15,
                        10,
                        15
                )
        );

        // Lecturer Menu
        JLabel menuTitle =
                new JLabel(
                        "LECTURER MENU"
                );

        menuTitle.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        11
                )
        );

        menuTitle.setForeground(
                SUBTEXT_COLOR
        );

        menuPanel.add(menuTitle);

        menuPanel.add(
                Box.createVerticalStrut(15)
        );

        // =================================================
        // DASHBOARD BUTTON
        // =================================================

        JButton dashboardButton =
                createMenuButton(
                        "Dashboard"
                );

        // Make dashboard bold because this is current page
        dashboardButton.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        13
                )
        );

        menuPanel.add(
                dashboardButton
        );

        // =================================================
        // MY COURSES BUTTON
        // =================================================

        JButton coursesButton =
                createMenuButton(
                        "My Courses"
                );

        /*
         * IMPORTANT
         *
         * This connects LecturerDashboard
         * with MyCourses.java
         */

        coursesButton.addActionListener(e -> {

            MyCourses myCourses =
                    new MyCourses();

            myCourses.setVisible(true);

            // Close dashboard
            dispose();
        });

        menuPanel.add(
                coursesButton
        );

        // =================================================
        // STUDENTS
        // =================================================

        JButton studentsButton =
                createMenuButton(
                        "Students"
                );

        studentsButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Students page will be added next."
            );

        });

        menuPanel.add(
                studentsButton
        );

        // =================================================
        // ATTENDANCE
        // =================================================

        JButton attendanceButton =
                createMenuButton(
                        "Attendance"
                );

        attendanceButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance page will be added next."
            );

        });

        menuPanel.add(
                attendanceButton
        );

        // =================================================
        // MARKS / GRADES
        // =================================================

        JButton marksButton =
                createMenuButton(
                        "Marks / Grades"
                );

        marksButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Marks / Grades page will be added next."
            );

        });

        menuPanel.add(
                marksButton
        );

        // =================================================
        // ASSIGNMENTS
        // =================================================

        JButton assignmentsButton =
                createMenuButton(
                        "Assignments"
                );

        assignmentsButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Assignments page will be added next."
            );

        });

        menuPanel.add(
                assignmentsButton
        );

        // =================================================
        // PROFILE
        // =================================================

        JButton profileButton =
                createMenuButton(
                        "My Profile"
                );

        profileButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Profile page will be added next."
            );

        });

        menuPanel.add(
                profileButton
        );

        sidebar.add(
                menuPanel,
                BorderLayout.NORTH
        );

        // =================================================
        // LOGOUT
        // =================================================

        JPanel logoutPanel =
                new JPanel(
                        new BorderLayout()
                );

        logoutPanel.setBackground(
                Color.WHITE
        );

        logoutPanel.setBorder(
                new EmptyBorder(
                        10,
                        15,
                        20,
                        15
                )
        );

        JButton logoutButton =
                new JButton(
                        "Logout"
                );

        logoutButton.setForeground(
                new Color(
                        190,
                        60,
                        60
                )
        );

        logoutButton.setBackground(
                Color.WHITE
        );

        logoutButton.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        12
                )
        );

        logoutButton.setBorderPainted(false);

        logoutButton.setFocusPainted(false);

        logoutButton.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        logoutButton.addActionListener(e -> {

            int answer =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to logout?",
                            "Logout",
                            JOptionPane.YES_NO_OPTION
                    );

            if (answer ==
                    JOptionPane.YES_OPTION) {

                System.exit(0);
            }
        });

        logoutPanel.add(
                logoutButton,
                BorderLayout.WEST
        );

        sidebar.add(
                logoutPanel,
                BorderLayout.SOUTH
        );

        return sidebar;
    }

    // =====================================================
    // CREATE MENU BUTTON
    // =====================================================

    private JButton createMenuButton(
            String text
    ) {

        JButton button =
                new JButton(text);

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );

        button.setPreferredSize(
                new Dimension(
                        160,
                        45
                )
        );

        button.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        13
                )
        );

        button.setForeground(
                TEXT_COLOR
        );

        button.setBackground(
                Color.WHITE
        );

        button.setBorderPainted(false);

        button.setFocusPainted(false);

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        return button;
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private JPanel createDashboard() {

        JPanel dashboard =
                new JPanel(
                        new BorderLayout()
                );

        dashboard.setBackground(
                BACKGROUND_COLOR
        );

        dashboard.setBorder(
                new EmptyBorder(
                        25,
                        25,
                        25,
                        25
                )
        );

        // =================================================
        // WELCOME
        // =================================================

        JPanel welcomePanel =
                new JPanel();

        welcomePanel.setBackground(
                BACKGROUND_COLOR
        );

        welcomePanel.setLayout(
                new BoxLayout(
                        welcomePanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel welcome =
                new JLabel(
                        "Welcome, Lecturer"
                );

        welcome.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );

        welcome.setForeground(
                TEXT_COLOR
        );

        JLabel description =
                new JLabel(
                        "Manage your courses, students, attendance and academic activities."
                );

        description.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );

        description.setForeground(
                SUBTEXT_COLOR
        );

        welcomePanel.add(welcome);

        welcomePanel.add(
                Box.createVerticalStrut(5)
        );

        welcomePanel.add(
                description
        );

        dashboard.add(
                welcomePanel,
                BorderLayout.NORTH
        );

        // =================================================
        // CENTER PANEL
        // =================================================

        JPanel centerPanel =
                new JPanel(
                        new BorderLayout(
                                0,
                                20
                        )
                );

        centerPanel.setBackground(
                BACKGROUND_COLOR
        );

        centerPanel.setBorder(
                new EmptyBorder(
                        25,
                        0,
                        0,
                        0
                )
        );

        // =================================================
        // CARDS
        // =================================================

        JPanel cardsPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                15,
                                0
                        )
                );

        cardsPanel.setBackground(
                BACKGROUND_COLOR
        );

        cardsPanel.add(
                createCard(
                        "My Courses",
                        "4",
                        new Color(
                                45,
                                90,
                                135
                        )
                )
        );

        cardsPanel.add(
                createCard(
                        "Students",
                        "86",
                        new Color(
                                40,
                                170,
                                160
                        )
                )
        );

        cardsPanel.add(
                createCard(
                        "Assignments",
                        "12",
                        new Color(
                                45,
                                70,
                                95
                        )
                )
        );

        cardsPanel.add(
                createCard(
                        "Pending Marks",
                        "8",
                        new Color(
                                125,
                                65,
                                180
                        )
                )
        );

        centerPanel.add(
                cardsPanel,
                BorderLayout.NORTH
        );

        // =================================================
        // ACTIVITIES
        // =================================================

        JPanel activitiesPanel =
                new JPanel();

        activitiesPanel.setBackground(
                Color.WHITE
        );

        activitiesPanel.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                new Color(
                                        225,
                                        230,
                                        235
                                )
                        ),

                        new EmptyBorder(
                                18,
                                18,
                                18,
                                18
                        )
                )
        );

        activitiesPanel.setLayout(
                new BoxLayout(
                        activitiesPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel activitiesTitle =
                new JLabel(
                        "Lecturer Activities"
                );

        activitiesTitle.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        17
                )
        );

        activitiesTitle.setForeground(
                TEXT_COLOR
        );

        activitiesPanel.add(
                activitiesTitle
        );

        activitiesPanel.add(
                createActivity(
                        "Attendance updated for ICT2132"
                )
        );

        activitiesPanel.add(
                createActivity(
                        "New assignment created"
                )
        );

        activitiesPanel.add(
                createActivity(
                        "Marks submitted for Programming"
                )
        );

        activitiesPanel.add(
                createActivity(
                        "Course materials updated"
                )
        );

        centerPanel.add(
                activitiesPanel,
                BorderLayout.CENTER
        );

        dashboard.add(
                centerPanel,
                BorderLayout.CENTER
        );

        return dashboard;
    }

    // =====================================================
    // CREATE CARD
    // =====================================================

    private JPanel createCard(
            String title,
            String value,
            Color valueColor
    ) {

        JPanel card =
                new JPanel();

        card.setBackground(
                Color.WHITE
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                new Color(
                                        225,
                                        230,
                                        235
                                )
                        ),

                        new EmptyBorder(
                                18,
                                18,
                                18,
                                18
                        )
                )
        );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        13
                )
        );

        titleLabel.setForeground(
                SUBTEXT_COLOR
        );

        JLabel valueLabel =
                new JLabel(value);

        valueLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        30
                )
        );

        valueLabel.setForeground(
                valueColor
        );

        card.add(
                titleLabel
        );

        card.add(
                Box.createVerticalStrut(8)
        );

        card.add(
                valueLabel
        );

        return card;
    }

    // =====================================================
    // CREATE ACTIVITY
    // =====================================================

    private JLabel createActivity(
            String text
    ) {

        JLabel activity =
                new JLabel(
                        "• " + text
                );

        activity.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        13
                )
        );

        activity.setForeground(
                SUBTEXT_COLOR
        );

        activity.setBorder(
                new EmptyBorder(
                        7,
                        0,
                        7,
                        0
                )
        );

        return activity;
    }

    // =====================================================
    // MAIN METHOD
    // =====================================================

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(() -> {

            LecturerDashboard dashboard =
                    new LecturerDashboard();

            dashboard.setVisible(true);

        });
    }
}