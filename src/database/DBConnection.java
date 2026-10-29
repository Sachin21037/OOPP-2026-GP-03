package database;

import dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DBConnection compatibility class.
 * Delegates to dao.DatabaseConnection to provide a unified connection
 * mechanism across all branches and modules of FAMS.
 */
public class DBConnection {

    /**
     * Obtains an active connection from the central DatabaseConnection manager.
     *
     * @return Connection instance, or null if database is unreachable.
     * @throws SQLException if a connection cannot be established.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Database connection is currently unavailable. Ensure MySQL is running in XAMPP.");
        }
        return conn;
    }

    /**
     * Safe connection getter that returns null on failure without throwing an exception.
     *
     * @return Connection or null
     */
    public static Connection getConnectionSafe() {
        return DatabaseConnection.getConnection();
    }

    public static boolean isConnected() {
        return DatabaseConnection.isConnected();
    }

    public static void closeConnection() {
        DatabaseConnection.closeConnection();
    }
}
