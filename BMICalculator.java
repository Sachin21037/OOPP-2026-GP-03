import javax.swing.*;
import java.awt.*;

public class BMICalculator extends JFrame {

    // Components
    private JTextField nameField;
    private JTextField heightField;
    private JTextField weightField;

    private JComboBox<String> weightUnitBox;

    private JTextField bmiField;
    private JLabel categoryLabel;

    private JButton calculateButton;
    private JButton clearButton;
    private JButton exitButton;

    public BMICalculator() {

        // Window
        setTitle("BMI Calculator");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title
        JLabel titleLabel = new JLabel("BMI CALCULATOR", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Fields
        nameField = new JTextField();
        heightField = new JTextField();
        weightField = new JTextField();

        // Weight unit
        String[] units = {"kg", "lb"};
        weightUnitBox = new JComboBox<>(units);

        // BMI output
        bmiField = new JTextField();
        bmiField.setEditable(false);

        categoryLabel = new JLabel(" ");

        // Buttons
        calculateButton = new JButton("CALCULATE");
        clearButton = new JButton("CLEAR");
        exitButton = new JButton("EXIT");

        // Add title
        add(titleLabel, BorderLayout.NORTH);

        // Add components
        panel.add(new JLabel("Name:"));
        panel.add(nameField);

        panel.add(new JLabel("Height (cm):"));
        panel.add(heightField);

        panel.add(new JLabel("Weight:"));
        panel.add(weightField);

        panel.add(new JLabel("Weight Unit:"));
        panel.add(weightUnitBox);

        panel.add(new JLabel(""));
        panel.add(calculateButton);

        panel.add(new JLabel("BMI:"));
        panel.add(bmiField);

        panel.add(new JLabel("Category:"));
        panel.add(categoryLabel);

        panel.add(clearButton);
        panel.add(exitButton);

        add(panel, BorderLayout.CENTER);

        // Calculate button
        calculateButton.addActionListener(e -> calculateBMI());

        // Clear button
        clearButton.addActionListener(e -> clearFields());

        // Exit button
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void calculateBMI() {

        try {

            double height = Double.parseDouble(heightField.getText());
            double weight = Double.parseDouble(weightField.getText());

            // Convert height cm to meters
            double heightInMeters = height / 100;

            // If weight is pounds, convert to kilograms
            String unit = (String) weightUnitBox.getSelectedItem();

            if (unit.equals("lb")) {
                weight = weight * 0.453592;
                height=heightInMeters*0.0254;
            }

            // BMI formula
            double bmi = weight / (heightInMeters * heightInMeters);

            bmiField.setText(String.format("%.2f", bmi));

            // BMI category
            if (bmi < 18.5) {
                categoryLabel.setText("Underweight");

            } else if (bmi < 25) {
                categoryLabel.setText("Normal Weight");

            } else if (bmi < 30) {
                categoryLabel.setText("Overweight");

            } else {
                categoryLabel.setText("Obese");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numbers.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearFields() {

        nameField.setText("");
        heightField.setText("");
        weightField.setText("");

        weightUnitBox.setSelectedIndex(0);

        bmiField.setText("");
        categoryLabel.setText(" ");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            BMICalculator frame = new BMICalculator();
            frame.setVisible(true);

        });
    }
}