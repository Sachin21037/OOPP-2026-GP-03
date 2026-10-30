package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reusable MySQL Database Connection Utility for FAMS (Faculty Academic Management System).
 *
 * Configured for XAMPP MySQL environment:
 * - Host: localhost
 * - Port: 3306 (with automatic fallback to 3307 if XAMPP was assigned port 3307)
 * - Database: fams
 * - Username: root
 * - Password: empty ("")
 *
 * Designed for Java 21 and standard JDBC / MySQL Connector/J driver.
 */
public class DBConnection {

    // Database Configuration Constants
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306"; // Primary default port
    private static final String ALT_PORT = "3307"; // Alternative port used by XAMPP when 3306 is occupied
    private static final String DB_NAME = "fams";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Default XAMPP MySQL password is empty

    // JDBC Driver Class
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    // Active port determined during runtime connection
    private static String activePort = DB_PORT;

    // Singleton instance and active connection holder
    private static DBConnection instance;
    private Connection connection;

    // Static block to explicitly load the JDBC driver
    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("======================================================================");
            System.err.println("[FAMS DBConnection WARNING] MySQL Connector/J driver class not found!");
            System.err.println("Driver Class: " + DRIVER_CLASS);
            System.err.println("Resolution  : Please ensure mysql-connector-j-*.jar is added to your");
            System.err.println("              IntelliJ Project Libraries (File -> Project Structure -> Libraries).");
            System.err.println("======================================================================");
        }
    }

    /**
     * Private constructor for Singleton pattern.
     */
    private DBConnection() {
        // Enforce singleton usage
    }

    /**
     * Builds the JDBC Connection URL for a specific port.
     */
    public static String buildJdbcUrl(String port) {
        return "jdbc:mysql://" + DB_HOST + ":" + port + "/" + DB_NAME
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    /**
     * Returns the singleton instance of DBConnection.
     *
     * @return DBConnection instance
     * @throws SQLException if a database connection error occurs
     */
    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Retrieves an active Connection. Reuses existing connection if still open and valid,
     * or opens a new one if it is null or closed.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection cannot be established
     */
    public static synchronized Connection getConnection() throws SQLException {
        DBConnection db = getInstance();
        if (db.connection == null || db.connection.isClosed()) {
            db.connection = createConnection();
        }
        return db.connection;
    }

    /**
     * Opens and returns a fresh, independent Connection.
     * Useful for transactions or separate worker threads.
     *
     * @return new java.sql.Connection
     * @throws SQLException if connection cannot be established
     */
    public static Connection getNewConnection() throws SQLException {
        return createConnection();
    }

    /**
     * Internal factory method to create a new Connection with automatic port detection
     * and detailed diagnostic error handling.
     */
    private static Connection createConnection() throws SQLException {
        // 1. Try primary port (3306)
        try {
            Connection conn = DriverManager.getConnection(buildJdbcUrl(activePort), DB_USER, DB_PASSWORD);
            return conn;
        } catch (SQLException primaryEx) {
            // If primary port 3306 failed (e.g. access denied or communication failure due to dual MySQL installations),
            // attempt alternative port 3307 (commonly assigned by XAMPP on macOS).
            if (!activePort.equals(ALT_PORT)) {
                try {
                    Connection altConn = DriverManager.getConnection(buildJdbcUrl(ALT_PORT), DB_USER, DB_PASSWORD);
                    activePort = ALT_PORT;
                    System.out.println("[FAMS DBConnection Notice] Connected to XAMPP MySQL on port " + ALT_PORT
                            + " (primary port " + DB_PORT + " was unavailable or assigned to a different MySQL instance).");
                    return altConn;
                } catch (SQLException ignored) {
                    // Fall through to report primary exception
                }
            }
            handleConnectionError(primaryEx, activePort);
            throw primaryEx;
        }
    }

    /**
     * Analyzes and outputs clear, user-friendly diagnostics for common connection errors.
     */
    private static void handleConnectionError(SQLException e, String attemptedPort) {
        System.err.println("\n[FAMS Database Connection Error]");
        System.err.println("• Error Message  : " + e.getMessage());
        System.err.println("• Error Code     : " + e.getErrorCode());
        System.err.println("• SQL State      : " + e.getSQLState());
        System.err.println("• Attempted Port : " + attemptedPort);
        System.err.println("• Target URL     : " + buildJdbcUrl(attemptedPort));

        int code = e.getErrorCode();
        if (code == 1049) {
            System.err.println("\nDiagnostic: Unknown database '" + DB_NAME + "'.");
            System.err.println("Solution  : Open phpMyAdmin in XAMPP (http://localhost/phpmyadmin) and ensure database '" + DB_NAME + "' exists.");
        } else if (code == 1045) {
            System.err.println("\nDiagnostic: Access denied for user '" + DB_USER + "'.");
            System.err.println("Solution  : Verify the MySQL username and password in XAMPP.");
        } else if (e.getMessage() != null && (e.getMessage().contains("Communications link failure") || e.getMessage().contains("Connection refused"))) {
            System.err.println("\nDiagnostic: Could not reach MySQL on " + DB_HOST + ":" + attemptedPort + ".");
            System.err.println("Solution  : Ensure MySQL is running in XAMPP Control Panel.");
        }
        System.err.println();
    }

    /**
     * Safely closes the active singleton connection.
     */
    public static synchronized void closeConnection() {
        if (instance != null && instance.connection != null) {
            try {
                if (!instance.connection.isClosed()) {
                    instance.connection.close();
                    System.out.println("[FAMS DBConnection] Connection closed successfully.");
                }
            } catch (SQLException e) {
                System.err.println("[FAMS DBConnection] Error while closing connection: " + e.getMessage());
            } finally {
                instance.connection = null;
            }
        }
    }

    /**
     * Tests the database connection and prints informative status and schema metadata.
     *
     * @return true if connection and query succeeded, false otherwise
     */
    public static boolean testConnection() {
        System.out.println("=================================================================");
        System.out.println("         FAMS - Database Connection Diagnostic Utility           ");
        System.out.println("=================================================================");
        System.out.println("Target Database : " + DB_NAME);
        System.out.println("Primary Host    : " + DB_HOST + ":" + DB_PORT);
        System.out.println("Alternative Port: " + ALT_PORT);
        System.out.println("Database User   : " + DB_USER);
        System.out.println("Attempting connection...");

        long startTime = System.currentTimeMillis();

        try (Connection conn = getNewConnection();
             Statement stmt = conn.createStatement()) {

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("\n[SUCCESS] Connection established successfully in " + elapsed + " ms!");
            System.out.println("Connected Port  : " + activePort);

            // Query server metadata
            try (ResultSet rs = stmt.executeQuery("SELECT DATABASE() AS current_db, VERSION() AS mysql_version")) {
                if (rs.next()) {
                    System.out.println("Active Database : " + rs.getString("current_db"));
                    System.out.println("MySQL Version   : " + rs.getString("mysql_version"));
                }
            }

            // Query existing tables in fams database
            try (ResultSet rsTables = stmt.executeQuery("SHOW TABLES")) {
                int tableCount = 0;
                System.out.println("\nExisting Tables in '" + DB_NAME + "':");
                while (rsTables.next()) {
                    tableCount++;
                    System.out.println(String.format("  [%2d] %s", tableCount, rsTables.getString(1)));
                }
                System.out.println("\nTotal tables verified: " + tableCount);
            }

            System.out.println("=================================================================");
            System.out.println("RESULT: SUCCESS - Ready for DAO and Service integration.");
            System.out.println("=================================================================");
            return true;

        } catch (SQLException e) {
            System.err.println("=================================================================");
            System.err.println("RESULT: FAILED - Connection could not be established.");
            System.err.println("=================================================================");
            return false;
        } catch (Throwable t) {
            System.err.println("\nUnexpected Error: " + t.getMessage());
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Standalone main method to verify the database connection directly.
     * Run this class in IntelliJ IDEA to test your XAMPP connection.
     */
    public static void main(String[] args) {
        boolean success = testConnection();
        if (success) {
            System.out.println("\n[OK] Database connection test passed successfully.");
        } else {
            System.err.println("\n[ERROR] Database connection test failed. Please check the diagnostic logs above.");
            System.exit(1);
        }
    }
}
