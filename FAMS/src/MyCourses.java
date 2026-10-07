package lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MyCourses extends JFrame {

    // ================= COLORS =================

    private final Color HEADER_COLOR = new Color(25, 67, 103);
    private final Color BACKGROUND_COLOR = new Color(247, 249, 252);
    private final Color TEXT_COLOR = new Color(35, 45, 55);
    private final Color SUBTEXT_COLOR = new Color(105, 115, 125);

    // ================= CONSTRUCTOR =================

    public MyCourses() {

        setTitle(
                "Faculty Academic Management System - My Courses"
        );

        setSize(
                1100,
                700
        );

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        setLayout(
                new BorderLayout()
        );

        // Header
        add(
                createHeader(),
                BorderLayout.NORTH
        );

        // Sidebar
        add(
                createSidebar(),
                BorderLayout.WEST
        );

        // Main Content
        add(
                createMainContent(),
                BorderLayout.CENTER
        );
    }

    // =====================================================
    // HEADER
    // =====================================================

    private JPanel createHeader() {

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(
                HEADER_COLOR
        );

        header.setPreferredSize(
                new Dimension(
                        0,
                        65
                )
        );

        header.setBorder(
                new EmptyBorder(
                        0,
                        20,
                        0,
                        20
                )
        );

        JLabel title =
                new JLabel(
                        "FACULTY ACADEMIC MANAGEMENT SYSTEM"
                );

        title.setForeground(
                Color.WHITE
        );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        JLabel role =
                new JLabel(
                        "LECTURER"
                );

        role.setForeground(
                Color.WHITE
        );

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

        // =================================================
        // MENU
        // =================================================

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

        menuPanel.add(
                menuTitle
        );

        menuPanel.add(
                Box.createVerticalStrut(
                        15
                )
        );

        // =================================================
        // DASHBOARD
        // =================================================

        JButton dashboardButton =
                createMenuButton(
                        "Dashboard"
                );

        /*
         * Connect back to
         * LecturerDashboard.java
         */

        dashboardButton.addActionListener(e -> {

            lecturer.LecturerDashboard dashboard =
                    new lecturer.LecturerDashboard();

            dashboard.setVisible(true);

            // Close My Courses page
            dispose();
        });

        menuPanel.add(
                dashboardButton
        );

        // =================================================
        // MY COURSES
        // =================================================

        JButton coursesButton =
                createMenuButton(
                        "My Courses"
                );

        // Bold because this is current page
        coursesButton.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        13
                )
        );

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
        // MARKS
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

        logoutButton.setBorderPainted(
                false
        );

        logoutButton.setFocusPainted(
                false
        );

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
    // MENU BUTTON
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

        button.setBorderPainted(
                false
        );

        button.setFocusPainted(
                false
        );

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
    // MAIN CONTENT
    // =====================================================

    private JPanel createMainContent() {

        JPanel mainPanel =
                new JPanel(
                        new BorderLayout(
                                0,
                                20
                        )
                );

        mainPanel.setBackground(
                BACKGROUND_COLOR
        );

        mainPanel.setBorder(
                new EmptyBorder(
                        25,
                        25,
                        25,
                        25
                )
        );

        // =================================================
        // TITLE AREA
        // =================================================

        JPanel titlePanel =
                new JPanel();

        titlePanel.setBackground(
                BACKGROUND_COLOR
        );

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel pageTitle =
                new JLabel(
                        "My Courses"
                );

        pageTitle.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );

        pageTitle.setForeground(
                TEXT_COLOR
        );

        JLabel description =
                new JLabel(
                        "View the courses assigned to you."
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

        titlePanel.add(
                pageTitle
        );

        titlePanel.add(
                Box.createVerticalStrut(5)
        );

        titlePanel.add(
                description
        );

        mainPanel.add(
                titlePanel,
                BorderLayout.NORTH
        );

        // =================================================
        // CENTER PANEL
        // =================================================

        JPanel centerPanel =
                new JPanel(
                        new BorderLayout(
                                0,
                                15
                        )
                );

        centerPanel.setBackground(
                BACKGROUND_COLOR
        );

        // =================================================
        // SUMMARY CARDS
        // =================================================

        JPanel summaryPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                15,
                                0
                        )
                );

        summaryPanel.setBackground(
                BACKGROUND_COLOR
        );

        summaryPanel.add(
                createSummaryCard(
                        "Total Courses",
                        "4"
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "Total Students",
                        "86"
                )
        );

        centerPanel.add(
                summaryPanel,
                BorderLayout.NORTH
        );

        // =================================================
        // TABLE PANEL
        // =================================================

        JPanel tablePanel =
                new JPanel(
                        new BorderLayout(
                                0,
                                15
                        )
                );

        tablePanel.setBackground(
                Color.WHITE
        );

        tablePanel.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                new Color(
                                        225,
                                        230,
                                        235
                                )
                        ),

                        new EmptyBorder(
                                15,
                                15,
                                15,
                                15
                        )
                )
        );

        JLabel tableTitle =
                new JLabel(
                        "Courses Assigned to Me"
                );

        tableTitle.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        17
                )
        );

        tableTitle.setForeground(
                TEXT_COLOR
        );

        tablePanel.add(
                tableTitle,
                BorderLayout.NORTH
        );

        // =================================================
        // TABLE DATA
        // =================================================

        String[] columns = {

                "Course Code",
                "Course Name",
                "Students",
                "Semester"
        };

        Object[][] data = {

                {
                        "ICT2132",
                        "Object Oriented Programming",
                        "28",
                        "Semester 1"
                },

                {
                        "ICT2142",
                        "E-Business",
                        "20",
                        "Semester 1"
                },

                {
                        "ICT2123",
                        "Database Management",
                        "22",
                        "Semester 1"
                },

                {
                        "ICT2152",
                        "Web Development",
                        "16",
                        "Semester 2"
                }
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        data,
                        columns
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {

                        return false;
                    }
                };

        JTable courseTable =
                new JTable(
                        model
                );

        courseTable.setRowHeight(
                40
        );

        courseTable.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        13
                )
        );

        courseTable.getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                13
                        )
                );

        courseTable.getTableHeader()
                .setBackground(
                        new Color(
                                240,
                                243,
                                247
                        )
                );

        courseTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        courseTable
                );

        tablePanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // =================================================
        // BUTTON PANEL
        // =================================================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        buttonPanel.setBackground(
                Color.WHITE
        );

        JButton refreshButton =
                new JButton(
                        "Refresh"
                );

        JButton viewButton =
                new JButton(
                        "View Course"
                );

        buttonPanel.add(
                refreshButton
        );

        buttonPanel.add(
                viewButton
        );

        tablePanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        // =================================================
        // VIEW COURSE BUTTON
        // =================================================

        viewButton.addActionListener(e -> {

            int selectedRow =
                    courseTable.getSelectedRow();

            // Nothing selected
            if (selectedRow == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select a course first.",
                        "No Course Selected",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            String courseCode =
                    courseTable.getValueAt(
                            selectedRow,
                            0
                    ).toString();

            String courseName =
                    courseTable.getValueAt(
                            selectedRow,
                            1
                    ).toString();

            String students =
                    courseTable.getValueAt(
                            selectedRow,
                            2
                    ).toString();

            String semester =
                    courseTable.getValueAt(
                            selectedRow,
                            3
                    ).toString();

            JOptionPane.showMessageDialog(
                    this,

                    "Course Code: "
                            + courseCode

                            + "\nCourse Name: "
                            + courseName

                            + "\nStudents: "
                            + students

                            + "\nSemester: "
                            + semester,

                    "Course Details",

                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        // =================================================
        // REFRESH BUTTON
        // =================================================

        refreshButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Course list refreshed."
            );

        });

        centerPanel.add(
                tablePanel,
                BorderLayout.CENTER
        );

        mainPanel.add(
                centerPanel,
                BorderLayout.CENTER
        );

        return mainPanel;
    }

    // =====================================================
    // SUMMARY CARD
    // =====================================================

    private JPanel createSummaryCard(
            String title,
            String value
    ) {

        JPanel card =
                new JPanel();

        card.setPreferredSize(
                new Dimension(
                        190,
                        90
                )
        );

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
                                12,
                                15,
                                12,
                                15
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
                new JLabel(
                        title
                );

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        12
                )
        );

        titleLabel.setForeground(
                SUBTEXT_COLOR
        );

        JLabel valueLabel =
                new JLabel(
                        value
                );

        valueLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        25
                )
        );

        valueLabel.setForeground(
                new Color(
                        45,
                        90,
                        135
                )
        );

        card.add(
                titleLabel
        );

        card.add(
                Box.createVerticalStrut(
                        5
                )
        );

        card.add(
                valueLabel
        );

        return card;
    }

    // =====================================================
    // MAIN
    // =====================================================

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(() -> {

            MyCourses myCourses =
                    new MyCourses();

            myCourses.setVisible(true);

        });
    }
}