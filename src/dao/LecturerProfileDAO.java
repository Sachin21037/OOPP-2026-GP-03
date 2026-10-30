package dao;

import database.DBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * LecturerProfileDAO - Data Access Object for Lecturer Profile Management.
 * Handles database operations, validation, and profile updates.
 */
public class LecturerProfileDAO {

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final Pattern PHONE_PATTERN =
            Pattern.compile("^(\\+?[0-9]{9,15}|0[0-9]{9})$");

    public static class ProfileData {
        public String id = "";
        public String staffNo = "";
        public String department = "";
        public String designation = "";
        public String firstName = "";
        public String lastName = "";
        public String fullName = "";
        public String email = "";
        public String phone = "";
        public String photoPath = null;
    }

    /**
     * Loads the lecturer profile details from MySQL.
     */
    public static ProfileData loadProfile(String lecturerId) throws SQLException {
        String safeId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";

        String sql = "SELECT u.user_id, l.lecturer_id, l.staff_no, l.department, l.designation, " +
                     "       u.first_name, u.last_name, u.email, u.phone, u.profile_picture " +
                     "FROM users u " +
                     "LEFT JOIN lecturers l ON u.user_id = l.lecturer_id " +
                     "WHERE u.user_id = ? OR l.lecturer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeId);
            ps.setString(2, safeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProfileData data = new ProfileData();
                    String id = rs.getString("lecturer_id");
                    data.id = (id != null && !id.isEmpty()) ? id : rs.getString("user_id");
                    data.staffNo = rs.getString("staff_no") != null ? rs.getString("staff_no") : "";
                    data.department = rs.getString("department") != null ? rs.getString("department") : "";
                    data.designation = rs.getString("designation") != null ? rs.getString("designation") : "";
                    data.firstName = rs.getString("first_name") != null ? rs.getString("first_name") : "";
                    data.lastName = rs.getString("last_name") != null ? rs.getString("last_name") : "";
                    data.email = rs.getString("email") != null ? rs.getString("email") : "";
                    data.phone = rs.getString("phone") != null ? rs.getString("phone") : "";
                    data.photoPath = rs.getString("profile_picture");

                    String fn = data.firstName.trim();
                    String ln = data.lastName.trim();
                    data.fullName = (fn + " " + ln).trim();

                    return data;
                }
            }
        }
        return null;
    }

    /**
     * Validates and updates lecturer profile in MySQL across users and lecturers tables.
     */
    public static String updateProfile(
            String lecturerId, String rawName, String rawEmail,
            String rawPhone, String rawDepartment, String rawDesignation,
            File pendingPhotoFile, String currentPhotoPath
    ) throws SQLException, IOException, IllegalArgumentException {
        String safeId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";

        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }
        if (rawName.trim().length() > 100) {
            throw new IllegalArgumentException("Full Name cannot exceed 100 characters.");
        }

        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email Address is required.");
        }
        if (rawEmail.trim().length() > 100) {
            throw new IllegalArgumentException("Email Address cannot exceed 100 characters.");
        }
        if (!EMAIL_PATTERN.matcher(rawEmail.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address (e.g. lecturer@university.edu).");
        }

        if (rawPhone != null && !rawPhone.trim().isEmpty()) {
            if (rawPhone.trim().length() > 20) {
                throw new IllegalArgumentException("Phone number cannot exceed 20 characters.");
            }
            if (!PHONE_PATTERN.matcher(rawPhone.trim()).matches()) {
                throw new IllegalArgumentException("Please enter a valid phone number format (e.g. 0718596874).");
            }
        }

        String newPhotoPath = currentPhotoPath;
        if (pendingPhotoFile != null && pendingPhotoFile.exists()) {
            File uploadDir = new File("uploads/profile_pictures");
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String ext = "png";
            String fn = pendingPhotoFile.getName();
            int dotIdx = fn.lastIndexOf('.');
            if (dotIdx > 0 && dotIdx < fn.length() - 1) {
                ext = fn.substring(dotIdx + 1).toLowerCase();
            }
            File dest = new File(uploadDir, safeId + "_" + System.currentTimeMillis() + "." + ext);
            Files.copy(pendingPhotoFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            newPhotoPath = dest.getPath();
        }

        String name = rawName.trim();
        String firstName = name;
        String lastName = "";
        int spaceIdx = name.indexOf(' ');
        if (spaceIdx > 0) {
            firstName = name.substring(0, spaceIdx).trim();
            lastName = name.substring(spaceIdx + 1).trim();
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check for duplicate email
            String checkEmailSql = "SELECT user_id FROM users WHERE email = ? AND user_id != ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkEmailSql)) {
                checkPs.setString(1, rawEmail.trim());
                checkPs.setString(2, safeId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException("The email address '" + rawEmail.trim() + "' is already registered to another user.");
                    }
                }
            }

            // Update users table
            String updateUsersSql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, profile_picture = ? WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateUsersSql)) {
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, rawEmail.trim());
                ps.setString(4, (rawPhone != null && !rawPhone.trim().isEmpty()) ? rawPhone.trim() : null);
                ps.setString(5, newPhotoPath);
                ps.setString(6, safeId);
                ps.executeUpdate();
            }

            // Update lecturers table if department or designation supplied
            if ((rawDepartment != null && !rawDepartment.trim().isEmpty()) || (rawDesignation != null && !rawDesignation.trim().isEmpty())) {
                String updateLecturersSql = "UPDATE lecturers SET department = COALESCE(?, department), designation = COALESCE(?, designation) WHERE lecturer_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(updateLecturersSql)) {
                    ps2.setString(1, (rawDepartment != null && !rawDepartment.trim().isEmpty()) ? rawDepartment.trim() : null);
                    ps2.setString(2, (rawDesignation != null && !rawDesignation.trim().isEmpty()) ? rawDesignation.trim() : null);
                    ps2.setString(3, safeId);
                    ps2.executeUpdate();
                }
            }
        }
        return newPhotoPath;
    }

    /**
     * Backward-compatible overload.
     */
    public static String updateProfile(
            String lecturerId, String rawName, String rawEmail,
            String rawPhone, File pendingPhotoFile, String currentPhotoPath
    ) throws SQLException, IOException, IllegalArgumentException {
        return updateProfile(lecturerId, rawName, rawEmail, rawPhone, null, null, pendingPhotoFile, currentPhotoPath);
    }
}
