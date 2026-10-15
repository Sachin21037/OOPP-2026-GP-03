package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton Database Connection Manager for Faculty Academic Management System (FAMS).
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/fams?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    // Tries configured password first, then empty password for standard XAMPP setup
    private static final String[] CANDIDATE_PASSWORDS = {"12345678", "", "root", "1234"};

    private static Connection connection = null;
    private static String workingPassword = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found! Ensure mysql-connector-j is in classpath.");
        }
    }

    private DBConnection() {}

    /**
     * Gets an active connection to the fams database.
     * Automatically reconnects if connection was closed or null.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        // If we already know the working password, try it first
        if (workingPassword != null) {
            try {
                connection = DriverManager.getConnection(URL, USER, workingPassword);
                return connection;
            } catch (SQLException ignored) {
                // fall through to iterate candidates
            }
        }

        SQLException lastEx = null;
        for (String pwd : CANDIDATE_PASSWORDS) {
            try {
                connection = DriverManager.getConnection(URL, USER, pwd);
                workingPassword = pwd;
                return connection;
            } catch (SQLException ex) {
                lastEx = ex;
            }
        }

        throw new SQLException("Could not connect to database 'fams'. Please ensure MySQL is running in XAMPP. Details: " + 
                (lastEx != null ? lastEx.getMessage() : "Unknown"));
    }

    /**
     * Closes the active connection safely.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }
}
