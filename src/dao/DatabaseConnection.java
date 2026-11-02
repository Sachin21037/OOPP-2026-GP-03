package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DatabaseConnection Utility Class
 * Unified connector delegating directly to database.DBConnection for robust MySQL (XAMPP) integration.
 */
public class DatabaseConnection {

    /**
     * Obtains an active connection from the unified DBConnection pool.
     */
    public static synchronized Connection getConnection() {
        try {
            return DBConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] MySQL connection error: " + e.getMessage());
            return null;
        }
    }

    public static String getActiveDbName() {
        return "fams";
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
        DBConnection.closeConnection();
    }
}
