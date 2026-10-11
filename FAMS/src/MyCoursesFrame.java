import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MyCoursesFrame extends JFrame {

    Color darkBlue = new Color(27, 67, 103);
    Color lightBackground = new Color(245, 247, 250);
    Color textColor = new Color(35, 45, 55);

    public MyCoursesFrame() {
        setTitle("My Courses - Faculty Academic Management System");
        setSize(950, 600);
        setLocationRelativeTo(null);
//Set it to DISPOSE_ON_CLOSE so that only this window closes, without closing the dashboard.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // header section
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(950, 60));

        JLabel titleLabel = new JLabel("  ENROLLED COURSES");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel countLabel = new JLabel("Total Courses: 6  ");
        countLabel.setForeground(new Color(180, 240, 220));
        countLabel.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        //content section
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(lightBackground);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        //subtitle
        JLabel subtitle = new JLabel("Level 2 Semester 1");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 110, 120));
        contentPanel.add(subtitle, BorderLayout.NORTH);

        //course table view
        String[] columns = {"Course Code", "Course Title", "Credits", "Lecturer / Department", "Status"};
        Object[][] data = {
                {"ICT2162", "Management Information Systems", "2", "Department of Information Communication Technology", "Ongoing"},
                {"ICT2142", "E-Business Systems", "2", "Department of Information Communication Technology", "Ongoing"},
                {"ICT2122", "Object Oriented Programming", "2", "Department of Information Communication Technology", "Ongoing"},
                {"ICT2132", "Object Oriented Programming Practicum", "2", "Department of Information Communication Technology", "Ongoing"},
                {"ICT2113", "Data Structures and Algorithms", "2", "Department of Information Communication Technology", "Ongoing"},
                {"ICT2152", "Object Oriented Analysis and Design", "2", "Department of Information Communication Technology", "Ongoing"},
                {"TCS2112", "Business Economics", "2", "Department of Information Communication Technology", "Ongoing"},
                {"TCS2121", "Soft Skills", "2", "Department of Information Communication Technology", "Ongoing"},
                {"ENG2112", "English III", "2", "Department of Information Communication Technology", "Ongoing"}



        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table cells edit කල නොහැකි ලෙස හැදීම
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 240, 245));
        table.getTableHeader().setForeground(textColor);
        table.setGridColor(new Color(230, 235, 240));
        table.setSelectionBackground(new Color(220, 235, 250));
        table.setSelectionForeground(Color.BLACK);

        // all table data alignment to center
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        //bottom close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(lightBackground);

        JButton btnClose = new JButton("Back to Dashboard");
        btnClose.setFont(new Font("Arial", Font.PLAIN, 13));
        btnClose.setBackground(darkBlue);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose()); //closing only this window
        bottomPanel.add(btnClose);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
}