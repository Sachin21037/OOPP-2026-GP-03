package ui.student;

import dao.StudentDAO;
import model.Course;
import model.Student;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MyCoursesFrame extends JFrame {

    private final Color darkBlue = new Color(27, 67, 103);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color textColor = new Color(35, 45, 55);

    private final StudentDAO studentDAO = new StudentDAO();
    private final Student student;

    public MyCoursesFrame() {
        this(null);
    }

    public MyCoursesFrame(Student currentStudent) {
        if (currentStudent == null) {
            // Default fallback to first student STU001
            this.student = studentDAO.getStudentProfile("STU001");
        } else {
            this.student = currentStudent;
        }

        setTitle("My Enrolled Courses - FAMS");
        setSize(950, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(darkBlue);
        headerPanel.setPreferredSize(new Dimension(950, 60));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Enrolled Courses");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        String subtitle = (student != null) 
                ? student.getFullName() + " (" + student.getIndexNo() + ")" 
                : "Student Academic Courses";
        JLabel studentLabel = new JLabel(subtitle);
        studentLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        studentLabel.setForeground(new Color(180, 240, 220));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(studentLabel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(lightBackground);
        contentPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        // Top info bar
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        infoBar.setBackground(Color.WHITE);
        infoBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                new EmptyBorder(5, 10, 5, 10)
        ));

        List<Course> courses = (student != null) ? studentDAO.getEnrolledCourses(student.getUserId()) : List.of();
        int totalCredits = courses.stream().mapToInt(Course::getCredits).sum();

        JLabel totalCoursesLabel = new JLabel("Total Registered Courses: " + courses.size());
        totalCoursesLabel.setFont(new Font("Arial", Font.BOLD, 13));
        totalCoursesLabel.setForeground(darkBlue);

        JLabel totalCreditsLabel = new JLabel("Total Credits: " + totalCredits);
        totalCreditsLabel.setFont(new Font("Arial", Font.BOLD, 13));
        totalCreditsLabel.setForeground(new Color(20, 165, 145));

        infoBar.add(totalCoursesLabel);
        infoBar.add(new JSeparator(SwingConstants.VERTICAL));
        infoBar.add(totalCreditsLabel);

        contentPanel.add(infoBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Course Code", "Course Title", "Credits", "Semester", "Academic Year", "Coordinator / Lecturer"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Course c : courses) {
            model.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredits() + " Credits",
                    c.getSemester(),
                    c.getAcademicYear(),
                    c.getLecturerName()
            });
        }

        JTable table = new JTable(model);
        table.setDefaultEditor(Object.class, null); // Strictly prevent editing
        table.setRowHeight(38);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setGridColor(new Color(235, 238, 242));
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setSelectionForeground(textColor);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);
        tableHeader.setFont(new Font("Arial", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 244, 248));
        tableHeader.setForeground(textColor);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 38));

        // Center align code and credits
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(lightBackground);

        JButton closeBtn = new JButton("Close Window");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 12));
        closeBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        closeBtn.setOpaque(true);
        closeBtn.setBackground(new Color(29, 78, 216));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(7, 18, 7, 18)
        ));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setBackground(new Color(37, 99, 235));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setBackground(new Color(29, 78, 216));
            }
        });
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);

        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
}
