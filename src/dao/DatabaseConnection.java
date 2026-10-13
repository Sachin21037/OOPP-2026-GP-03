package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection Utility Class
 * Manages JDBC connections to MySQL database (XAMPP).
 * Includes resilient graceful degradation when database server is offline.
 */
public class DatabaseConnection {

    private static final String[] DB_NAMES = {"fams", "fams_db"};
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static String activeDbName = null;

    private static Connection connection = null;
    private static boolean driverAvailable = false;
    private static boolean connectionAttempted = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverAvailable = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                driverAvailable = true;
            } catch (ClassNotFoundException ignored) {
                driverAvailable = false;
            }
        }
    }

    public static synchronized Connection getConnection() {
        if (!driverAvailable) {
            return null;
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            // Try connecting to fams (from fams.sql) first, then fallback to fams_db
            SQLException lastEx = null;
            for (String db : DB_NAMES) {
                try {
                    String url = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                    connection = DriverManager.getConnection(url, USER, PASSWORD);
                    activeDbName = db;
                    return connection;
                } catch (SQLException ex) {
                    lastEx = ex;
                }
            }

            if (!connectionAttempted) {
                System.out.println("[DatabaseConnection] XAMPP MySQL database not reachable: " + (lastEx != null ? lastEx.getMessage() : "unknown"));
                System.out.println("[DatabaseConnection] Using high-fidelity in-memory local data store aligned with fams.sql.");
                connectionAttempted = true;
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public static String getActiveDbName() {
        return activeDbName;
    }

    public static boolean isConnected() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
        }
    }
}
