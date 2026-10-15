package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Reusable database connection class for the Faculty Academic Management System (FAMS).
 * Connects to the local MySQL database named 'fams' via JDBC.
 */
public class DBConnection {

    // JDBC Connection URL for database 'fams'
    private static final String URL = "jdbc:mysql://localhost:3306/fams";

    // Default username for MySQL
    private static final String USER = "root";

    // Configurable password:
    // Change this value to match your local MySQL setup (e.g., "" for default XAMPP/local root, or "root", "1234", etc.)
    // Can also be set programmatically via setPassword("...") or via JVM option -Dfams.db.password=your_pwd
    private static String password = "";

    // MySQL Connector/J driver class name
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] WARNING: MySQL JDBC Driver '" + DRIVER_CLASS + "' was not found in classpath.");
            System.err.println("[DBConnection] Please add mysql-connector-j.jar to IntelliJ Project Structure -> Libraries.");
        }
    }

    // Private constructor to enforce utility pattern
    private DBConnection() {
    }

    /**
     * Establishes and returns an active Connection to the 'fams' MySQL database.
     *
     * @return Connection active JDBC connection
     * @throws SQLException if a database access error occurs or credentials/URL are invalid
     */
    public static Connection getConnection() throws SQLException {
        String activePassword = System.getProperty("fams.db.password", password);
        return DriverManager.getConnection(URL, USER, activePassword);
    }

    /**
     * Configure or change the database password at runtime.
     *
     * @param newPassword the new MySQL root password
     */
    public static void setPassword(String newPassword) {
        password = newPassword;
    }

    /**
     * Get the currently configured password.
     *
     * @return current password
     */
    public static String getPassword() {
        return password;
    }

    /**
     * Get the JDBC URL.
     *
     * @return URL string
     */
    public static String getUrl() {
        return URL;
    }

    /**
     * Get the database user.
     *
     * @return username string
     */
    public static String getUser() {
        return USER;
    }

    /**
     * Safely closes a database connection if open.
     *
     * @param connection the Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
            }
        }
    }
}
