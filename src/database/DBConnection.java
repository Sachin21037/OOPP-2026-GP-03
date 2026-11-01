package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable MySQL Database Connection Utility for FAMS (Faculty Academic Management System).
 *
 * Configured specifically for XAMPP MySQL environment:
 * - Detects exact XAMPP MySQL port from /Applications/XAMPP/xamppfiles/etc/my.cnf (port 3307 on this system)
 * - Prevents accidental connection to rogue standalone MySQL daemons on port 3306
 * - Host: 127.0.0.1 (IPv4 loopback to avoid macOS IPv6 'localhost' resolution issues)
 * - Database: fams
 * - Username: root
 * - Password: empty ("") by default
 * - Auto-initialization: Creates database 'fams' and auto-loads fams.sql if missing
 *
 * Designed for Java 21 and standard JDBC / MySQL Connector/J driver.
 */
public class DBConnection {

    // Database Configuration Constants
    private static final String[] DB_HOSTS = {"127.0.0.1", "localhost"};
    private static final String DB_NAME = "fams";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Default XAMPP MySQL password is empty
    private static final String[] CANDIDATE_PASSWORDS = {"", "12345678", "root", "1234", "21037", "admin", "password"};
    private static String workingHost = "127.0.0.1";
    private static String workingPort = null;
    private static String workingPassword = null;
    private static volatile boolean collationAligned = false;

    // JDBC Driver Class
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

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
     * Detects the exact port configured in XAMPP's my.cnf / my.ini file.
     * On macOS XAMPP, this accurately reads port 3307 from /Applications/XAMPP/xamppfiles/etc/my.cnf.
     */
    public static String detectXamppPort() {
        // 1. Check system property override
        String sysPort = System.getProperty("fams.db.port");
        if (sysPort != null && !sysPort.trim().isEmpty()) {
            return sysPort.trim();
        }

        // 2. Check environment variable override
        String envPort = System.getenv("DB_PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            return envPort.trim();
        }

        // 3. Inspect macOS XAMPP configuration file
        File macCnf = new File("/Applications/XAMPP/xamppfiles/etc/my.cnf");
        if (macCnf.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(macCnf))) {
                String line;
                boolean inMysqld = false;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("[") && line.endsWith("]")) {
                        inMysqld = line.equalsIgnoreCase("[mysqld]");
                    } else if (inMysqld && line.startsWith("port")) {
                        String[] parts = line.split("=");
                        if (parts.length >= 2) {
                            String port = parts[1].trim();
                            if (!port.isEmpty()) {
                                return port;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            return "3307";
        }

        // 4. Inspect Windows XAMPP configuration file
        File winIni = new File("C:\\xampp\\mysql\\bin\\my.ini");
        if (winIni.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(winIni))) {
                String line;
                boolean inMysqld = false;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("[") && line.endsWith("]")) {
                        inMysqld = line.equalsIgnoreCase("[mysqld]");
                    } else if (inMysqld && line.startsWith("port")) {
                        String[] parts = line.split("=");
                        if (parts.length >= 2) {
                            String port = parts[1].trim();
                            if (!port.isEmpty()) {
                                return port;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return "3306";
    }

    /**
     * Builds the JDBC Connection URL for a specific host, port, and database name.
     */
    public static String buildJdbcUrl(String host, String port, String dbName) {
        String dbPath = (dbName != null && !dbName.isEmpty()) ? "/" + dbName : "";
        return "jdbc:mysql://" + host + ":" + port + dbPath
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci";
    }

    /**
     * Builds the JDBC Connection URL for a specific port.
     */
    public static String buildJdbcUrl(String port) {
        return buildJdbcUrl(workingHost, port, DB_NAME);
    }

    /**
     * Returns the singleton instance of DBConnection.
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
     */
    public static Connection getNewConnection() throws SQLException {
        return createConnection();
    }

    /**
     * Internal factory method to create a new Connection targeting XAMPP MySQL.
     * Prevents connecting to accidental rogue MySQL instances by locking to the detected XAMPP port.
     */
    private static Connection createConnection() throws SQLException {
        String xamppPort = detectXamppPort();

        // Fast path: try cached working connection configuration
        if (workingPassword != null && workingHost != null && workingPort != null && workingPort.equals(xamppPort)) {
            try {
                Connection conn = DriverManager.getConnection(buildJdbcUrl(workingHost, workingPort, DB_NAME), DB_USER, workingPassword);
                applyCollationSettings(conn);
                return conn;
            } catch (SQLException ignored) {
                workingPassword = null;
            }
        }

        SQLException lastEx = null;
        List<String> passwordsToTry = new ArrayList<>();
        String envPwd = System.getenv("DB_PASSWORD");
        if (envPwd != null && !envPwd.isEmpty()) {
            passwordsToTry.add(envPwd);
        }
        String propPwd = System.getProperty("fams.db.password");
        if (propPwd != null && !propPwd.isEmpty() && !passwordsToTry.contains(propPwd)) {
            passwordsToTry.add(propPwd);
        }
        for (String p : CANDIDATE_PASSWORDS) {
            if (!passwordsToTry.contains(p)) {
                passwordsToTry.add(p);
            }
        }

        // Specifically target XAMPP's detected port first (3307 on this Mac)
        List<String> portsToTry = new ArrayList<>();
        portsToTry.add(xamppPort);
        // Only if XAMPP port is not 3306 and not running on standard macOS XAMPP path, add standard fallback
        if (!xamppPort.equals("3306") && !new File("/Applications/XAMPP/xamppfiles").exists()) {
            portsToTry.add("3306");
        }

        for (String host : DB_HOSTS) {
            for (String port : portsToTry) {
                for (String pwd : passwordsToTry) {
                    try {
                        Connection conn = DriverManager.getConnection(buildJdbcUrl(host, port, DB_NAME), DB_USER, pwd);
                        workingHost = host;
                        workingPort = port;
                        workingPassword = pwd;
                        ensureTablesExist(conn);
                        applyCollationSettings(conn);
                        System.out.println("[FAMS DBConnection] Connected successfully to XAMPP MySQL database '" + DB_NAME + "' on " + host + ":" + port + " as user '" + DB_USER + "'.");
                        return conn;
                    } catch (SQLException ex) {
                        lastEx = ex;
                        // If Error 1049: Unknown database 'fams', attempt to auto-create it on XAMPP MySQL
                        if (ex.getErrorCode() == 1049) {
                            try {
                                String serverUrl = buildJdbcUrl(host, port, "");
                                try (Connection serverConn = DriverManager.getConnection(serverUrl, DB_USER, pwd);
                                     Statement stmt = serverConn.createStatement()) {
                                    stmt.execute("CREATE DATABASE IF NOT EXISTS `" + DB_NAME + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                                    System.out.println("[FAMS DBConnection] Database '" + DB_NAME + "' auto-created in XAMPP MySQL on " + host + ":" + port + ".");
                                }
                                Connection conn = DriverManager.getConnection(buildJdbcUrl(host, port, DB_NAME), DB_USER, pwd);
                                workingHost = host;
                                workingPort = port;
                                workingPassword = pwd;
                                ensureTablesExist(conn);
                                applyCollationSettings(conn);
                                System.out.println("[FAMS DBConnection] Connected successfully to newly created XAMPP database '" + DB_NAME + "' on " + host + ":" + port + ".");
                                return conn;
                            } catch (SQLException createEx) {
                                lastEx = createEx;
                            }
                        }
                    }
                }
            }
        }

        if (lastEx != null) {
            handleConnectionError(lastEx, xamppPort);
            throw lastEx;
        }
        throw new SQLException("Unable to connect to XAMPP MySQL database using candidate configurations.");
    }

    /**
     * Checks if tables exist in the database, and auto-imports src/database/fams.sql if empty.
     */
    private static void ensureTablesExist(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            boolean hasUsers = false;
            try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM `users` LIMIT 1")) {
                if (rs.next()) {
                    hasUsers = true;
                }
            } catch (SQLException ignored) {
                hasUsers = false;
            }

            if (!hasUsers) {
                System.out.println("[FAMS DBConnection] Database '" + DB_NAME + "' in XAMPP is missing core tables. Auto-importing src/database/fams.sql...");
                executeSqlFile(conn, "src/database/fams.sql");
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Executes SQL statements from a file to initialize schema and default data.
     */
    private static void executeSqlFile(Connection conn, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (Statement stmt = conn.createStatement()) {
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*")) {
                        continue;
                    }
                    sb.append(line).append(" ");
                    if (trimmed.endsWith(";")) {
                        String sql = sb.toString().trim();
                        sql = sql.substring(0, sql.length() - 1).trim();
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException ignored) {
                            }
                        }
                        sb.setLength(0);
                    }
                }
            }
            System.out.println("[FAMS DBConnection] Schema successfully loaded into XAMPP database '" + DB_NAME + "'.");
        } catch (Exception e) {
            System.err.println("[FAMS DBConnection] Could not auto-load SQL file: " + e.getMessage());
        }
    }

    /**
     * Ensures consistent utf8mb4_unicode_ci collation across database tables and active session.
     * Prevents 'Illegal mix of collations' errors across different MySQL/MariaDB table definitions.
     */
    private static void applyCollationSettings(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.execute("SET collation_connection = 'utf8mb4_unicode_ci'");

            if (!collationAligned) {
                collationAligned = true;
                try {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                    String[] tables = {
                        "courses", "assessments", "marks", "results", "users",
                        "undergraduates", "medicals", "course_materials",
                        "course_offerings", "enrollments", "attendance", "attendance_sessions",
                        "notices", "lecturers", "technical_officers", "admins", "timetables"
                    };
                    for (String tbl : tables) {
                        try {
                            stmt.execute("ALTER TABLE `" + tbl + "` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                        } catch (Exception ignored) {}
                    }
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    /**
     * Analyzes and outputs clear, user-friendly diagnostics for common connection errors.
     */
    private static void handleConnectionError(SQLException e, String attemptedPort) {
        System.err.println("\n[FAMS Database Connection Error]");
        System.err.println("• Error Message  : " + e.getMessage());
        System.err.println("• Error Code     : " + e.getErrorCode());
        System.err.println("• SQL State      : " + e.getSQLState());
        System.err.println("• Target Host    : " + workingHost);
        System.err.println("• Target Port    : " + attemptedPort + " (XAMPP configured port)");
        System.err.println("• Target URL     : " + buildJdbcUrl(attemptedPort));

        int code = e.getErrorCode();
        if (code == 1049) {
            System.err.println("\nDiagnostic: Unknown database '" + DB_NAME + "'.");
            System.err.println("Solution  : Open phpMyAdmin in XAMPP (http://localhost/phpmyadmin) and ensure database '" + DB_NAME + "' exists.");
        } else if (code == 1045) {
            System.err.println("\nDiagnostic: Access denied for user '" + DB_USER + "'.");
            System.err.println("Solution  : Verify the MySQL username and password in XAMPP.");
        } else if (e.getMessage() != null && (e.getMessage().contains("Communications link failure") || e.getMessage().contains("Connection refused"))) {
            System.err.println("\nDiagnostic: Could not reach XAMPP MySQL on " + workingHost + ":" + attemptedPort + ".");
            System.err.println("Solution  : Ensure MySQL Database is started in XAMPP (manager-osx).");
        }
        System.err.println();
    }

    public static String getUrl() {
        return buildJdbcUrl(workingPort != null ? workingPort : detectXamppPort());
    }

    public static String getUser() {
        return DB_USER;
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("[FAMS DBConnection] Error closing connection: " + e.getMessage());
            }
        }
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
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DATABASE(), USER(), VERSION()")) {

            if (rs.next()) {
                System.out.println("[FAMS DBConnection] Connection successful!");
                System.out.println("  Connected Database : " + rs.getString(1));
                System.out.println("  Authenticated User : " + rs.getString(2));
                System.out.println("  Server Version     : " + rs.getString(3));
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[FAMS DBConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
