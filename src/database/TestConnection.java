package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Utility test runner to verify MySQL database connectivity for FAMS.
 * Run this main method in IntelliJ IDEA or command line to test your connection.
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   FAMS Database Connection Test (MySQL JDBC)    ");
        System.out.println("=================================================");
        System.out.println("Connecting to URL : " + DBConnection.getUrl());
        System.out.println("Username          : " + DBConnection.getUser());
        System.out.println("-------------------------------------------------");

        Connection connection = null;
        try {
            // Attempt to obtain database connection
            connection = DBConnection.getConnection();

            if (connection != null && !connection.isClosed()) {
                System.out.println("[SUCCESS] Connected to MySQL database successfully!");
                System.out.println("-------------------------------------------------");

                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("Catalog / DB Name : " + connection.getCatalog());
                System.out.println("Database Product  : " + metaData.getDatabaseProductName());
                System.out.println("Database Version  : " + metaData.getDatabaseProductVersion());
                System.out.println("Driver Name       : " + metaData.getDriverName());
                System.out.println("Driver Version    : " + metaData.getDriverVersion());
                System.out.println("-------------------------------------------------");
                System.out.println("Connection status: OK (Database ready for use)");
            } else {
                System.err.println("[FAILURE] Connection returned null or was closed immediately.");
            }

        } catch (SQLException e) {
            System.err.println("[FAILURE] Database connection failed with SQLException!");
            System.err.println("Error Message : " + e.getMessage());
            System.err.println("SQL State     : " + e.getSQLState());
            System.err.println("Error Code    : " + e.getErrorCode());
            System.err.println("-------------------------------------------------");
            System.err.println("Troubleshooting suggestions:");

            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("access denied")) {
                System.err.println(" -> Invalid credentials. Check the password in DBConnection.java.");
            } else if (msg.contains("communications link failure") || msg.contains("connection refused")) {
                System.err.println(" -> MySQL server is not running or port 3306 is not accessible.");
                System.err.println("    Please start your MySQL service (e.g. MySQL80 via Services or Workbench).");
            } else if (msg.contains("unknown database 'fams'")) {
                System.err.println(" -> The database 'fams' does not exist.");
                System.err.println("    Please create it in MySQL: CREATE DATABASE fams;");
            } else {
                System.err.println(" -> Verify MySQL is running, database 'fams' exists, and credentials are correct.");
            }
        } catch (Exception e) {
            System.err.println("[FAILURE] An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the test connection safely
            DBConnection.closeConnection(connection);
            System.out.println("=================================================");
            System.out.println("Test execution completed.");
            System.out.println("=================================================");
        }
    }
}
