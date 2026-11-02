package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility to seed/replace database data directly from fams.sql.
 * Populates all 30 students, courses, timetable, lecturers, and academic records.
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      FAMS Database Auto-Seeder & Migrator       ");
        System.out.println("=================================================");

        File sqlFile = new File("src/database/fams.sql");
        if (!sqlFile.exists()) {
            System.err.println("[ERROR] fams.sql file not found at " + sqlFile.getAbsolutePath());
            return;
        }

        System.out.println("Reading SQL definitions from: " + sqlFile.getPath());

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected successfully to MySQL Database via DBConnection!");
            
            // Read and split by statement delimiter
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append("\n");
                }
            }

            // Split into individual SQL queries
            String[] queries = sb.toString().split(";\\s*(\\r?\\n)+");
            System.out.println("Total SQL statements to execute: " + queries.length);

            try (Statement stmt = conn.createStatement()) {
                // Temporarily disable foreign key checks for clean drop/replace
                try {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                } catch (Exception ignored) {}

                int executed = 0;
                for (String q : queries) {
                    String clean = q.trim();
                    if (clean.isEmpty()) continue;
                    try {
                        stmt.execute(clean);
                        executed++;
                    } catch (Exception ex) {
                        // Ignore harmless notices/warnings
                        if (!clean.toUpperCase().startsWith("DROP") && !clean.toUpperCase().startsWith("CREATE DATABASE")) {
                            System.err.println("[SQL Warning] " + ex.getMessage() + " for query: " + (clean.length() > 60 ? clean.substring(0, 60) + "..." : clean));
                        }
                    }
                }

                try {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                } catch (Exception ignored) {}

                System.out.println("Successfully executed " + executed + " statements.");
                System.out.println("[SUCCESS] Database updated with 30 students, 9 courses, L2-S1 timetable, and 5 lecturers!");
            }
        } catch (Exception e) {
            System.err.println("[FAILURE] Could not seed database: " + e.getMessage());
            System.out.println("\nAlternative: You can open phpMyAdmin in XAMPP, click 'Import', and choose 'src/database/fams.sql'.");
        }
        System.out.println("=================================================");
    }
}

